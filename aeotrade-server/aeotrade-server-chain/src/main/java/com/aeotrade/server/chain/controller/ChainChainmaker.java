package com.aeotrade.server.chain.controller;

import com.aeotrade.chainmaker.constant.CertUsageEnum;
import com.aeotrade.chainmaker.constant.UserTypeEnum;
import com.aeotrade.chainmaker.model.ChainCertCaTenant;
import com.aeotrade.chainmaker.model.ChainCertNodeTenant;
import com.aeotrade.chainmaker.model.ChainCertUserMember;
import com.aeotrade.chainmaker.model.ChainSdkCert;
import com.aeotrade.chainmaker.repository.ChainCertCaTenantMapper;
import com.aeotrade.chainmaker.repository.ChainCertNodeTenantMapper;
import com.aeotrade.chainmaker.repository.ChainCertUserMemberMapper;
import com.aeotrade.chainmaker.repository.ChainSdkCertMapper;
import com.aeotrade.server.chain.constant.RoleCodeRulesEnum;
import com.aeotrade.server.chain.vo.CertConnInitVo;
import com.aeotrade.suppot.BaseController;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.domain.Example;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @Auther: 吴浩
 * @Date: 2022-11-18 9:54
 */
@Slf4j
@RestController
public class ChainChainmaker  extends BaseController {

    private final RedisTemplate redisTemplate;
    private final ChainCertCaTenantMapper chainCertCaTenantMapper;
    private final ChainCertUserMemberMapper chainCertUserMemberMapper;
    private final ChainCertNodeTenantMapper chainCertNodeTenantMapper;
    private final ChainSdkCertMapper chainSdkCertMapper;

    public ChainChainmaker(RedisTemplate redisTemplate, ChainCertCaTenantMapper chainCertCaTenantMapper, ChainCertUserMemberMapper chainCertUserMemberMapper, ChainCertNodeTenantMapper chainCertNodeTenantMapper, ChainSdkCertMapper chainSdkCertMapper) {
        this.redisTemplate = redisTemplate;
        this.chainCertCaTenantMapper = chainCertCaTenantMapper;
        this.chainCertUserMemberMapper = chainCertUserMemberMapper;
        this.chainCertNodeTenantMapper = chainCertNodeTenantMapper;
        this.chainSdkCertMapper = chainSdkCertMapper;
    }

    @GetMapping("/find/chainmaker")
    public Object chainmaker( String cmb){
        Object chain = redisTemplate.opsForValue().get("AEOTRADECHAIN:" + cmb);
        return JSONObject.parseObject(chain.toString(), Document.class);

    }
    @GetMapping("get/data")
    public Object get() {
        Object chain = redisTemplate.opsForValue().get("AEOTRADECHAIN:Decimal");
        JSONObject jsonObject = null;
        if (chain != null) { // 配合解决上链数据表数据被移走的那部分统计
            jsonObject = JSON.parseObject(chain.toString());
            JSONObject object = jsonObject.getJSONObject("Response").getJSONObject("Data");
            long longValue = object.getLongValue("TransactionNum");
            object.put("TransactionNum", 43206416 + longValue);
            jsonObject.put("Data", object);
        }
        return JSONObject.parseObject(jsonObject.toString(), Document.class);

    }

    /**
     * 初始化证书模式链连接配置
     * 使用算法：EC 格式：X.509
     * @param certConnInit 必须的配置信息
     */
    @PostMapping("/cert/conn/init")
    public Object initializeCertModelChainConnectionConfig(CertConnInitVo certConnInit) throws CertificateException {
        if (isAnyCertFileNullOrEmpty(certConnInit)) {
            return handleFail("CERT IS NULL");
        }

        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        // CA证书
        Map<String, String> caMap = parserOrgId(cf, certConnInit.getCaSignCertFile());
        if (caMap == null) return handleFail("Failed to parse CA certificate");
        ChainSdkCert caCert = createAndSaveCert(caMap, certConnInit.getCaSignCertFile(), certConnInit.getCaSignKeyFile(), UserTypeEnum.CA, CertUsageEnum.SIGN);

        // 添加chain_cert_ca_tenant
        ChainCertCaTenant chainCertCaTenant = createAndSaveCaTenant(certConnInit, caCert);

        // Admin角色证书
        Map<String, String> adminSignMap = parserOrgId(cf, certConnInit.getAdminSignCertFile());
        if (adminSignMap == null) return handleFail("Failed to parse Admin sign certificate");
        ChainSdkCert adminSignCert = createAndSaveCert(adminSignMap, certConnInit.getAdminSignCertFile(), certConnInit.getAdminSignKeyFile(), UserTypeEnum.ADMIN, CertUsageEnum.SIGN);

        Map<String, String> adminTlsMap = parserOrgId(cf, certConnInit.getAdminTlsCertFile());
        if (adminTlsMap == null) return handleFail("Failed to parse Admin TLS certificate");
        ChainSdkCert adminTlsCert = createAndSaveCert(adminTlsMap, certConnInit.getAdminTlsCertFile(), certConnInit.getAdminTlsKeyFile(), UserTypeEnum.ADMIN, CertUsageEnum.TLS);

        // 添加chain_cert_user_member
        ChainCertUserMember adminChainCertUserMember = createAndSaveUserMember(chainCertCaTenant, certConnInit, adminSignCert, adminTlsCert);

        // 添加chain_cert_node_tenant
        ChainCertNodeTenant chainCertNodeTenant = createAndSaveNodeTenant(certConnInit, caCert, chainCertCaTenant);

        return handleOK();
    }

    private boolean isAnyCertFileNullOrEmpty(CertConnInitVo certConnInit) {
        return isFileNullOrEmpty(certConnInit.getCaSignCertFile())
                || isFileNullOrEmpty(certConnInit.getCaSignKeyFile())
                || isFileNullOrEmpty(certConnInit.getAdminSignCertFile())
                || isFileNullOrEmpty(certConnInit.getAdminSignKeyFile())
                || isFileNullOrEmpty(certConnInit.getAdminTlsCertFile())
                || isFileNullOrEmpty(certConnInit.getAdminTlsKeyFile());
    }

    private boolean isFileNullOrEmpty(MultipartFile file) {
        return file == null || file.isEmpty();
    }

    private ChainSdkCert createAndSaveCert(Map<String, String> certMap, MultipartFile certFile, MultipartFile keyFile, UserTypeEnum userType, CertUsageEnum certUsage) {
        ChainSdkCert cert = new ChainSdkCert();
        cert.setOrgId(certMap.get("O"));
        cert.setOrgName(certMap.get("CN"));
        Optional<ChainSdkCert> certOptional = chainSdkCertMapper.findOne(Example.of(cert));
        if (certOptional.isPresent()) {
            return certOptional.get();
        }
        cert.setCreateAt(new Date());
        cert.setIsDel(false);
        cert.setUpdateAt(new Date());
        cert.setExpirationDate(certMap.get("exp") != null ? Long.valueOf(certMap.get("exp")) : null);
        cert.setCert(readText(certFile));
        cert.setPrivateKey(readText(keyFile));
        cert.setCertType(userType.getIndex());
        cert.setCertUse(certUsage.getIndex());
        chainSdkCertMapper.save(cert);
        return cert;
    }

    private ChainCertCaTenant createAndSaveCaTenant(CertConnInitVo certConnInit, ChainSdkCert caCert) {
        ChainCertCaTenant chainCertCaTenant = new ChainCertCaTenant();
        chainCertCaTenant.setCaOrgId(caCert.getOrgId());
        chainCertCaTenant.setIsChainAuth(null);
        chainCertCaTenant.setIsVote(null);
        chainCertCaTenant.setIsConsensus(null);
        Optional<ChainCertCaTenant> tenantOptional = chainCertCaTenantMapper.findOne(Example.of(chainCertCaTenant));
        if (tenantOptional.isPresent()) {
            return tenantOptional.get();
        }
        chainCertCaTenant.setChainId(certConnInit.getChainId());
        chainCertCaTenant.setTenantType(RoleCodeRulesEnum.PINGTAI.getCode());
        chainCertCaTenant.setTenantId("0001001"); // Consider passing this as a parameter
        chainCertCaTenant.setCreateAt(new Date());
        chainCertCaTenant.setUpdateAt(new Date());
        chainCertCaTenant.setIsConsensus(true);
        chainCertCaTenant.setIsWhether(false);
        chainCertCaTenant.setIsVote(true);
        chainCertCaTenant.setIsDel(false);
        chainCertCaTenant.setIsChainAuth(true);
        chainCertCaTenant.setCertId(caCert.getId());
        chainCertCaTenant.setTenantName(certConnInit.getTenantName());
        chainCertCaTenant.setUscc(certConnInit.getUscc());
        chainCertCaTenantMapper.save(chainCertCaTenant);
        return chainCertCaTenant;
    }

    private ChainCertUserMember createAndSaveUserMember(ChainCertCaTenant chainCertCaTenant, CertConnInitVo certConnInit, ChainSdkCert adminSignCert, ChainSdkCert adminTlsCert) {
        SecureRandom secureRandom = new SecureRandom();
        ChainCertUserMember adminChainCertUserMember = new ChainCertUserMember();
        adminChainCertUserMember.setSignCertId(adminSignCert.getId());
        adminChainCertUserMember.setTlsCertId(adminTlsCert.getId());
        Optional<ChainCertUserMember> memberOptional = chainCertUserMemberMapper.findOne(Example.of(adminChainCertUserMember));
        if (memberOptional.isPresent()) {
            return memberOptional.get();
        }
        adminChainCertUserMember.setCreateAt(new Date());
        adminChainCertUserMember.setUpdateAt(new Date());
        adminChainCertUserMember.setIsDel(false);
        adminChainCertUserMember.setTenantId(chainCertCaTenant.getTenantId());
        adminChainCertUserMember.setTenantName(certConnInit.getTenantName());
        adminChainCertUserMember.setUscc(certConnInit.getUscc());
        adminChainCertUserMember.setCertType(UserTypeEnum.ADMIN.getIndex());
        adminChainCertUserMember.setUserId(String.valueOf(1000 + secureRandom.nextInt(9000)));
        adminChainCertUserMember.setUserType(UserTypeEnum.ADMIN.getName());
        chainCertUserMemberMapper.save(adminChainCertUserMember);
        return adminChainCertUserMember;
    }

    private ChainCertNodeTenant createAndSaveNodeTenant(CertConnInitVo certConnInit, ChainSdkCert caCert, ChainCertCaTenant chainCertCaTenant) {
        ChainCertNodeTenant chainCertNodeTenant = new ChainCertNodeTenant();
        chainCertNodeTenant.setChainId(certConnInit.getChainId());
        chainCertNodeTenant.setNodeAddr(certConnInit.getNodeAddr());
        chainCertNodeTenant.setConnCnt(null);
        chainCertNodeTenant.setEnableTls(null);
        chainCertNodeTenant.setIsShare(null);
        Optional<ChainCertNodeTenant> nodeOptional = chainCertNodeTenantMapper.findOne(Example.of(chainCertNodeTenant));
        if (nodeOptional.isPresent()) {
            return nodeOptional.get();
        }
        chainCertNodeTenant.setTenantId(chainCertCaTenant.getTenantId());
        chainCertNodeTenant.setCreateAt(new Date());
        chainCertNodeTenant.setUpdateAt(new Date());
        chainCertNodeTenant.setIsDel(false);
        chainCertNodeTenant.setEnableTls(certConnInit.getEnableTls());
        chainCertNodeTenant.setCertId(caCert.getId());
        chainCertNodeTenant.setTlsHostName(certConnInit.getTlsHostName());
        chainCertNodeTenant.setIsShare(true);
        chainCertNodeTenant.setConnCnt(5);
        chainCertNodeTenantMapper.save(chainCertNodeTenant);
        return chainCertNodeTenant;
    }
    //    O = a.genesis.org 组织标识
    //    OU = client  证书角色
    //    CN = client1.sign.a.genesis.org  当前证书标识
    private Map<String, String> parserOrgId(CertificateFactory cf, MultipartFile file) {
        try {
            // 从输入流中生成 X509Certificate 对象
            ByteArrayInputStream bais = new ByteArrayInputStream(file.getBytes());
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
    private String readText(MultipartFile file) {
        StringBuilder content = new StringBuilder();
        // 使用 try-with-resources 确保资源被正确关闭
        try (InputStream is = file.getInputStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return null;
        }
        return content.toString();
    }
}
