package com.aeotrade.provider.mamber.vo;


import com.aeotrade.provider.mamber.entity.UawWorkbench;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @Auther: 吴浩
 * @Date: 2022-04-21 17:41
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkbenchTypeVo extends UawWorkbench {
    private int isChoice;
}
