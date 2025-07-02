package com.aeotrade.provider.admin.entiy;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author aeo
 * @since 2023-11-10
 */
@Getter
@Setter
@TableName("client_version")
public class ClientVersion implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 版本名称
     */
    private String versionName;

    /**
     * 版本号
     */
    private String versionNumber;

    /**
     * 是否强制更新0否1是
     */
    private Integer isCoerceUpdate;

    /**
     * 版本更新内容
     */
    private String versionContent;

    /**
     * 版本备注
     */
    private String versionRemark;

    /**
     * oss下载地址
     */
    private String downloadOss;

    /**
     * 删除
     */
    private Integer status;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime creatTime;
    /**
     * 更新方式1全量更新2增量更新
     */
    private Integer updateMode;

    /**
     * 是否是公开版本0否1是
     */
    private Integer isPublic;
    /**
     * 是否可以更新0否1是
     */
    private Integer isUpdate;
    /**
     * 终端类型，默认空为原保管箱应用
     */
    private String appType;
}
