package ru.yandex.practicum.order.feign.product;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.order.exception.BusinessException;
import ru.yandex.practicum.order.exception.ProductServiceUnavailableException;
import ru.yandex.practicum.order.feign.product.dto.OrderProductDto;

import java.util.List;

@Slf4j
@Component
public class ProductClientFallbackFactory implements FallbackFactory<ProductClient> {

    @Override
    public ProductClient create(Throwable cause) {

        if (cause instanceof BusinessException business) {
            throw business;
        }

        return new ProductClient() {

            @Override
            public OrderProductDto getProductById(Long id) {
                log.error("product-service недоступен при getProductById: productId={}", id, cause);
                throw new ProductServiceUnavailableException(id, cause);
            }

            @Override
            public List<OrderProductDto> getProductsByIds(List<Long> ids) {
                log.error("product-service недоступен при getProductsByIds: productIds={}", ids, cause);
                throw new ProductServiceUnavailableException(ids, cause);
            }
        };
    }
}
