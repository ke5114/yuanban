package org.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class Swagger2Config {

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("org.example.controller"))
                .paths(PathSelectors.any())
                .build()
                .enableUrlTemplating(false) // 禁用 URL 模板，减少解析复杂度
                .ignoredParameterTypes(Object.class); // 忽略复杂类型，只解析显式注解的参数
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("愈安伴系统API文档")
                .version("1.0")
                .contact(new Contact("开发团队", "", ""))
                .build();
    }
}