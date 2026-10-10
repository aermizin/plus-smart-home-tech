package ru.yandex.practicum.order.feign.product;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.order.feign.config.ProductFeignConfig;
import ru.yandex.practicum.order.feign.product.dto.OrderProductDto;

import java.util.List;

@FeignClient(name = "product-service",
        configuration = ProductFeignConfig.class,
        fallbackFactory = ProductClientFallbackFactory.class)
public interface ProductClient {

    @GetMapping("/api/products/{id}")
    OrderProductDto getProductById(@PathVariable("id") Long id);

    @GetMapping(value = "/api/products", params = "id")
    List<OrderProductDto> getProductsByIds(@RequestParam("id") List<Long> ids);
}
