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

        // If the readable bytes is less than 4, it means that the data length is not yet complete
        if (in.readableBytes() < 4) {
            return;
        }

        // Mark the current read index so that we can reset it when needed
        in.markReaderIndex();
        // 读取数据长度
        int dataLength = in.readInt();
        if (dataLength < 0) {
            ctx.close();
            return;
        }

        // If the readable bytes is less than the data length, it means that the data is not yet complete
        if (in.readableBytes() < dataLength) {
            in.resetReaderIndex();
            return;
        }

        // Read the data
        byte[] data = new byte[dataLength];
        in.readBytes(data);

        // Convert the data to an object
        Object deserialize = SerializeUtil.deserialize(data, genericClass);
        out.add(deserialize);
    }
}
