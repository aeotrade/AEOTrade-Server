package com.aeotrade.provider.mamber.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 企业店铺
 * </p>
 *
 * @author aeo
 * @since 2023-11-07
 */
@Getter
@Setter
@TableName("uac_member_shop")
public class UacMemberShop {

    private Long id;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 店铺描述
     */
    private String shopDescription;

    /**
     * 店铺图标
     */
    private String shopLogo;

    /**
     * 店铺宣传图
     */
    private String shopBanner;

    /**
     * 开通状态
     */
    private Integer shopStatus;

    /**
     * 企业ID
     */
    private Long memberId;

    /**
     * 企业名称
     */
    private String memberName;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 企业类型
     */
    private Long kindId;

    /**
     * 企业图片
     */
    private String logoImg;

    /**
     * 店铺类型
     */
    private String shopType;

    /**
     * 乐观锁
     */
    private Integer revision;

    /**
     * 删除
     */
    private Integer status;
}
