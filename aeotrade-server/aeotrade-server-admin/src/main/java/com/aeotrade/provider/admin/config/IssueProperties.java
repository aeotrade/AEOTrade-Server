package com.aeotrade.provider.admin.config;

import com.aeotrade.provider.admin.adminVo.IssueConfigMd;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
@Data
@Component
@ConfigurationProperties(prefix = "hmtx.chain")
public class IssueProperties {
    private List<IssueConfigMd> issueConfig;
}