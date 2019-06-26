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

import org.elasticsearch.action.support.IndicesOptions;

/**
 * Needs to be implemented by all {@link org.elasticsearch.action.ActionRequest} subclasses that relate to
 * one or more indices. Allows to retrieve which indices the action relates to.
 * In case of internal requests  originated during the distributed execution of an external request,
 * they will still return the indices that the original request related to.
 * 需要由与一个或多个索引相关的所有@link org.elasticsearch.action.actionRequest子类实现。允许检索与操作相关的索引。
 * 如果内部请求是在分布式执行外部请求期间发出的，那么它们仍然会返回与原始请求相关的索引。
 */
public interface IndicesRequest {

    /**
     * Returns the array of indices that the action relates to
     * 返回与操作相关的索引数组
     */
    String[] indices();

    /**
     * Returns the indices options used to resolve indices. They tell for instance whether a single index is
     * accepted, whether an empty array will be converted to _all, and how wildcards will be expanded if needed.
     * 返回用于解析索引的索引选项。例如，它们说明是否接受单个索引，是否将空数组转换为“全部”，以及在需要时如何扩展通配符。
     */
    IndicesOptions indicesOptions();

    interface Replaceable extends IndicesRequest {
        /**
         * Sets the indices that the action relates to.
         * 设置与操作相关的索引。
         */
        IndicesRequest indices(String... indices);
    }
}
