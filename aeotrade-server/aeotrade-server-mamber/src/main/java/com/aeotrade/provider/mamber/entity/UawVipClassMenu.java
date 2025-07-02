package com.aeotrade.provider.mamber.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.data.annotation.Id;


/**
 * @Auther: 吴浩
 * @Date: 2021-12-15 14:04
 */
@Data
@TableName("uaw_vip_class_menu")
public class UawVipClassMenu {


    private Long id;


    private Long classId;


    private Long menuId;
}
