package com.aeotrade.provider.admin.event;

import com.aeotrade.provider.admin.entiy.UacMember;
import com.aeotrade.provider.admin.entiy.UawVipType;
import org.springframework.context.ApplicationEvent;

/**
 * 添加企业和用户事件
 */
public class AddMemberEvent extends ApplicationEvent {
    private UacMember uacMember;
    private UawVipType vipType;
    public AddMemberEvent(Object source, UacMember uacMember,UawVipType vipType) {
        super(source);
        this.uacMember = uacMember;
        this.vipType = vipType;
    }
    public UacMember getUacMember() {
        return uacMember;
    }
    public UawVipType getVipType() {
        return vipType;
    }
}
