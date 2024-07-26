package com.oneinstep.myrpc.core.codec;

import com.oneinstep.myrpc.core.serialize.SerializeUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * RPC encoder
 */
@Slf4j
public class RpcEncoder extends MessageToByteEncoder<Object> {
    /**
     * Generic class type
     */
    private final Class<?> genericClass;

    public RpcEncoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) {
        // If the generic class is an instance of the message, serialize the message
        if (genericClass.isInstance(msg)) {
            // Serialize the message
            byte[] bytes = SerializeUtil.serialize(msg);
            // Write the length of the serialized data
            out.writeInt(bytes.length);
            out.writeBytes(bytes);
        }
    }

}