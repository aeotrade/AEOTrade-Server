package com.aeotrade.server.chain.utils;

import com.aeotrade.utlis.PinYinUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

/**
 * 上链业务工具类
 */
@Component
public class AeotradeChainUtil {
    @Autowired
    private RedisTemplate redisTemplate;

    //当日流水
    public String inc(String key,Integer len){
        Objects.requireNonNull(key,"sequence redis key not null");
        Objects.requireNonNull(key,"sequence redis id format len not null");

        StringBuffer sb=new StringBuffer();
        for (int i=1;i<=len;i++){
            sb.append("9");
        }

        Long c = redisTemplate.boundHashOps("sequence").increment(key,1);
        if (c==Long.valueOf(sb.toString())){
            redisTemplate.boundHashOps("sequence").increment(key,-99);
        }
        String format = String.format("%0" + (len != null ? len : 1) + "d", c-1);
        return format;
    }
    //企业拼音前四首字母大写
    public String qiye(String name,Integer len) throws Exception {
        Objects.requireNonNull(len,"sequence pinyin qiye not null");
        if(StringUtils.isEmpty(name)){
            throw new Exception("企业名称不能为空");
        }
        String py = PinYinUtil.converterToFirstSpell(name);
        if (py.length()>=len){
            return py.substring(0,len).toUpperCase(Locale.ROOT);
        }
        return py.toUpperCase(Locale.ROOT);
    }

    private static SimpleDateFormat SDFormat = new SimpleDateFormat("yyyyMMdd");
    public String riqi(){
        return SDFormat.format(new Date());
    }

    /**
     * 组织名称生成规则
     *      1. 区块链组织ID 对应 “AT_”+4位企业拼音前四首字母大写+“_”+两位角色码+8位日期码+2位流水号，如AT_HMTX_012022051801
     *      1. 流水号：从01、02一直到99这样以“1”为单位的递进，出现重复流水号递增
     * @param qiye
     * @param roleCodeRulesEnum
     * @return
     */
    public String seqCaId(String qiye, String roleCodeRulesEnum,String dateTime) throws Exception {
        Long code = Long.valueOf(roleCodeRulesEnum);
        Long riqi = Long.valueOf(riqi());
        Long dateTime1 = Long.valueOf(dateTime);
        long aLong = code+riqi+dateTime1;
        return "AT_"+qiye(qiye,4)+"_"+roleCodeRulesEnum.toString()+riqi()+codeUpdate(aLong%37);
    }

    //验证码转换
    public String codeUpdate(Long code){
        String[] s={"0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};
        if(code==0){
            return "0";
        }else{
            return s[Math.toIntExact(code)-1];
        }
    }
    /**
     * 用户名称生成规则
     *      1. 链上用户名称 对应 组织id+"_"+4位流水号，如：AT_HMTX_012022051801_0001
     * @param caId
     * @return
     */
    public String seqUserId(String caId){
        return caId+"_"+inc(caId,4);
    }

}
