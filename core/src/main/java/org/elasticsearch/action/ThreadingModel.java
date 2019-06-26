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
 *
 */
public enum ThreadingModel {
    NONE((byte) 0),
    OPERATION((byte) 1),
    LISTENER((byte) 2),
    OPERATION_LISTENER((byte) 3);

    private byte id;

    ThreadingModel(byte id) {
        this.id = id;
    }

    public byte id() {
        return this.id;
    }

    /**
     * <tt>true</tt> if the actual operation the action represents will be executed
     * on a different thread than the calling thread (assuming it will be executed
     * on the same node).
     * 如果操作所代表的实际操作将在与调用线程不同的线程上执行（假设它将在同一节点上执行）。
     */
    public boolean threadedOperation() {
        return this == OPERATION || this == OPERATION_LISTENER;
    }

    /**
     * <tt>true</tt> if the invocation of the action result listener will be executed
     * on a different thread (than the calling thread or an "expensive" thread, like the
     * IO thread).
     * 如果动作结果侦听器的调用将在不同的线程（而不是调用线程或“昂贵”线程，如IO线程）上执行。
     */
    public boolean threadedListener() {
        return this == LISTENER || this == OPERATION_LISTENER;
    }

    public ThreadingModel addListener() {
        if (this == NONE) {
            return LISTENER;
        }
        if (this == OPERATION) {
            return OPERATION_LISTENER;
        }
        return this;
    }

    public ThreadingModel removeListener() {
        if (this == LISTENER) {
            return NONE;
        }
        if (this == OPERATION_LISTENER) {
            return OPERATION;
        }
        return this;
    }

    public ThreadingModel addOperation() {
        if (this == NONE) {
            return OPERATION;
        }
        if (this == LISTENER) {
            return OPERATION_LISTENER;
        }
        return this;
    }

    public ThreadingModel removeOperation() {
        if (this == OPERATION) {
            return NONE;
        }
        if (this == OPERATION_LISTENER) {
            return LISTENER;
        }
        return this;
    }

    public static ThreadingModel fromId(byte id) {
        if (id == 0) {
            return NONE;
        } else if (id == 1) {
            return OPERATION;
        } else if (id == 2) {
            return LISTENER;
        } else if (id == 3) {
            return OPERATION_LISTENER;
        } else {
            throw new IllegalArgumentException("No threading model for [" + id + "]");
        }
    }
}
