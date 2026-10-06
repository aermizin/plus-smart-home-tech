package ru.yandex.practicum.product.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.product.dto.CreateProductRequest;
import ru.yandex.practicum.product.dto.ProductDto;
import ru.yandex.practicum.product.dto.UpdateProductRequest;
import ru.yandex.practicum.product.entity.Category;
import ru.yandex.practicum.product.entity.Product;
import ru.yandex.practicum.product.exception.NotFoundException;
import ru.yandex.practicum.product.mapper.ProductMapper;
import ru.yandex.practicum.product.repository.CategoryRepository;
import ru.yandex.practicum.product.repository.ProductRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> getAllProducts() {
        List<Product> products = productRepository.findAll();
        log.debug("Найдено {} товаров", products.size());

        return products.stream()
                .map(productMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Товар с указанным id = " + id + " не найден"));
        log.debug("Найден товар с id {}", id);

        return productMapper.toDto(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> getProductsByIds(List<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }

        List<Product> products = productRepository.findByIdIn(ids);
        log.debug("Найдено {} товаров", products.size());

        return products.stream()
                .map(productMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> searchProducts(String query) {
        List<Product> products = productRepository.findByNameContainingIgnoreCase(query);

        log.debug("Найдено {} товаров c частичным совпадением в названии {}", products.size(), query);

        return products.stream()
                .map(productMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> getProductsByCategory(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new NotFoundException("Категория с id = " + categoryId + " не найдена");
        }

        List<Product> products = productRepository.findByCategoryId(categoryId);
        log.debug("Найдено товаров {} в категории с id {}",products.size(), categoryId);

        return products.stream()
                .map(productMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public ProductDto createProduct(CreateProductRequest request) {
        Product product = productMapper.toEntity(request);

        Long categoryId = request.categoryId();
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new NotFoundException("Категория с id = " + categoryId + " не найдена"));
            product.setCategory(category);
        }

        Product savedProduct = productRepository.save(product);
        log.debug("Создан новый товар с id = {}", savedProduct.getId());
        return productMapper.toDto(savedProduct);
    }

    @Override
    @Transactional
    public ProductDto updateProduct(Long id, UpdateProductRequest request) {
        Product product = productRepository.findByIdUpdate(id)
                .orElseThrow(() -> new NotFoundException("Товар с id = " + id + " не найден"));

        applyUpdates(product, request);

        if (request.categoryId() != null) {
            Category category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new NotFoundException("Категория с id = " + request.categoryId() + " не найдена"));
            product.setCategory(category);
        }

        Product updatedProduct = productRepository.save(product);
        log.debug("Обновлен товар с id = {}", updatedProduct.getId());

        return productMapper.toDto(updatedProduct);
    }

    private void applyUpdates(Product product, UpdateProductRequest request) {
        if (request.name() != null)        product.setName(request.name());
        if (request.description() != null) product.setDescription(request.description());
        if (request.price() != null)       product.setPrice(request.price());
        if (request.imageUrl() != null)    product.setImageUrl(request.imageUrl());
        if (request.active() != null)      product.setActive(request.active());
    }
}
