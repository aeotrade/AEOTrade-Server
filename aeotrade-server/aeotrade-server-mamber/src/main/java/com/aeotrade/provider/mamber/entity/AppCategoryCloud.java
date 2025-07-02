package com.aeotrade.provider.mamber.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * @Auther: 吴浩
 * @Date: 2025/2/19 13:53
 */
@Getter
@Setter
@TableName("app_category_cloud")
public class AppCategoryCloud {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 应用id
     */
    private Long cloudId;


    /**
     * 类目id
     */
    private Long categoryId;
}
