package com.hzj.utils;

import java.util.UUID;

public enum SerialNumberUtils {

    // 单例;
    X;

    public String generateSerialNumber() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}