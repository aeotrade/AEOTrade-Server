package com.aeotrade.provider.admin.entiy;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * <p>
 * 企业员工
 * </p>
 *
 * @author aeo
 * @since 2023-11-02
 */
@Getter
@Setter
@TableName("uac_staff")
public class UacStaff {

    /**
     * ID
     */
    private Long id;

    /**
     * 企业ID
     */
    private Long memberId;

    /**
     * 客服名称
     */
    private String staffName;

    /**
     * 个人实名认证状态0未绑定1绑定
     */
    private String staffStatus;

    /**
     * 登录帐号
     */
    private String loginAccount;

    /**
     * 登录密码
     */
    private String loginPaswd;

    /**
     * 微信OpenId
     */
    private String wxOpenid;

    /**
     * 头像
     */
    private String wxLogo;

    /**
     * 乐观锁
     */
    private Integer revision;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;

    /**
     * 更新人
     */
    private String updatedBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;

    /**
     * 删除
     */
    private Integer status;

    /**
     * 部门
     */
    private String dept;

    /**
     * 角色 管理员 :admin 普通员工:user
     */
    private String role;

    /**
     * 绑定微信状态 未绑定 0 ;已绑定 1
     */
    private Integer sgsStatus;

    /**
     * 手机号
     */
    private String tel;

    /**
     * 微信unionid
     */
    private String wxUnionid;

    /**
     * 联系人邮箱
     */
    private String contactEmail;

    /**
     * 联系人QQ
     */
    private String contactQq;

    /**
     * 联系人微信ID
     */
    private String contactWeixin;

    /**
     * 联系人微信二维码
     */
    private String contactWeixinqr;

    /**
     * 0:个人；1：企业
     */
    private Integer staffType;

    /**
     * 认证状态
     */
    private Integer authStatus;

    /**
     * 渠道0单证网盘
     */
    private String channelMark;

    /**
     * 来源0慧贸OS1北京单一窗口
     */
    private String sourceMark;

    /**
     * 是否第一次登录0否1是
     */
    private Integer isLogin;

    /**
     * 用户最后访问工作台id
     */
    private Long lastWorkbenchId;

    /**
     * 用户最后访问企业id
     */
    private Long lastMemberId;

    /**
     * 频道栏目id
     */
    private Long channelColumnsId;
}
