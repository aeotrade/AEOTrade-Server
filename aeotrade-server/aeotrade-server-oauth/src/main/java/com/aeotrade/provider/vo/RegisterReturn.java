package com.aeotrade.provider.vo;

import com.aeotrade.provider.model.UacStaff;
import lombok.Data;

import java.io.Serializable;

/**
 * @Auther: 吴浩
 * @Date: 2021/4/26 11:08
 */
@Data
public class RegisterReturn implements Serializable {

    private UacStaff uacStaff;
    private Long memberId;

    public RegisterReturn(UacStaff uacStaff, Long memberId) {
        this.uacStaff = uacStaff;
        this.memberId = memberId;
    }
}
