package com.aeotrade.provider.event;

import com.aeotrade.provider.util.MgLogEntity;
import org.springframework.context.ApplicationEvent;

public class MemberAuthEvent extends ApplicationEvent {
    private MgLogEntity entity;
    public MemberAuthEvent(Object source, MgLogEntity entity) {
        super(source);
        this.entity = entity;
    }
    public MgLogEntity getEntity() {
        return entity;
    }
}
