package com.aeotrade.provider.service.fallback;

import com.aeotrade.annotation.Ex;
import com.aeotrade.provider.service.feign.FlaskFeign;
import com.aeotrade.suppot.BaseController;
import com.aeotrade.utlis.HttpRequestUtils;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;


@Service
public class FlaskFeignCallback extends BaseController implements FlaskFeign {

    private static final String url = "http://localhost:5000";


    @Override
    @Ex(value = "登录erp", count = 1, timeUnit = TimeUnit.HOURS)
    public String sendToken(Map map) throws Exception {

        String s = HttpRequestUtils.httpPost(url + "/login", map);
        return s;

    }

    @Override
    @Ex(value = "创建企业同步erp", count = 1, timeUnit = TimeUnit.HOURS)
    public String sendMember(Map map) throws Exception {
        String s = HttpRequestUtils.httpPost(url + "/create_company", map);
        return s;

    }

    @Override
    @Ex(value = "企业同步修改erp", count = 1, timeUnit = TimeUnit.HOURS)
    public String updateMember(Map map) throws Exception {

        String s = HttpRequestUtils.httpPost(url + "/sync_user_nfo", map);
        return s;

    }

    @Override
    public String testPost(Map<String, Object> post_data) throws Exception {
        try {
            String s = HttpRequestUtils.httpPost(url + "/post_test", post_data);
            return s;
        } catch (Exception e) {
            throw e;
        }
    }

}
