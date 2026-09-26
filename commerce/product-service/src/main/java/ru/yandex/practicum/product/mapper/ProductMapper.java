package ru.yandex.practicum.product.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.product.dto.CreateProductRequest;
import ru.yandex.practicum.product.dto.ProductDto;
import ru.yandex.practicum.product.entity.Product;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class})
public interface ProductMapper {

    ProductDto toDto(Product product);

    @Mapping(target = "category", ignore = true)
    @Mapping(target = "id", ignore = true)
    Product toEntity(CreateProductRequest request);
}
