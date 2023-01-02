package com.zerobase.cms.order.application;

import com.zerobase.cms.order.domain.model.Product;
import com.zerobase.cms.order.domain.model.ProductItem;
import com.zerobase.cms.order.domain.product.AddProductCartForm;
import com.zerobase.cms.order.domain.redis.Cart;
import com.zerobase.cms.order.domain.repository.ProductRepository;
import com.zerobase.cms.order.service.CartService;
import com.zerobase.cms.order.service.ProductSearchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

//@ExtendWith(MockitoExtension.class)
@SpringBootTest
class CartApplicationTest2 {
//    @Mock
//    private ProductSearchService productSearchService;
//
//    @Mock
//    private CartService cartService;
//
//    @Mock
//    private ProductRepository productRepository;
//
//    @InjectMocks
//    private CartApplication application;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductSearchService productSearchService;

    @Autowired
    private CartApplication application;

    @Test
    void testAddCart() {
        // given
        Long sellerId = 10L;

        ProductItem item1 = ProductItem.builder()
                .sellerId(sellerId)
                .name("item1")
                .price(1000)
                .count(10)
                .build();

        ProductItem item2 = ProductItem.builder()
                .sellerId(sellerId)
                .name("item2")
                .price(1000)
                .count(10)
                .build();

        Product product = Product.builder()
                .name("product")
                .description("product")
                .sellerId(sellerId)
                .productItems(Arrays.asList(item1, item2))
                .build();

        Product savedProduct = productRepository.save(product);

        Long productId = savedProduct.getId();

        Product p = productSearchService.getByProductId(productId);

        makeAddForm(p);

        // when
//        Cart cart = cartService.getCart(customerId);

        // then
    }

    AddProductCartForm makeAddForm(Product p) {
        AddProductCartForm.ProductItem productItem =
                AddProductCartForm.ProductItem.builder()
                        .id(p.getProductItems().get(0).getId())
                        .name(p.getProductItems().get(0).getName())
                        .count(5)
                        .price(20000)   // 변경됨
                        .build();

        return AddProductCartForm.builder()
                .id(p.getId())
                .sellerId(p.getSellerId())
                .name(p.getName())
                .description(p.getDescription())
                .items(List.of(productItem))
                .build();
    }
}