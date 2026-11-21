package ru.psu.catalogservice.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.psu.catalogservice.dto.ProductRequest;
import ru.psu.catalogservice.dto.ProductResponse;
import ru.psu.catalogservice.dto.StockReduceRequest;
import ru.psu.catalogservice.entity.ProductEntity;
import ru.psu.catalogservice.repository.ProductRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        List<ProductEntity> entities = productRepository.findAll();
        List<ProductResponse> responses = new ArrayList<>();

        for (ProductEntity entity : entities) {
            ProductResponse dto = mapToResponse(entity);
            responses.add(dto);
        }

        return responses;
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Optional<ProductEntity> optionalEntity = productRepository.findById(id);

        if (optionalEntity.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "ID " + id + " not found!!!!"
            );
        }

        ProductEntity entity = optionalEntity.get();

        return mapToResponse(entity);
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        ProductEntity entity = new ProductEntity();

        entity.setName(request.getName());
        entity.setPrice(request.getPrice());
        entity.setQuantity(request.getQuantity());
        entity.setImageURL(request.getImageURL());
        entity.setManufactureDate(request.getManufactureDate());
        entity.setExpirationDays(request.getExpirationDays());

        ProductEntity savedEntity = productRepository.save(entity);

        return mapToResponse(savedEntity);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Optional<ProductEntity> optionalEntity = productRepository.findById(id);

        if (optionalEntity.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "ID " + id + " not found!!!!"
            );
        }

        ProductEntity entity = optionalEntity.get();

        entity.setName(request.getName());
        entity.setPrice(request.getPrice());
        entity.setQuantity(request.getQuantity());
        entity.setImageURL(request.getImageURL());

        ProductEntity updatedEntity = productRepository.save(entity);

        return mapToResponse(updatedEntity);
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "ID " + id + " not found!!!!"
            );
        }

        productRepository.deleteById(id);
    }

    private ProductResponse mapToResponse(ProductEntity entity) {
        ProductResponse dto = new ProductResponse();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setPrice(entity.getPrice());
        dto.setQuantity(entity.getQuantity());
        dto.setImageURL(entity.getImageURL());
        dto.setManufactureDate(entity.getManufactureDate());
        dto.setExpirationDays(entity.getExpirationDays());
        return dto;
    }

    @Transactional
    public void reduceStock(List<StockReduceRequest> items) {
        for (StockReduceRequest item : items) {
            Optional<ProductEntity> optionalProduct = productRepository.findById(item.getProductId());

            if (optionalProduct.isEmpty()) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Товар с ID " + item.getProductId() + " не найден"
                );
            }

            ProductEntity product = optionalProduct.get();

            if (product.getQuantity() < item.getQuantity()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Недостаточно товара '" + product.getName() + "'"
                );
            }

            product.setQuantity(product.getQuantity() - item.getQuantity());
            productRepository.save(product);
        }
    }
}
