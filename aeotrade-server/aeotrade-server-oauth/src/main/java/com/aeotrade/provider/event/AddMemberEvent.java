package com.aeotrade.provider.event;

import org.springframework.context.ApplicationEvent;

import java.util.Map;

public class AddMemberEvent extends ApplicationEvent {
    private Map<String, Object> map;
    public AddMemberEvent(Object source,Map<String, Object> map) {
        super(source);
        this.map = map;
    }
    public Map<String, Object> getMap() {
        return map;
    }
}
