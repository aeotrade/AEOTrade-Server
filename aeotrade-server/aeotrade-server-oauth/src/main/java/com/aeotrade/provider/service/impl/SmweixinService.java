package com.aeotrade.provider.service.impl;

import com.aeotrade.annotation.Ex;
import com.aeotrade.base.constant.BizConstant;
import com.aeotrade.base.constant.HeadPortraitConstant;
import com.aeotrade.exception.AeotradeException;
import com.aeotrade.provider.dto.WxTokenDto;
import com.aeotrade.provider.dto.WxUserinfoDto;
import com.aeotrade.provider.event.AddMemberEvent;
import com.aeotrade.provider.mapper.UacAdminMapper;
import com.aeotrade.provider.mapper.UacMemberSynchronizationMapper;
import com.aeotrade.provider.mapper.UacStaffMapper;
import com.aeotrade.provider.model.*;
import com.aeotrade.provider.service.*;
import com.aeotrade.provider.service.async.StaffMemberAsync;
import com.aeotrade.provider.service.async.UserConnectionService;
import com.aeotrade.provider.service.feign.UacFeign;
import com.aeotrade.provider.util.HmmChatUtil;
import com.aeotrade.provider.util.WechatUtil;
import com.aeotrade.provider.util.WxStaffChat;
import com.aeotrade.provider.vo.RegisterOne;
import com.aeotrade.provider.vo.RegisterReturn;
import com.aeotrade.provider.vo.wxUser;
import com.aeotrade.service.MqSend;
import com.aeotrade.suppot.BaseController;
import com.aeotrade.suppot.RespResult;
import com.aeotrade.utlis.*;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

@Slf4j
@Service
public class SmweixinService extends BaseController {
    private static String code;
    @Autowired
    private UacUserConnectionService uacUserConnectionService;
    @Autowired
    private UacStaffService uacStaffService;
    @Autowired
    private UacStaffMapper uacStaffMapper;
    @Autowired
    private WxStaffChat wxStaffChat;
    @Autowired
    private UacMemberService uacMemberService;
    @Autowired
    private WechatUtil wechatUtil;
    @Autowired
    private UserConnectionService userConnectionService;
    @Autowired
    private UacMemberSynchronizationMapper uacMemberSynchronizationMapper;
    @Autowired
    private UacAdminMapper uacAdminMapper;
    @Autowired
    private StaffMemberAsync staffMemberAsync;
    @Autowired
    private UacFeign uacFeign;
    @Autowired
    private UacMemberStaffService uacMemberStaffService;
    @Autowired
    private HmmChatUtil hmmChatUtil;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UacUserService uacUserService;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Value("${hmtx.tijianzhinang.url:}")
    private String url;

    @Value("${hmtx.crm.url:}")
    private String crmurl;
    @Value("${hmtx.login.gateway-url}")
    private String gatewayUrl;
    @Autowired
    private MqSend mqSend;
    @Autowired
    private UawWorkbenchService uawWorkbenchService;
    @Autowired
    private UawVipTypeService uawVipTypeService;

    public Callable<RespResult> callBack_new(String code, String state, Long xpid, String member, String loginType) {
        Callable<RespResult> result = new Callable<RespResult>() {
            @Override
            public RespResult call() throws Exception {
                if (StringUtils.isBlank(code)) {
                    return handleFail(new RuntimeException("请重新扫码"));
                }
                WxTokenDto wxTokenDto = null;
                WxUserinfoDto wxUserinfoDto = null;
                try {
                    wxTokenDto = wechatUtil.getWxTokenDto(code);
                    if (wxTokenDto.getAccess_token() == null || wxTokenDto.getAccess_token().equals("null")) {
                        handleFail(new RuntimeException("请刷新二维码"));
                    }
                    wxUserinfoDto = wechatUtil.getWxUserinfoDto(wxTokenDto.getAccess_token(), wxTokenDto.getOpenid());

                    //保存微信用户数据
                    LambdaQueryWrapper<UacUserConnection> uacUserConnectionLambdaQueryWrapper=new LambdaQueryWrapper<>();
                    uacUserConnectionLambdaQueryWrapper.eq(UacUserConnection::getProviderId,BizConstant.ClientEnum.WECHAT.toString().toLowerCase())
                            .eq(UacUserConnection::getUnionid,wxUserinfoDto.getUnionid());
                    UacUserConnection userConnection = uacUserConnectionService.getOne(uacUserConnectionLambdaQueryWrapper);

                    /**新用户添加*/
                    Future<UacUserConnection> uacUserConnectionFuture = userConnectionService.inorupdateUacUserConnection(wxTokenDto, wxUserinfoDto, userConnection);
                    Future<OAuth2AccessToken> oAuth2AccessTokenFuture = userConnectionService.findByOpenId(uacUserConnectionFuture.get().getStaffId(), loginType);
                    Map map = null;
                    while (true) {//死循环，每隔2000ms执行一次，判断一下这三个异步调用的方法是否全都执行完了。
                        if (oAuth2AccessTokenFuture.isDone() && uacUserConnectionFuture.isDone()) {//使用Future的isDone()方法返回该方法是否执行完成
                            UacUserConnection connection = uacUserConnectionFuture.get();

                            try {
                                log.info("创建会员信息---------------------------------------------------------");
                                log.info("" + connection.getStaffId());
                                Map<String, Object> getMap = new HashMap<>();
                                getMap.put("id", connection.getStaffId());
                                getMap.put("apply", 0);

                                String s = HttpRequestUtils.httpGet(gatewayUrl+"/mam/uaw/VipMessage/loginMessage", getMap, null);
                                log.info("http调用结果" + s);
                                RespResult respResult = JSONObject.parseObject(s, RespResult.class);
                            } catch (Exception e) {
                                log.warn(e.getMessage());
                                throw e;

                            }
                            //如果异步方法全部执行完，跳出循环
                            OAuth2AccessToken oAuth2AccessToken = oAuth2AccessTokenFuture.get();
                            map = JacksonUtil.parseJson(JacksonUtil.toJsonWithFormat(oAuth2AccessToken), Map.class);

                            connection.setProviderUserId(wxUserinfoDto.getOpenid());
                            connection.setImageUrl(wxUserinfoDto.getHeadimgurl());
                            connection.setDisplayName(wxUserinfoDto.getNickname());
                            map = loginjson(map, connection, null);
                            /*新注册用户完善基本信息*/
                            map.put("xpid", 0);
                            map.put("member", "");
                            if (xpid != null) {
                                map.put("xpid", xpid);
                                map.put("member", member);
                                Long memid = Long.valueOf(((Map<String, Object>) map.get("bind")).get("memberid").toString());
                                staffMemberAsync.additionalStaffMember(xpid, memid, connection.getStaffId(),
                                        wxUserinfoDto.getHeadimgurl(), wxUserinfoDto.getOpenid(), wxUserinfoDto.getUnionid());
                            }

                            break;
                        }
                    }
                    return handleResult(map);
                } catch (Exception e) {
                    throw e;
                }
            }
        };

        return result;
    }

    public Map loginjson(Map map, UacUserConnection connection, UacStaff uacStaff) {

        log.info("=======================================uacStaffuacStaff" + JSONObject.toJSONString(uacStaff));
        //RespResult<List<Document>> vip = mamberFeign.findVip(0, connection.getStaffId());
        if (connection != null) {
            map.put("provideruserid", connection.getProviderUserId());
            map.put("providermqid", connection.getProviderMpId());
            map.put("unionid", connection.getUnionid());
            map.put("nickname", connection.getDisplayName());
            map.put("headimgurl", connection.getImageUrl());
        } else {
            map.put("provideruserid", StringUtils.isNotBlank(uacStaff.getWxOpenid()) ? uacStaff.getWxOpenid() : null);
            map.put("providermqid", null);
            map.put("unionid", StringUtils.isNotBlank(uacStaff.getWxUnionid()) ? uacStaff.getWxUnionid() : null);
            map.put("nickname", uacStaff.getStaffName());
            map.put("headimgurl", uacStaff.getWxLogo());
        }
        //判断是否已经绑定企业
        map.put("isbind", false);
        Map<String, Object> bindmap = new HashMap<>();
        bindmap.put("memberid", StringUtils.EMPTY);
        bindmap.put("lastWorkbenchId", StringUtils.EMPTY);
        bindmap.put("lastMemberId", StringUtils.EMPTY);
        bindmap.put("channelColumnsId", StringUtils.EMPTY);
        bindmap.put("workbenchName", StringUtils.EMPTY);
        bindmap.put("lastKindId", StringUtils.EMPTY);
        bindmap.put("membername", StringUtils.EMPTY);
        bindmap.put("staffid", StringUtils.EMPTY);
        bindmap.put("staffname", StringUtils.EMPTY);
        bindmap.put("isatff", 0);
        bindmap.put("memberstatus", 0);
        bindmap.put("uscc", StringUtils.EMPTY);
        bindmap.put("stasfTel", StringUtils.EMPTY);//电话
        bindmap.put("email", StringUtils.EMPTY);//邮箱
        bindmap.put("kindid", 0);//企业类型
        bindmap.put("sgsStatus", StringUtils.EMPTY);
        bindmap.put("staffimg", StringUtils.EMPTY);
        bindmap.put("memberimg", StringUtils.EMPTY);
        bindmap.put("vipType", StringUtils.EMPTY);
        bindmap.put("vipIco", StringUtils.EMPTY);

        if (connection != null && connection.getStaffId() != null) {
            uacStaff = uacStaffService.getById(connection.getStaffId());
            if (uacStaff != null && uacStaff.getSgsStatus() != null) {
                if (uacStaff.getStatus() == 1) {
                    throw new AeotradeException("该账号异常");
                }
            }
        }
        if (uacStaff.getLastMemberId() != null && uacStaff.getLastWorkbenchId() != null) {
            UacMember uacMember = uacMemberService.getById(uacStaff.getLastMemberId());
            map.put("isbind", true);
            bindmap.put("memberid", uacMember.getId().toString());
            map.put("memberId",uacMember.getId().toString());
            bindmap.put("staffid", uacStaff.getId().toString());
            if (connection != null) {
                bindmap.put("staffname", StringUtils.isEmpty(uacStaff.getStaffName()) ? connection.getDisplayName() : uacStaff.getStaffName());
                bindmap.put("memberimg", StringUtils.isBlank(uacMember.getLogoImg()) ? connection.getImageUrl() : uacMember.getLogoImg());
            } else {
                bindmap.put("staffname", uacStaff.getStaffName());
                bindmap.put("memberimg", uacMember.getLogoImg());
            }
            bindmap.put("kindid", uacMember.getKindId());
            bindmap.put("isatff", 0);
            bindmap.put("uscc", uacMember.getUscCode());
            bindmap.put("membername", uacMember.getMemberName());
            bindmap.put("memberstatus", uacMember.getMemberStatus());
            bindmap.put("lastWorkbenchId", uacStaff.getLastWorkbenchId());
            bindmap.put("lastMemberId", uacStaff.getLastMemberId());
            bindmap.put("channelColumnsId", uacStaff.getChannelColumnsId());
            bindmap.put("workbenchName", uawWorkbenchService.getById(uacStaff.getLastWorkbenchId()).getWorkbenchName());
            bindmap.put("lastKindId", uacMember.getKindId());
            LambdaQueryWrapper<UacAdmin> uacAdminLambdaQueryWrapper=new LambdaQueryWrapper<>();
            uacAdminLambdaQueryWrapper.eq(UacAdmin::getStaffId,uacStaff.getId()).eq(UacAdmin::getStatus,1).eq(UacAdmin::getIsTab,1);
            List<UacAdmin> uacAdmins = uacAdminMapper.selectList(uacAdminLambdaQueryWrapper);
            UacAdmin uacAdmin =uacAdmins.size()>0?uacAdmins.get(0):null;
            if (uacAdmin != null && StringUtils.isNotBlank(uacAdmin.getMobile())) {
                bindmap.put("stasfTel", uacAdmin.getMobile());//电话
            }
            bindmap.put("email", uacMember.getEmail());//邮箱

            map.put("bind", bindmap);

        } else {
            UacMember uacMember = uacMemberService.getById(uacStaff.getMemberId());
            bindmap.put("memberid", uacStaff.getMemberId().toString());
            bindmap.put("membername", uacMember.getMemberName());
            bindmap.put("memberstatus", uacMember.getMemberStatus());
            bindmap.put("memberimg", uacMember.getLogoImg());
            bindmap.put("staffid", uacStaff.getId().toString());
            if (connection != null) {
                bindmap.put("staffname", StringUtils.isEmpty(uacStaff.getStaffName()) ? connection.getDisplayName() : uacStaff.getStaffName());
            } else {
                bindmap.put("staffname", uacStaff.getStaffName());
            }
            bindmap.put("uscc", uacMember.getUscCode());
            bindmap.put("stasfTel", uacStaff.getTel());//电话
            bindmap.put("email", uacMember.getEmail());//邮箱
            bindmap.put("kindid", uacMember.getKindId());
            if (uacMember.getKindId() != null && uacMember.getKindId().equals(BizConstant.MemberKindEnum.STAFF_KINDID.getValue())) {
                bindmap.put("isatff", 1);
            }
            if (uacMember.getKindId() != null && uacMember.getKindId() != 88 && uacMember.getKindId() != 99) {
                UacMember uac = new UacMember();
                BeanUtils.copyProperties(uacMember, uac);
                if (connection != null) {
                    uac.setMemberName(connection.getDisplayName());
                    uac.setLogoImg(connection.getImageUrl());
                } else {
                    uac.setMemberName(uacStaff.getStaffName());
                    uac.setLogoImg(uacStaff.getWxLogo());
                }
                if (uacStaff.getLastMemberId() != null && uacStaff.getLastWorkbenchId() != null) {
                    bindmap.put("lastWorkbenchId", uacStaff.getLastWorkbenchId());
                    bindmap.put("lastMemberId", uacStaff.getLastMemberId());
                    bindmap.put("channelColumnsId", uacStaff.getChannelColumnsId());
                    bindmap.put("workbenchName", uawWorkbenchService.getById(uacStaff.getLastWorkbenchId()).getWorkbenchName());
                    bindmap.put("lastKindId", uacMemberService.getById(uacStaff.getLastMemberId()).getKindId());
                } else if (uacStaff.getLastWorkbenchId() != null && uacStaff.getLastMemberId() == null) {
                    bindmap.put("lastWorkbenchId", uacStaff.getLastWorkbenchId());
                    bindmap.put("channelColumnsId", uacStaff.getChannelColumnsId());
                    bindmap.put("workbenchName", uawWorkbenchService.getById(uacStaff.getLastWorkbenchId()).getWorkbenchName());
                    bindmap.put("lastKindId", 99L);
                }
                uac.setKindId(99L);
                uac.setId(null);
                uacMemberService.save(uac);
                UacMemberStaff uacMemberStaff = new UacMemberStaff();
                uacMemberStaff.setMemberId(uac.getId());
                uacMemberStaff.setStaffId(uacStaff.getId());
                uacMemberStaff.setIsAdmin(0);
                uacMemberStaff.setCreatedTime(new Timestamp(System.currentTimeMillis()).toLocalDateTime());
                uacMemberStaffService.save(uacMemberStaff);
                uacStaff.setMemberId(uac.getId());
                uacStaff.setRevision(1);
                uacStaffMapper.updateById(uacStaff);
                bindmap.put("memberid", uac.getId());
                bindmap.put("membername", uacMember.getMemberName());
                bindmap.put("memberstatus", uacMember.getMemberStatus());
                bindmap.put("memberimg", uac.getLogoImg());
                bindmap.put("staffid", uacStaff.getId().toString());
                if (connection != null) {
                    bindmap.put("staffname", StringUtils.isEmpty(uacStaff.getStaffName()) ? connection.getDisplayName() : uacStaff.getStaffName());
                } else {
                    bindmap.put("staffname", uacStaff.getStaffName());
                }
                bindmap.put("uscc", uacMember.getUscCode());
                bindmap.put("stasfTel", uacMember.getStasfTel());//电话
                bindmap.put("email", uacMember.getEmail());//邮箱
                bindmap.put("kindid", uac.getKindId());
            }
            map.put("bind", bindmap);

        }
        return map;
    }

    public Callable<RespResult> callBack_add_subadmin(String code, String state, Long pStaffId, Long pMemberId) {
        log.info("进入授权回调,code:{},state:{}", code, state);
        if (SmweixinService.code != null && SmweixinService.code.equals(code)) {
            return new Callable<RespResult>() {
                @Override
                public RespResult call() throws Exception {
                    return handleOK();
                }
            };
        }
        SmweixinService.code = code;
        if (pMemberId == null || pStaffId == null) {
            return new Callable<RespResult>() {
                @Override
                public RespResult call() throws Exception {
                    return handleFail("管理员参数为必填项");
                }
            };
        }
        Callable<RespResult> result = new Callable<RespResult>() {
            @Override
            public RespResult call() throws Exception {
                if (StringUtils.isBlank(code)) {
                    return handleFail(new RuntimeException("请重新扫码"));
                }
                WxTokenDto wxTokenDto = null;
                WxUserinfoDto wxUserinfoDto = null;
                try {
                    wxTokenDto = wxStaffChat.getWxTokenDto(code);
                    if (wxTokenDto.getAccess_token() == null) {
                        handleFail(new RuntimeException("请刷新二维码"));
                    }
                    wxUserinfoDto = wxStaffChat.getWxUserinfoDto(wxTokenDto.getAccess_token(), wxTokenDto.getOpenid());

                    //保存微信用户数据
                    LambdaQueryWrapper<UacUserConnection> uacUserConnectionLambdaQueryWrapper=new LambdaQueryWrapper<>();
                    uacUserConnectionLambdaQueryWrapper.eq(UacUserConnection::getProviderId,BizConstant.ClientEnum.WECHAT.toString().toLowerCase())
                            .eq(UacUserConnection::getUnionid,wxUserinfoDto.getUnionid());
                    List<UacUserConnection> list = uacUserConnectionService.list(uacUserConnectionLambdaQueryWrapper);
                    UacUserConnection userConnection = list.size()>0?list.get(0):null;
                    log.info("---------------------------------------------");
                    log.info("企业id是:" + pMemberId);
                    log.info("员工id是:" + pStaffId);
                    //判断 子管理员不能再创建子管理员
                    UacMember uacMember = uacMemberService.getById(pMemberId);
                    if (uacMember != null && uacMember.getStaffId() == null) {
                        uacMember.setStaffId(pStaffId);
                        uacMemberService.updateById(uacMember);
                    }
                    try {
                        if (uacMember != null && uacMember.getStaffId() != null && uacMember.getStaffId().longValue() != pStaffId.longValue()) {
                            return handleFail("没有添加子管理员的权限");
                        }
                    } catch (Exception e) {
                        throw new AeotradeException("没有添加子管理员的权限");
                    }

                    Future<UacUserConnection> uacUserConnectionFuture = userConnectionService.subUserConnection(
                            wxTokenDto, wxUserinfoDto, userConnection, pStaffId, pMemberId);

                    while (true) {
                        if (uacUserConnectionFuture.isDone()) {

                            RespResult respResult = uacFeign.subAdminList(uacUserConnectionFuture.get().getStaffId());
                            if (respResult != null) {
                                return respResult;
                            } else {
                                return handleOK();
                            }
                        }
                    }

                } catch (Exception e) {
                    throw e;
                }
            }
        };
        return result;

    }

    @Transactional(rollbackFor = Exception.class)
    public RespResult callBack_wx_build(String code, String state, Long staffId) {
        log.info("进入授权回调,code:{},state:{}", code, state);
        if (SmweixinService.code != null && SmweixinService.code.equals(code)) {
            return handleOK();
        }
        SmweixinService.code = code;
        if (staffId == null) {
            return handleFail("定义ID参数为必填项");
        }

        if (StringUtils.isBlank(code)) {
            return handleFail(new RuntimeException("请重新扫码"));
        }
        // 判断当前staffId是否合法
        UacStaff uacStaff = uacStaffMapper.selectById(staffId);
        if (uacStaff == null) {
            return handleFail("staffId 不合法");
        }
        WxTokenDto wxTokenDto = null;
        WxUserinfoDto wxUserinfoDto = null;
        try {
            wxTokenDto = wxStaffChat.getWxTokenDto(code);
            if (wxTokenDto.getAccess_token() == null) {
                return handleFail(new RuntimeException("请刷新二维码"));
            }
            wxUserinfoDto = wxStaffChat.getWxUserinfoDto(wxTokenDto.getAccess_token(), wxTokenDto.getOpenid());
            //判断当前微信号是否已经使用
            List<UacStaff> uacStaffs = uacStaffService.lambdaQuery().eq(UacStaff::getWxUnionid,wxUserinfoDto.getUnionid()).list();
            if (uacStaffs != null && uacStaffs.size() > 0 && uacStaffs.get(0).getTel() != null) {
                if (uacStaffs.stream().filter(f -> f.getId().longValue() == staffId.longValue()).count() > 0) {
                    //自己扫自己
                    return handleOK();
                } else {
                    return handleFail("该微信号已经使用");
                }
            }
            //保存微信用户数据
            LambdaQueryWrapper<UacUserConnection> uacUserConnectionLambdaQueryWrapper=new LambdaQueryWrapper<>();
            uacUserConnectionLambdaQueryWrapper.eq(UacUserConnection::getProviderId,BizConstant.ClientEnum.WECHAT.toString().toLowerCase())
                    .eq(UacUserConnection::getStaffId,staffId);
            List<UacUserConnection> list = uacUserConnectionService.list(uacUserConnectionLambdaQueryWrapper);
            UacUserConnection userConnection =list.size()>0?list.get(0):null;

            //绑定 员工
            if (userConnection == null) {
                userConnection = new UacUserConnection();
                userConnection.setStaffId(staffId);
                userConnection.setProviderUserId(wxUserinfoDto.getOpenid());
                userConnection.setProviderId(BizConstant.ClientEnum.WECHAT.toString().toLowerCase());
                userConnection.setDisplayName(wxUserinfoDto.getNickname());
                userConnection.setImageUrl(wxUserinfoDto.getHeadimgurl());
                userConnection.setUnionid(wxUserinfoDto.getUnionid());
                userConnection.setAccessToken(wxTokenDto.getAccess_token());
                userConnection.setRefreshToken(wxTokenDto.getRefresh_token());
                userConnection.setExpireTime(DateUtil.addSecondToLocalDateTime(wxTokenDto.getExpires_in()));
                uacUserConnectionService.save(userConnection);

                uacStaff.setWxLogo(userConnection.getImageUrl());
                uacStaff.setWxOpenid(userConnection.getProviderId());
                uacStaff.setWxUnionid(userConnection.getUnionid());
                uacStaff.setSgsStatus(1); //绑定微信状态 未绑定 0 ;已绑定 1
                uacStaff.setUpdatedTime(new Timestamp(System.currentTimeMillis()).toLocalDateTime());
                uacStaffMapper.updateById(uacStaff);
            } else {
                userConnection.setStaffId(staffId);
                userConnection.setProviderId(BizConstant.ClientEnum.WECHAT.toString().toLowerCase());
                userConnection.setProviderUserId(wxUserinfoDto.getOpenid());
                userConnection.setDisplayName(wxUserinfoDto.getNickname());
                userConnection.setImageUrl(wxUserinfoDto.getHeadimgurl());
                userConnection.setUnionid(wxUserinfoDto.getUnionid());
                uacUserConnectionService.updateById(userConnection);
                uacStaff.setWxLogo(wxUserinfoDto.getHeadimgurl());
                uacStaff.setWxOpenid(wxUserinfoDto.getOpenid());
                uacStaff.setWxUnionid(wxUserinfoDto.getUnionid());
                uacStaff.setSgsStatus(1); //绑定微信状态 未绑定 0 ;已绑定 1
                uacStaff.setUpdatedTime(new Timestamp(System.currentTimeMillis()).toLocalDateTime());
                uacStaffMapper.updateById(uacStaff);
            }

            return handleResult(wxUserinfoDto.getHeadimgurl());
        } catch (Exception e) {
            return handleFail(e);
        }

    }

    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public Callable<RespResult> callBack_staff(String code, String state, Long memberId) {
        log.info("进入授权回调,code:{},state:{}", code, state);

        Callable<RespResult> result = new Callable<RespResult>() {
            @Override
            public RespResult call() throws Exception {
                if (StringUtils.isBlank(code)) {
                    return handleFail(new RuntimeException("请重新扫码"));
                }
                UacMember uacMember = uacMemberService.getById(memberId);
                log.info("企业信息++++++++++++++++++++++++++" + uacMember);
                if (uacMember == null || uacMember.getStaffId() == null) {
                    return handleFail(new RuntimeException("请重新扫码"));
                }
                WxTokenDto wxTokenDto = null;
                WxUserinfoDto wxUserinfoDto = null;
                try {
                    wxTokenDto = wxStaffChat.getWxTokenDto(code);
                    if (wxTokenDto == null) {
                        throw new AeotradeException("授权失败");
                    }
                    if (wxTokenDto.getAccess_token() == null) {
                        handleFail(new AeotradeException("请刷新二维码"));
                    }
                    wxUserinfoDto = wxStaffChat.getWxUserinfoDto(wxTokenDto.getAccess_token(), wxTokenDto.getOpenid());
                    if (wxUserinfoDto == null) {
                        throw new AeotradeException("授权失败");
                    }
                    //查询微信用户数据
                    LambdaQueryWrapper<UacUserConnection> uacUserConnectionLambdaQueryWrapper=new LambdaQueryWrapper<>();
                    uacUserConnectionLambdaQueryWrapper.eq(UacUserConnection::getProviderId,BizConstant.ClientEnum.WECHAT.toString().toLowerCase())
                            .eq(UacUserConnection::getUnionid,wxUserinfoDto.getUnionid());
                    List<UacUserConnection> list = uacUserConnectionService.list(uacUserConnectionLambdaQueryWrapper);
                    UacUserConnection userConnection =list.size()>0?list.get(0):null;

                    Future<UacUserConnection> uacUserConnectionFuture = userConnectionService.inorupdateUacUserConnection(wxTokenDto, wxUserinfoDto, userConnection);

                    Future<OAuth2AccessToken> oAuth2AccessTokenFuture = userConnectionService.getOAuth2AccessToken();

                    Map map = null;
                    while (true) {//死循环，每隔2000ms执行一次，判断一下这三个异步调用的方法是否全都执行完了。
                        if (oAuth2AccessTokenFuture.isDone() && uacUserConnectionFuture.isDone()) {//使用Future的isDone()方法返回该方法是否执行完成
                            UacUserConnection connection = uacUserConnectionFuture.get();
                            //如果异步方法全部执行完，跳出循环
                            OAuth2AccessToken oAuth2AccessToken = oAuth2AccessTokenFuture.get();
                            map = JacksonUtil.parseJson(JacksonUtil.toJsonWithFormat(oAuth2AccessToken), Map.class);

                            connection.setProviderUserId(wxUserinfoDto.getOpenid());
                            connection.setImageUrl(wxUserinfoDto.getHeadimgurl());
                            connection.setDisplayName(wxUserinfoDto.getNickname());
                            map = loginjson(map, connection, null);

                            log.info("++++++++++++++++++++++++++++++++++++++++++++准备同步");
                            log.info("" + connection);
                            /** 添加员工*/
                            if (connection.getStaffId() != null) {
                                ThreadPoolUtils.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        uacMemberStaffService.update(connection.getStaffId(), memberId, uacMember.getMemberName(), uacMember.getUscCode());
                                    }
                                });
                            }
                            break;
                        }
                    }
                    return handleResult(map);
                } catch (Exception e) {
                    throw e;
                }
            }
        };

        return result;

    }

    public Callable<RespResult> hmmCallBack_new(String code, String state, Long xpid, String member) {
        log.info("进入授权回调,code:{},state:{}", code, state);

        Callable<RespResult> result = new Callable<RespResult>() {
            @Override
            public RespResult call() throws Exception {
                if (StringUtils.isBlank(code)) {
                    return handleFail(new RuntimeException("请重新扫码"));
                }
                WxTokenDto wxTokenDto = null;
                WxUserinfoDto wxUserinfoDto = null;
                try {
                    wxTokenDto = hmmChatUtil.getWxTokenDto(code);
                    if (wxTokenDto.getAccess_token() == null) {
                        handleFail(new RuntimeException("请刷新二维码"));
                    }
                    wxUserinfoDto = hmmChatUtil.getWxUserinfoDto(wxTokenDto.getAccess_token(), wxTokenDto.getOpenid());

                    //保存微信用户数据
                    LambdaQueryWrapper<UacUserConnection> uacUserConnectionLambdaQueryWrapper=new LambdaQueryWrapper<>();
                    uacUserConnectionLambdaQueryWrapper.eq(UacUserConnection::getProviderId,BizConstant.ClientEnum.WECHAT.toString().toLowerCase())
                            .eq(UacUserConnection::getUnionid,wxUserinfoDto.getUnionid());
                    List<UacUserConnection> list = uacUserConnectionService.list(uacUserConnectionLambdaQueryWrapper);
                    UacUserConnection userConnection =list.size()>0?list.get(0):null;

                    Future<UacUserConnection> uacUserConnectionFuture = userConnectionService.inorupdateUacUserConnection(wxTokenDto, wxUserinfoDto, userConnection);

                    Future<OAuth2AccessToken> oAuth2AccessTokenFuture = userConnectionService.getOAuth2AccessToken();
                    Map map = null;
                    while (true) {//死循环，每隔2000ms执行一次，判断一下这三个异步调用的方法是否全都执行完了。
                        if (oAuth2AccessTokenFuture.isDone() && uacUserConnectionFuture.isDone()) {//使用Future的isDone()方法返回该方法是否执行完成
                            UacUserConnection connection = uacUserConnectionFuture.get();
                            //如果异步方法全部执行完，跳出循环
                            OAuth2AccessToken oAuth2AccessToken = oAuth2AccessTokenFuture.get();
                            map = JacksonUtil.parseJson(JacksonUtil.toJsonWithFormat(oAuth2AccessToken), Map.class);

                            connection.setProviderUserId(wxUserinfoDto.getOpenid());
                            connection.setImageUrl(wxUserinfoDto.getHeadimgurl());
                            connection.setDisplayName(wxUserinfoDto.getNickname());
                            map = loginjson(map, connection, null);
                            /*新注册用户完善基本信息*/
                            map.put("xpid", 0);
                            map.put("member", "");
                            if (xpid != null) {
                                map.put("xpid", xpid);
                                map.put("member", member);
                                Long memid = Long.valueOf(((Map<String, Object>) map.get("bind")).get("memberid").toString());
                                staffMemberAsync.additionalStaffMember(xpid, memid, connection.getStaffId(),
                                        wxUserinfoDto.getHeadimgurl(), wxUserinfoDto.getOpenid(), wxUserinfoDto.getUnionid());
                            }
                            break;
                        }
                    }
                    return handleResult(map);
                } catch (Exception e) {
                    throw e;
                }
            }
        };

        return result;

    }

    public Callable<RespResult> callBack_atcl(String code, String state) {
        log.info("进入授权回调,code:{},state:{}", code, state);

        Callable<RespResult> result = new Callable<RespResult>() {
            @Override
            public RespResult call() throws Exception {
                if (StringUtils.isBlank(code)) {
                    throw new AeotradeException("请重新扫码");
                    //return handleFail(new RuntimeException("请重新扫码"));
                }
                WxTokenDto wxTokenDto = null;
                WxUserinfoDto wxUserinfoDto = null;
                try {
                    wxTokenDto = wechatUtil.getWxTokenDto(code);
                    if (wxTokenDto.getAccess_token() == null) {
                        throw new AeotradeException("请刷新二维码");
                        // handleFail(new RuntimeException("请刷新二维码"));
                    }
                    wxUserinfoDto = wechatUtil.getWxUserinfoDto(wxTokenDto.getAccess_token(), wxTokenDto.getOpenid());

                    //保存微信用户数据
                    LambdaQueryWrapper<UacUserConnection> uacUserConnectionLambdaQueryWrapper=new LambdaQueryWrapper<>();
                    uacUserConnectionLambdaQueryWrapper.eq(UacUserConnection::getProviderId,BizConstant.ClientEnum.WECHAT.toString().toLowerCase())
                            .eq(UacUserConnection::getUnionid,wxUserinfoDto.getUnionid());
                    List<UacUserConnection> list = uacUserConnectionService.list(uacUserConnectionLambdaQueryWrapper);
                    UacUserConnection userConnection = list.size()>0?list.get(0):null;
                    /**1.如果为新用户 插入Connection表 ,staff表,member表*/
                    Future<UacUserConnection> uacUserConnectionFuture = userConnectionService.inorupdateUacUserConnection(wxTokenDto, wxUserinfoDto, userConnection);

                    Future<OAuth2AccessToken> oAuth2AccessTokenFuture = userConnectionService.getOAuth2AccessToken();
                    Map map = null;
                    while (true) {//死循环，每隔2000ms执行一次，判断一下这三个异步调用的方法是否全都执行完了。
                        if (oAuth2AccessTokenFuture.isDone() && uacUserConnectionFuture.isDone()) {//使用Future的isDone()方法返回该方法是否执行完成
                            UacUserConnection connection = uacUserConnectionFuture.get();
                            //如果异步方法全部执行完，跳出循环
                            OAuth2AccessToken oAuth2AccessToken = oAuth2AccessTokenFuture.get();
                            //返回token
                            map = JacksonUtil.parseJson(JacksonUtil.toJsonWithFormat(oAuth2AccessToken), Map.class);
                            connection.setProviderUserId(wxUserinfoDto.getOpenid());
                            connection.setImageUrl(wxUserinfoDto.getHeadimgurl());
                            connection.setDisplayName(wxUserinfoDto.getNickname());
                            //将员工Id返回
                            map.put("staffId", connection.getStaffId());
                            break;
                        }
                    }
                    return handleResult(map);
                } catch (Exception e) {
                    return handleFail(e);
                }
            }
        };
        return result;
    }

    public int findUserByName(String name) {
        LambdaQueryWrapper<UacAdmin> uacAdminLambdaQueryWrapper=new LambdaQueryWrapper<>();
        uacAdminLambdaQueryWrapper.eq(UacAdmin::getUsername,name).eq(UacAdmin::getStatus,1).eq(UacAdmin::getIsTab,1);
        List<UacAdmin> uacUsers = uacAdminMapper.selectList(uacAdminLambdaQueryWrapper);
        if (uacUsers.size() != 0) {
            return 1;
        } else {
            return 0;
        }
    }

    public UacAdmin findUserByStaffId(Long staffId) {
        LambdaQueryWrapper<UacAdmin> uacAdminLambdaQueryWrapper=new LambdaQueryWrapper<>();
        uacAdminLambdaQueryWrapper.eq(UacAdmin::getStaffId,staffId).eq(UacAdmin::getStatus,1)
                .eq(UacAdmin::getIsTab,1).orderByDesc(UacAdmin::getCreateTime);
        List<UacAdmin> uacAdmins = uacAdminMapper.selectList(uacAdminLambdaQueryWrapper);
        return uacAdmins.size()>0?uacAdmins.get(0):null;
    }

    public Object loginMobile(String loginType, String mobile, String code) throws Exception {
        LambdaQueryWrapper<UacAdmin> uacAdminLambdaQueryWrapper=new LambdaQueryWrapper<>();
        uacAdminLambdaQueryWrapper.eq(UacAdmin::getMobile,mobile).eq(UacAdmin::getStatus,1)
                .eq(UacAdmin::getIsTab,1).orderByDesc(UacAdmin::getCreateTime);
        List<UacAdmin> uacAdmins = uacAdminMapper.selectList(uacAdminLambdaQueryWrapper);
        UacAdmin byMobile =uacAdmins.size()>0?uacAdmins.get(0):null;
        if (byMobile == null) {
            throw new AeotradeException("该手机号未绑定用户");
        }
        if (byMobile.getId() == null) {
            throw new AeotradeException("无员工信息");
        }
        log.info(passwordEncoder.encode("wxsecret"));
        OAuth2AccessToken oAuth2AccessTokenFuture = userConnectionService.findByMobile(String.valueOf(byMobile.getStaffId()), loginType, mobile, code);
        Map map = null;
        if (!CommonUtil.isEmpty(oAuth2AccessTokenFuture)) {//使用Future的isDone()方法返回该方法是否执行完成
            //如果异步方法全部执行完，跳出循环
            map = JacksonUtil.parseJson(JacksonUtil.toJsonWithFormat(oAuth2AccessTokenFuture), Map.class);
            UacStaff uacStaff = uacStaffMapper.selectById(byMobile.getStaffId());
            try {
                this.loginMessage(byMobile.getStaffId(), 0);
            } catch (Exception e) {
                throw new AeotradeException(e.getMessage());
            }
            map = loginjson(map, null, uacStaff);
            return map;
        }
        return null;
    }

    @DS("aeotrade")
    public Map<String, Object> formDataRegister(RegisterOne registerOne, UawWorkbench lastWorkbench, JSONObject formData) {
        Map<String, Object>  map = new HashMap<String, Object>();
        map.put("id", formData.getInteger("id"));
        map.put("jionResult", 1);
        map.put("enroll", 0);
        map.put("orgEnroll", 0);
        map.put("memberId", 0L);
        try {
            Long instrer = null;
            UacStaff uacStaff = null;
            LambdaQueryWrapper<UacAdmin> uacAdminLambdaQueryWrapper = new LambdaQueryWrapper<>();
            uacAdminLambdaQueryWrapper.eq(UacAdmin::getMobile, registerOne.getPhone()).eq(UacAdmin::getStatus, 1)
                    .eq(UacAdmin::getIsTab, 1).orderByDesc(UacAdmin::getCreateTime);
            List<UacAdmin> uacAdmins = uacAdminMapper.selectList(uacAdminLambdaQueryWrapper);
            UacAdmin byMobile = !uacAdmins.isEmpty() ? uacAdmins.get(0) : null;
            if (byMobile == null) {
                LambdaQueryWrapper<UacStaff> uacStaffLambdaQueryWrapper = new LambdaQueryWrapper<>();
                uacStaffLambdaQueryWrapper.eq(UacStaff::getTel, registerOne.getPhone());
                uacStaffLambdaQueryWrapper.eq(UacStaff::getStatus, 0);
                List<UacStaff> uacStaffList = uacStaffMapper.selectList(uacStaffLambdaQueryWrapper);
                if (uacStaffList.isEmpty()) {
                    uacStaff = new UacStaff();
                    uacStaff.setStaffName(registerOne.getStaffName());
                    uacStaff.setTel(registerOne.getPhone());
                    uacStaff.setStaffType(0);
                    uacStaff.setSourceMark("活动报名收集");
                    uacStaff.setAuthStatus(0);
                    uacStaff.setSgsStatus(0);
                    uacStaff.setIsLogin(0);
                    uacStaff.setWxLogo(HeadPortraitConstant.InputDataType.STAFF_HEAD.getValue());
                    uacStaffMapper.insert(uacStaff);
                }else {
                    uacStaff = uacStaffList.get(0);
                }
                UacStaff finalUacStaff = uacStaff;
                ThreadPoolUtils.execute(() -> {
                    try {
                        this.loginMessage(finalUacStaff.getId(), 0);
                    } catch (Exception e) {
                        log.warn(e.getMessage());
                    }
                });

                if (registerOne.getUscCode() != null) {
                    List<UacMember> uacb = uacMemberService.lambdaQuery().eq(UacMember::getUscCode, registerOne.getUscCode()).list();
                    if (uacb.isEmpty()) {
                        instrer = this.creatMember(uacStaff.getId(), registerOne.getUscCode(), registerOne);
                        map.put("orgEnroll", 1);
                        map.put("orgJoinTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    } else {
                        instrer = uacb.get(0).getId();
                        map.put("orgJoinTime", uacb.get(0).getCreatedTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                        //检查临时埋点标记
                        String memberClientid = uacb.get(0).getMemberClientid();
                        if (memberClientid != null && memberClientid.equalsIgnoreCase(registerOne.getSourceMark())) {
                            map.put("orgEnroll", 1);
                        }
                    }
                    map.put("memberId", instrer);

                    if(uacMemberStaffService.lambdaQuery().eq(UacMemberStaff::getStaffId, uacStaff.getId())
                            .eq(UacMemberStaff::getMemberId, uacb.get(0).getId()).exists()) {
                        UacMemberStaff MemberStaff = new UacMemberStaff();
                        MemberStaff.setStaffId(uacStaff.getId());
                        MemberStaff.setMemberId(instrer);
                        MemberStaff.setIsAdmin(0);
                        MemberStaff.setKindId(0L);
                        uacMemberStaffService.save(MemberStaff);
                    }

                } else {
                    this.creatMember(uacStaff.getId(), null, registerOne);
                }
                uacStaff.setMemberId(instrer);
                uacStaff.setStatus(0);
                uacStaff.setRevision(0);
                uacStaff.setLastMemberId(instrer);
                if (lastWorkbench != null) {
                    uacStaff.setLastWorkbenchId(lastWorkbench.getId());
                    uacStaff.setChannelColumnsId(lastWorkbench.getChannelColumnsId());
                }
                uacStaffService.updateById(uacStaff);

                UacAdmin uacAdmin=new UacAdmin();
                uacAdmin.setMobile(registerOne.getPhone());
                uacAdmin.setStatus(1);
                uacAdmin.setIsTab(1);
                uacAdmin.setCreateTime(LocalDateTime.now());
                uacAdmin.setUpdateTime(LocalDateTime.now());
                uacAdmin.setStaffId(uacStaff.getId());
                if (registerOne.getSourceMark()!=null) {
                    uacAdmin.setUserId(registerOne.getSourceMark());
                }
                uacAdminMapper.insert(uacAdmin);
                map.put("enroll", 1);
                map.put("joinTime",uacAdmin.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            }else {
                map.put("joinTime",byMobile.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                if (byMobile.getUserId()!=null&&byMobile.getUserId().equalsIgnoreCase(registerOne.getSourceMark())){
                    map.put("enroll", 1);
                }
                //判断企业是否存在
                Long staffId = byMobile.getStaffId();
                uacStaff = uacStaffService.getById(staffId);
                if (registerOne.getUscCode() != null) {
                    List<UacMember> uacb = uacMemberService.lambdaQuery().eq(UacMember::getUscCode, registerOne.getUscCode()).list();
                    if (uacb.isEmpty()) {
                        instrer = this.creatMember(staffId, registerOne.getUscCode(), registerOne);
                        map.put("orgEnroll", 1);
                        map.put("orgJoinTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    } else {
                        instrer = uacb.get(0).getId();
                        map.put("orgJoinTime", uacb.get(0).getCreatedTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                        //检查临时埋点标记
                        String memberClientid = uacb.get(0).getMemberClientid();
                        if (memberClientid != null && memberClientid.equalsIgnoreCase(registerOne.getSourceMark())) {
                            map.put("orgEnroll", 1);
                        }
                    }
                    map.put("memberId", instrer);

                    if(uacMemberStaffService.lambdaQuery().eq(UacMemberStaff::getStaffId, uacStaff.getId())
                            .eq(UacMemberStaff::getMemberId, uacb.get(0).getId()).exists()) {
                        UacMemberStaff MemberStaff = new UacMemberStaff();
                        MemberStaff.setStaffId(uacStaff.getId());
                        MemberStaff.setMemberId(instrer);
                        MemberStaff.setIsAdmin(0);
                        MemberStaff.setKindId(0L);
                        uacMemberStaffService.save(MemberStaff);
                    }

                }
            }
        } catch (Exception e) {
            log.warn("记录用户失败: {}", e.getMessage());
        }
        return map;
    }

    @Transactional
    public RegisterReturn register(RegisterOne registerOne,UawWorkbench lastWorkbench) throws Exception {
        String s = registerOne.getUscCode().replaceAll(" ", "");
        if (s.length() != 18) {
            throw new AeotradeException("请输入正确的统一社会信用代码（长度必须为18位）");
        }
        List<UacMember> uacb = uacMemberService.lambdaQuery().eq(UacMember::getUscCode,s).ne(UacMember::getStatus,1).notIn(UacMember::getKindId,88,99).list();
        if (uacb != null && uacb.size() != 0) {
            throw new AeotradeException("该企业已存在，请前去登录");
        }
        LambdaQueryWrapper<UacAdmin> uacAdminLambdaQueryWrapper=new LambdaQueryWrapper<>();
        uacAdminLambdaQueryWrapper.eq(UacAdmin::getMobile,registerOne.getPhone()).eq(UacAdmin::getStatus,1)
                .eq(UacAdmin::getIsTab,1).orderByDesc(UacAdmin::getCreateTime);
        List<UacAdmin> uacAdmins = uacAdminMapper.selectList(uacAdminLambdaQueryWrapper);
        UacAdmin byMobile = uacAdmins.size()>0?uacAdmins.get(0):null;
        if (byMobile != null) {
            return this.StaffCreaet(s, byMobile.getStaffId(), registerOne,lastWorkbench);
        }
        return this.visitorCreaet(s, registerOne,lastWorkbench);
    }

    /**
     * 邀请用户，并将新用户绑定到指定企业，及设置用户角色
     */
    @Transactional(rollbackFor = Exception.class)
    public void buildNewStaffAndMember(String phoneNumber, String unionId, Long memberId, String name, String roleId, String deptId) {
        UacMember buildUacMember = uacMemberService.getById(memberId);
        UacStaff buildUacStaff = uacStaffService.getById(buildUacMember.getStaffId());
        if (buildUacStaff == null) {
            throw new RuntimeException("企业用户数据不合法，请联系系统管理员处理");
        }

        // 1. 添加员工 aeotrade.uac_staff
        UacStaff uacStaff = new UacStaff();
        uacStaff.setWxUnionid(unionId);
        uacStaff.setTel(phoneNumber);
        uacStaff.setStaffName(name);
        uacStaff.setLastWorkbenchId(buildUacStaff.getLastWorkbenchId());
        uacStaff.setLastMemberId(memberId);
        uacStaff.setChannelColumnsId(buildUacStaff.getChannelColumnsId());
        uacStaff.setAuthStatus(0);
        uacStaff.setId(null);
        uacStaff.setCreatedTime(DateUtil.getData().toLocalDateTime());
        uacStaff.setStatus(0);
        uacStaff.setIsLogin(0);
        uacStaff.setRevision(0);
        uacStaff.setStaffType(0);
        uacStaff.setSourceMark("小程序邀请");
        uacStaff.setSgsStatus(0);
        uacStaff.setWxLogo(HeadPortraitConstant.InputDataType.STAFF_HEAD.getValue());
        uacStaffMapper.insert(uacStaff);
        uacStaff.getId();

        // 2. 添加登录账户 aeotrade_admin.uac_admin ？？用户默认需要给什么角色
        UacAdmin uacAdmin = new UacAdmin();
        uacAdmin.setMobile(phoneNumber);
        uacAdmin.setIcon(HeadPortraitConstant.InputDataType.STAFF_HEAD.getValue());
        uacAdmin.setNickName(name);
        uacAdmin.setStatus(1);
        uacAdmin.setIsTab(1);
        uacAdmin.setCreateTime(DateUtil.getData().toLocalDateTime());
        uacAdmin.setStaffId(uacStaff.getId());
        uacAdmin.setUpdateTime(DateUtil.getData().toLocalDateTime());
        uacAdmin.setId(null);
        uacAdminMapper.insert(uacAdmin);
        uacAdmin.getId();

        // 3. 添加个人租户 aeotrade.uac_member
        UacMember uacMember = new UacMember();
        uacMember.setStaffId(uacStaff.getId());
        uacMember.setCreatedTime(DateUtil.getData().toLocalDateTime());
        uacMember.setKindId(99L);
        uacMember.setRevision(0);
        uacMember.setStatus(0);
        uacMember.setRemark("1");
        uacMember.setLogoImg(HeadPortraitConstant.InputDataType.STAFF_HEAD.getValue());
        uacMember.setSgsStatus(0);
        uacMember.setIsTest(0);
        uacMember.setRevision(0);
        uacMember.setCreatedTime(DateUtil.getData().toLocalDateTime());
        uacMember.setId(null);
        uacMember.setAtpwStatus(0);
        uacMemberService.save(uacMember);
        uacMember.getId();
        if (uacStaff.getId() != null) {
            uacStaff.setMemberId(uacMember.getId());
            uacStaff.setUpdatedTime(DateUtil.getData().toLocalDateTime());
            uacStaffService.updateById(uacStaff);
        }

        // 4. 添加与微信关联 aeotrade.uac_connection
        UacUserConnection uacUserConnection = new UacUserConnection();
        uacUserConnection.setProviderId(BizConstant.ClientEnum.WECHAT.toString().toLowerCase());
        uacUserConnection.setUnionid(unionId);
        uacUserConnection.setStaffId(uacStaff.getId());
        uacUserConnection.setImageUrl(HeadPortraitConstant.InputDataType.STAFF_HEAD.getValue());
        uacUserConnection.setAccessToken("null"); // 给个默认值，否则插入失败
        uacUserConnectionService.save(uacUserConnection);
        uacUserConnection.getId();

        // 5. 将用户与邀请企业绑定
        uacMemberStaffService.buildStaffAndMember(uacStaff.getId(), memberId, name, roleId,deptId);
    }

    @Transactional
    @Ex(value = "调用开通默认会员异常")
    public void loginMessage(Long id, int apply) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("apply", apply);
        log.info("http调用开始" + JSONObject.toJSONString(map));
        String http = HttpRequestUtils.httpGet(gatewayUrl+"/mam/uaw/VipMessage/loginMessage", map);
        RespResult respResult = JSONObject.parseObject(http, RespResult.class);
        if (respResult.getCode() != 200) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw new AeotradeException("注册失败,请重试");
        }
    }

    @Transactional
    public RegisterReturn StaffCreaet(String s, Long StaffId, RegisterOne registerOne,UawWorkbench lastWorkbench) throws Exception {
        UacStaff uacStaff = uacStaffMapper.selectById(StaffId);
        Long instrer = this.creatMember(uacStaff.getId(), s, registerOne);
        UacMemberStaff MemberStaff = new UacMemberStaff();
        MemberStaff.setStaffId(uacStaff.getId());
        MemberStaff.setMemberId(instrer);
        MemberStaff.setIsAdmin(0);
        MemberStaff.setKindId(0L);
        uacMemberStaffService.save(MemberStaff);
        uacStaff.setStatus(0);
        uacStaff.setLastMemberId(instrer);
        if (lastWorkbench != null) {
            uacStaff.setLastWorkbenchId(lastWorkbench.getId());
            uacStaff.setChannelColumnsId(lastWorkbench.getChannelColumnsId());
        }
        uacStaffService.updateById(uacStaff);
        return new RegisterReturn(uacStaff, instrer);
    }

    @Transactional
    public RegisterReturn visitorCreaet(String s, RegisterOne registerOne,UawWorkbench lastWorkbench) throws Exception {
        UacStaff uacStaff = new UacStaff();
        uacStaff.setStaffName(registerOne.getStaffName());
        uacStaff.setTel(registerOne.getPhone());
        uacStaff.setStaffType(0);
        uacStaff.setSourceMark("慧贸OSPC端");
        uacStaff.setAuthStatus(0);
        uacStaff.setSgsStatus(0);
        uacStaff.setIsLogin(0);
        uacStaff.setWxLogo(HeadPortraitConstant.InputDataType.STAFF_HEAD.getValue());
        uacStaffMapper.insert(uacStaff);
        this.loginMessage(uacStaff.getId(), 0);
        Long instrer = this.creatMember(uacStaff.getId(), s, registerOne);
        uacStaff.setMemberId(instrer);
        uacStaff.setStatus(0);
        uacStaff.setRevision(0);
        uacStaff.setLastMemberId(instrer);
        if (lastWorkbench != null) {
            uacStaff.setLastWorkbenchId(lastWorkbench.getId());
            uacStaff.setChannelColumnsId(lastWorkbench.getChannelColumnsId());
        }
        uacStaffService.updateById(uacStaff);
        UacMemberStaff MemberStaff = new UacMemberStaff();
        MemberStaff.setStaffId(uacStaff.getId());
        MemberStaff.setMemberId(instrer);
        MemberStaff.setIsAdmin(0);
        MemberStaff.setKindId(0L);
        uacMemberStaffService.save(MemberStaff);
        uacUserService.bindingMobile(registerOne.getPhone(), uacStaff.getId(),null,null);
        return new RegisterReturn(uacStaff, instrer);
    }

    @Transactional
    public Long creatMember(Long staffId, String s, RegisterOne registerOne) throws Exception {
        UacMember uacMember = new UacMember();
        uacMember.setStaffId(staffId);
        uacMember.setMemberName(registerOne.getMemberName());
        if (s==null){
            uacMember.setKindId(99L);
        }else {
            uacMember.setKindId(1L);
        }
        uacMember.setUscCode(s);
        uacMember.setMemberStatus(0);
        uacMember.setStaffName(registerOne.getStaffName());
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        uacMember.setCreatedTime(timestamp.toLocalDateTime());
        uacMember.setStasfTel(registerOne.getPhone());
        uacMember.setSgsStatus(0);
        uacMember.setStatus(0);
        uacMember.setAtpwStatus(0);
        uacMember.setLogoImg(HeadPortraitConstant.InputDataType.MEMBER_HEAD.getValue());
        if (registerOne.getSourceMark() != null) {
            uacMember.setMemberClientid(registerOne.getSourceMark());
        }
        boolean instrer = uacMemberService.save(uacMember);
        ThreadPoolUtils.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    tongbuerp(uacMember);
                    Map<String, Object> map = new HashMap<>();
                    map.put("memberName", registerOne.getMemberName());
                    map.put("uscCode", s);
                    map.put("staffName", registerOne.getStaffName());
                    map.put("mobile", registerOne.getPhone());
                    UawVipType vipType = uawVipTypeService.getById(registerOne.getVipTypeId());
                    map.put("role", vipType.getTypeName());
                    map.put("memberId", uacMember.getId());
                    map.put("userId", staffId);
                    //以事件方式处理邮件
                    AddMemberEvent addMemberEvent=new AddMemberEvent(this,map);
                    eventPublisher.publishEvent(addMemberEvent);

                    HashMap<String, String> chain = new HashMap<>();
                    chain.put("tenantId", String.valueOf(uacMember.getId()));
                    chain.put("tenantName", uacMember.getMemberName());
                    chain.put("uscc", uacMember.getUscCode());
                    SimpleDateFormat SDFormat = new SimpleDateFormat("MMddHHmmssSSS");
                    chain.put("creatTime", SDFormat.format(timestamp));
                    chain.put("userType", "员工");
                    chain.put("userId", String.valueOf(uacMember.getStaffId()));
                    chain.put("roleCodeRulesEnum", vipType.getRelevancyTypeId());
                    chain.put("chainId", "aeotradechain");
                    chain.put("userTypeEnum", "管理员");
                    mqSend.sendChain(JSONObject.toJSONString(chain), "chain");
                } catch (Exception e) {
                    log.warn(e.getMessage());
                }
            }
        });
        return instrer?uacMember.getId():0L;
    }


    @Ex("同步体检智囊异常")
    public void tongbuerp(UacMember uacMember) throws Exception {
        if (StringUtils.isNotEmpty(url) && StringUtils.isNotEmpty(uacMember.getMemberName()) && StringUtils.isNotEmpty(uacMember.getUscCode())) {
            Map<String, Object> map = new HashMap<>();
            map.put("company_name", uacMember.getMemberName());
            map.put("hmm_company_id", uacMember.getId());
            map.put("usc_code", uacMember.getUscCode());
            UacMemberSynchronization uacMemberSynchronization = new UacMemberSynchronization();
            uacMemberSynchronization.setMemberId(uacMember.getId());
            uacMemberSynchronization.setMemberNaem(uacMember.getMemberName());
            uacMemberSynchronization.setMemberUscc(uacMember.getUscCode());
            uacMemberSynchronization.setSynchronousStatus(1);
            String http = HttpRequestUtils.httpPost(url, map);
            RespResult httprespResult = JSONObject.parseObject(http, RespResult.class);
            uacMemberSynchronization.setReturnResult(httprespResult.toString());
            uacMemberSynchronizationMapper.insert(uacMemberSynchronization);
            if (httprespResult.getCode() != 200) {
                uacMemberSynchronization.setSynchronousStatus(0);
                uacMemberSynchronizationMapper.updateById(uacMemberSynchronization);
            }
        }
    }


    public Map loginWxUser(wxUser wxUser,String loginType) {
        try {
            UacUserConnection userConnection = getUserConnection(wxUser);
            Future<OAuth2AccessToken> oAuth2AccessTokenFuture = userConnectionService.findByOpenId(userConnection.getStaffId(), loginType);


            Map map = null;
            OAuth2AccessToken oAuth2AccessToken = oAuth2AccessTokenFuture.get();

            sendLoginMessage(userConnection.getStaffId(),oAuth2AccessToken.getValue());

            map = JacksonUtil.parseJson(JacksonUtil.toJsonWithFormat(oAuth2AccessToken), Map.class);

            userConnection.setProviderUserId(wxUser.getOpenid());
            map = loginjson(map, userConnection, null);

            return map;
        }catch (Exception e){
            log.warn(e.getMessage());
            return null;
        }
    }

    private UacUserConnection getUserConnection(wxUser wxUser) throws Exception {
        //保存微信用户数据
        LambdaQueryWrapper<UacUserConnection> uacUserConnectionLambdaQueryWrapper=new LambdaQueryWrapper<>();
        uacUserConnectionLambdaQueryWrapper.eq(UacUserConnection::getProviderId,BizConstant.ClientEnum.WECHAT.toString().toLowerCase())
                .eq(UacUserConnection::getUnionid, wxUser.getUnionid());
        UacUserConnection userConnection = uacUserConnectionService.getOne(uacUserConnectionLambdaQueryWrapper);
        if (null == userConnection) {
            userConnection = new UacUserConnection();
            userConnection.setProviderUserId(wxUser.getOpenid());
            userConnection.setProviderId(BizConstant.ClientEnum.WECHAT.toString().toLowerCase());
            userConnection.setUnionid(wxUser.getUnionid());
            uacUserConnectionService.save(userConnection);
            /**新用户添加*/
            UacStaff uacStaff = uacMemberStaffService.initUacStaffAndUacMember(userConnection, null, null, wxUser.getPhone());

            UacUserConnection uccn = new UacUserConnection();
            uccn.setStaffId(uacStaff.getId());
            uccn.setId(userConnection.getId());
            uacUserConnectionService.updateById(uccn);
            userConnection.setStaffId(uacStaff.getId());
        } else {
            UacUserConnection uccn = new UacUserConnection();
            BeanUtils.copyProperties(userConnection, uccn);
            uccn.setId(userConnection.getId());
            uccn.setProviderUserId(wxUser.getOpenid());
            uccn.setProviderId(BizConstant.ClientEnum.WECHAT.toString().toLowerCase());
            uccn.setUnionid(wxUser.getUnionid());
            uacUserConnectionService.updateById(uccn);
        }

        return userConnection;
    }

    public OAuth2AccessToken mobileWeixinLogin(wxUser wxUser,String loginType) {
        try {
            UacUserConnection userConnection = getUacUserConnection(wxUser);
            Future<OAuth2AccessToken> oAuth2AccessTokenFuture = userConnectionService.findAeoTokenByMobileWxLogin(userConnection.getStaffId(), loginType);

            OAuth2AccessToken oAuth2AccessToken = oAuth2AccessTokenFuture.get();

            sendLoginMessage(userConnection.getStaffId(),oAuth2AccessToken.getValue());

            return oAuth2AccessToken;

        }catch (Exception e){
            log.warn(e.getMessage());
            return null;
        }
    }

    private UacUserConnection getUacUserConnection(wxUser wxUser) throws Exception {
        //保存微信用户数据
        UacUserConnection userConnection = getUserConnection(wxUser);
        return userConnection;
    }

    private void sendLoginMessage(Long uacStaffId,String token){
        try {
            Map<String, Object> getMap = new HashMap<>();
            getMap.put("id", uacStaffId);
            getMap.put("apply", 0);
            HttpRequestUtils.httpGets(gatewayUrl+"/mam/uaw/VipMessage/loginMessage", getMap, token);
        }catch (Exception e){
            log.warn("登录后记录用户情况失败: {}",e.getMessage());
        }

    }
}
