package ru.psu.cartservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import ru.psu.cartservice.dto.AddToCartRequest;
import ru.psu.cartservice.dto.CartItemResponse;
import ru.psu.cartservice.dto.ProductCatalogDto;
import ru.psu.cartservice.entity.CartItemEntity;
import ru.psu.cartservice.repository.CartItemRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final RestClient restClient;
    private final String catalogServiceUrl;

    public CartService(CartItemRepository cartItemRepository,
                       @Value("${catalog.service.url}") String catalogServiceUrl
    ) {
        this.cartItemRepository = cartItemRepository;
        this.catalogServiceUrl = catalogServiceUrl;
        this.restClient = RestClient.create();
    }

    @Transactional
    public void addToCart(Long userId, AddToCartRequest request) {
        ProductCatalogDto product = fetchProductFromCatalog(request.getProductId());

        Optional<CartItemEntity> existingItemOpt =
                cartItemRepository.findByUserIdAndProductId(userId, request.getProductId());

        int newQuantity = request.getQuantity();
        if (existingItemOpt.isPresent()) {
            newQuantity += existingItemOpt.get().getQuantity();
        }

        // Если положил в корзину больше, чем есть на складе
        if (product.getQuantity() < newQuantity) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "You cannot add more items than are available in stock. In stock: " + product.getQuantity()
            );
        }

        if (existingItemOpt.isPresent()) {
            CartItemEntity existingItem = existingItemOpt.get();
            existingItem.setQuantity(newQuantity);
            cartItemRepository.save(existingItem);
        } else {
            CartItemEntity newItem = new CartItemEntity();
            newItem.setUserId(userId);
            newItem.setProductId(request.getProductId());
            newItem.setQuantity(request.getQuantity());
            cartItemRepository.save(newItem);
        }
    }

    @Transactional(readOnly = true)
    public List<CartItemResponse> getCart(Long userId) {
        List<CartItemEntity> items = cartItemRepository.findByUserId(userId);
        List<CartItemResponse> responseList = new ArrayList<>();

        for (CartItemEntity item : items) {
            ProductCatalogDto product = fetchProductFromCatalog(item.getProductId());

            CartItemResponse response = new CartItemResponse();
            response.setProductId(item.getProductId());
            response.setProductName(product.getName());
            response.setImageURL(product.getImageURL());
            response.setPrice(product.getPrice());
            response.setQuantity(item.getQuantity());

            // Итоговая стоимость позиции (цена * количество)
            BigDecimal total = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            response.setTotalPrice(total);

            responseList.add(response);
        }

        return responseList;
    }

    @Transactional
    public void removeFromCart(Long userId, Long productId) {
        Optional<CartItemEntity> itemOpt = cartItemRepository.findByUserIdAndProductId(userId, productId);
        if (itemOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found in the cart");
        }
        cartItemRepository.delete(itemOpt.get());
    }

    @Transactional
    public void clearCart(Long userId) {
        cartItemRepository.deleteByUserId(userId);
    }



    private ProductCatalogDto fetchProductFromCatalog(Long productId) {
        try {
            return restClient.get()
                    .uri(catalogServiceUrl + "/" + productId)
                    .retrieve()
                    .body(ProductCatalogDto.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Product (ID: " + productId + ") not found in catalog!!!!"
            );
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "The catalog service is temporarily unavailable"
            );
        }
    }
}
