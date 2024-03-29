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
package org.elasticsearch;

import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.rest.RestStatus;

import java.io.IOException;

/**
 * Generic ResourceNotFoundException corresponding to the {@link RestStatus#NOT_FOUND} status code
 * 与@link reststatus未找到状态代码对应的通用资源未找到异常
 */
public class ResourceNotFoundException extends ElasticsearchException {

    public ResourceNotFoundException(String msg, Object... args) {
        super(msg, args);
    }

    public ResourceNotFoundException(String msg, Throwable cause, Object... args) {
        super(msg, cause, args);
    }

    public ResourceNotFoundException(StreamInput in) throws IOException {
        super(in);
    }

    @Override
    public final RestStatus status() {
        return RestStatus.NOT_FOUND;
    }
}
