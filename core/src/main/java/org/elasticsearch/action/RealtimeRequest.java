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

/**
 * Indicates that a request can execute in realtime (reads from the translog).
 * All {@link ActionRequest} that are realtime should implement this interface.
 * 指示请求可以实时执行（从音译中读取）。所有实时的@link actionrequest都应该实现这个接口。
 */
public interface RealtimeRequest {

    /**
     * @param realtime Controls whether this request should be realtime by reading from the translog.
     *                 通过从音译中读取来控制此请求是否应为实时请求。
     */
    <R extends RealtimeRequest> R realtime(boolean realtime);

}
