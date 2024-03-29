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
 * Unchecked exception that is translated into a {@code 400 BAD REQUEST} error when it bubbles out over HTTP.
 * 未选中的异常，当它在HTTP上冒泡时转换为@code 400 bad request错误。
 */
public class ElasticsearchParseException extends ElasticsearchException {

    public ElasticsearchParseException(String msg, Object... args) {
        super(msg, args);
    }

    public ElasticsearchParseException(String msg, Throwable cause, Object... args) {
        super(msg, cause, args);
    }

    public ElasticsearchParseException(StreamInput in) throws IOException {
        super(in);
    }
    @Override
    public RestStatus status() {
        return RestStatus.BAD_REQUEST;
    }
}
