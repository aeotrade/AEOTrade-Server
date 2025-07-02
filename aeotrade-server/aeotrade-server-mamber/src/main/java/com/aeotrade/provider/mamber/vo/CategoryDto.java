package com.aeotrade.provider.mamber.vo;


import com.aeotrade.provider.mamber.entity.AppCategory;
import com.aeotrade.provider.mamber.vo.AppListVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class CategoryDto extends AppCategory implements Serializable {

    private Integer rows;

    private List<AppListVo> appListVos;

}
