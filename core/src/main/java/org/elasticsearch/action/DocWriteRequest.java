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

import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.index.VersionType;

import java.io.IOException;
import java.util.Locale;

/**
 * Generic interface to group ActionRequest, which perform writes to a single document
 * Action requests implementing this can be part of {@link org.elasticsearch.action.bulk.BulkRequest}
 * 组actionRequest的通用接口，它执行对单个文档的写入。
 * 实现此功能的操作请求可以是@link org.elasticsearch.action.bulk.bulkrequest的一部分
 */
public interface DocWriteRequest<T> extends IndicesRequest {

    /**
     * Get the index that this request operates on  获取此请求操作的索引
     * @return the index
     */
    String index();

    /**
     * Get the type that this request operates on   获取此请求操作的类型
     * @return the type
     */
    String type();

    /**
     * Get the id of the document for this request  获取此请求的文档ID
     * @return the id
     */
    String id();

    /**
     * Get the options for this request 获取此请求的选项
     * @return the indices options
     */
    IndicesOptions indicesOptions();

    /**
     * Set the routing for this request 设置此请求的路由
     * @return the Request
     */
    T routing(String routing);

    /**
     * Get the routing for this request 获取此请求的路由
     * @return the Routing
     */
    String routing();


    /**
     * Get the parent for this request  获取此请求的父级
     * @return the Parent
     */
    String parent();

    /**
     * Get the document version for this request    获取此请求的文档版本
     * @return the document version
     */
    long version();

    /**
     * Sets the version, which will perform the operation only if a matching
     * version exists and no changes happened on the doc since then.
     * 设置版本，只有匹配的版本存在且此后没有对文档进行更改时，该版本才会执行操作。
     */
    T version(long version);

    /**
     * Get the document version type for this request   获取此请求的文档版本类型
     * @return the document version type
     */
    VersionType versionType();

    /**
     * Sets the versioning type. Defaults to {@link VersionType#INTERNAL}.
     * 设置版本控制类型。默认为@link versiontype internal。
     */
    T versionType(VersionType versionType);

    /**
     * Get the requested document operation type of the request
     * 获取请求的文档操作类型
     * @return the operation type {@link OpType}
     */
    OpType opType();

    /**
     * Requested operation type to perform on the document
     * 请求对文档执行的操作类型
     */
    enum OpType {
        /**
         * Index the source. If there an existing document with the id, it will
         * be replaced.
         * 来源索引如果有一份与标识符相符的文件，将予以替换。
         */
        INDEX(0),
        /**
         * Creates the resource. Simply adds it to the index, if there is an existing
         * document with the id, then it won't be removed.
         * 创建资源。只需将其添加到索引中，如果存在具有ID的现有文档，则不会将其删除。
         */
        CREATE(1),
        /** Updates a document 更新文档*/
        UPDATE(2),
        /** Deletes a document */
        DELETE(3);

        private final byte op;
        private final String lowercase;

        OpType(int op) {
            this.op = (byte) op;
            this.lowercase = this.toString().toLowerCase(Locale.ROOT);
        }

        public byte getId() {
            return op;
        }

        public String getLowercase() {
            return lowercase;
        }

        public static OpType fromId(byte id) {
            switch (id) {
                case 0: return INDEX;
                case 1: return CREATE;
                case 2: return UPDATE;
                case 3: return DELETE;
                default: throw new IllegalArgumentException("Unknown opType: [" + id + "]");
            }
        }

        public static OpType fromString(String sOpType) {
            String lowerCase = sOpType.toLowerCase(Locale.ROOT);
            for (OpType opType : OpType.values()) {
                if (opType.getLowercase().equals(lowerCase)) {
                    return opType;
                }
            }
            throw new IllegalArgumentException("Unknown opType: [" + sOpType + "]");
        }
    }

    /** read a document write (index/delete/update) request
     * 读取文档写入（索引/删除/更新）请求*/
    static DocWriteRequest readDocumentRequest(StreamInput in) throws IOException {
        byte type = in.readByte();
        DocWriteRequest docWriteRequest;
        if (type == 0) {
            IndexRequest indexRequest = new IndexRequest();
            indexRequest.readFrom(in);
            docWriteRequest = indexRequest;
        } else if (type == 1) {
            DeleteRequest deleteRequest = new DeleteRequest();
            deleteRequest.readFrom(in);
            docWriteRequest = deleteRequest;
        } else if (type == 2) {
            UpdateRequest updateRequest = new UpdateRequest();
            updateRequest.readFrom(in);
            docWriteRequest = updateRequest;
        } else {
            throw new IllegalStateException("invalid request type [" + type+ " ]");
        }
        return docWriteRequest;
    }

    /** write a document write (index/delete/update) request*/
    static void writeDocumentRequest(StreamOutput out, DocWriteRequest request)  throws IOException {
        if (request instanceof IndexRequest) {
            out.writeByte((byte) 0);
            ((IndexRequest) request).writeTo(out);
        } else if (request instanceof DeleteRequest) {
            out.writeByte((byte) 1);
            ((DeleteRequest) request).writeTo(out);
        } else if (request instanceof UpdateRequest) {
            out.writeByte((byte) 2);
            ((UpdateRequest) request).writeTo(out);
        } else {
            throw new IllegalStateException("invalid request [" + request.getClass().getSimpleName() + " ]");
        }
    }
}
