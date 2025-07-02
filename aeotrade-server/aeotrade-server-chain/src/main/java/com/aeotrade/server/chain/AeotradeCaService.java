package com.aeotrade.server.chain;

import com.aeotrade.chainmaker.constant.CertUsageEnum;
import com.aeotrade.chainmaker.constant.UserTypeEnum;
import com.aeotrade.chainmaker.event.CertSignSuccessEvent;
import com.aeotrade.chainmaker.exception.AeoChainException;
import com.aeotrade.chainmaker.model.ChainCertCaTenant;
import com.aeotrade.chainmaker.model.ChainCertUserMember;
import com.aeotrade.chainmaker.model.ChainSdkCert;
import com.aeotrade.chainmaker.repository.ChainCertCaTenantMapper;
import com.aeotrade.chainmaker.repository.ChainCertUserMemberMapper;
import com.aeotrade.chainmaker.repository.ChainSdkCertMapper;
import com.aeotrade.server.chain.constant.RoleCodeRulesEnum;
import com.aeotrade.server.chain.utils.AeotradeChainUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.chainmaker.sdk.ChainClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Slf4j
@Component
public class AeotradeCaService {
    @Value("${hmtx.ca.server-url}")
    private String CA_SERVER_URL;
    @Value("${hmtx.browser.server-url}")
    private String HMTX_BROWSER_SERVER;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private AeotradeChainUtil aeotradeChainUtil;
    @Autowired
    private ChainCertCaTenantMapper chainCertCaTenantService;
    @Autowired
    private ChainSdkCertMapper chainSdkCertService;
    @Autowired
    private ChainCertUserMemberMapper chainCertUserMemberService;
    @Autowired
    private ChainTransactionService chainTransactionService;
    private final ApplicationEventPublisher eventPublisher;

    public AeotradeCaService(RestTemplate restTemplate, ApplicationEventPublisher eventPublisher) {
        this.restTemplate = restTemplate;
        this.eventPublisher = eventPublisher;
    }

    /**
     * 生成证书
     *
     * 请求方式：POST
     * 请求参数：
     *
     * 字段	类型	含义	备注
     * orgId	string	组织ID	必填
     * userId	string	用户ID	*选填
     * userType	string	用户类型	必填
     * certUsage	string	证书用途	必填
     * privateKeyPwd	string	密钥密码	选填
     * country	string	证书字段（国家）	必填
     * locality	string	证书字段（城市）	必填
     * province	string	证书字段（省份）	必填
     * token	string	token	选填
     * userType: 1.root , 2.ca , 3.admin , 4.client , 5.consensus , 6.common
     * certUsage: 1.sign , 2.tls , 3.tls-sign , 4.tls-enc
     * *注：
     *
     * userId 只有在申请的用户类型是ca的类型时，可以填写为空。在申请节点证书时，需要保证链上节点ID唯一。
     * 返回数据：
     *
     * {
     *     "code": 200,
     *     "msg": "The request service returned successfully",
     *     "data": {
     *         "cert": "-----BEGIN CERTIFICATE-----\nMIICRTCCAeygAwIBAgIIHRMopTJcqQYwCgYIKoZIzj0EAwIwXzELMAkGA1UEBhMC\nQ04xEDAOBgNVBAgTB0JlaWppbmcxEDAOBgNVBAcTB0JlaWppbmcxDTALBgNVBAoT\nBG9yZzExCzAJBgNVBAsTAmNhMRAwDgYDVQQDEwdjYS5vcmc4MB4XDTIxMDYxMTA5\nMTgwNloXDTIzMDYxMTA5MTgwNlowYzEOMAwGA1UEBhMFY2hpbmExEDAOBgNVBAgT\nB2JlaWppbmcxEDAOBgNVBAcTB2hhaWRpYW4xDTALBgNVBAoTBG9yZzExDjAMBgNV\nBAsTBWFkbWluMQ4wDAYDVQQDEwV1c2VyMjBZMBMGByqGSM49AgEGCCqGSM49AwEH\nA0IABAKZjYa/bYc9Vp6eNJHRp5AwYTZxj2e5HLDAuuJkW4c53V8D/Tl/VWMKi70E\nOpUZMXoLhMBFhQrQn8Ydcl8kyECjgY0wgYowDgYDVR0PAQH/BAQDAgP4MBMGA1Ud\nJQQMMAoGCCsGAQUFBwMCMCkGA1UdDgQiBCAKiUV2poJIpNQT5Xpusdm+boynJ3kS\nzOtCTrIOD0Ox6TArBgNVHSMEJDAigCDJC+s7sFB6/d9DFghQiuhBwXsduZYUz8Ds\nqjYS272EIzALBgNVHREEBDACggAwCgYIKoZIzj0EAwIDRwAwRAIgCQmmzIuIKMH+\nc6BpZWBNtZqSWCuQpkwBtgJR09M/Z8cCICdZCyKrKuEAcHaVC9CeJVK4yXE/44Vt\nKrfKbpmDyMBp\n-----END CERTIFICATE-----\n",
     *         "privateKey": "-----BEGIN EC PRIVATE KEY-----\nMHcCAQEEILHD/QaWovCdBtUnxgMGJrN63A6ZsLen/dCRYLhrvJ+/oAoGCCqGSM49\nAwEHoUQDQgAEApmNhr9thz1Wnp40kdGnkDBhNnGPZ7kcsMC64mRbhzndXwP9OX9V\nYwqLvQQ6lRkxeguEwEWFCtCfxh1yXyTIQA==\n-----END EC PRIVATE KEY-----\n"
     *     }
     * }
     * 字段	类型	含义
     * cert	string	证书内容
     * privateKey	string	密钥内容
     * @param map
     * @return
     */
    public JSONObject gencert(Map<String,Object> map){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        //用HttpEntity封装整个请求报文
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(map, headers);
        return restTemplate.postForObject(CA_SERVER_URL.concat("/api/ca/gencert"), request,JSONObject.class);

    }
    public JSONObject getcert(Map<String,Object> map){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        //用HttpEntity封装整个请求报文
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(map, headers);
        return restTemplate.postForObject(CA_SERVER_URL.concat("/api/ca/querycerts"), request,JSONObject.class);

    }

    /**
     * 多条件查询证书
     *
     *  请求方式：POST
     *
     * 请求参数：
     *
     * 字段	类型	含义	备注
     * orgId	string	组织ID	选填
     * userId	string	用户ID	选填
     * userType	string	用户类型	选填
     * certUsage	string	证书用途	选填
     * token	string	token	选填
     * userType: 1.root , 2.ca , 3.admin , 4.client , 5.consensus , 6.common
     * certUsage: 1.sign , 2.tls , 3.tls-sign , 4.tls-enc
     * 返回数据：
     *
     * {
     * 	"code": 200,
     *     "msg": "The request service returned successfully",
     *     "data": [
     *         {
     *             "userId": "user1",
     *             "orgId": "org1",
     *             "userType": "admin",
     *             "certUsage": "tls-enc",
     *             "certSn": 2723312771322077578,
     *             "issuerSn":3450584804990292327,
     *             "certContent": "-----BEGIN CERTIFICATE-----\nMIICSDCCAe+gAwIBAgIINZ4eqVdx6XMwCgYIKoZIzj0EAwIwXzELMAkGA1UEBhMC\nQ04xEDAOBgNVBAgTB0JlaWppbmcxEDAOBgNVBAcTB0JlaWppbmcxDTALBgNVBAoT\nBG9yZzExCzAJBgNVBAsTAmNhMRAwDgYDVQQDEwdjYS5vcmc4MB4XDTIxMDYxMTA5\nMzE1MFoXDTI1MDYxMDA5MzE1MFowZjEOMAwGA1UEBhMFY2hpbmExEDAOBgNVBAgT\nB2JlaWppbmcxEDAOBgNVBAcTB2hhaWRpYW4xDTALBgNVBAoTBG9yZzExDzANBgNV\nBAsTBmNsaWVudDEQMA4GA1UEAxMHdXNlcjExMTBZMBMGByqGSM49AgEGCCqGSM49\nAwEHA0IABPISUU2pW5fqKWHpFoqFPWjnqivLZfHXQrSHEwRL94ay91m1m91/TOfe\n9lVpcrCvoCwBP4wukI57ih8bd+p9QPijgY0wgYowDgYDVR0PAQH/BAQDAgP4MBMG\nA1UdJQQMMAoGCCsGAQUFBwMCMCkGA1UdDgQiBCB73+mPJoYO3KbqHKZnccbQhgIO\nlrqWf6ZtHh+lGX5yVjArBgNVHSMEJDAigCDJC+s7sFB6/d9DFghQiuhBwXsduZYU\nz8DsqjYS272EIzALBgNVHREEBDACggAwCgYIKoZIzj0EAwIDRwAwRAIgLND6yzE/\nHVs9DYLMLcSq4STiri4k/KhMwneErZDd4PACIGPQEgKf0DtSJvZ4bMMUrCuenjjD\nSKoDGSKdDIxWNaw1\n-----END CERTIFICATE-----\n",
     *             "expirationDate": 1685695242
     *         },
     *         {
     *             "userId": "user1",
     *             "orgId": "org1",
     *             "userType": "admin",
     *             "certUsage": "tls-enc",
     *             "certSn": 3450584804990292327,
     *             "issuerSn":3450584804990292327,
     *             "certContent": "-----BEGIN CERTIFICATE-----\nMIICSDCCAe+gAwIBAgIINZ4eqVdx6XMwCgYIKoZIzj0EAwIwXzELMAkGA1UEBhMC\nQ04xEDAOBgNVBAgTB0JlaWppbmcxEDAOBgNVBAcTB0JlaWppbmcxDTALBgNVBAoT\nBG9yZzExCzAJBgNVBAsTAmNhMRAwDgYDVQQDEwdjYS5vcmc4MB4XDTIxMDYxMTA5\nMzE1MFoXDTI1MDYxMDA5MzE1MFowZjEOMAwGA1UEBhMFY2hpbmExEDAOBgNVBAgT\nB2JlaWppbmcxEDAOBgNVBAcTB2hhaWRpYW4xDTALBgNVBAoTBG9yZzExDzANBgNV\nBAsTBmNsaWVudDEQMA4GA1UEAxMHdXNlcjExMTBZMBMGByqGSM49AgEGCCqGSM49\nAwEHA0IABPISUU2pW5fqKWHpFoqFPWjnqivLZfHXQrSHEwRL94ay91m1m91/TOfe\n9lVpcrCvoCwBP4wukI57ih8bd+p9QPijgY0wgYowDgYDVR0PAQH/BAQDAgP4MBMG\nA1UdJQQMMAoGCCsGAQUFBwMCMCkGA1UdDgQiBCB73+mPJoYO3KbqHKZnccbQhgIO\nlrqWf6ZtHh+lGX5yVjArBgNVHSMEJDAigCDJC+s7sFB6/d9DFghQiuhBwXsduZYU\nz8DsqjYS272EIzALBgNVHREEBDACggAwCgYIKoZIzj0EAwIDRwAwRAIgLND6yzE/\nHVs9DYLMLcSq4STiri4k/KhMwneErZDd4PACIGPQEgKf0DtSJvZ4bMMUrCuenjjD\nSKoDGSKdDIxWNaw1\n-----END CERTIFICATE-----\n",
     *             "expirationDate": 1685695408
     *         }
     *     ]
     * }
     * 字段	类型	含义	备注
     * certSn	int64	证书序列号
     * issuerSn	int64	签发者证书序列号
     * certContent	string	证书内容
     * userId	string	用户ID
     * orgId	string	组织ID
     * userType	string	用户类型
     * certUsage	string	证书用途
     * expirationDate	int64	到期时间	unix时间戳
     *
     * @param map
     * @return
     */
    public JSONObject querycerts(Map<String,String> map){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        //用HttpEntity封装整个请求报文
        HttpEntity<Map<String, String>> request = new HttpEntity<>(map, headers);
        return restTemplate.postForObject(CA_SERVER_URL.concat("/api/ca/querycerts"),request, JSONObject.class);
    }

    /**
     * 调用浏览器服务进行重新订阅
     * @param map
     *    {
     *     "ChainId":"aeotradechain",
     *     "Addr":"47.93.25.253:9004",
     *     "OrgId":"portassociation",
     *     "Tls":true,
     *     "OrgCA":"-----BEGIN CERTIFICATE-----\nMIICjTCCAjKgAwIBAgIDC+02MAoGCCqGSM49BAMCMHwxCzAJBgNVBAYTAmNuMRAw\nDgYDVQQIEwdiZWlqaW5nMRAwDgYDVQQHEwdiZWlqaW5nMRgwFgYDVQQKEw9wb3J0\nYXNzb2NpYXRpb24xEjAQBgNVBAsTCXJvb3QtY2VydDEbMBkGA1UEAxMSY2EucG9y\ndGFzc29jaWF0aW9uMB4XDTIyMDIyODAxMDU1OFoXDTMxMDIyNjAxMDU1OFowfDEL\nMAkGA1UEBhMCY24xEDAOBgNVBAgTB2JlaWppbmcxEDAOBgNVBAcTB2JlaWppbmcx\nGDAWBgNVBAoTD3BvcnRhc3NvY2lhdGlvbjESMBAGA1UECxMJcm9vdC1jZXJ0MRsw\nGQYDVQQDExJjYS5wb3J0YXNzb2NpYXRpb24wWTATBgcqhkjOPQIBBggqhkjOPQMB\nBwNCAAQuOYPJrmcCE5PgE8DIWHtPvsfVHnc3bWjkunKk9U74F5usNCIRMpEPGfFY\nBMmH4/0UCDBFZ/zt6RSQe9DSqOJVo4GiMIGfMA4GA1UdDwEB/wQEAwIBpjAPBgNV\nHSUECDAGBgRVHSUAMA8GA1UdEwEB/wQFMAMBAf8wKQYDVR0OBCIEICSyB+8oZN2O\ns5WjbMsJVTflu2PbJlvn8VFmuJQvgXXGMEAGA1UdEQQ5MDeCCWxvY2FsaG9zdIIM\nYWVvdHJhZGUuY29thwR/AAABhwQnZ5VjhwQnY4zfhwQvXRn9hwQnZ5KZMAoGCCqG\nSM49BAMCA0kAMEYCIQDkNC3FynxeeKN8H9H3Tj3TMiVgyV461GqVM5kPUuSrdwIh\nAJg3qy006kFy0m/N9vyrdTBIfIf3A9yDhHQHkx/NVzan\n-----END CERTIFICATE-----\n",
     *     "UserCert":"-----BEGIN CERTIFICATE-----\nMIICujCCAmGgAwIBAgIDB3/jMAoGCCqGSM49BAMCMHwxCzAJBgNVBAYTAmNuMRAw\nDgYDVQQIEwdiZWlqaW5nMRAwDgYDVQQHEwdiZWlqaW5nMRgwFgYDVQQKEw9wb3J0\nYXNzb2NpYXRpb24xEjAQBgNVBAsTCXJvb3QtY2VydDEbMBkGA1UEAxMSY2EucG9y\ndGFzc29jaWF0aW9uMB4XDTIyMDIyODAxMDgxNloXDTMxMDIyNjAxMDgxNlowgY4x\nCzAJBgNVBAYTAmNuMRAwDgYDVQQIEwdiZWlqaW5nMRAwDgYDVQQHEwdiZWlqaW5n\nMRgwFgYDVQQKEw9wb3J0YXNzb2NpYXRpb24xDjAMBgNVBAsTBWFkbWluMTEwLwYD\nVQQDEyhwb3J0YXNzb2NpYXRpb251c2VyLnNpZ24ucG9ydGFzc29jaWF0aW9uMFkw\nEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEp18VrquhYQjsQ1PtCBcD5RO+tKU5s49/\ne5qjMtLX9nm9qWqXsllp9S9Bw+5WczAsLafFP0W104VWO+6txu5W06OBvjCBuzAO\nBgNVHQ8BAf8EBAMCAaYwDwYDVR0lBAgwBgYEVR0lADApBgNVHQ4EIgQg+OWU6lyJ\nvTN7xvKE/6OMGHq61pCLhfsT6f2t9JrN6dswKwYDVR0jBCQwIoAgJLIH7yhk3Y6z\nlaNsywlVN+W7Y9smW+fxUWa4lC+BdcYwQAYDVR0RBDkwN4IJbG9jYWxob3N0ggxh\nZW90cmFkZS5jb22HBH8AAAGHBCdnlWOHBCdjjN+HBC9dGf2HBCdnkpkwCgYIKoZI\nzj0EAwIDRwAwRAIgXtybRDtVGsn/VJLk0F4NPDGMP1QtBUB/9i9gQurTqSUCIEg+\nUv+PQ0QEiISXjXPkt0YWGajjQpVLCYkdLLPioJve\n-----END CERTIFICATE-----\n",
     *     "UserKey":"-----BEGIN EC PRIVATE KEY-----\nMHcCAQEEIDO5QlrRQpbLqSMraGPfE32tA2dmpy8vli+uPqPDzkN2oAoGCCqGSM49\nAwEHoUQDQgAEp18VrquhYQjsQ1PtCBcD5RO+tKU5s49/e5qjMtLX9nm9qWqXsllp\n9S9Bw+5WczAsLafFP0W104VWO+6txu5W0w==\n-----END EC PRIVATE KEY-----\n",
     *     "TLSHostName":"aeotrade.com"
     *    }
     * @return
     */
    public JSONObject modifySubscribe(Map<String,String> map){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        //用HttpEntity封装整个请求报文
        HttpEntity<Map<String, String>> request = new HttpEntity<>(map, headers);
        return restTemplate.postForObject(HMTX_BROWSER_SERVER.concat("?cmb=ModifySubscribe"),request, JSONObject.class);
    }

    /**
     * 调用浏览器服务进行取消订阅
     * @param map
     *      {
     *       "ChainId":"aeotradechain"
     *      }
     * @return
     */
    public JSONObject cancelSubscribe(Map<String,String> map){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        //用HttpEntity封装整个请求报文
        HttpEntity<Map<String, String>> request = new HttpEntity<>(map, headers);
        return restTemplate.postForObject(HMTX_BROWSER_SERVER.concat("?cmb=CancelSubscribe"),request, JSONObject.class);
    }

    /**
     * 生成组织证书
     * @param tenantId 企业Id
     * @param tenantName  企业名称
     * @param userType 对应的业务类型，员工、机器人、应用
     * @param userId 企业员工标识，或机器、应用数据的ID标识
     * @param roleCodeRulesEnum 企业类型，政府、物流等
     * @param chainId 链ID
     * @param userTypeEnum 证书角色，admin 管理员， client 员工
     */
    @Transactional(rollbackFor={RuntimeException.class, Exception.class, AeoChainException.class})
    public void genCaCert(String tenantId,String tenantName,String uscc,String creatTime,String userType/*员工、机器人、应用*/,String userId, String roleCodeRulesEnum,String chainId,UserTypeEnum userTypeEnum) throws Exception {
        //添加企业标识的判断Long.valueOf(tenantId)
        ChainCertUserMember userIdAndIsDel = chainCertUserMemberService.findTop1ByTenantIdAndUserIdAndIsDel(tenantId, userId, false);
        if (userIdAndIsDel != null) {
            return;
        }
        ChainCertCaTenant chainCertCaTenant=chainCertCaTenantService.findTop1ByTenantIdAndIsDel(tenantId,false);
        if (chainCertCaTenant==null) {
            String seq = aeotradeChainUtil.seqCaId(tenantName, roleCodeRulesEnum, creatTime);
            JSONObject signJsonObject = findUserCert(seq, "", UserTypeEnum.CA.getName(), CertUsageEnum.SIGN.getName());
            if (signJsonObject.getObject("data", List.class) != null) {
                log.warn("ca cert orgId {} is Already",seq);
                return;
            }
            //发送生成证书请求
            Map<String, Object> map = new HashMap<>(1);
            map.put("orgId", seq);
            map.put("userId", "");
            map.put("userType", "ca");
            map.put("certUsage", "sign");
            map.put("privateKeyPwd", "");
            map.put("country", "CN");
            map.put("locality", "Beijing");
            map.put("province", "Beijing");
            JSONObject jsonObject = this.gencert(map);

            if (jsonObject.getObject("code", Integer.class) == 500) {
                throw new AeoChainException("ca service request error : " + jsonObject.getString("data"));
            }

            //添加证书数据
            ChainSdkCert cert = saveCert(seq, seq, jsonObject, UserTypeEnum.CA, CertUsageEnum.SIGN);

            //添加组织证书管理表数据
            chainCertCaTenant = new ChainCertCaTenant();
            chainCertCaTenant.setChainId(chainId);
            chainCertCaTenant.setCertId(cert.getId());
            chainCertCaTenant.setTenantId(tenantId);
            chainCertCaTenant.setUscc(uscc);
            chainCertCaTenant.setTenantType(roleCodeRulesEnum);
            chainCertCaTenant.setCaOrgId(seq);
            chainCertCaTenant.setCreateAt(new Date());
            chainCertCaTenant.setIsChainAuth(false);
            chainCertCaTenant.setIsVote(false);
            chainCertCaTenant.setIsConsensus(false);
            chainCertCaTenant.setIsWhether(true);
            chainCertCaTenant.setTenantName(tenantName);
            chainCertCaTenant.setUpdateAt(new Date());
            chainCertCaTenantService.save(chainCertCaTenant);

            try {
                //链上身份鉴权
                ChainClient chainClient = chainTransactionService.addCa("0001001", chainCertCaTenant, chainId);
                //记录数据库
                chainCertCaTenant.setIsChainAuth(true);
                chainCertCaTenant.setIsVote(true);
                chainCertCaTenant.setUpdateAt(new Date());
                chainCertCaTenant.setIsWhether(false);
                log.info("++++++++++++++++++++++++++++++++++++++"+chainCertCaTenant.getIsChainAuth()+"++++++++++++++++++++++++++++++++++++++++++++++++");
                chainCertCaTenantService.save(chainCertCaTenant);

                //发布证书签名成功事件
                CertSignSuccessEvent certSignSuccessEvent = new CertSignSuccessEvent(chainCertCaTenant, tenantId,null,"_id",chainCertCaTenant.getId(), cert.getCert(), cert.getPrivateKey(),chainClient);
                eventPublisher.publishEvent(certSignSuccessEvent);
            } catch (Exception e) {
                log.error(e.getMessage());
                throw new AeoChainException(e.getMessage());
            }
        }
        ChainClient chainClient = chainTransactionService.addCa("0001001", chainCertCaTenant, chainId);
        //添加企业管理员证书
        ChainCertUserMember chainCertUserMember = genUserCertByOrgId(chainClient,chainCertCaTenant.getCaOrgId(),tenantId,userId,userType,userTypeEnum,tenantName,uscc,null);

    }
    @Transactional(rollbackFor={RuntimeException.class, Exception.class, AeoChainException.class})
    public void delCaCert(String tenantId,String tenantName,String userType/*员工、机器人、应用*/,String userId, RoleCodeRulesEnum roleCodeRulesEnum,String chainId,UserTypeEnum userTypeEnum){
        //添加企业标识的判断
        ChainCertCaTenant chainCertCaTenant=chainCertCaTenantService.findTop1ByTenantIdAndIsDel(tenantId,false);

        if (chainCertCaTenant==null){
            throw new AeoChainException("the tenantId is not found");
        }

        chainCertCaTenant.setIsVote(false);
        chainCertCaTenant.setIsDel(true);
        chainCertCaTenantService.save(chainCertCaTenant);

        //删除用户
        List<ChainCertUserMember> lists = chainCertUserMemberService.findByTenantId(tenantId);
        if(lists.size()>0){
            lists.forEach(i->{
                i.setIsDel(true);
                chainCertUserMemberService.save(i);
            });
        }

        try {
            //链上身份鉴权
            chainTransactionService.delCa(userId,chainCertCaTenant,chainId);
        }catch (Exception e){
            throw new AeoChainException(e.getMessage());
        }

    }
    @Synchronized
    private ChainSdkCert saveCert(String orgId,String certId,JSONObject jsonObject,UserTypeEnum userTypeEnum,CertUsageEnum certUsageEnum){
        ChainSdkCert chainSdkCert=new ChainSdkCert();
        chainSdkCert.setCert(jsonObject.getJSONObject("data").getString("cert"));
        chainSdkCert.setPrivateKey(jsonObject.getJSONObject("data").getString("privateKey"));
        chainSdkCert.setCertName(certId+(certUsageEnum==CertUsageEnum.TLS?".tls":""));
        chainSdkCert.setCertType(userTypeEnum.getIndex());
        chainSdkCert.setCertUse(certUsageEnum.getIndex());
        chainSdkCert.setOrgId(orgId);
        chainSdkCert.setCreateAt(new Date());
        chainSdkCert.setIsDel(false);
        chainSdkCert.setUpdateAt(new Date());
        Map<String, String> parsersCert = parserCert(chainSdkCert.getCert());
        if (parsersCert != null) {
            chainSdkCert.setOrgId(parsersCert.get("O"));
            chainSdkCert.setOrgName(parsersCert.get("CN"));
            chainSdkCert.setExpirationDate(Long.valueOf(parsersCert.get("exp")));
        }

        chainSdkCertService.save(chainSdkCert);
        return chainSdkCert;
    }

    private JSONObject postForUserCert(String orgId,String userId,String userType,String certUsage){
        Map<String,Object> map=new HashMap<>(1);
        map.put("orgId",orgId);
        map.put("userId",userId);
        map.put("userType",userType);
        map.put("certUsage",certUsage);
        map.put("privateKeyPwd","");
        map.put("country","CN");
        map.put("locality","Beijing");
        map.put("province","Beijing");
        return this.gencert(map);
    }
    private JSONObject findUserCert(String orgId,String userId,String userType,String certUsage){
        Map<String,Object> map=new HashMap<>(1);
        map.put("orgId",orgId);
        map.put("userId",userId);
        map.put("userType",userType);
        map.put("certUsage",certUsage);
        return this.getcert(map);
    }

    /**
     * 生成企业员工证书
     * @param tenantId 企业标识
     * @param userId  员工标识、或机器人标识、或应用标识
     * @param userType 类型标识，员工、机器人、应用等
     * @param userTypeEnum 链上身份角色
     */
    public ChainCertUserMember genUserCert(String tenantId,String userId,String userType,UserTypeEnum userTypeEnum){
        ChainCertUserMember chainCertUserMember=this.haveUserCert(tenantId,userId);
        if (chainCertUserMember!=null){
            return chainCertUserMember;
        }
        ChainCertCaTenant chainCertCaTenant = chainCertCaTenantService.findTop1ByTenantIdAndIsDel(tenantId,false);
        String seq=aeotradeChainUtil.seqUserId(chainCertCaTenant.getCaOrgId());

        JSONObject signJsonObject = postForUserCert(chainCertCaTenant.getCaOrgId(),seq,userTypeEnum.getName(),CertUsageEnum.SIGN.getName());
        JSONObject tlsJsonObject = postForUserCert(chainCertCaTenant.getCaOrgId(),seq,userTypeEnum.getName(),CertUsageEnum.TLS.getName());

        ChainSdkCert userSign=saveCert(chainCertCaTenant.getCaOrgId(),seq,signJsonObject,userTypeEnum,CertUsageEnum.SIGN);
        ChainSdkCert userTls=saveCert(chainCertCaTenant.getCaOrgId(),seq,tlsJsonObject,userTypeEnum,CertUsageEnum.TLS);

        chainCertUserMember=new ChainCertUserMember();
        chainCertUserMember.setSignCertId(userSign.getId());
        chainCertUserMember.setTlsCertId(userTls.getId());
        chainCertUserMember.setTenantId(tenantId);
        chainCertUserMember.setCertType(userTypeEnum.getIndex());
        chainCertUserMember.setUserId(userId);
        chainCertUserMember.setUserType(userType);
        chainCertUserMemberService.save(chainCertUserMember);
        chainCertUserMember.getId();
        return chainCertUserMember;
    }
    public String getUserSeq(String caOrgId){
        String code="";
        while (true){
            String seq = aeotradeChainUtil.seqUserId(caOrgId);
            ChainSdkCert cert=chainSdkCertService.findTop1ByCertNameAndIsDel(seq,false);
            if(cert==null){
                code=code+seq;
                break;
            }
        }
        return code;
    }
    public ChainCertUserMember genUserCertByOrgId(ChainClient chainClient,String caOrgId,String tenantId,String userId,String userType,UserTypeEnum userTypeEnum,String name,String uscc,String seq){
        ChainCertUserMember chainCertUserMember=this.haveUserCert(tenantId,userId);
        if (chainCertUserMember!=null){
            return chainCertUserMember;
        }
        if (seq==null) {
            seq=getUserSeq(caOrgId);
        }

        JSONObject signJsonObject = postForUserCert(caOrgId,seq,userTypeEnum.getName(),CertUsageEnum.SIGN.getName());
        JSONObject tlsJsonObject = postForUserCert(caOrgId,seq,userTypeEnum.getName(),CertUsageEnum.TLS.getName());

        ChainSdkCert userSign=saveCert(caOrgId,seq,signJsonObject,userTypeEnum,CertUsageEnum.SIGN);
        ChainSdkCert userTls=saveCert(caOrgId,seq,tlsJsonObject,userTypeEnum,CertUsageEnum.TLS);

        chainCertUserMember=new ChainCertUserMember();
        chainCertUserMember.setSignCertId(userSign.getId());
        chainCertUserMember.setTlsCertId(userTls.getId());
        chainCertUserMember.setTenantId(tenantId);
        chainCertUserMember.setCertType(userTypeEnum.getIndex());
        chainCertUserMember.setUserId(userId);
        chainCertUserMember.setUserType(userType);
        chainCertUserMember.setTenantName(name);
        chainCertUserMember.setCreateAt(new Date());
        chainCertUserMember.setUscc(uscc);
        chainCertUserMember.setUpdateAt(new Date());
        chainCertUserMemberService.save(chainCertUserMember);
        chainCertUserMember.getId();

        //发布证书签名成功事件
        CertSignSuccessEvent certSignSuccessEvent = new CertSignSuccessEvent(chainCertUserMember, tenantId,userId,"_id",chainCertUserMember.getId(), userSign.getCert(), userSign.getPrivateKey(),chainClient);
        eventPublisher.publishEvent(certSignSuccessEvent);

        return chainCertUserMember;
    }

    private ChainCertUserMember haveUserCert(String tenantId,String userId){
        ChainCertUserMember isChainCertUserMember = chainCertUserMemberService.findTop1ByTenantIdAndUserIdAndIsDel(tenantId,userId,false);
        if (isChainCertUserMember==null){
            return null;
        }
        return isChainCertUserMember;
    }

    private Map<String, String> parserCert(String crt) {
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            // 从输入流中生成 X509Certificate 对象
            ByteArrayInputStream bais = new ByteArrayInputStream(crt.getBytes(StandardCharsets.UTF_8));
            X509Certificate cert = (X509Certificate) cf.generateCertificate(bais);

            // Subject
            LdapName ldapName = new LdapName(cert.getSubjectX500Principal().getName());
            Map<String, String> subMap = new HashMap<>(6);
            for (Rdn rdn : ldapName.getRdns()) {
                subMap.put(rdn.getType(), rdn.getValue().toString());
            }
            subMap.put("exp", String.valueOf(cert.getNotAfter().getTime()));
            return subMap;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
}