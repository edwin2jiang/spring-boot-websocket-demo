package com.yyj.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * @author Z09418208_蒋伟伟
 * @Description
 * @create 2021-07-04 21:28
 */

@Configuration
public class WebSocketConfig {

    /**
     * 注入ServerEndpointExporter bean对象，自动注册使用了@ServerEndpoint注解的Bean
     * @return
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter(){
        return new ServerEndpointExporter();
    }
}
