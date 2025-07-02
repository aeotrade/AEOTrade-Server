package com.aeotrade.provider.admin.entiy;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 角色与单证文件关联表
 * </p>
 *
 * @author aeo
 * @since 2023-11-10
 */
@Getter
@Setter
@TableName("uac_role_docment")
public class UacRoleDocment {

    private Long id;

    /**
     * 角色id
     */
    private Long roleId;

    /**
     * 文件单证类型id
     */
    private Long documentTypeId;

    /**
     * 文件单证类型名称
     */
    private String documentTypeName;

    /**
     * 文件单证类型标识
     */
    private String documentTypeNameEn;

    /**
     * 可见范围
     */
    private String visibleRange;

    /**
     * 可见字段
     */
    private String visibleField;

    /**
     * 是否可以上传
     */
    private Integer isUploading;

    /**
     * 是否可以下载
     */
    private Integer isDownload;

    /**
     * 是否可以删除
     */
    private Integer isDelete;

    /**
    /**
     * 是否可以导出
     */
    private Integer isExport;

    /**
     * 是否可见
     */
    private Integer isLook;
}
