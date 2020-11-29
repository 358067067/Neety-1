package com.hzj.protocol;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ResponseMessagePacket extends BaseMessagePacket {

    /**
     * error code
     */
    private Long errorCode;

    /**
     * 消息描述
     */
    private String message;

    /**
     * 消息载荷
     */
    private Object payload;
}