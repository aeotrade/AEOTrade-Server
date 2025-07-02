//package com.aeotrade.provider.oauth.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.bind.annotation.RestController;
//import springfox.documentation.builders.ApiInfoBuilder;
//import springfox.documentation.builders.PathSelectors;
//import springfox.documentation.builders.RequestHandlerSelectors;
//import springfox.documentation.service.ApiInfo;
//import springfox.documentation.spi.DocumentationType;
//import springfox.documentation.spring.web.plugins.Docket;
//import springfox.documentation.swagger2.annotations.EnableSwagger2;
//
//@Configuration
//@EnableSwagger2
//public class UacSwaggerConfiguration {
//    @Bean("oauthApis")
//    public Docket dalApis() {
//        return new Docket(DocumentationType.SWAGGER_2).groupName("平台认证鉴权中心").select().apis(
//                RequestHandlerSelectors.withClassAnnotation(RestController.class))
//                .paths(PathSelectors.any()).build().apiInfo(apiInfo()).enable(true);
//    }
//
//    private ApiInfo apiInfo() {
//        return new ApiInfoBuilder().title("平台认证鉴权中心接口文档").description("平台认证鉴权中心").termsOfServiceUrl("").version("1.0")
//                .build();
//    }
//
//}
