package com.aeotrade.provider.admin.controller;



import com.aeotrade.provider.admin.service.impl.UacBanknameServiceImpl;
import com.aeotrade.suppot.BaseController;
import com.aeotrade.suppot.RespResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * UawResourceController  银行类型controller
 */
@RestController
@RequestMapping(value = "/uac/BankName/",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
//@Api(value = "UacBankNameController",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@Slf4j
@CrossOrigin
public class UacBankNameController extends BaseController {
    @Autowired
    private UacBanknameServiceImpl uacBankNameService;

    /**
     * 列出所有的银行名称
     * @param
     * @return
     */
    @GetMapping("find/BankName")
    //@ApiOperation(httpMethod = "GET", value = "列出所有的银行名称")
    public RespResult findRightsTypeList() {
        try {
            return handleResult(uacBankNameService.list());
        } catch (Exception e) {
            return handleFail(e);
        }
    }
}
