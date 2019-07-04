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

package org.elasticsearch.action.admin.cluster.snapshots.create;

import org.elasticsearch.ElasticsearchGenerationException;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.action.IndicesRequest;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.support.master.MasterNodeRequest;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.action.ValidateActions.addValidationError;
import static org.elasticsearch.common.Strings.EMPTY_ARRAY;
import static org.elasticsearch.common.settings.Settings.readSettingsFromStream;
import static org.elasticsearch.common.settings.Settings.writeSettingsToStream;
import static org.elasticsearch.common.settings.Settings.Builder.EMPTY_SETTINGS;
import static org.elasticsearch.common.xcontent.support.XContentMapValues.lenientNodeBooleanValue;
import static org.elasticsearch.common.xcontent.support.XContentMapValues.nodeBooleanValue;

/**
 * Create snapshot request
 * <p>
 * The only mandatory parameter is repository name. The repository name has to satisfy the following requirements
 * 惟一必需的参数是存储库名称。存储库名称必须满足以下要求
 * <ul>
 * <li>be a non-empty string</li>
 * <li>must not contain whitespace (tabs or spaces)</li>
 * <li>must not contain comma (',')</li>
 * <li>must not contain hash sign ('#')</li>
 * <li>must not start with underscore ('-')</li>
 * <li>must be lowercase</li>  必须是小写的
 * <li>must not contain invalid file name characters {@link org.elasticsearch.common.Strings#INVALID_FILENAME_CHARS} </li>
 * 必须不包含无效的文件名字符
 * </ul>
 */
public class CreateSnapshotRequest extends MasterNodeRequest<CreateSnapshotRequest> implements IndicesRequest.Replaceable {

    private String snapshot;

    private String repository;

    private String[] indices = EMPTY_ARRAY;

    private IndicesOptions indicesOptions = IndicesOptions.strictExpandOpen();

    private boolean partial = false;

    private Settings settings = EMPTY_SETTINGS;

    private boolean includeGlobalState = true;

    private boolean waitForCompletion;

    public CreateSnapshotRequest() {
    }

    /**
     * Constructs a new put repository request with the provided snapshot and repository names
     * 使用提供的快照和存储库名称构造新的put存储库请求
     *
     * @param repository repository name
     * @param snapshot   snapshot name
     */
    public CreateSnapshotRequest(String repository, String snapshot) {
        this.snapshot = snapshot;
        this.repository = repository;
    }

    @Override
    public ActionRequestValidationException validate() {
        ActionRequestValidationException validationException = null;
        if (snapshot == null) {
            validationException = addValidationError("snapshot is missing", validationException);
        }
        if (repository == null) {
            validationException = addValidationError("repository is missing", validationException);
        }
        if (indices == null) {
            validationException = addValidationError("indices is null", validationException);
        } else {
            for (String index : indices) {
                if (index == null) {
                    validationException = addValidationError("index is null", validationException);
                    break;
                }
            }
        }
        if (indicesOptions == null) {
            validationException = addValidationError("indicesOptions is null", validationException);
        }
        if (settings == null) {
            validationException = addValidationError("settings is null", validationException);
        }
        return validationException;
    }

    /**
     * Sets the snapshot name
     *
     * @param snapshot snapshot name
     */
    public CreateSnapshotRequest snapshot(String snapshot) {
        this.snapshot = snapshot;
        return this;
    }

    /**
     * The snapshot name
     *
     * @return snapshot name
     */
    public String snapshot() {
        return this.snapshot;
    }

    /**
     * Sets repository name
     *
     * @param repository name
     * @return this request
     */
    public CreateSnapshotRequest repository(String repository) {
        this.repository = repository;
        return this;
    }

    /**
     * Returns repository name
     *
     * @return repository name
     */
    public String repository() {
        return this.repository;
    }

    /**
     * Sets a list of indices that should be included into the snapshot
     * 设置快照中应该包含的索引列表
     * <p>
     * The list of indices supports multi-index syntax. For example: "+test*" ,"-test42" will index all indices with
     * prefix "test" except index "test42". Aliases are supported. An empty list or {"_all"} will snapshot all open
     * indices in the cluster.
     * 索引列表支持多索引语法。例如:“+test*”、“-test42”将索引除索引“test42”外的所有前缀为“test”的索引。
     * 支持别名。空列表或{"_all"}将快照集群中所有打开的索引。
     *
     * @return this request
     */
    @Override
    public CreateSnapshotRequest indices(String... indices) {
        this.indices = indices;
        return this;
    }

    /**
     * Sets a list of indices that should be included into the snapshot
     * 设置快照中应该包含的索引列表
     * <p>
     * The list of indices supports multi-index syntax. For example: "+test*" ,"-test42" will index all indices with
     * prefix "test" except index "test42". Aliases are supported. An empty list or {"_all"} will snapshot all open
     * indices in the cluster.
     * 索引列表支持多索引语法。例如:“+test*”、“-test42”将索引除索引“test42”外的所有前缀为“test”的索引。
     * 支持别名。空列表或{"_all"}将快照集群中所有打开的索引。
     *
     * @return this request
     */
    public CreateSnapshotRequest indices(List<String> indices) {
        this.indices = indices.toArray(new String[indices.size()]);
        return this;
    }

    /**
     * Returns a list of indices that should be included into the snapshot
     *
     * @return list of indices
     */
    @Override
    public String[] indices() {
        return indices;
    }

    /**
     * Specifies the indices options. Like what type of requested indices to ignore. For example indices that don't exist.
     * 指定索引选项。比如需要忽略什么类型的索引。例如不存在的索引。
     *
     * @return the desired behaviour regarding indices options
     */
    @Override
    public IndicesOptions indicesOptions() {
        return indicesOptions;
    }

    /**
     * Specifies the indices options. Like what type of requested indices to ignore. For example indices that don't exist.
     *
     * @param indicesOptions the desired behaviour regarding indices options
     * @return this request
     */
    public CreateSnapshotRequest indicesOptions(IndicesOptions indicesOptions) {
        this.indicesOptions = indicesOptions;
        return this;
    }


    /**
     * Returns true if indices with unavailable shards should be be partially snapshotted.
     * 如果应该部分快照具有不可用碎片的索引，则返回true。
     *
     * @return the desired behaviour regarding indices options
     */
    public boolean partial() {
        return partial;
    }

    /**
     * Set to true to allow indices with unavailable shards to be partially snapshotted.
     *
     * @param partial true if indices with unavailable shards should be be partially snapshotted.
     * @return this request
     */
    public CreateSnapshotRequest partial(boolean partial) {
        this.partial = partial;
        return this;
    }

    /**
     * If set to true the operation should wait for the snapshot completion before returning.
     * 如果设置为true，则操作应在返回前等待快照完成。
     *
     * By default, the operation will return as soon as snapshot is initialized. It can be changed by setting this
     * flag to true.
     * 默认情况下，快照初始化后，操作将立即返回。可以通过将此标志设置为true来更改它。
     *
     * @param waitForCompletion true if operation should wait for the snapshot completion
     * @return this request
     */
    public CreateSnapshotRequest waitForCompletion(boolean waitForCompletion) {
        this.waitForCompletion = waitForCompletion;
        return this;
    }

    /**
     * Returns true if the request should wait for the snapshot completion before returning
     * 如果请求在返回前应等待快照完成，则返回true
     *
     * @return true if the request should wait for completion
     */
    public boolean waitForCompletion() {
        return waitForCompletion;
    }

    /**
     * Sets repository-specific snapshot settings.  设置特定于存储库的快照设置。
     * <p>
     * See repository documentation for more information.   有关更多信息，请参见存储库文档。
     *
     * @param settings repository-specific snapshot settings
     * @return this request
     */
    public CreateSnapshotRequest settings(Settings settings) {
        this.settings = settings;
        return this;
    }

    /**
     * Sets repository-specific snapshot settings.
     * <p>
     * See repository documentation for more information.
     *
     * @param settings repository-specific snapshot settings
     * @return this request
     */
    public CreateSnapshotRequest settings(Settings.Builder settings) {
        this.settings = settings.build();
        return this;
    }

    /**
     * Sets repository-specific snapshot settings in JSON or YAML format
     * <p>
     * See repository documentation for more information.
     *
     * @param source repository-specific snapshot settings
     * @return this request
     * @deprecated use {@link #settings(String, XContentType)} to avoid content type detection
     */
    @Deprecated
    public CreateSnapshotRequest settings(String source) {
        this.settings = Settings.builder().loadFromSource(source).build();
        return this;
    }

    /**
     * Sets repository-specific snapshot settings in JSON or YAML format
     * <p>
     * See repository documentation for more information.
     *
     * @param source repository-specific snapshot settings
     * @param xContentType the content type of the source
     * @return this request
     */
    public CreateSnapshotRequest settings(String source, XContentType xContentType) {
        this.settings = Settings.builder().loadFromSource(source, xContentType).build();
        return this;
    }

    /**
     * Sets repository-specific snapshot settings.
     * <p>
     * See repository documentation for more information.
     *
     * @param source repository-specific snapshot settings
     * @return this request
     */
    public CreateSnapshotRequest settings(Map<String, Object> source) {
        try {
            XContentBuilder builder = XContentFactory.contentBuilder(XContentType.JSON);
            builder.map(source);
            settings(builder.string(), builder.contentType());
        } catch (IOException e) {
            throw new ElasticsearchGenerationException("Failed to generate [" + source + "]", e);
        }
        return this;
    }

    /**
     * Returns repository-specific snapshot settings
     *
     * @return repository-specific snapshot settings
     */
    public Settings settings() {
        return this.settings;
    }

    /**
     * Set to true if global state should be stored as part of the snapshot
     * 如果全局状态应该作为快照的一部分存储，则将其设置为true
     *
     * @param includeGlobalState true if global state should be stored
     * @return this request
     */
    public CreateSnapshotRequest includeGlobalState(boolean includeGlobalState) {
        this.includeGlobalState = includeGlobalState;
        return this;
    }

    /**
     * Returns true if global state should be stored as part of the snapshot
     *
     * @return true if global state should be stored as part of the snapshot
     */
    public boolean includeGlobalState() {
        return includeGlobalState;
    }

    /**
     * Parses snapshot definition.  解析快照的定义。
     *
     * @param source snapshot definition
     * @return this request
     */
    public CreateSnapshotRequest source(Map<String, Object> source) {
        for (Map.Entry<String, Object> entry : ((Map<String, Object>) source).entrySet()) {
            String name = entry.getKey();
            if (name.equals("indices")) {
                if (entry.getValue() instanceof String) {
                    indices(Strings.splitStringByCommaToArray((String) entry.getValue()));
                } else if (entry.getValue() instanceof ArrayList) {
                    indices((ArrayList<String>) entry.getValue());
                } else {
                    // 格式不正确的索引段，应该是字符串数组
                    throw new IllegalArgumentException("malformed indices section, should be an array of strings");
                }
            } else if (name.equals("partial")) {
                partial(lenientNodeBooleanValue(entry.getValue(), name));
            } else if (name.equals("settings")) {
                if (!(entry.getValue() instanceof Map)) {
                    throw new IllegalArgumentException("malformed settings section, should indices an inner object");
                }
                settings((Map<String, Object>) entry.getValue());
            } else if (name.equals("include_global_state")) {
                includeGlobalState = lenientNodeBooleanValue(entry.getValue(), name);
            }
        }
        indicesOptions(IndicesOptions.fromMap((Map<String, Object>) source, IndicesOptions.lenientExpandOpen()));
        return this;
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
        snapshot = in.readString();
        repository = in.readString();
        indices = in.readStringArray();
        indicesOptions = IndicesOptions.readIndicesOptions(in);
        settings = readSettingsFromStream(in);
        includeGlobalState = in.readBoolean();
        waitForCompletion = in.readBoolean();
        partial = in.readBoolean();
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeString(snapshot);
        out.writeString(repository);
        out.writeStringArray(indices);
        indicesOptions.writeIndicesOptions(out);
        writeSettingsToStream(settings, out);
        out.writeBoolean(includeGlobalState);
        out.writeBoolean(waitForCompletion);
        out.writeBoolean(partial);
    }

    @Override
    public String getDescription() {
        return "snapshot [" + repository + ":" + snapshot + "]";
    }
}
