package com.aeotrade.provider.service.feign;
import com.aeotrade.provider.service.fallback.FlaskFeignCallback;
import feign.Param;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import java.util.Map;
public interface FlaskFeign {
    /**
     * 登录erp
     * @param map
     * @return
     */
    String sendToken( @Param("map") Map map ) throws Exception;

    /**
     * 创建公司
     * @param map
     * @return
     */
    String sendMember(@Param("map") Map map) throws Exception;

    /**同步用户信息*/
    String updateMember(@Param("map") Map map) throws Exception;

    String testPost( @Param("post_data") Map<String,Object> post_data) throws Exception;
}
