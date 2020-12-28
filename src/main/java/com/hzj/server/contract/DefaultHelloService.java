package com.hzj.server.contract;

import com.hzj.contract.HelloService;
import org.springframework.stereotype.Service;

// 实现
@Service
public class DefaultHelloService implements HelloService {

    @Override
    public String sayHello(String name) {
        return String.format("%s say hello!", name);
    }
}
