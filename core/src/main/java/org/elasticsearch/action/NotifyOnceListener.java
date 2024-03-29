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

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A listener that ensures that only one of onResponse or onFailure is called. And the method
 * the is called is only called once. Subclasses should implement notification logic with
 * innerOnResponse and innerOnFailure.
 * 确保只调用OnResponse或OnFailure中的一个的侦听器。
 * 调用的方法只调用一次。子类应该使用InnerOnResponse和InnerOnFailure实现通知逻辑。
 */
public abstract class NotifyOnceListener<Response> implements ActionListener<Response> {

    private final AtomicBoolean hasBeenCalled = new AtomicBoolean(false);

    protected abstract void innerOnResponse(Response response);

    protected abstract void innerOnFailure(Exception e);

    @Override
    public final void onResponse(Response response) {
        if (hasBeenCalled.compareAndSet(false, true)) {
            innerOnResponse(response);
        }
    }

    @Override
    public final void onFailure(Exception e) {
        if (hasBeenCalled.compareAndSet(false, true)) {
            innerOnFailure(e);
        }
    }
}
