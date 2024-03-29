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

import org.elasticsearch.action.Action;
import org.elasticsearch.client.ElasticsearchClient;

/**
 * Action for explaining shard allocation for a shard in the cluster
 * 解释群集中碎片分配的操作
 */
public class ClusterAllocationExplainAction extends Action<ClusterAllocationExplainRequest,
                                                               ClusterAllocationExplainResponse,
                                                               ClusterAllocationExplainRequestBuilder> {

    public static final ClusterAllocationExplainAction INSTANCE = new ClusterAllocationExplainAction();
    public static final String NAME = "cluster:monitor/allocation/explain";

    private ClusterAllocationExplainAction() {
        super(NAME);
    }

    @Override
    public ClusterAllocationExplainResponse newResponse() {
        return new ClusterAllocationExplainResponse();
    }

    @Override
    public ClusterAllocationExplainRequestBuilder newRequestBuilder(ElasticsearchClient client) {
        return new ClusterAllocationExplainRequestBuilder(client, this);
    }
}
