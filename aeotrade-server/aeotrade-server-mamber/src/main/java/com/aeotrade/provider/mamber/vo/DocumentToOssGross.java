package com.aeotrade.provider.mamber.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @Auther: 吴浩
 * @Date: 2021-07-16 10:54
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Document("atci_document_to_oss_gross")
public class DocumentToOssGross {
    private String id;

    private String uscc;
    /**
     * 可用总量,单位GB
     */
    private String gross;

    /**
     * 可用总量,单位bety
     */
    private Long grossNumber;
}
