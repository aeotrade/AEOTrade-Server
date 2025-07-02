package com.aeotrade.provider.mamber.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Author: yewei
 * @Date: 2020/3/31 19:20
 */
@Data
@AllArgsConstructor
public class AticlesDto {
    private String title;

    private String thumb_media_id;

    private String author;

    private String digest;

    private String show_cover_pic;

    private String content;

//    private String content_source_url;
    private String contentSourceUrl;

    private String need_open_comment;

    private String only_fans_can_comment;
    private String description;
    private String icon;
    private String iconHover;
}
