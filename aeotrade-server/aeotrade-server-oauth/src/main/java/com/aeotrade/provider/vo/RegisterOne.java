package com.aeotrade.provider.vo;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * @Auther: 吴浩
 * @Date: 2021/3/5 15:17
 */
@Data
public class RegisterOne implements Serializable {
    @NotEmpty(message = "统一社会信用代码不能为空")
    private String uscCode;


    @NotEmpty(message = "企业名称不能为空")
    private String memberName;


    @NotEmpty(message = "用户姓名不能为空")
    private String staffName;

    @NotEmpty(message = "用户名不能为空")
    private String username;

    @NotEmpty(message = "密码不能为空")
    private String password;



    @NotEmpty(message = "用户手机号不能为空")
    @Pattern(regexp = "^\\d{11}$", message = "手机号码格式错误")
    private String phone;

    //来源渠道应用
    private String channelMark;

    //来源渠道网站
    private String sourceMark;

    //会员类型Id
    private Long vipTypeId;

    //会员类型标识
    private String workMark;

    //短信验证码
    private String code;

    @Email(message = "邮箱格式错误")
    private String email;

    private String haiguanNum;


    @NotEmpty(message = "用户唯一标识不能为空")
    private String swuid;

}
