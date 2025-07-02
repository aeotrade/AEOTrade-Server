package com.aeotrade.provider.mamber.vo;

import com.aeotrade.suppot.PageList;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.Document;

import java.io.Serializable;
import java.util.Map;

/**
 * @Author: yewei
 * @Date: 15:52 2021/1/20
 * @Description:
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PmsCatalogInfoDto extends  PmsCatalogInfo implements Serializable {
    private Map<Long,String> category;
    private PageList<Document> pageList;
    private Integer total;
}
