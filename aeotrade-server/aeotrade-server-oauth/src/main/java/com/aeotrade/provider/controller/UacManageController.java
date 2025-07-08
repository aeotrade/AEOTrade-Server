package com.aeotrade.provider.controller;

import com.aeotrade.annotation.Ex;
import com.aeotrade.base.constant.BizConstant;
import com.aeotrade.exception.AeotradeException;
import com.aeotrade.provider.dto.WxTokenDto;
import com.aeotrade.provider.dto.WxTencentDto;
import com.aeotrade.provider.dto.WxUacStaffDto;
import com.aeotrade.provider.model.*;
import com.aeotrade.provider.service.*;
import com.aeotrade.provider.service.async.UserConnectionService;
import com.aeotrade.provider.util.HttpRequestUtils;
import com.aeotrade.suppot.BaseController;
import com.aeotrade.suppot.RespResult;
import com.aeotrade.utlis.JacksonUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @Author: yewei
 * @Date: 2020/2/26 10:52
 */
@RestController
@RequestMapping(value = "/social/")
@Slf4j
public class UacManageController extends BaseController {
    @Autowired
    private UacUserConnectionService uacUserConnectionService;
    @Autowired
    private UserConnectionService userConnectionService;
    @Autowired
    private UacStaffService uacStaffService;
    @Autowired
    private UacMemberService uacMemberService;
    @Autowired
    private UacMemberStaffService uacMemberStaffService;

    @Value("${wx.login.appid:}")
    private String appid;
    @Value("${wx.login.appsecret:}")
    private String secret;



    /**
     * 用户接受邀请
     *
     * @param wxUacStaffDto
     * @return
     */
    @Ex(value = "插入并同步企业信息UacErp",timeUnit = TimeUnit.HOURS)
    @PostMapping("save/user")
    public Callable<RespResult> saveUser(@RequestBody WxUacStaffDto wxUacStaffDto) {
        String code = wxUacStaffDto.getCode();
        String state = wxUacStaffDto.getState();
        log.info("进入授权回调,code:{},state:{}", code, state);
        Callable<RespResult> result = new Callable<RespResult>() {
            @Override
            public RespResult call() throws Exception {
                if (appid == null || secret == null){
                    return handleFail(new RuntimeException("请配置appid和secret"));
                }
                if (StringUtils.isBlank(code)) {
                    return handleFail(new RuntimeException("请重新扫码"));
                }
                WxTokenDto wxTokenDto = null;
                WxTencentDto wxTencentDto = null;
                // wxTokenDto=wechatUtil.getWxTokenDto(code);
                /**1.通过code获取access_token*/
                String codeUrl = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" +
                        appid + "&secret=" + secret + "&code=" + code + "&grant_type=authorization_code";
                String access = HttpRequestUtils.httpGet(codeUrl, null, null);
                wxTokenDto = JacksonUtil.parseJson(access, WxTokenDto.class);
                if (wxTokenDto.getAccess_token() == null) {
                    handleFail(new RuntimeException("请刷新二维码"));
                }
                //wxUserinfoDto = wechatUtil.getWxUserinfoDto(wxTokenDto.getAccess_token(),wxTokenDto.getOpenid());
                /**2.通过access_token和openid获取用户信息*/
                if (StringUtils.isNotEmpty(wxTokenDto.getAccess_token()) && StringUtils.isNotEmpty(wxTokenDto.getOpenid())) {
                    String url = "https://api.weixin.qq.com/sns/userinfo?access_token=" + wxTokenDto.getAccess_token() +
                            "&openid=" + wxTokenDto.getOpenid();
                    String userInfoStr = HttpRequestUtils.httpPost(url, null);
                    wxTencentDto = JacksonUtil.parseJson(userInfoStr, WxTencentDto.class);
                    if (null == wxTencentDto) {
                        throw new AeotradeException("用户信息获取失败");
                    }
                } else {
                    throw new AeotradeException("access_token或openid为空");
                }
                List<UacUserConnection> list = uacUserConnectionService.lambdaQuery()
                        .eq(UacUserConnection::getProviderUserId, BizConstant.ClientEnum.WECHAT.toString().toLowerCase())
                        .eq(UacUserConnection::getUnionid, wxTencentDto.getUnionid()).list();
                //保存微信用户数据
                UacUserConnection userConnection =list.size()>0?list.get(0):null;

                if (userConnection != null && userConnection.getStaffId() != null && StringUtils.isNotEmpty(userConnection.getUnionid())) {
                    if (userConnection.getUnionid().equals(wxTencentDto.getUnionid()) == true) {
                        /**1.获得已有的staffid*/
                        userConnection.getStaffId();
                        /**2.获得现有的staffid*/
                        wxUacStaffDto.getId();
                        /**3.查询现有员工加入的企业*/
                        List<UacMemberStaff> uacMemberStaffs = uacMemberStaffService.lambdaQuery().eq(UacMemberStaff::getStaffId,wxUacStaffDto.getId()).list();
                        if (uacMemberStaffs.size() != 0) {
                            UacMemberStaff meStaff = null;
                            for (UacMemberStaff staff : uacMemberStaffs) {
                                meStaff = new UacMemberStaff();
                                meStaff.setMemberId(staff.getMemberId());
                                meStaff.setStaffId(userConnection.getStaffId());
                                uacMemberStaffService.save(meStaff);
                            }
                            /**删除重复数据*/
                            uacMemberStaffService.deleteStaff(wxUacStaffDto.getId());
                            wxUacStaffDto.setId(userConnection.getStaffId());
                        } else {
                            throw new AeotradeException("查询企业员工中间表时数据缺失");
                        }
                    }
                }
                Future<UacUserConnection> uacUserConnectionFuture = userConnectionService.inorupdateUacConnectionTencents(wxTokenDto, wxTencentDto, userConnection, wxUacStaffDto.getId());
                /**修改员工信息*/
                if (wxUacStaffDto.getId() != null) {
                    uacStaffService.updatestaff(wxTencentDto, wxUacStaffDto, wxTokenDto);
                }
                Future<OAuth2AccessToken> oAuth2AccessTokenFuture = userConnectionService.getOAuth2AccessToken();
                List<UacUserConnection> connections = uacUserConnectionService.lambdaQuery()
                        .eq(UacUserConnection::getProviderUserId, BizConstant.ClientEnum.WECHAT.toString().toLowerCase())
                        .eq(UacUserConnection::getUnionid, wxTencentDto.getUnionid()).list();
                UacUserConnection user =connections.size()>0?connections.get(0):null;
                Map map = null;
                while (true) {//死循环，每隔2000ms执行一次，判断一下这三个异步调用的方法是否全都执行完了。
                    if (oAuth2AccessTokenFuture.isDone()) {//使用Future的isDone()方法返回该方法是否执行完成
                        UacUserConnection connection = uacUserConnectionFuture.get();
                        //如果异步方法全部执行完，跳出循环
                        OAuth2AccessToken oAuth2AccessToken = oAuth2AccessTokenFuture.get();
                        map = JacksonUtil.parseJson(JacksonUtil.toJsonWithFormat(oAuth2AccessToken), Map.class);
                        map.put("provideruserid", wxTencentDto.getOpenid());
                        map.put("providermqid", connection.getProviderMpId());
                        map.put("unionid", connection.getUnionid());

                        map.put("nickname", wxTencentDto.getNickname());
                        map.put("headimgurl", wxTencentDto.getHeadimgurl());
                        log.debug("tokenjson=" + JacksonUtil.toJson(oAuth2AccessToken));
                        //判断是否已经绑定企业
                        map.put("isbind", false);
                        Map<String, Object> bindmap = new HashMap<>();
                        bindmap.put("memberid", StringUtils.EMPTY);
                        bindmap.put("membername", StringUtils.EMPTY);
                        bindmap.put("staffid", StringUtils.EMPTY);
                        bindmap.put("staffname", StringUtils.EMPTY);
                        bindmap.put("isatff", 0);
                        bindmap.put("memberstatus", 0);
                        bindmap.put("uscc", StringUtils.EMPTY);
                        if (user != null && user.getStaffId() != null) {
                            map.put("isbind", true);
                            UacStaff uacStaff = uacStaffService.getById(user.getStaffId());
                            UacMember uacMember = uacMemberService.getById(uacStaff.getMemberId());
                            bindmap.put("memberid", uacStaff.getMemberId().toString());
                            bindmap.put("membername", uacMember.getMemberName());
                            // bindmap.put("memberstatus", uacMember.getPersonageStatus());
                            bindmap.put("staffid", uacStaff.getId().toString());
                            bindmap.put("staffname", uacStaff.getStaffName());
                            bindmap.put("uscc", uacMember.getUscCode());
                            if (uacMember.getKindId() != null && uacMember.getKindId().equals(BizConstant.MemberKindEnum.STAFF_KINDID.getValue())) {
                                bindmap.put("isatff", 1);
                            }
                        }
                        map.put("bind", bindmap);
                        break;
                    }
                }

                return handleResult(map);

            }
        };

        return result;

    }

}
