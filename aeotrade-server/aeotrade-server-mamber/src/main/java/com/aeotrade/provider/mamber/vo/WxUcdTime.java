package com.aeotrade.provider.mamber.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: yewei
 * @Date: 2020/6/15 14:10
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WxUcdTime {
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
    private long  createTime;
    private long  updateTime;
    private Long cid;
    private String icon;
    private String iconHover;

}
