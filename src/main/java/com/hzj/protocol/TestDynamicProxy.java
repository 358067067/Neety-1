package com.hzj.protocol;

import com.alibaba.fastjson.JSON;
import com.hzj.contract.HelloService;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class TestDynamicProxy {

    public static void main(String[] args) {
        Class<HelloService> interfaceKlass = HelloService.class;
        InvocationHandler handler = new HelloServiceImpl(interfaceKlass);
        HelloService helloService = (HelloService) Proxy.newProxyInstance(interfaceKlass.getClassLoader(), new Class[]{interfaceKlass}, handler);
        System.out.println(helloService.sayHello("netty test"));
    }

    @RequiredArgsConstructor
    private static class HelloServiceImpl implements InvocationHandler {

        private final Class<?> interfaceKlass;

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // 这里应该根据方法的返回值类型去决定返回结果
            return String.format("[%s#%s]方法被调用,参数列表:%s", interfaceKlass.getName(), method.getName(),
                    JSON.toJSONString(args));
        }
    }
}
