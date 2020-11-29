package com.hzj.protocol.serializer;

public interface Serializer {

    byte[] encode(Object target);

    Object decode(byte[] bytes, Class<?> targetClass);
}
