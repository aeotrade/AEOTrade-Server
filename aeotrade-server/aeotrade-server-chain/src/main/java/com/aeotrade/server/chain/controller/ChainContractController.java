package com.aeotrade.server.chain.controller;

import com.aeotrade.chainmaker.exception.AeoChainException;
import com.aeotrade.chainmaker.model.BizInvokeRecords;
import com.aeotrade.chainmaker.model.ChainContractByteCodes;
import com.aeotrade.chainmaker.repository.ChainContractByteCodesMapper;
import com.aeotrade.chainmaker.service.BizInvokeRecordsService;
import com.aeotrade.chainmaker.service.ChainContractByteCodesService;
import com.aeotrade.server.chain.ChainTransactionService;
import com.aeotrade.server.chain.domain.BizContractParam;
import com.aeotrade.server.chain.domain.BizVoteContractParam;
import com.aeotrade.suppot.BaseController;
import com.aeotrade.suppot.PageList;
import com.aeotrade.suppot.RespResult;
import com.aeotrade.suppot.RespResultMapper;
import com.aeotrade.utlis.ThreadPoolUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.Binary;
import org.chainmaker.pb.common.ContractOuterClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class ChainContractController extends BaseController {
    @Autowired
    private ChainContractByteCodesService chainContractByteCodesImpl;
    @Autowired
    private BizInvokeRecordsService bizInvokeRecordsService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private ChainTransactionService chainTransactionService;
    @Autowired
    private ChainContractByteCodesMapper chainContractByteCodesMapper;

    /**
     * 投票创建合约
     * @param bizVoteContractParam
     * @throws IOException
     */
    @PostMapping(value = "contract/vote/create")
    public RespResult createVoteContract(BizVoteContractParam bizVoteContractParam) throws AeoChainException {

        if (bizVoteContractParam==null||bizVoteContractParam.getVoteTall()==null
                ||bizVoteContractParam.getContractName()==null||bizVoteContractParam.getOrgId()==null){
            return handleFail("所有参数必填");
        }
        // 添加合约名的规则，只能使用 字母开头，后面可加下划线(_)或者数字，名称长度小于128
        Pattern p = Pattern.compile("^[a-z0-9A-Z][_a-z0-9A-Z]{3,127}$");
        Matcher m = p.matcher(bizVoteContractParam.getContractName());
        if (!m.matches()){// 不符合规则给于提示
            return handleFail("只能使用字母数字开头,后面可加下划线(_)或者数字,名称长度大于3小于128");
        }

        BizInvokeRecords bizInvokeRecords=new BizInvokeRecords();
        bizInvokeRecords.setContractName(bizVoteContractParam.getContractName());

        List<BizInvokeRecords> list = bizInvokeRecordsService.findListByExample(bizInvokeRecords);
        if (list.stream().anyMatch(r->
            r.getIsDel()&&r.getContractName().equals(bizVoteContractParam.getContractName().trim())
        )){
            return handleFail("合约名称已经存在，或投票已经完成");
        }
        if (!list.isEmpty()){
            BizInvokeRecords records = list.get(0);
            if (records.getVoteTall().intValue()!=bizVoteContractParam.getVoteTall().intValue()){
                return handleFail("投票总数与之前的不一样");
            }
            if (list.stream().filter(r->r.getOrgId().equals(bizVoteContractParam.getOrgId())).findFirst().isPresent()) {
                return handleFail("已经投过票了");
            }
        }
        if (list.size()==bizVoteContractParam.getVoteTall()){
            return RespResultMapper.wrap(RespResult.SUCCESS_CODE, "投票成功", null);
        }

        bizInvokeRecords.setVoteTall(bizVoteContractParam.getVoteTall());
        bizInvokeRecords.setOrgId(bizVoteContractParam.getOrgId());

        bizInvokeRecords.setCreateAt(new Date());
        bizInvokeRecords.setUpdateAt(new Date());
        bizInvokeRecords.setIsDel(false);

        if (bizVoteContractParam.getContractId()!=null&&bizVoteContractParam.getByteCodes()==null){
            if (!chainContractByteCodesImpl.existsById(bizVoteContractParam.getContractId())){
                return handleFail("contractId 不正确");
            }
            bizInvokeRecords.setContractId(bizVoteContractParam.getContractId());
        }

        if (bizVoteContractParam.getByteCodes()!=null){
            try {
                bizInvokeRecords.setByteCodes(new Binary(bizVoteContractParam.getByteCodes().getBytes()));
            } catch (IOException e) {
                return handleFail("合约包错误");
            }
        }
        if (bizVoteContractParam.getRuntimeType()!=null){
            bizInvokeRecords.setRuntimeType(bizVoteContractParam.getRuntimeType().getNumber());
        }
        if (bizVoteContractParam.getVersion()!=null){
            bizInvokeRecords.setVersion(bizInvokeRecords.getVersion());
        }

        // 添加记数
        if (invokeTimes("invoke_times:"+bizInvokeRecords.getContractName(),bizInvokeRecords.getVoteTall())){
            ChainContractByteCodes chainContractByteCodes=new ChainContractByteCodes();
            chainContractByteCodes.setIsDel(false);
            List<ChainContractByteCodes> list1 = chainContractByteCodesImpl.findListByExample(chainContractByteCodes);
            log.info("合约程序包个数{}",list1.size());
            ChainContractByteCodes chainContractByteCodes1=list1.get(0);
            // 上链
            log.info("上链中。。。 {}-{}-{}-{}-{}-{}",chainContractByteCodes1.getUserId(),chainContractByteCodes1.getChainId(),bizInvokeRecords.getContractName(),
                    chainContractByteCodes1.getVersion(), ContractOuterClass.RuntimeType.forNumber(chainContractByteCodes1.getRuntimeType().intValue()),chainContractByteCodes1.getByteCodes().getData().length);

            // 上链消费时间大于10秒，暂时使用异步执行
            ThreadPoolUtils.execute(()-> {
                    chainTransactionService.createContrant(chainContractByteCodes1.getUserId(),chainContractByteCodes1.getChainId(),bizInvokeRecords.getContractName(),
                            chainContractByteCodes1.getVersion(), ContractOuterClass.RuntimeType.forNumber(chainContractByteCodes1.getRuntimeType().intValue()),
                            null, chainContractByteCodes1.getByteCodes().getData());

            });

        }

        bizInvokeRecordsService.save(bizInvokeRecords);

        return RespResultMapper.wrap(RespResult.SUCCESS_CODE, "投票成功", new PageList<>());
    }

    /**
     * 创建链上合约
     * @param bizVoteContractParam
     * @return
     */
    @PostMapping(value = "contract/create")
    public RespResult createContract(BizVoteContractParam bizVoteContractParam){
        if (StringUtils.isBlank(bizVoteContractParam.getContractName())
                || StringUtils.isBlank(bizVoteContractParam.getVersion())
                || bizVoteContractParam.getRuntimeType()==null
                || bizVoteContractParam.getByteCodes()==null){
            return RespResultMapper.wrap(RespResult.ERROR_CODE, "比传参数不全", null);
        }
        try {
            chainTransactionService.createContrant("0001001","aeotradechain", bizVoteContractParam.getContractName(),
                    bizVoteContractParam.getVersion(), ContractOuterClass.RuntimeType.forNumber(bizVoteContractParam.getRuntimeType().getNumber()),
                    null, bizVoteContractParam.getByteCodes().getBytes());
        } catch (IOException e) {
            return RespResultMapper.wrap(RespResult.ERROR_CODE, "FAIL", e.getMessage());
        }

        return RespResultMapper.wrap(RespResult.SUCCESS_CODE, "合约升级成功", null);
    }

    /**
     * 升级合约 版本号
     * @param bizVoteContractParam
     * @return
     */
    @PostMapping(value = "contract/upgrade")
    public RespResult upgradeContract(BizVoteContractParam bizVoteContractParam){
        if (StringUtils.isBlank(bizVoteContractParam.getContractName())
                || StringUtils.isBlank(bizVoteContractParam.getVersion())
                || bizVoteContractParam.getRuntimeType()==null
                || bizVoteContractParam.getByteCodes()==null){
            return RespResultMapper.wrap(RespResult.ERROR_CODE, "比传参数不全", null);
        }
        try {
            chainTransactionService.upgradeContrant("0001001","aeotradechain", bizVoteContractParam.getContractName(),
                    bizVoteContractParam.getVersion(), ContractOuterClass.RuntimeType.forNumber(bizVoteContractParam.getRuntimeType().getNumber()),
                    null, bizVoteContractParam.getByteCodes().getBytes());
        } catch (IOException e) {
            return RespResultMapper.wrap(RespResult.ERROR_CODE, "FAIL", e.getMessage());
        }

        return RespResultMapper.wrap(RespResult.SUCCESS_CODE, "合约升级成功", null);
    }

    /**
     * 准备合约程序包
     * @param bizContractParam
     */
    @PostMapping(value = "contract/package/create")
    public Long createContract(BizContractParam bizContractParam) throws IOException {
        ChainContractByteCodes chainContractByteCodes=new ChainContractByteCodes();
        chainContractByteCodes.setIsDel(false);
        List<ChainContractByteCodes> contractList = chainContractByteCodesImpl.findListByExample(chainContractByteCodes);
        for (ChainContractByteCodes cbc : contractList) {
            cbc.setIsDel(true);
            chainContractByteCodesMapper.save(cbc);
        }
        if (StringUtils.isBlank(bizContractParam.getUserId())){
            SecureRandom secureRandom = new SecureRandom();
            chainContractByteCodes.setUserId(String.valueOf(1000 + secureRandom.nextInt(9000)));
        }else {
            chainContractByteCodes.setUserId(bizContractParam.getUserId());
        }
        chainContractByteCodes.setCreateAt(new Date());
        chainContractByteCodes.setUpdateAt(new Date());
        chainContractByteCodes.setChainId(bizContractParam.getChainId());
        chainContractByteCodes.setByteCodes(new Binary(bizContractParam.getByteCodes().getBytes()));
        chainContractByteCodes.setContractName(bizContractParam.getContractName());
        chainContractByteCodes.setVersion(bizContractParam.getVersion());
        chainContractByteCodes.setRuntimeType(bizContractParam.getRuntimeType().getNumber());
        chainContractByteCodes.setIsDel(false);
        chainContractByteCodes.setId(null);
        chainContractByteCodesImpl.save(chainContractByteCodes);
        return chainContractByteCodes.getId();
    }

    /**
     * 查询所有的合约名单，包括系统合约和用户合约
     * @return
     */
    @GetMapping(value = "contract/list")
    public RespResult contractList(@RequestParam(value = "chainId",defaultValue = "chain1") String chainId){
        try {
            List<ContractOuterClass.Contract> contrantlist = chainTransactionService.contrantlist("0001001", chainId);
            return handleResult(contrantlist.stream().map(ContractOuterClass.Contract::getName).collect(Collectors.toList()));
        } catch (Exception e) {
            return handleFail(e);
        }

    }
    /**
     * 判断同一个key是否到达了最高值
     * @param key   键
     * @param voteTall  投票总数
     * @return
     */
    public Boolean invokeTimes(String key, int voteTall) {

        // 判断在redis中是否有key值
        Boolean redisKey = stringRedisTemplate.hasKey(key);
        if (redisKey) {
            // 获取key所对应的value
            Integer hasKey =Integer.parseInt(stringRedisTemplate.opsForValue().get(key));
            if (hasKey == voteTall) {
                stringRedisTemplate.expire(key,30, TimeUnit.DAYS);
                return true;
            }
            // 对value进行加1操作
            stringRedisTemplate.opsForValue().increment(key,1);
            return false;
        }else {
            // 如果没有key值，对他进行添加到redis中
            stringRedisTemplate.opsForValue().set(key,String.valueOf(voteTall));
            if (voteTall==1){
                return true;
            }
        }
        return false;
    }
}
