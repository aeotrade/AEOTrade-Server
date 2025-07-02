package com.aeotrade.provider.admin.adminVo;

import lombok.Getter;
import lombok.Setter;

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
public class SysThemeVo {


    private Integer id;

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
