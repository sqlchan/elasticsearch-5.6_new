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

import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.block.ClusterBlocks;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.common.settings.ClusterSettings;
import org.elasticsearch.common.settings.Settings;

import static org.elasticsearch.cluster.ClusterState.builder;

/**
 * Updates transient and persistent cluster state settings if there are any changes
 * due to the update.
 * 如果由于更新而发生任何更改，则更新临时和持久集群状态设置。
 */
final class SettingsUpdater {
    final Settings.Builder transientUpdates = Settings.builder();
    final Settings.Builder persistentUpdates = Settings.builder();
    private final ClusterSettings clusterSettings;

    SettingsUpdater(ClusterSettings clusterSettings) {
        this.clusterSettings = clusterSettings;
    }

    synchronized Settings getTransientUpdates() {
        return transientUpdates.build();
    }

    synchronized Settings getPersistentUpdate() {
        return persistentUpdates.build();
    }

    synchronized ClusterState updateSettings(final ClusterState currentState, Settings transientToApply, Settings persistentToApply) {
        boolean changed = false;
        Settings.Builder transientSettings = Settings.builder();
        transientSettings.put(currentState.metaData().transientSettings());
        changed |= clusterSettings.updateDynamicSettings(transientToApply, transientSettings, transientUpdates, "transient");

        Settings.Builder persistentSettings = Settings.builder();
        persistentSettings.put(currentState.metaData().persistentSettings());
        changed |= clusterSettings.updateDynamicSettings(persistentToApply, persistentSettings, persistentUpdates, "persistent");

        final ClusterState clusterState;
        if (changed) {
            MetaData.Builder metaData = MetaData.builder(currentState.metaData())
                    .persistentSettings(persistentSettings.build())
                    .transientSettings(transientSettings.build());

            ClusterBlocks.Builder blocks = ClusterBlocks.builder().blocks(currentState.blocks());
            boolean updatedReadOnly = MetaData.SETTING_READ_ONLY_SETTING.get(metaData.persistentSettings())
                    || MetaData.SETTING_READ_ONLY_SETTING.get(metaData.transientSettings());
            if (updatedReadOnly) {
                blocks.addGlobalBlock(MetaData.CLUSTER_READ_ONLY_BLOCK);
            } else {
                blocks.removeGlobalBlock(MetaData.CLUSTER_READ_ONLY_BLOCK);
            }
            boolean updatedReadOnlyAllowDelete = MetaData.SETTING_READ_ONLY_ALLOW_DELETE_SETTING.get(metaData.persistentSettings())
                    || MetaData.SETTING_READ_ONLY_ALLOW_DELETE_SETTING.get(metaData.transientSettings());
            if (updatedReadOnlyAllowDelete) {
                blocks.addGlobalBlock(MetaData.CLUSTER_READ_ONLY_ALLOW_DELETE_BLOCK);
            } else {
                blocks.removeGlobalBlock(MetaData.CLUSTER_READ_ONLY_ALLOW_DELETE_BLOCK);
            }
            clusterState = builder(currentState).metaData(metaData).blocks(blocks).build();
        } else {
            clusterState = currentState;
        }

        /*
         * Now we try to apply things and if they are invalid we fail. This dry run will validate, parse settings, and trigger deprecation
         * logging, but will not actually apply them.
         * 现在我们尝试应用一些东西，如果它们无效，我们就失败了。此演练将验证、解析设置并触发弃用日志记录，但实际上并不应用它们。
         */
        final Settings settings = clusterState.metaData().settings();
        clusterSettings.validateUpdate(settings);

        return clusterState;
    }


}
