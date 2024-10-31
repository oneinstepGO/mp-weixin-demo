package com.oneinstep.myrpc.core.codec;

import com.oneinstep.myrpc.core.serialize.SerializeUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * RPC decoder
 */
@Slf4j
public class RpcDecoder extends ByteToMessageDecoder {

    /**
     * Generic class type
     */
    private final Class<?> genericClass;

    public RpcDecoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        // If the readable length of the ByteBuf is less than 4, return directly
        if (in.readableBytes() < 4) {
            return;
        }
        // 由于使用了 LengthFieldBasedFrameDecoder 解码器，前 4 个字节是消息的长度
        byte[] data = new byte[in.readableBytes()];
        in.readBytes(data);

        // 反序列化
        Object deserialize = SerializeUtil.deserialize(data, genericClass);
        out.add(deserialize);
    }
}
