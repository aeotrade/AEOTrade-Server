package com.aeotrade.provider.admin.controller;

import com.aeotrade.provider.admin.adminVo.SysThemeVo;
import com.aeotrade.provider.admin.entiy.SysTheme;
import com.aeotrade.provider.admin.service.SysThemeService;
import com.aeotrade.suppot.BaseController;
import com.aeotrade.suppot.RespResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 前端页面主题配置
 */
@RestController
@RequestMapping("theme")
public class PageThemeController extends BaseController {
    @Autowired
    private SysThemeService sysThemeService;

    /**
     * 主题列表
     *
     * @param size
     * @param current
     * @param memberId
     * @param userId
     * @return
     */
    @GetMapping("/page")
    public RespResult page(@RequestParam(name = "size", required = false, defaultValue = "10") Integer size,
                           @RequestParam(name = "current", required = false, defaultValue = "1") Integer current,
                           String memberId, String userId, String theme) {
        LambdaQueryWrapper<SysTheme> sysThemeLambdaQueryWrapper = new LambdaQueryWrapper<>();
        sysThemeLambdaQueryWrapper.eq(SysTheme::getType, 1).eq(SysTheme::getEnabling, 1)
                .eq(!StringUtils.isEmpty(theme), SysTheme::getTheme, theme)
                .or(!StringUtils.isEmpty(memberId), wrapper -> wrapper.eq(SysTheme::getType, 2).eq(SysTheme::getCustomizer, memberId))
                .or(!StringUtils.isEmpty(userId), wrapper -> wrapper.eq(SysTheme::getType, 3).eq(SysTheme::getCustomizer, userId))
                .orderByDesc(SysTheme::getType, SysTheme::getCreateTime);
        IPage paged = sysThemeService.page(new Page<>(current, size), sysThemeLambdaQueryWrapper);
        return handleResult(paged);
    }

    /**
     * 添加主题
     *
     * @param sysThemeVo
     * @return
     */
    @PostMapping("/save")
    public RespResult save(@RequestBody SysThemeVo sysThemeVo) {
        if (sysThemeVo.getTheme().isEmpty()) {
            return handleFail("主题名称不能为空");
        }
        if (sysThemeVo.getThemeContent().isEmpty()) {
            return handleFail("主题配置内容不能为空");
        }
        SysTheme sysTheme = new SysTheme();
        BeanUtils.copyProperties(sysThemeVo, sysTheme);
        sysTheme.setCreateTime(LocalDateTime.now());
        if (sysThemeVo.getType() == null) {
            sysTheme.setType(1);
        }
        if (sysThemeVo.getEnabling() == null) {
            sysTheme.setEnabling(1);
        }
        sysTheme.setId(null);//使用表主键自动生成
        sysThemeService.save(sysTheme);
        return handleOK();
    }

    /**
     * 修改主题
     *
     * @param sysThemeVo
     * @return
     */
    @PostMapping("/update")
    public RespResult update(@RequestBody SysThemeVo sysThemeVo) {
        if (sysThemeVo.getId() == null) {
            return handleFail("编辑数据时ID参数不能为空");
        }
        SysTheme sysTheme = new SysTheme();
        BeanUtils.copyProperties(sysThemeVo, sysTheme);
        sysTheme.setCreateTime(LocalDateTime.now());
        sysThemeService.updateById(sysTheme);
        return handleOK();
    }

    /**
     * 根据ID查询主题
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public RespResult getTheme(@PathVariable String id) {
        return handleResult(sysThemeService.getById(id));
    }

    /**
     * 账户对应的当前主题
     *
     * @param memberId
     * @param userId
     * @return
     */
    @GetMapping("/current")
    public RespResult currentTheme(String memberId, String userId, String theme) {
        LambdaQueryWrapper<SysTheme> themeLambdaQueryWrapper = new LambdaQueryWrapper<>();
        themeLambdaQueryWrapper.eq(SysTheme::getType, 1).eq(SysTheme::getEnabling, 1)
                .eq(!StringUtils.isEmpty(theme), SysTheme::getTheme, theme)
                .or(!StringUtils.isEmpty(memberId), wrapper -> wrapper.eq(SysTheme::getType, 2).eq(SysTheme::getCustomizer, memberId))
                .or(!StringUtils.isEmpty(userId), wrapper -> wrapper.eq(SysTheme::getType, 3).eq(SysTheme::getCustomizer, userId))
                .orderByDesc(SysTheme::getType, SysTheme::getCreateTime);
        List<SysTheme> list = sysThemeService.list(themeLambdaQueryWrapper);
        return handleResult(list.size() == 0 ? null : list.get(0));
    }


    //主题表存储结构
    // id,create_time时间，type类型（1全局、2企业、3个人），enabling启用状态（0 false、1 true）,customizer 自定义者标识ID，theme 配置内容
}
