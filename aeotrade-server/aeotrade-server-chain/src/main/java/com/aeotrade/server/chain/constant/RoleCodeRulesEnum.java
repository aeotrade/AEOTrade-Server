package com.aeotrade.server.chain.constant;

public enum RoleCodeRulesEnum {
//    角色码规则：
//            1. 01: 贸易企业
//            2. 02: 物流服务商
//            3. 03: 综合服务商
//            4. 04: 专业服务商
//            5. 05: 应用服务商
//            6. 06: 金融机构
//            7. 07: 行业组织
//            8. 08: 政府机构
    PINGTAI("00","平台"),
    /** 贸易企业 */
    MAOYI("01","贸易企业"),
    /** 物流服务商 */
    WULIU("02","物流服务商"),
    /** 综合服务商 */
    ZONGHE("03","综合服务商"),
    /** 专业服务商 */
    ZHANYE("04","专业服务商"),
    /** 应用服务商 */
    YINGYONG("05","应用服务商"),
    /** 金融机构 */
    JINRONG("06","金融机构"),
    /** 行业组织 */
    HANGYE("07","行业组织"),
    /** 政府机构 */
    ZHENGFU("08","政府机构");

    private String code;
    private String name;

    RoleCodeRulesEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return code;
    }
}
