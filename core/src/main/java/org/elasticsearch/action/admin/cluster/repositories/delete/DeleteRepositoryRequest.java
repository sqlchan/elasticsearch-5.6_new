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

package org.elasticsearch.action.admin.cluster.repositories.delete;

import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.action.support.master.AcknowledgedRequest;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

import java.io.IOException;

import static org.elasticsearch.action.ValidateActions.addValidationError;

/**
 * Unregister repository request.  注销请求存储库。
 * <p>
 * The unregister repository command just unregisters the repository. No data is getting deleted from the repository.
 * 取消注册存储库命令只是取消注册存储库。没有从存储库中删除任何数据。
 */
public class DeleteRepositoryRequest extends AcknowledgedRequest<DeleteRepositoryRequest> {

    private String name;

    public DeleteRepositoryRequest() {
    }

    /**
     * Constructs a new unregister repository request with the provided name.
     * 使用提供的名称构造一个新的取消注册存储库请求。
     *
     * @param name name of the repository
     */
    public DeleteRepositoryRequest(String name) {
        this.name = name;
    }

    @Override
    public ActionRequestValidationException validate() {
        ActionRequestValidationException validationException = null;
        if (name == null) {
            validationException = addValidationError("name is missing", validationException);
        }
        return validationException;
    }

    /**
     * Sets the name of the repository to unregister.
     * 设置要取消注册的存储库名称。
     *
     * @param name name of the repository
     */
    public DeleteRepositoryRequest name(String name) {
        this.name = name;
        return this;
    }

    /**
     * The name of the repository.  存储库的名称。
     *
     * @return the name of the repository
     */
    public String name() {
        return this.name;
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
        name = in.readString();
        readTimeout(in);
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeString(name);
        writeTimeout(out);
    }
}
