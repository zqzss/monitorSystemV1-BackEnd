package com.seewin.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.Charset;
import java.util.List;


@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Autowired
    TokenInterceptor tokenInterceptor;

    @Bean
    public HttpMessageConverter responseBodyConverter() {
        //解决返回值中文乱码
        StringHttpMessageConverter converter = new StringHttpMessageConverter(Charset.forName("UTF-8"));
        return converter;
    }

    //解决返回值中文乱码
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(responseBodyConverter());
    }

    //    跨域配置
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 匹配所有路径
//                .allowedOrigins("*") // 允许来自所有域的请求
                .allowedMethods("*") // 允许所有 HTTP 方法
                .allowedHeaders("*") // 允许所有请求头

                .allowedOriginPatterns("*") // 支持域

                .allowedHeaders("*")
                .exposedHeaders("*");


    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //配置拦截器
        registry.addInterceptor(tokenInterceptor).addPathPatterns("/**").excludePathPatterns("/login");
    }
}
