package com.aeotrade.server.log.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * <p>
 * 
 * </p>
 *
 * @author yewei
 * @since 2022-10-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user_log_info")
public class UserLogInfo extends Model<UserLogInfo> {

    private static final long serialVersionUID = 1L;

    private Long id;

    //操作人名称
    private String userName;

    //操作企业名称
    private String menberName;

    //页面url
    private String webUrl;

    //用户ip地址
    private String ip;

    //请求时间
    private Timestamp requestTime;

    //请求url
    private String requestUrl;

    //请求类型
    private String requestType;

    //请求参数
    private String requestParameter;

    //请求属性
    private String requestNature;

    //请求接口名称
    private String urlName;

    //页面名称
    private String webName;

    //所属系统
    private String webSys;

    //ip所属地
    private String ipAddress;

    //慧贸OS组织id
    private String memberId;

    //慧贸OS组织统一社会信用代码
    private String memberUscc;

    //ip所在省/市
    private String ipProvince;

    //ip所在市/区
    private String ipCity;

    @Override
    public Serializable pkVal() {
        return null;
    }

}
