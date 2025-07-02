package com.aeotrade.provider.admin.entiy;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * <p>
 * 单一窗口认证表
 * </p>
 *
 * @author aeo
 * @since 2023-11-02
 */
@Getter
@Setter
@TableName("sgs_sw_info")
public class SgsSwInfo {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long memberId;

    /**
     * 认证状态(0:未认证,1:认证中,2:认证成功)
     */
    private Integer sgsStatus;

    /**
     * 乐观锁
     */
    private Integer revision;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;

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
     * 企业名称
     */
    private String memberName;

    /**
     * uscc
     */
    private String uscc;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 密码
     */
    private String passWord;

    /**
     * 认证信息ID
     */
    private Long sgsApplyId;
}
