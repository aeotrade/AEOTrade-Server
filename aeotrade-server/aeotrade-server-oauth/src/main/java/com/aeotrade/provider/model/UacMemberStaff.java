package com.aeotrade.provider.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author aeo
 * @since 2023-11-03
 */
@Getter
@Setter
@TableName("uac_member_staff")
public class UacMemberStaff {

    private Long id;

    private Long memberId;

    private Long staffId;

    /**
     * 是否是子管理员0->不是1->是
     */
    private Integer isAdmin;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 企业类型
     */
    private Long kindId;
}
