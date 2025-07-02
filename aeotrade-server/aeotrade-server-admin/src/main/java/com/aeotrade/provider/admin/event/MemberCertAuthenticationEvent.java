package com.aeotrade.provider.admin.event;

import com.aeotrade.provider.admin.entiy.SgsCertInfo;
import org.springframework.context.ApplicationEvent;

/**
 * 用户证件认证事件
 */
public class MemberCertAuthenticationEvent extends ApplicationEvent {
    private SgsCertInfo sgsCertInfo;
    private String[] recipient;
    public MemberCertAuthenticationEvent(Object source, SgsCertInfo sgsCertInfo, String[] recipient) {
        super(source);
        this.sgsCertInfo = sgsCertInfo;
        this.recipient = recipient;
    }
    public SgsCertInfo getSgsCertInfo() {
        return sgsCertInfo;
    }
    public String[] getRecipient() {
        return recipient;
    }
}
