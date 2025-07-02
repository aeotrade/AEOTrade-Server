package com.aeotrade.provider.admin.event;

import com.aeotrade.provider.admin.uacVo.SgsBankDto;
import org.springframework.context.ApplicationEvent;

/**
 * 用户银行认证事件
 */
public class MemberBankAuthenticationEvent extends ApplicationEvent {
    private SgsBankDto sgsBankDto;
    private String[] recipient;
    public MemberBankAuthenticationEvent(Object source, SgsBankDto sgsBankDto, String[] recipient) {
        super(source);
        this.sgsBankDto = sgsBankDto;
        this.recipient = recipient;
    }
    public SgsBankDto getSgsBankDto() {
        return sgsBankDto;
    }
    public String[] getRecipient() {
        return recipient;
    }
}
