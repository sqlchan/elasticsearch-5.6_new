/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.action.admin.cluster.settings;

import org.apache.logging.log4j.message.ParameterizedMessage;
import org.apache.logging.log4j.util.Supplier;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.master.TransportMasterNodeAction;
import org.elasticsearch.cluster.AckedClusterStateUpdateTask;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.block.ClusterBlockException;
import org.elasticsearch.cluster.block.ClusterBlockLevel;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.cluster.routing.allocation.AllocationService;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.Priority;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.ClusterSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

public class TransportClusterUpdateSettingsAction extends
    TransportMasterNodeAction<ClusterUpdateSettingsRequest, ClusterUpdateSettingsResponse> {

    private final AllocationService allocationService;

    private final ClusterSettings clusterSettings;

    @Inject
    public TransportClusterUpdateSettingsAction(Settings settings, TransportService transportService, ClusterService clusterService,
                                                ThreadPool threadPool, AllocationService allocationService, ActionFilters actionFilters,
                                                IndexNameExpressionResolver indexNameExpressionResolver, ClusterSettings clusterSettings) {
        super(settings, ClusterUpdateSettingsAction.NAME, false, transportService, clusterService, threadPool, actionFilters,
            indexNameExpressionResolver, ClusterUpdateSettingsRequest::new);
        this.allocationService = allocationService;
        this.clusterSettings = clusterSettings;
    }

    @Override
    protected String executor() {
        return ThreadPool.Names.SAME;
    }

    @Override
    protected ClusterBlockException checkBlock(ClusterUpdateSettingsRequest request, ClusterState state) {
        // allow for dedicated changes to the metadata blocks, so we don't block those to allow to "re-enable" it
        // 允许对元数据块进行专门的更改，因此我们不会阻止那些允许“重新启用”元数据块的更改
        if (request.transientSettings().size() + request.persistentSettings().size() == 1) {
            // only one setting  只有一个设置
            if (MetaData.SETTING_READ_ONLY_SETTING.exists(request.persistentSettings())
                || MetaData.SETTING_READ_ONLY_SETTING.exists(request.transientSettings())
                || MetaData.SETTING_READ_ONLY_ALLOW_DELETE_SETTING.exists(request.transientSettings())
                || MetaData.SETTING_READ_ONLY_ALLOW_DELETE_SETTING.exists(request.persistentSettings())) {
                // one of the settings above as the only setting in the request means - resetting the block!
                // 上面的设置之一作为请求中的唯一设置意味着——重置块!
                return null;
            }
        }
        return state.blocks().globalBlockedException(ClusterBlockLevel.METADATA_WRITE);
    }


    @Override
    protected ClusterUpdateSettingsResponse newResponse() {
        return new ClusterUpdateSettingsResponse();
    }

    @Override
    protected void masterOperation(final ClusterUpdateSettingsRequest request, final ClusterState state,
                                   final ActionListener<ClusterUpdateSettingsResponse> listener) {
        final SettingsUpdater updater = new SettingsUpdater(clusterSettings);
        clusterService.submitStateUpdateTask("cluster_update_settings",
                new AckedClusterStateUpdateTask<ClusterUpdateSettingsResponse>(Priority.IMMEDIATE, request, listener) {

            private volatile boolean changed = false;

            @Override
            protected ClusterUpdateSettingsResponse newResponse(boolean acknowledged) {
                return new ClusterUpdateSettingsResponse(acknowledged, updater.getTransientUpdates(), updater.getPersistentUpdate());
            }

            @Override
            public void onAllNodesAcked(@Nullable Exception e) {
                if (changed) {
                    reroute(true);
                } else {
                    super.onAllNodesAcked(e);
                }
            }

            @Override
            public void onAckTimeout() {
                if (changed) {
                    reroute(false);
                } else {
                    super.onAckTimeout();
                }
            }

            private void reroute(final boolean updateSettingsAcked) {
                // We're about to send a second update task, so we need to check if we're still the elected master
                // For example the minimum_master_node could have been breached and we're no longer elected master,
                // so we should *not* execute the reroute.
                // 我们将发送第二个更新任务，因此需要检查我们是否仍然是当选的master。
                // 例如，minimum_master_node可能已经被破坏，我们不再被选为主节点，所以我们应该“不”执行重路由。
                if (!clusterService.state().nodes().isLocalNodeElectedMaster()) {
                    // 跳过集群更新设置后的重新路由，因为节点不再是主节点
                    logger.debug("Skipping reroute after cluster update settings, because node is no longer master");
                    listener.onResponse(new ClusterUpdateSettingsResponse(updateSettingsAcked, updater.getTransientUpdates(),
                        updater.getPersistentUpdate()));
                    return;
                }

                // The reason the reroute needs to be send as separate update task, is that all the *cluster* settings are encapsulate
                // in the components (e.g. FilterAllocationDecider), so the changes made by the first call aren't visible
                // to the components until the ClusterStateListener instances have been invoked, but are visible after
                // the first update task has been completed.
                // 变更的原因需要发送作为单独的更新任务,是所有集群* *设置封装的组件(例如FilterAllocationDecider),所以第一次调用所做的不可见的组件,
                // 直到ClusterStateListener实例调用,但是是可见的在第一次更新任务已经完成。
                clusterService.submitStateUpdateTask("reroute_after_cluster_update_settings",
                        new AckedClusterStateUpdateTask<ClusterUpdateSettingsResponse>(Priority.URGENT, request, listener) {

                    @Override
                    public boolean mustAck(DiscoveryNode discoveryNode) {
                        //we wait for the reroute ack only if the update settings was acknowledged
                        //只有在确认更新设置之后，我们才等待重新路由ack
                        return updateSettingsAcked;
                    }

                    @Override
                    // we return when the cluster reroute is acked or it times out but the acknowledged flag depends on whether the
                    // update settings was acknowledged
                    // 当集群路由被锁定或超时时返回，但是确认标志取决于是否确认更新设置
                    protected ClusterUpdateSettingsResponse newResponse(boolean acknowledged) {
                        return new ClusterUpdateSettingsResponse(updateSettingsAcked && acknowledged, updater.getTransientUpdates(),
                            updater.getPersistentUpdate());
                    }

                    @Override
                    public void onNoLongerMaster(String source) {
                        // 更新群集设置后未能预形成路由-当前节点不再是主节点
                        logger.debug("failed to preform reroute after cluster settings were updated - current node is no longer a master");
                        listener.onResponse(new ClusterUpdateSettingsResponse(updateSettingsAcked, updater.getTransientUpdates(),
                            updater.getPersistentUpdate()));
                    }

                    @Override
                    public void onFailure(String source, Exception e) {
                        //if the reroute fails we only log  如果重路由失败，我们只进行日志记录
                        //未能执行
                        //更新设置失败后重新路由
                        logger.debug((Supplier<?>) () -> new ParameterizedMessage("failed to perform [{}]", source), e);
                        listener.onFailure(new ElasticsearchException("reroute after update settings failed", e));
                    }

                    @Override
                    public ClusterState execute(final ClusterState currentState) {
                        // now, reroute in case things that require it changed (e.g. number of replicas)
                        // 现在，在需要更改的情况下重新路由(例如，副本的数量)
                        return allocationService.reroute(currentState, "reroute after cluster update settings");
                    }
                });
            }

            @Override
            public void onFailure(String source, Exception e) {
                logger.debug((Supplier<?>) () -> new ParameterizedMessage("failed to perform [{}]", source), e);
                super.onFailure(source, e);
            }

            @Override
            public ClusterState execute(final ClusterState currentState) {
                ClusterState clusterState = updater.updateSettings(currentState, request.transientSettings(), request.persistentSettings());
                changed = clusterState != currentState;
                return clusterState;
            }
        });
    }

}
