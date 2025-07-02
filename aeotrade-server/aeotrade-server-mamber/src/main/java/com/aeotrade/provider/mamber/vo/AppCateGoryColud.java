package com.aeotrade.provider.mamber.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.util.List;

/**
 * @Author yewei
 * @Date 2022/5/30 10:00
 * @Description:
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppCateGoryColud   implements Serializable {


    private String appTypeName;


    @Id
    private Long cid;

    private Integer sort;

    private java.sql.Timestamp updatedTime;

    private List<AppCloudDto> appList;

    private String img;

    private String description;

}
