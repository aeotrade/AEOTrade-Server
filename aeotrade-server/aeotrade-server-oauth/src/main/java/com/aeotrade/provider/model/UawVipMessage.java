package com.aeotrade.provider.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * <p>
 * 会员信息表
 * </p>
 *
 * @author aeo
 * @since 2023-11-03
 */
@Getter
@Setter
@TableName("uaw_vip_message")
public class UawVipMessage {

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 企业Id
     */
    private Long memberId;

    /**
     * 员工ID
     */
    private Long staffId;

    /**
     * 删除
     */
    private Integer status;

    /**
     * 乐观锁
     */
    private Integer revision;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 修改时间
     */
    private LocalDateTime updatedTime;

    /**
     * 会员状态0到期1使用中
     */
    private Integer vipStatus;

    /**
     * 会员认证状态0未认证1已认证
     */
    private Integer sgsStatus;

    /**
     * 会员信息
     */
    private String vipDetails;

    /**
     * 会员开始时间
     */
    private LocalDateTime startTime;

    /**
     * 会员结束时间
     */
    private LocalDateTime endTime;

    /**
     * 0个人/1企业
     */
    private Integer userType;

    /**
     * 会员等级编号
     */
    private String classSerialNumber;

    /**
     * 类型id
     */
    private Long typeId;
}
