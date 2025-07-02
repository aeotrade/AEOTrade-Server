package com.aeotrade.provider.admin.entiy;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @Auther: 吴浩
 * @Date: 2023-03-17 14:53
 */

@Data
@TableName("uac_white_list")
public class UacWhiteList {



    private Long id;


    private String ip;


    private Integer status;
}
