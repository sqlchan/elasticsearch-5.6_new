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

package org.elasticsearch.action.admin.cluster.reroute;

import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.action.support.master.AcknowledgedRequest;
import org.elasticsearch.cluster.routing.allocation.command.AllocationCommand;
import org.elasticsearch.cluster.routing.allocation.command.AllocationCommands;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

import java.io.IOException;
import java.util.Objects;

/**
 * Request to submit cluster reroute allocation commands
 * 请求提交集群重路由分配命令
 */
public class ClusterRerouteRequest extends AcknowledgedRequest<ClusterRerouteRequest> {
    private AllocationCommands commands = new AllocationCommands();
    private boolean dryRun;
    private boolean explain;
    private boolean retryFailed;

    public ClusterRerouteRequest() {
    }

    /**
     * Adds allocation commands to be applied to the cluster. Note, can be empty, in which case
     * will simply run a simple "reroute".
     * 添加要应用于集群的分配命令。注意，可以为空，在这种情况下将简单地运行一个简单的“重新路由”。
     */
    public ClusterRerouteRequest add(AllocationCommand... commands) {
        this.commands.add(commands);
        return this;
    }

    /**
     * Sets a dry run flag (defaults to <tt>false</tt>) allowing to run the commands without
     * actually applying them to the cluster state, and getting the resulting cluster state back.
     * 设置一个预运行标志(默认值为<tt>false</tt>)，允许运行命令，而不需要将它们实际应用于集群状态，并返回结果集群状态。
     */
    public ClusterRerouteRequest dryRun(boolean dryRun) {
        this.dryRun = dryRun;
        return this;
    }

    /**
     * Returns the current dry run flag which allows to run the commands without actually applying them,
     * just to get back the resulting cluster state back.
     * 返回当前的预运行标志，该标志允许在不实际应用命令的情况下运行命令，只返回生成的集群状态。
     */
    public boolean dryRun() {
        return this.dryRun;
    }

    /**
     * Sets the explain flag, which will collect information about the reroute
     * request without executing the actions. Similar to dryRun,
     * but human-readable.
     * 设置explain标志，该标志将收集关于重新路由请求的信息，而不执行操作。类似于dryRun，但人类可读。
     */
    public ClusterRerouteRequest explain(boolean explain) {
        this.explain = explain;
        return this;
    }

    /**
     * Sets the retry failed flag (defaults to <tt>false</tt>). If true, the
     * request will retry allocating shards that can't currently be allocated due to too many allocation failures.
     * 设置重试失败标志(默认为<tt>false</tt>)。如果为真，则请求将重试分配由于太多分配失败而当前无法分配的碎片。
     */
    public ClusterRerouteRequest setRetryFailed(boolean retryFailed) {
        this.retryFailed = retryFailed;
        return this;
    }

    /**
     * Returns the current explain flag  返回当前解释标志
     */
    public boolean explain() {
        return this.explain;
    }

    /**
     * Returns the current retry failed flag
     */
    public boolean isRetryFailed() {
        return this.retryFailed;
    }


    /**
     * Set the allocation commands to execute.
     */
    public ClusterRerouteRequest commands(AllocationCommands commands) {
        this.commands = commands;
        return this;
    }

    /**
     * Returns the allocation commands to execute
     */
    public AllocationCommands getCommands() {
        return commands;
    }

    @Override
    public ActionRequestValidationException validate() {
        return null;
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
        commands = AllocationCommands.readFrom(in);
        dryRun = in.readBoolean();
        explain = in.readBoolean();
        retryFailed = in.readBoolean();
        readTimeout(in);
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        AllocationCommands.writeTo(commands, out);
        out.writeBoolean(dryRun);
        out.writeBoolean(explain);
        out.writeBoolean(retryFailed);
        writeTimeout(out);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ClusterRerouteRequest other = (ClusterRerouteRequest) obj;
        // Override equals and hashCode for testing
        return Objects.equals(commands, other.commands) &&
                Objects.equals(dryRun, other.dryRun) &&
                Objects.equals(explain, other.explain) &&
                Objects.equals(timeout, other.timeout) &&
                Objects.equals(retryFailed, other.retryFailed) &&
                Objects.equals(masterNodeTimeout, other.masterNodeTimeout);
    }

    @Override
    public int hashCode() {
        // Override equals and hashCode for testing
        return Objects.hash(commands, dryRun, explain, timeout, retryFailed, masterNodeTimeout);
    }
}
