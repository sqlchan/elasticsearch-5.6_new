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

import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.support.WriteRequest.RefreshPolicy;
import org.elasticsearch.action.support.WriteResponse;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.io.stream.Writeable;
import org.elasticsearch.common.xcontent.StatusToXContentObject;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.rest.RestStatus;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;

import static org.elasticsearch.common.xcontent.XContentParserUtils.ensureExpectedToken;

/**
 * A base class for the response of a write operation that involves a single doc
 * 涉及单个文档的写入操作响应的基类
 */
public abstract class DocWriteResponse extends ReplicationResponse implements WriteResponse, StatusToXContentObject {

    private static final String _SHARDS = "_shards";
    private static final String _INDEX = "_index";
    private static final String _TYPE = "_type";
    private static final String _ID = "_id";
    private static final String _VERSION = "_version";
    private static final String RESULT = "result";
    private static final String FORCED_REFRESH = "forced_refresh";

    /**
     * An enum that represents the the results of CRUD operations, primarily used to communicate the type of
     * operation that occurred.
     * 表示CRUD操作结果的枚举，主要用于通信发生的操作类型。
     */
    public enum Result implements Writeable {
        CREATED(0),
        UPDATED(1),
        DELETED(2),
        NOT_FOUND(3),
        NOOP(4);

        private final byte op;
        private final String lowercase;

        Result(int op) {
            this.op = (byte) op;
            this.lowercase = this.toString().toLowerCase(Locale.ENGLISH);
        }

        public byte getOp() {
            return op;
        }

        public String getLowercase() {
            return lowercase;
        }

        public static Result readFrom(StreamInput in) throws IOException{
            Byte opcode = in.readByte();
            switch(opcode){
                case 0:
                    return CREATED;
                case 1:
                    return UPDATED;
                case 2:
                    return DELETED;
                case 3:
                    return NOT_FOUND;
                case 4:
                    return NOOP;
                default:
                    throw new IllegalArgumentException("Unknown result code: " + opcode);
            }
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
            out.writeByte(op);
        }
    }

    private ShardId shardId;
    private String id;
    private String type;
    private long version;
    private boolean forcedRefresh;
    protected Result result;

    public DocWriteResponse(ShardId shardId, String type, String id, long version, Result result) {
        this.shardId = shardId;
        this.type = type;
        this.id = id;
        this.version = version;
        this.result = result;
    }

    // needed for deserialization
    protected DocWriteResponse() {
    }

    /**
     * The change that occurred to the document.
     */
    public Result getResult() {
        return result;
    }

    /**
     * The index the document was changed in.
     */
    public String getIndex() {
        return this.shardId.getIndexName();
    }

    /**
     * The exact shard the document was changed in.
     */
    public ShardId getShardId() {
        return this.shardId;
    }

    /**
     * The type of the document changed.
     */
    public String getType() {
        return this.type;
    }

    /**
     * The id of the document changed.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Returns the current version of the doc.
     */
    public long getVersion() {
        return this.version;
    }

    /**
     * Did this request force a refresh? Requests that set {@link WriteRequest#setRefreshPolicy(RefreshPolicy)} to
     * {@link RefreshPolicy#IMMEDIATE} will always return true for this. Requests that set it to {@link RefreshPolicy#WAIT_UNTIL} will
     * only return true here if they run out of refresh listener slots (see {@link IndexSettings#MAX_REFRESH_LISTENERS_PER_SHARD}).
     * 此请求是否强制刷新？将@link writerequest set refreshpolicy（refreshpolicy）设置为@link refreshpolicy immediate的请求将始终为此返回true。
     * 将其设置为@link refreshpolicy等待的请求只有在刷新侦听器插槽用完时才会在此处返回true（请参见@link indexsettings max refresh _listeners _per shard）。
     */
    public boolean forcedRefresh() {
        return forcedRefresh;
    }

    @Override
    public void setForcedRefresh(boolean forcedRefresh) {
        this.forcedRefresh = forcedRefresh;
    }

    /** returns the rest status for this response (based on {@link ShardInfo#status()}
     * 返回此响应的其余状态（基于@link shardinfo status（）*/
    @Override
    public RestStatus status() {
        return getShardInfo().status();
    }

    /**
     * Return the relative URI for the location of the document suitable for use in the {@code Location} header. The use of relative URIs is
     * permitted as of HTTP/1.1 (cf. https://tools.ietf.org/html/rfc7231#section-7.1.2).
     * 返回适合在@code location头中使用的文档位置的相对URI。
     * 从http/1.1开始允许使用相对URI（参见https://tools.ietf.org/html/rfc7231第7.1.2节）。
     *
     * @param routing custom routing or {@code null} if custom routing is not used
     * @return the relative URI for the location of the document
     */
    public String getLocation(@Nullable String routing) {
        final String encodedIndex;
        final String encodedType;
        final String encodedId;
        final String encodedRouting;
        try {
            // encode the path components separately otherwise the path separators will be encoded
            // 编码的路径组件分离器分别否则将路径编码
            encodedIndex = URLEncoder.encode(getIndex(), "UTF-8");
            encodedType = URLEncoder.encode(getType(), "UTF-8");
            encodedId = URLEncoder.encode(getId(), "UTF-8");
            encodedRouting = routing == null ? null : URLEncoder.encode(routing, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
        final String routingStart = "?routing=";
        final int bufferSizeExcludingRouting = 3 + encodedIndex.length() + encodedType.length() + encodedId.length();
        final int bufferSize;
        if (encodedRouting == null) {
            bufferSize = bufferSizeExcludingRouting;
        } else {
            bufferSize = bufferSizeExcludingRouting + routingStart.length() + encodedRouting.length();
        }
        final StringBuilder location = new StringBuilder(bufferSize);
        location.append('/').append(encodedIndex);
        location.append('/').append(encodedType);
        location.append('/').append(encodedId);
        if (encodedRouting != null) {
            location.append(routingStart).append(encodedRouting);
        }

        return location.toString();
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
        shardId = ShardId.readShardId(in);
        type = in.readString();
        id = in.readString();
        version = in.readZLong();
        forcedRefresh = in.readBoolean();
        result = Result.readFrom(in);
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        shardId.writeTo(out);
        out.writeString(type);
        out.writeString(id);
        out.writeZLong(version);
        out.writeBoolean(forcedRefresh);
        result.writeTo(out);
    }

    @Override
    public final XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject();
        innerToXContent(builder, params);
        builder.endObject();
        return builder;
    }

    public XContentBuilder innerToXContent(XContentBuilder builder, Params params) throws IOException {
        ReplicationResponse.ShardInfo shardInfo = getShardInfo();
        builder.field(_INDEX, shardId.getIndexName())
                .field(_TYPE, type)
                .field(_ID, id)
                .field(_VERSION, version)
                .field(RESULT, getResult().getLowercase());
        if (forcedRefresh) {
            builder.field(FORCED_REFRESH, true);
        }
        builder.field(_SHARDS, shardInfo);
        return builder;
    }

    /**
     * Parse the output of the {@link #innerToXContent(XContentBuilder, Params)} method.
     *
     * This method is intended to be called by subclasses and must be called multiple times to parse all the information concerning
     * {@link DocWriteResponse} objects. It always parses the current token, updates the given parsing context accordingly
     * if needed and then immediately returns.
     */
    protected static void parseInnerToXContent(XContentParser parser, Builder context) throws IOException {
        XContentParser.Token token = parser.currentToken();
        ensureExpectedToken(XContentParser.Token.FIELD_NAME, token, parser::getTokenLocation);

        String currentFieldName = parser.currentName();
        token = parser.nextToken();

        if (token.isValue()) {
            if (_INDEX.equals(currentFieldName)) {
                // index uuid and shard id are unknown and can't be parsed back for now.
                context.setShardId(new ShardId(new Index(parser.text(), IndexMetaData.INDEX_UUID_NA_VALUE), -1));
            } else if (_TYPE.equals(currentFieldName)) {
                context.setType(parser.text());
            } else if (_ID.equals(currentFieldName)) {
                context.setId(parser.text());
            } else if (_VERSION.equals(currentFieldName)) {
                context.setVersion(parser.longValue());
            } else if (RESULT.equals(currentFieldName)) {
                String result = parser.text();
                for (Result r :  Result.values()) {
                    if (r.getLowercase().equals(result)) {
                        context.setResult(r);
                        break;
                    }
                }
            } else if (FORCED_REFRESH.equals(currentFieldName)) {
                context.setForcedRefresh(parser.booleanValue());
            }
        } else if (token == XContentParser.Token.START_OBJECT) {
            if (_SHARDS.equals(currentFieldName)) {
                context.setShardInfo(ShardInfo.fromXContent(parser));
            } else {
                parser.skipChildren(); // skip potential inner objects for forward compatibility
            }
        } else if (token == XContentParser.Token.START_ARRAY) {
            parser.skipChildren(); // skip potential inner arrays for forward compatibility
        }
    }

    /**
     * Base class of all {@link DocWriteResponse} builders. These {@link DocWriteResponse.Builder} are used during
     * xcontent parsing to temporarily store the parsed values, then the {@link Builder#build()} method is called to
     * instantiate the appropriate {@link DocWriteResponse} with the parsed values.
     */
    public abstract static class Builder {

        protected ShardId shardId = null;
        protected String type = null;
        protected String id = null;
        protected Long version = null;
        protected Result result = null;
        protected boolean forcedRefresh;
        protected ShardInfo shardInfo = null;

        public ShardId getShardId() {
            return shardId;
        }

        public void setShardId(ShardId shardId) {
            this.shardId = shardId;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public void setVersion(Long version) {
            this.version = version;
        }

        public void setResult(Result result) {
            this.result = result;
        }

        public void setForcedRefresh(boolean forcedRefresh) {
            this.forcedRefresh = forcedRefresh;
        }

        public void setShardInfo(ShardInfo shardInfo) {
            this.shardInfo = shardInfo;
        }

        public abstract DocWriteResponse build();
    }
}
