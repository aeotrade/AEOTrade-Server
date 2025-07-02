package com.aeotrade.server.message.service;

import com.aeotrade.server.message.model.MsgMessage;
import com.aeotrade.server.message.model.MsgMessageUser;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Auther: 吴浩
 * @Date: 2024-02-07 11:08
 */
public interface MsgMessageUserService extends IService<MsgMessageUser> {

    public void startMessage(MsgMessage msgMessage);
}
