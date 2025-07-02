package com.aeotrade.provider.admin.controller;

import com.aeotrade.provider.admin.entiy.UacMember;
import com.aeotrade.provider.admin.service.impl.UacMemberServiceImpl;
import com.aeotrade.base.social.AeotradeUser;
import com.aeotrade.exception.AeotradeException;
import com.aeotrade.provider.admin.uacVo.FindByIds;
import com.aeotrade.provider.admin.uacVo.UacMemberDto;
import com.aeotrade.suppot.BaseController;
import com.aeotrade.suppot.PageList;
import com.aeotrade.suppot.RespResult;
import com.aeotrade.suppot.RespResultMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 用户管理中心
 *
 * @author lifei
 */
@RestController
@RequestMapping(value = "/uac/member")
//@Api(value = "Web - UacMemberController")
@Slf4j
public class UacMemberController extends BaseController {
    @Autowired
    private UacMemberServiceImpl memberService;

    @GetMapping("findlist")
    //@ApiOperation(httpMethod = "GET", value = "查询企业")
    public RespResult find(Long id) {
        UacMember memberList = memberService.getById(id);
        return handleResult(memberList);
    }

    @GetMapping("ids")//get改为post 为保证后端更新不影响前端所以保留get请求 后面可以删掉get请求接口
    //@ApiOperation(httpMethod = "GET", value = "根据ID集合查询企业")
    public RespResult findByIds(Long[] ids) {
        if(ids.length==0){
            return handleResult(new ArrayList<>());
        }
        return handleResult(memberService.lambdaQuery().in(UacMember::getId,ids).list());
    }

    @PostMapping("ids")
    //@ApiOperation(httpMethod = "POST", value = "根据ID集合查询企业")
    public RespResult findByIds(@RequestBody FindByIds findByIds) {
        if(findByIds.getIds().length==0){
            return handleResult(new ArrayList<>());
        }
        return handleResult(memberService.lambdaQuery().in(UacMember::getId,findByIds.getIds()).list());
    }

    @GetMapping("list")
    //@ApiOperation(httpMethod = "GET", value = "列出所有的企业")
    //@ApiImplicitParams({//@ApiImplicitParam(name = "pageSize", value = "每页多少条", defaultValue = "10"),
            //@ApiImplicitParam(name = "pageNo", value = "当前要查询的页码", defaultValue = "1")})
    public RespResult listMembers(Map map,/*提取当前用户对象信息*/AeotradeUser aeotradeUser, int pageSize, int pageNo) {
        PageList<UacMember> memberList = memberService.findAllMemberPage(pageSize, pageNo);
        return handleResult(memberList);
    }

    @PostMapping("create")
    //@ApiOperation(httpMethod = "POST", value = "新增一家企业")
    public RespResult<UacMemberDto> createMember(@RequestBody UacMemberDto member, AeotradeUser aeotradeUser) {
        try {
            memberService.insertMember(member);
            return handleResult(member);
        } catch (Exception ex) {
            return handleFail(ex);
        }

    }

    @PostMapping("update")
    //@ApiOperation(httpMethod = "POST", value = "修改一家企业")
    public RespResult createMember(@RequestBody UacMember uacMember) {
        try {
            if (null == uacMember.getUscCode() || uacMember.getUscCode().length() != 18) {
                throw new AeotradeException("统一社会信用代码有误,请确认无误后在提交");
            }
            memberService.updateById(uacMember);
            return handleResult(uacMember);
        } catch (Exception ex) {
            return handleFail(ex);
        }

    }

    //@ApiOperation(httpMethod = "GET", value = "查询企业")
    @GetMapping("list/kindId")
    //@ApiImplicitParam(name = "kindId", value = "企业类型", defaultValue = "1")
    public RespResult listMembersByKind(Long kindId) {
        if (null == kindId || kindId == 0) {
            List<UacMember> memberList = memberService.lambdaQuery().list();
            return handleResult(memberList);
        }
        List<UacMember> memberList = memberService.lambdaQuery().eq(UacMember::getKindId,kindId).list();
        return handleResult(memberList);
    }

    //@ApiOperation(httpMethod = "GET", value = "查询个人名下所有企业")
    @GetMapping("list/staffId")
    //@ApiImplicitParam(name = "staffId", value = "用户id", defaultValue = "1")
    public RespResult listMembersByStaffId(Long staffId) {
        if (null == staffId) {
            throw new AeotradeException("用户ID不能为空");
        }
        List<String> memberList = memberService.listMembersByStaffId(staffId);
        return handleResult(memberList);
    }

    //@ApiOperation(httpMethod = "GET", value = "根据企业名称和企业法人查询企业")
    @GetMapping("list/membername")
    public RespResult listMemberByName(@RequestParam String name) {
        List<UacMember> uacMembers = memberService.listMemberByName(name);
        return handleResult(uacMembers);
    }

    //@ApiOperation(httpMethod = "GET", value = "根据token和企业id查询企业信息")
    //备注：第三方服务在使用，企业名片的 以前的体检智囊
    @GetMapping("/get/token")
    public RespResult getToken(@RequestParam Long memberId, HttpServletRequest request) {
        if (null == memberId) {
            throw new AeotradeException("参数不能为空");
        }
        String authorization = request.getHeader("Authorization");
        String token = null;
        if (authorization.contains("bearer ")) {
            token = authorization.replaceAll("bearer ", "");
        }
        if (authorization.contains("Bearer ")) {
            token = authorization.replaceAll("Bearer ", "");
        }
        if (token == null) {
            return RespResultMapper.wrap(200, "success", "0");
        }
        Boolean aBoolean = memberService.getToken(token, memberId);
        if (aBoolean) {
            UacMember uacMember = memberService.get(Long.valueOf(memberId));
            return RespResultMapper.wrap(200, "success", uacMember.getUscCode());
        }
        return RespResultMapper.wrap(200, "success", "0");
    }

    //@ApiOperation(httpMethod = "GET", value = "查询所有企业")
    @GetMapping("list/memberAll")
    public RespResult memberAll() {
        List<UacMember> uacMembers = memberService.listMemberAll();
//        System.out.println("接口返回：" + uacMembers);
        return handleResult(uacMembers);
    }

    //@ApiOperation(httpMethod = "GET", value = "根据全称或USCC查询企业")
    @GetMapping("name")
    public RespResult findMemberList(@RequestParam String uscc){
        if (StringUtils.isBlank(uscc)){
            return handleFail("参数不能为空");
        }
        if (uscc.length()<2){
            return handleResult(new ArrayList<>());
        }
        if (uscc.trim().equals("有限公司")){
            return handleResult(new ArrayList<>());
        }
        List<UacMember> uacMemberList = memberService.listMemberAllForUsccOrName(uscc);
        return handleResult(uacMemberList);
    }
}
