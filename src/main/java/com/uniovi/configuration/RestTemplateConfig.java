package com.uniovi.configuration;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

@Configuration
public class RestTemplateConfig {
    @Bean
    public RestTemplate restTemplate() {
        int timeout = 5000; // Tiempo de espera en milisegundos (5 segundos)

        // Configuración del tiempo de espera
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(timeout, TimeUnit.MILLISECONDS)    // Tiempo para establecer conexión
                .setResponseTimeout(timeout, TimeUnit.MILLISECONDS)   // Tiempo para leer la respuesta
                .build();

        // Crear el HttpClient con la configuración
        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();

        // Crear un RestTemplate con el HttpClient configurado
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        return new RestTemplate(factory);
    }
}
