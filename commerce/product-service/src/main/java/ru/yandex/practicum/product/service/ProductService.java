package ru.yandex.practicum.product.service;

import ru.yandex.practicum.product.dto.CreateProductRequest;
import ru.yandex.practicum.product.dto.ProductDto;
import ru.yandex.practicum.product.dto.UpdateProductRequest;

import java.util.List;

public interface ProductService {

    List<ProductDto> getAllProducts();

    ProductDto getProductById(Long id);

    List<ProductDto> searchProducts(String query);

    List<ProductDto> getProductsByCategory(Long categoryId);

    ProductDto createProduct(CreateProductRequest request);

    ProductDto updateProduct(Long id, UpdateProductRequest request);
}
