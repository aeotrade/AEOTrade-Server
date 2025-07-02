package com.aeotrade.provider.admin.entiy;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 企业运营业务类型标签列表
 * </p>
 *
 * @author aeo
 * @since 2023-10-30
 */
@Getter
@Setter
@TableName("atci_archive_business_member_label")
public class AtciArchiveBusinessMemberLabel {

    /**
     * 企业业务标签ID
     */
    private Long id;

    /**
     * 业务标签ID
     */
    private Long archiveBusinessTypeLabelId;

    /**
     * 运营业务类型ID
     */
    private Long archiveBusinessTypeId;

    /**
     * 运营业务名称
     */
    private String name;

    /**
     * 企业ID
     */
    private Long memberId;

    /**
     * 企业信用代码
     */
    private String uscc;

    /**
     * 企业生成业务唯一标识规则
     */
    private String generateIdRules;

    /**
     * 显示与隐藏
     */
    private String showOrHide;

    /**
     * 英文名字
     */
    private String eName;

    /**
     * code
     */
    private String formula;

    /**
     * 对应的字典解释分类
     */
    private String dictionary;
}
