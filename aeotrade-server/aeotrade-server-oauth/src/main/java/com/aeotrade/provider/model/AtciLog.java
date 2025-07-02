package com.aeotrade.provider.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * <p>
 * 单证网盘日志
 * </p>
 *
 * @author aeo
 * @since 2023-11-03
 */
@Getter
@Setter
@TableName("atci_log")
public class AtciLog {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 1.最近业务档案,2.最近在线单证,3.最近文件单证
     */
    private Integer billWay;

    /**
     * 企业Id
     */
    private Long memberId;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 乐观锁
     */
    private Integer revision;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 数据状态
     */
    private Integer status;

    /**
     * 业务唯一标识
     */
    private String soleId;

    /**
     * 业务类型
     */
    private Integer businessType;

    /**
     * 更新方式
     */
    private String updateMode;

    /**
     * 单证编号
     */
    private String documentCode;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 单证类型ID
     */
    private Long archiveDocumentTypeId;

    /**
     * 单证格式ID
     */
    private Long archiveDocumentFormId;

    private Integer billCount;

    private String billDate;

    private Integer billType;

    private String memberUscc;
}
