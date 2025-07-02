package com.aeotrade.provider.admin.controller;

import com.aeotrade.provider.admin.entiy.ClientVersion;
import com.aeotrade.provider.admin.service.ClientVersionService;
import com.aeotrade.suppot.BaseController;
import com.aeotrade.suppot.RespResult;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;


/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author Auto-generator
 * @since 2022-05-26
 */
@RestController
@RequestMapping("/client/version")
public class ClientVersionController extends BaseController {


    @Autowired
    private ClientVersionService clientVersionService;

    /**
     * 通过id查询
     */
    @GetMapping("/get/byid/{id}")
    public RespResult getById(@PathVariable(value = "id") Long id) {
        return handleResult(clientVersionService.getById(id));
    }

    /**
     * 通过版本号查询
     */
    @GetMapping("/get/version/number/{number}")
    public RespResult getByNumber(@PathVariable(value = "number") String number) {
        return handleResult(clientVersionService.findByTypeAndNumber(null,number));
    }
    @GetMapping("/get/version/number/{number}/{type}")
    public RespResult getByAppTypeAndNumber(@PathVariable(value = "number") String number,@PathVariable(value = "type") String appType) {
        return handleResult(clientVersionService.findByTypeAndNumber(appType,number));
    }

    /**
     * 通过终端类型和版本号查询
     */
    @GetMapping("/get/{type}/{number}")
    public RespResult getByType(@PathVariable(value = "type",required = false) String appType,@PathVariable(value = "number") String number) {
        return handleResult(clientVersionService.findByTypeAndNumber(appType,number));
    }

    /**
     * 新增
     */
    @PostMapping("/save")
    public RespResult save(@RequestBody ClientVersion clientVersion) {
        clientVersion.setStatus(0);
        clientVersion.setCreatTime(LocalDateTime.now());
        int insert = clientVersionService.insert(clientVersion);
        if (insert == 3) {
            return handleResult(500, "版本号不能重复");
        }
        return handleOK();
    }

    /**
     * 通过id删除
     */
    @GetMapping("/delete/{id}")
    public RespResult delete(@PathVariable(value = "id") String ids) {
        String[] idsStrs = ids.split(",");
        for (String id : idsStrs) {
            ClientVersion clientVersion = clientVersionService.getById(Long.valueOf(id));
            clientVersion.setStatus(1);
            clientVersionService.updateClientVersion(clientVersion);
        }
        return handleOK();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    public RespResult updateById(@RequestBody ClientVersion clientVersion) {
        clientVersion.setStatus(0);
        int version = clientVersionService.updateClientVersion(clientVersion);
        if (version == 3) {
            return handleResult(500, "版本号不能重复");
        }
        return handleOK();
    }

    /**
     * 查询新版本数据
     * 沿用旧接口的基础是添加终端类型
     */
    @GetMapping("/list/new")
    public RespResult findListNew(@RequestParam(required = false) String type,@RequestParam String number) {
        return handleResult(clientVersionService.findNewList(type,number));
    }

    /**
     * 下载安装包
     * 沿用旧接口的基础是添加终端类型
     */
    @GetMapping("/find/new")
    public RespResult findNew(@RequestParam(required = false) String type, HttpServletResponse response) throws IOException {
        ClientVersion clientVersion = clientVersionService.findNew(type);
        if (null != clientVersion) {
            response.sendRedirect(clientVersion.getDownloadOss());
        }
        return handleOK();
    }

    /**
     * 分页查询
     */
    @GetMapping("/page")
    public RespResult page(@RequestParam(required = false) String type,@RequestParam Integer pageSize, @RequestParam Integer pageNo) {
        Page<ClientVersion> listByPage = clientVersionService.findListByPage(type,pageSize, pageNo);
        return handleResult(listByPage);
    }
}
