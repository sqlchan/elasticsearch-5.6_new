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

package org.elasticsearch.action.admin.cluster.node.tasks.get;

import org.elasticsearch.action.Action;
import org.elasticsearch.client.ElasticsearchClient;

/**
 * Action for retrieving a list of currently running tasks
 * 检索当前运行任务列表的操作
 */
public class GetTaskAction extends Action<GetTaskRequest, GetTaskResponse, GetTaskRequestBuilder> {

    public static final GetTaskAction INSTANCE = new GetTaskAction();
    public static final String NAME = "cluster:monitor/task/get";

    private GetTaskAction() {
        super(NAME);
    }

    @Override
    public GetTaskResponse newResponse() {
        return new GetTaskResponse();
    }

    @Override
    public GetTaskRequestBuilder newRequestBuilder(ElasticsearchClient client) {
        return new GetTaskRequestBuilder(client, this);
    }
}
