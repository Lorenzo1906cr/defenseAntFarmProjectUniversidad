package com.antfarmprojectcalidad.defense.config;

import com.antfarmprojectcalidad.defense.service.ExternalService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExternalServiceConfig {
    @Bean
    public ExternalService externalService(
            @Value("${url.entorno.threats}") String entornoUrl
    ) {
        return new ExternalService(entornoUrl);
    }
}
