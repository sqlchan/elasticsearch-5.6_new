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

package org.elasticsearch.action.admin.cluster.allocation;

import org.elasticsearch.action.support.master.MasterNodeOperationRequestBuilder;
import org.elasticsearch.client.ElasticsearchClient;

/**
 * Builder for requests to explain the allocation of a shard in the cluster
 * 用于解释群集中碎片分配的请求的生成器
 */
public class ClusterAllocationExplainRequestBuilder
        extends MasterNodeOperationRequestBuilder<ClusterAllocationExplainRequest,
                                                          ClusterAllocationExplainResponse,
                                                          ClusterAllocationExplainRequestBuilder> {

    public ClusterAllocationExplainRequestBuilder(ElasticsearchClient client, ClusterAllocationExplainAction action) {
        super(client, action, new ClusterAllocationExplainRequest());
    }

    /** The index name to use when finding the shard to explain
     * 查找要解释的碎片时使用的索引名称*/
    public ClusterAllocationExplainRequestBuilder setIndex(String index) {
        request.setIndex(index);
        return this;
    }

    /** The shard number to use when finding the shard to explain
     * 找到要解释的碎片时要使用的碎片编号 */
    public ClusterAllocationExplainRequestBuilder setShard(int shard) {
        request.setShard(shard);
        return this;
    }

    /** Whether the primary or replica should be explained
     * 是否应该解释主副本*/
    public ClusterAllocationExplainRequestBuilder setPrimary(boolean primary) {
        request.setPrimary(primary);
        return this;
    }

    /** Whether to include "YES" decider decisions in the response instead of only "NO" decisions
     * 是否在答复中包括“是”决定，而不仅仅是“否”决定*/
    public ClusterAllocationExplainRequestBuilder setIncludeYesDecisions(boolean includeYesDecisions) {
        request.includeYesDecisions(includeYesDecisions);
        return this;
    }

    /** Whether to include information about the gathered disk information of nodes in the cluster
     * 是否包含集群中节点收集到的磁盘信息信息*/
    public ClusterAllocationExplainRequestBuilder setIncludeDiskInfo(boolean includeDiskInfo) {
        request.includeDiskInfo(includeDiskInfo);
        return this;
    }

    /**
     * Requests the explain API to explain an already assigned replica shard currently allocated to
     * the given node.
     * 请求解释API解释当前分配给给定节点的已分配副本碎片。
     */
    public ClusterAllocationExplainRequestBuilder setCurrentNode(String currentNode) {
        request.setCurrentNode(currentNode);
        return this;
    }

    /**
     * Signal that the first unassigned shard should be used
     * 表示应使用第一个未分配的碎片
     */
    public ClusterAllocationExplainRequestBuilder useAnyUnassignedShard() {
        request.setIndex(null);
        request.setShard(null);
        request.setPrimary(null);
        return this;
    }

}
