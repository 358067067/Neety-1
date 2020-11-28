package com.hzj.codec;

import com.alibaba.fastjson.JSON;

public enum FastJsonSerializer implements Serializer{

    //单例实现
    x;

    @Override
    public byte[] encode(Object target) {
        return JSON.toJSONBytes(target);
    }

    @Override
    public Object decode(byte[] bytes, Class<?> targetClass) {
        return JSON.parseObject(bytes, targetClass);
    }

}
