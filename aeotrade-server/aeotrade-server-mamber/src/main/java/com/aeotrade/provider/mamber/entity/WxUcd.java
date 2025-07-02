package com.aeotrade.provider.mamber.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 *
 * @Author: yewei
 * @Date: 2020/3/30 15:00
 */
@Data
@TableName("wx_ucd")
@EqualsAndHashCode(callSuper = true)
public class WxUcd extends Model<WxUcd>{
    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.AUTO)
    private Long id;
    private String titleImage;
    private String mediaId;
    private String title;
    private String thumbMediaId;
    private String showCoverPic;
    private String author;
    private String digest;
    private String content;
    private String url;
    private String contentSourceUrl;
    private String type;
    private String description;
    private String downUrl;
    private String  name;
    private Date createdTime;
    private Date updateTime;
    private Long cid;
    private String icon;
    private String iconHover;



}
