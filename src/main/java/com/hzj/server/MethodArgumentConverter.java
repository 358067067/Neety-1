package com.hzj.server;

/**
 * 方法参数转换器接口
 */
public interface MethodArgumentConverter {
    ArgumentConvertOutput convert(ArgumentConvertInput input);
}
