package com.aeotrade.server.log.service.impl;

import com.aeotrade.server.log.config.AddressUtil;
import com.aeotrade.server.message.model.AdminLogInfo;
import com.aeotrade.server.message.model.RequertEntity;
import com.aeotrade.server.log.mapper.UserAdminLogInfoMapper;
import com.aeotrade.server.log.mapper.UserDocumentLogMapper;
import com.aeotrade.server.log.mapper.UserLogInfoMapper;
import com.aeotrade.server.log.model.AeotradeUserLogInfo;
import com.aeotrade.server.log.model.UserAdminLogInfo;
import com.aeotrade.server.log.model.UserDocumentLog;
import com.aeotrade.server.log.model.UserLogInfo;
import com.aeotrade.server.log.service.UserLogInfoService;
import com.aeotrade.utlis.CommonUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author yewei
 * @since 2022-10-27
 */
@Service
@DS("log")
public class UserLogInfoServiceImpl extends ServiceImpl<UserLogInfoMapper, UserLogInfo> implements UserLogInfoService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private UserDocumentLogMapper userDocumentLogMapper;
    @Autowired
    private UserLogInfoMapper userLogInfoMapper;
    @Autowired
    private UserAdminLogInfoMapper userAdminLogInfoMapper;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${hmtx.request.url:/atcl/acti/recycle/bin/delete,/atcl/acti/recycle/bin/delete/recycle,/atcl/acti/recycle/bin/restore/recycle," +
            "/sys/role/listMenu,/sys/role/updateStatus,/atcl/query/record/get,/auth/authorization/info/get/byid}")
    private String requestUrl;

    public static String formatTime(Long ms) {
        Integer ss = 1000;
        Integer mi = ss * 60;
        Integer hh = mi * 60;
        Integer dd = hh * 24;

        Long day = ms / dd;
        Long hour = (ms - day * dd) / hh;
        Long minute = (ms - day * dd - hour * hh) / mi;
        Long second = (ms - day * dd - hour * hh - minute * mi) / ss;
        Long milliSecond = ms - day * dd - hour * hh - minute * mi - second * ss;

        StringBuilder sb = new StringBuilder();
        if(day > 0) {
            sb.append(day+"天");
        }
        if(hour > 0) {
            sb.append(hour+"小时");
        }
        if(minute > 0) {
            sb.append(minute+"分");
        }
        if(second > 0) {
            sb.append(second+"秒");
        }
        if(milliSecond > 0) {
            sb.append(milliSecond+"毫秒");
        }
        return sb.toString();
    }

    public void insertUserLog(String payload) {
        //请求头字符串转换实体类
        RequertEntity requertEntity = JSONObject.parseObject(payload, RequertEntity.class);
        String user = String.valueOf(requertEntity.getHeaders().get("log"));
        if(user.equals("null")){
            user= String.valueOf(requertEntity.getHeaders().get("Log"));
        }
        String Cookie = String.valueOf(requertEntity.getHeaders().get("cookie"));
        if(Cookie.equals("null")){
            Cookie= String.valueOf(requertEntity.getHeaders().get("Cookie"));
        }
        String[] cookie = Cookie.split(";");
        if (StringUtils.isNotEmpty(user) && !user.equals("null")) {
            String requestUri = requertEntity.getRequestUri();
            if (requestUrl(requestUri)) {
                String substring = requestUri.substring(requestUri.lastIndexOf("/") + 1);
                requertEntity.setParam("{\"value\":\"" + substring + "\"}");
                String replaceAll = requestUri.replaceAll("/" + substring, "");
                requestUri = replaceAll;
            }
            //请求头中用户信息Base64解码
            String users = new String(Base64.decodeBase64(user));
            //根据逗号分隔用户信息
            String[] split = users.split(",");
            //获取个人用户名称
            String username = split[0];
            //判断获取企业用户名称
            String defaultStr="undefined";
            String member = StringUtils.isEmpty(split[1]) && split[1].equals(defaultStr) ? "个人" : split[1];
            //获取页面名称
            String webname = StringUtils.isEmpty(split[2]) && split[2].equals(defaultStr) ? "未知" : split[2];
            //获取页面所属系统
            String websys = StringUtils.isEmpty(split[3]) && split[3].equals(defaultStr) ? "未知" : split[3];
            //获取页面id
            String webId = StringUtils.isEmpty(split[4]) && split[4].equals(defaultStr) ? "未知" : split[4];
            String memberId="未知";
            String memberUscc="未知";
            if(!Cookie.equals("null")){
                for (int i = 0; i < cookie.length; i++) {
                    if(cookie[i].contains("UC")){
                        memberUscc = cookie[i].split("=")[1].replace("\"]","");
                    }
                    if(cookie[i].contains("memberId")){
                        memberId = cookie[i].split("=")[1].replace("\"]","");
                    }
                }
            }

            String aeotrade = String.valueOf(stringRedisTemplate.opsForHash().get("AEOTRADE_LOG_All", requestUri + "||" + webId));

            if (StringUtils.isNotEmpty(aeotrade) && !aeotrade.equals("null")) {
                //根据接口类型获取接口参数
                String pram = requertEntity.getRequestMethod().equals("GET") ?
                        requertEntity.getParam() :
                        requertEntity.getBodyParam();
                //获取用户请求IP地址
                List<String> list = (List<String>) requertEntity.getHeaders().get("X-Real-IP");
                //根据ip地址获取用户所在城市信息
                String cityInfo = AddressUtil.getCityInfo(list.get(0));
                //获取用户请求页面地址
                List<String> referer = (List<String>) requertEntity.getHeaders().get("referer");
                if(null==referer){
                    referer = (List<String>) requertEntity.getHeaders().get("Referer");
                }
                String[] split1 = aeotrade.split(",");
                //获取接口名称和接口类型
                Map<String, String> map = requestName(requestUri, split1, pram);
                try {
                    UserLogInfo userLogInfo = new UserLogInfo();
                    AeotradeUserLogInfo aeotradeUserLogInfo=new AeotradeUserLogInfo();
                    userLogInfo.setUserName(username);
                    aeotradeUserLogInfo.setUser_name(username);
                    userLogInfo.setMenberName(member);
                    aeotradeUserLogInfo.setMenber_name(member);
                    userLogInfo.setWebUrl(referer.get(0));
                    aeotradeUserLogInfo.setWeb_url(referer.get(0));
                    userLogInfo.setIp(list.get(0));
                    aeotradeUserLogInfo.setIp(list.get(0));
                    userLogInfo.setRequestTime(new Timestamp(requertEntity.getCreateTime()));
                    aeotradeUserLogInfo.setRequest_time(new Timestamp(requertEntity.getCreateTime()));
                    userLogInfo.setRequestUrl(requestUri);
                    aeotradeUserLogInfo.setRequest_url(requestUri);
                    userLogInfo.setRequestType(requertEntity.getRequestMethod());
                    aeotradeUserLogInfo.setRequest_type(requertEntity.getRequestMethod());
                    userLogInfo.setRequestParameter(pram);
                    aeotradeUserLogInfo.setRequest_parameter(pram);
                    userLogInfo.setRequestNature(map.get("type"));
                    aeotradeUserLogInfo.setRequest_nature(map.get("type"));
                    userLogInfo.setUrlName(map.get("name"));
                    aeotradeUserLogInfo.setUrl_name(map.get("name"));
                    userLogInfo.setWebName(webname);
                    aeotradeUserLogInfo.setWeb_name(webname);
                    userLogInfo.setWebSys(websys);
                    aeotradeUserLogInfo.setWeb_sys(websys);
                    userLogInfo.setIpAddress(cityInfo);
                    userLogInfo.setMemberId(memberId);
                    aeotradeUserLogInfo.setMember_id(memberId);
                    userLogInfo.setMemberUscc(memberUscc);
                    aeotradeUserLogInfo.setMember_uscc(memberUscc);
                    if(StringUtils.isNotEmpty(cityInfo)){
                        String[] strings = cityInfo.split("\\|");
                        if(!strings[0].equals("中国")){
                            userLogInfo.setIpProvince(strings[0]);
                            userLogInfo.setIpCity(strings[0]);
                        }else{
                            userLogInfo.setIpProvince(strings[2]);
                            userLogInfo.setIpCity(strings[3]);
                        }
                    }
                    rabbitTemplate.convertAndSend("AEO_LOG_STAT", JSONObject.toJSONString(aeotradeUserLogInfo));
                    userLogInfoMapper.insert(userLogInfo);
                } catch (Exception e) {
                    log.warn(e.getClass().getName() + ": " + e.getMessage());
                }
                log.trace("新增数据成功！");
            }
        }

    }

    public void insertDo(String payload) {
        try {
            //请求头字符串转换实体类
            UserDocumentLog requertEntity = JSONObject.parseObject(payload, UserDocumentLog.class);
            if (CommonUtil.isNotEmpty(requertEntity)) {
                userDocumentLogMapper.insert(requertEntity);
            }
        } catch (Exception e) {
            log.warn(e.getMessage());
        }


    }

    public Boolean requestUrl(String url) {
        if (url.contains("/atcl/acti/recycle/bin/delete")) {
            return true;
        }
        if (url.contains("/atcl/acti/recycle/bin/delete/recycle")) {
            return true;
        }
        if (url.contains("/atcl/acti/recycle/bin/restore/recycle")) {
            return true;
        }
        if (url.contains("/sys/role/listMenu")) {
            return true;
        }
        if (url.contains("/sys/role/updateStatus")) {
            return true;
        }
        if (url.contains("/atcl/query/record/get")) {
            return true;
        }
        if (url.contains("/auth/authorization/info/get/byid")) {
            return true;
        }
        if (url.contains(requestUrl)) {
            return true;
        }

        return false;
    }

    public Map<String, String> requestName(String url, String[] split1, String pram) {
        Map<String, String> map = new HashMap<>();
        map.put("name", split1[0]);
        map.put("type", split1[1]);
        switch (url) {
            case "/atcl/query/record/type/list":
                if (pram.contains("isExport")) {
                    map.put("name", "导出档案案卷列表");
                    map.put("type", "导出");
                } else {
                    map.put("name", "查看档案案卷列表");
                    map.put("type", "查看");
                }
                break;
            case "/atcl/query/record/type/search":
            case "/atcl/query/document/type/search":
                if (pram.contains("isExport")) {
                    map.put("name", "导出档案文件列表");
                    map.put("type", "导出");
                } else {
                    map.put("name", "查看档案文件列表");
                    map.put("type", "查看");
                }
                break;
            case "/atcl/archive/storage/save/relevance/new":
                    map.put("name", "数据关联");
                    map.put("type", "添加");
                break;
            default:
                break;
        }
        return map;
    }

    public void insertAdminLog(String payload) {
        AdminLogInfo requertEntity = JSONObject.parseObject(payload, AdminLogInfo.class);
        UserAdminLogInfo userAdminLogInfo = new UserAdminLogInfo();
        if (StringUtils.isNotEmpty(requertEntity.getCpagename())) {
            userAdminLogInfo.setCPagename(requertEntity.getCpagename());
        }
        if (StringUtils.isNotEmpty(requertEntity.getCurl())) {
            userAdminLogInfo.setCUrl(requertEntity.getCurl());
        }
        userAdminLogInfo.setDurationTime(formatTime(requertEntity.getDurationtime()));
        userAdminLogInfo.setFPagename(requertEntity.getFpagename());
        userAdminLogInfo.setFurl(requertEntity.getFurl());
        userAdminLogInfo.setName(requertEntity.getName());
        userAdminLogInfo.setRole(requertEntity.getRole());
        userAdminLogInfo.setUserName(requertEntity.getUsername());
        userAdminLogInfo.setTime(new Timestamp(requertEntity.getTime()));
        userAdminLogInfo.setType(requertEntity.getType());
        userAdminLogInfoMapper.insert(userAdminLogInfo);
    }


}
