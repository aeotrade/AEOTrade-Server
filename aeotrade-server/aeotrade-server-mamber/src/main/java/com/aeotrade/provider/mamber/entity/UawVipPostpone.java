package com.aeotrade.provider.mamber.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author aeo
 * @since 2023-11-07
 */
@Getter
@Setter
@TableName("uaw_vip_postpone")
public class UawVipPostpone {

    private Long id;

    /**
     * 操作人id
     */
    private Long operatorId;

    /**
     * 操作人姓名
     */
    private String operator;

    /**
     * 操作时间
     */
    private LocalDateTime operatorTime;

    /**
     * 被操作企业id
     */
    private Long memberId;

    /**
     * 被操作企业名称
     */
    private String memberName;

    /**
     * 操作会员等级编号
     */
    private String classSerialNumber;

    /**
     * 操作会员等级名称
     */
    private String mamberName;

    /**
     * 延期天数（单位：天）
     */
    private Integer day;

    /**
     * 会员类型名称
     */
    private String vipTypeName;

    /**
     * 会员类型名称
     */
    private Long vipTypeId;
}
