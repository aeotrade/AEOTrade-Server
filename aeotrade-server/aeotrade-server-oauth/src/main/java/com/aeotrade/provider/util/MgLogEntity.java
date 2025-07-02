package com.aeotrade.provider.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * @Author: yewei
 * @Date: 17:18 2020/11/26
 * @Description:
 */
@Document(collection = "hmm_log")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MgLogEntity implements Serializable {
    private String id =null;

    private String userAgent=null;

    private Map<String, String> userAgentDetils=null;

    private String name=null;
    private String qudao=null;
    private String app=null;
    private String tel =null;
    private String time =null;
    private String staffId;
}
