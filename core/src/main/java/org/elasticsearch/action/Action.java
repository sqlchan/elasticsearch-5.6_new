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

package org.elasticsearch.action;

import org.elasticsearch.client.ElasticsearchClient;

/**
 * Base action. Supports building the <code>Request</code> through a <code>RequestBuilder</code>.
 * 基本动作。支持构建<code>request<code>through a<code>requestbuilder<code>
 */
public abstract class Action<Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response, RequestBuilder>>
        extends GenericAction<Request, Response> {

    protected Action(String name) {
        super(name);
    }

    /**
     * Creates a new request builder given the client provided as argument
     * 为作为参数提供的客户端创建新的请求生成器
     */
    public abstract RequestBuilder newRequestBuilder(ElasticsearchClient client);
}
