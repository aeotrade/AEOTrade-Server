package com.aeotrade.provider.mamber.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
/**
 * 文章分类
 * @Author: yewei
 * @Date: 2020/3/30 19:38
 */
@Data
@TableName("wx_cat")
@EqualsAndHashCode(callSuper = true)
public class WxCat extends Model<WxCat>{
    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Integer status;		//状态
    private Integer sortOrder;	//商品分类排序号
    private Date created;
    private Date updated;
}
