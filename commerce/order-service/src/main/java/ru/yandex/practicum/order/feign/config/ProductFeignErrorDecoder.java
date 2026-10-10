package ru.yandex.practicum.order.feign.config;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.order.exception.ExternalServiceException;
import ru.yandex.practicum.order.exception.ServiceUnavailableException;

public class ProductFeignErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        int status = response.status();

        if (status == 503) {
            return new ServiceUnavailableException("Сервис временно недоступен, повторите позже");
        }

        if (status >= 500) {
            return new ExternalServiceException("Ошибка внешнего сервиса");
        }

        return new ExternalServiceException("Ошибка внешнего сервиса");
    }
}
