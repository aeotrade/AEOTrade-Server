package com.aeotrade.server.log.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * <p>
 *
 * </p>
 *
 * @author yewei
 * @since 2022-10-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user_document_log")
public class UserDocumentLog extends Model<UserDocumentLog> {

    private static final long serialVersionUID = 1L;
    private Long id;

    //企业名称
    private String memberName;

    //单证类型code
    private String fileTypeCode;

    //单证类型
    private String fileType;

    //数据来源
    private String datasourceTypeCode;

    //采集时间
    private Timestamp createTime;

    @Override
    public Serializable pkVal() {
        return null;
    }

}
