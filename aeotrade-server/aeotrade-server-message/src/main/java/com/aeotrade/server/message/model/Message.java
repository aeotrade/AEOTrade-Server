package com.aeotrade.server.message.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author: yewei
 * @Date: 10:50 2021/2/20
 * @Description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message implements Serializable {
    private String staffId;
    private String id;
    private String event;
    private String data;
}
