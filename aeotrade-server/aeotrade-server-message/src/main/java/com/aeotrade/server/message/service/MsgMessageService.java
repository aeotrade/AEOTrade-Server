package com.aeotrade.server.message.service;

import com.aeotrade.server.message.model.MsgMessage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Auther: 吴浩
 * @Date: 2024-02-07 11:06
 */
public interface MsgMessageService extends IService<MsgMessage> {
    void insert(MsgMessage msgMessage);
}
