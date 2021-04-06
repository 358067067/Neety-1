package com.hzj.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Component
@Aspect
@Slf4j
public class AopUtils {

    @Pointcut("execution(public * com.hzj.server.contract.*.*(..))")
    public void cut(){};

    @Before(value = "cut()")
    public void doBefore(JoinPoint joinPoint) {
        log.info("调用到了代理方法");
    }
}
