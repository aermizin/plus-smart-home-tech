package ru.yandex.practicum.order.feign.config;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

public class ProductFeignConfig {

    @Bean
    public ErrorDecoder errorDecoder() {
        return new ProductFeignErrorDecoder();
    }
}
