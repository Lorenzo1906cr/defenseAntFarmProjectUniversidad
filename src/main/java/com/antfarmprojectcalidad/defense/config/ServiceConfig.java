package com.antfarmprojectcalidad.defense.config;

import com.antfarmprojectcalidad.defense.service.CommunicationService;
import com.antfarmprojectcalidad.defense.service.ExternalService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {
    @Bean
    public ExternalService externalService(
            @Value("${url.entorno.threats}") String entornoUrl
    ) {
        return new ExternalService(entornoUrl);
    }

    @Bean
    public CommunicationService communicationService(
            @Value("${url.comm.message}") String commUrl
    ) {
        return new CommunicationService(commUrl);
    }
}
