package com.aeotrade.provider.event;

import com.aeotrade.provider.model.UacMember;
import com.aeotrade.provider.model.UacStaff;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

/**
 * 添加员工事件
 */
@Setter
@Getter
public class AddStaffEvent extends ApplicationEvent {
    private UacMember uacMember;
    private UacStaff uacStaff;
    private String userType;
    public AddStaffEvent(Object source,  UacMember uacMember, UacStaff uacStaff, String userType) {
        super(source);
        this.uacMember = uacMember;
        this.uacStaff = uacStaff;
        this.userType = userType;
    }

}
