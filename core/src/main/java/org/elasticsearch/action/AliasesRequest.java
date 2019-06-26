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
 * Needs to be implemented by all {@link org.elasticsearch.action.ActionRequest} subclasses that relate to
 * one or more indices and one or more aliases. Meant to be used for aliases management requests (e.g. add/remove alias,
 * get aliases) that hold aliases and indices in separate fields.
 * Allows to retrieve which indices and aliases the action relates to.
 * 需要由与一个或多个索引和一个或多个别名相关的所有@link org.elasticsearch.action.actionRequest子类实现。
 * 用于别名管理请求（例如，添加/删除别名、获取别名），将别名和索引保存在单独的字段中。允许检索与操作相关的索引和别名。
 */
public interface AliasesRequest extends IndicesRequest.Replaceable {

    /**
     * Returns the array of aliases that the action relates to
     * 返回与操作相关的别名数组
     */
    String[] aliases();

    /**
     * Sets the array of aliases that the action relates to
     * 设置与操作相关的别名数组
     */
    AliasesRequest aliases(String... aliases);

    /**
     * Returns true if wildcards expressions among aliases should be resolved, false otherwise
     * 如果应解析别名之间的通配符表达式，则返回true，否则返回false
     */
    boolean expandAliasesWildcards();
}
