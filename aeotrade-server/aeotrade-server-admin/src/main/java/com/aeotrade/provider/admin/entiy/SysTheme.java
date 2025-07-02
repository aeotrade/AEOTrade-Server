package com.aeotrade.provider.admin.entiy;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author aeo
 * @since 2024-01-04
 */
@Getter
@Setter
@TableName("sys_theme")
public class SysTheme {

    /**
     * ID

     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 类型（1全局、2企业、3个人）
     */
    private Integer type;

    /**
     * 启用状态（0 false、1 true）

     */
    private Integer enabling;

    /**
     * 自定义者标识ID

     */
    private String customizer;

    /**
     * 主题
     */
    private String theme;

    /**
     * 主题配置内容
     */
    private String themeContent;
}
