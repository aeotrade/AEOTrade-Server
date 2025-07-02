package com.aeotrade.provider.mamber.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
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
@TableName("uaw_workbench")
public class UawWorkbench {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 工作台名称
     */
    private String workbenchName;

    /**
     * 工作台图标
     */
    private String ico;

    /**
     * 删除
     */
    private Integer status;

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
    private LocalDateTime createdTime;

    /**
     * 更新人
     */
    private String updatedBy;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 工作台状态
     */
    private Integer workbenchStatus;

    /**
     * 工作台描述
     */
    private String description;

    /**
     * 登录链接地址
     */
    private String loginUrl;

    /**
     * 频道栏目id
     */
    private Long channelColumnsId;

    /**
     * 广告图
     */
    private String banner;

    /**
     * 所属平台
     */
    private Integer platformType;
}
