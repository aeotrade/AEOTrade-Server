package com.aeotrade.server.chain.controller;

import com.aeotrade.server.chain.ChainTransactionService;
import com.aeotrade.server.chain.constant.ContractConstant;
import com.aeotrade.suppot.BaseController;
import com.aeotrade.suppot.RespResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ChainDataController extends BaseController {

    @Autowired
    private ChainTransactionService chainTransactionService;

    /**
     * 文件智能合约,数据上链
     * @return
     */
    public RespResult fileContractData(String userId, Map<String, byte[]> params){
        chainTransactionService.addChainData(
                userId, ContractConstant.FILE_CONTRACT,ContractConstant.FILE_CONTRACT_METHOD,params,"aeotradechain");
        return handleOK();
    }

    /**
     * 事件智能合约,数据上链
     * @return
     */
    public RespResult eventContractData(String userId, Map<String, byte[]> params){
        chainTransactionService.addChainData(
                userId, ContractConstant.EVENT_CONTRACT,ContractConstant.EVENT_CONTRACT_METHOD,params,"aeotradechain");
        return handleOK();
    }

    /**
     * 通关协同智能合约,数据上链
     * @return
     */
    public RespResult cusExchangeContract(String userId, Map<String, byte[]> params){
        chainTransactionService.addChainData(
                userId, ContractConstant.CUS_EXCHANGE_CONTRACT,ContractConstant.CUS_EXCHANGE_CONTRACT_METHOD,params,"aeotradechain");
        return handleOK();
    }

    public RespResult registerChainCA(String tenantId,String name){
        //chainTransactionService.addCa(null,tenantId,"aeotradechain");
        return handleOK();
    }
}
