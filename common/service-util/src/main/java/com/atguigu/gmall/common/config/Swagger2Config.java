package com.atguigu.gmall.common.config;

import com.google.common.base.Predicates;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

/**
 * Swagger2配置信息
 */
@Configuration
@EnableSwagger2 //开启Swagger2
public class Swagger2Config {

    /**
     * Docket: 用来构建API文档的配置类
     * groupName: 分组名称
     * apiInfo: API信息
     * select: 选择哪些路径生成文档
     * build: 构建API文档
     * globalOperationParameters: 添加全局参数
     * @return
     */
    @Bean
    public Docket webApiConfig(){

        //添加head参数start
        List<Parameter> pars = new ArrayList<>();
        ParameterBuilder tokenPar = new ParameterBuilder();
        tokenPar.name("userId")
                .description("用户ID")
                .defaultValue("1")
                .modelRef(new ModelRef("string")) // 数据类型
                .parameterType("header") // header类型
                .required(false) // 是否必填
                .build();
        pars.add(tokenPar.build());

        ParameterBuilder tmpPar = new ParameterBuilder();
        tmpPar.name("userTempId")
                .description("临时用户ID")
                .defaultValue("1")
                .modelRef(new ModelRef("string"))
                .parameterType("header")
                .required(false)
                .build();
        pars.add(tmpPar.build());
        //添加head参数end

        return new Docket(DocumentationType.SWAGGER_2) // 指定文档类型为Swagger2
                .groupName("webApi") // 分组名称
                .apiInfo(webApiInfo()) // API信息
                .select() // 选择哪些路径生成文档
                //过滤掉admin路径下的所有页面
                .paths(Predicates.and(PathSelectors.regex("/api/.*"))) // 正则匹配，只显示api开头的路径
                //过滤掉所有error或error.*页面
                //.paths(Predicates.not(PathSelectors.regex("/error.*")))
                .build()
                .globalOperationParameters(pars);

    }

    /**
     * Docket: 用来构建API文档的配置类
     * groupName: 分组名称
     * apiInfo: API信息
     * select: 选择哪些路径生成文档
     * build: 构建API文档
     * @return
     */
    @Bean
    public Docket adminApiConfig(){

        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("adminApi")
                .apiInfo(adminApiInfo())
                .select()
                //只显示admin路径下的页面
                .paths(Predicates.and(PathSelectors.regex("/admin/.*")))
                .build();

    }

    /**
     * API信息
     * title: 标题
     * description: 描述
     * version: 版本
     * contact: 联系人信息
     * @return
     */
    private ApiInfo webApiInfo(){

        return new ApiInfoBuilder()
                .title("网站-API文档")
                .description("本文档描述了网站微服务接口定义")
                .version("1.0")
                .contact(new Contact("Helen", "http://atguigu.com", "55317332@qq.com"))
                .build();
    }

    /**
     * API信息
     * title: 标题
     * description: 描述
     * version: 版本
     * contact: 联系人信息
     * @return
     */
    private ApiInfo adminApiInfo(){

        return new ApiInfoBuilder()
                .title("后台管理系统-API文档")
                .description("本文档描述了后台管理系统微服务接口定义")
                .version("1.0")
                .contact(new Contact("Helen", "http://atguigu.com", "55317332@qq.com"))
                .build();
    }


}
