package com.hzj.server;

public interface MethodMatcher {

    MethodMatchOutput selectOneBestMatchMethod(MethodMatchInput input);
}
