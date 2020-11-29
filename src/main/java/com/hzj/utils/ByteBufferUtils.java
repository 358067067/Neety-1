package com.hzj.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;

public enum ByteBufferUtils {
    X;

    public void encodeUft8CharSequence(ByteBuf byteBuf, CharSequence charSequence) {
        int writerIndex = byteBuf.writerIndex();
        byteBuf.writeInt(0);
        int length = ByteBufUtil.writeUtf8(byteBuf, charSequence);
        byteBuf.setInt(writerIndex, length);
    }

    public byte[] readBytes(ByteBuf byteBuf) {
        int len = byteBuf.readableBytes();
        byte[] bytes = new byte[len];
        byteBuf.readBytes(bytes);
        byteBuf.release();
        return bytes;
    }
}
