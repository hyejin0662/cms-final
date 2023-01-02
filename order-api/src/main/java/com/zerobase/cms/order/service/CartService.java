package com.zerobase.cms.order.service;

import com.zerobase.cms.order.client.RedisClient;
import com.zerobase.cms.order.domain.product.AddProductCartForm;
import com.zerobase.cms.order.domain.redis.Cart;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CartService {
    private final RedisClient redisClient;

    public Cart getCart(Long customerId) {
        Cart cart = redisClient.get(customerId, Cart.class);
        return cart != null ? cart : new Cart(customerId);
    }

    public Cart putCart(Long customerId, Cart cart){
        redisClient.put(customerId, cart);
        return cart;
    }

    public Cart addCart(Long customerId, AddProductCartForm form) {
        // Redis에서 customerId에 해당하는 값을 가져와서 Cart로 파싱
        Cart cart = redisClient.get(customerId, Cart.class);

        // 가져온 값이 null 일경우 새로운 객체 생성
        if (cart == null) {
            cart = new Cart();
            cart.setCustomerId(customerId);
        }

        // 이전에 같은 상품이 있는지
        Optional<Cart.Product> productOptional = cart.getProducts().stream()
                .filter(product1 -> product1.getId().equals(form.getId()))
                .findFirst();

        // 이미 같은 상품이 존재할경우 redis 업데이트
        if (productOptional.isPresent()) {
            Cart.Product redisProduct = productOptional.get();
            // requested (form에 작성한 아이템들을 리스트로 모음)
            List<Cart.ProductItem> items = form.getItems().stream()
                    .map(Cart.ProductItem::from).collect(Collectors.toList());

            // itemId : item 형태의 map 객체
            Map<Long, Cart.ProductItem> redisItemMap = redisProduct.getItems().stream()
                    .collect(Collectors.toMap(Cart.ProductItem::getId, it -> it));

            // 상품의 내용이 달라진 경우(id값으로 동일한 상품을 가져온 상태)
            if (!redisProduct.getName().equals(form.getName())) {
                cart.addMessage(redisProduct.getName() +
                        "의 정보가 변경되었습니다. 확인 부탁드립니다.");
            }

            for(Cart.ProductItem item : items) {    // 폼에서 가져온 아이템 리스트
                Cart.ProductItem redisItem = redisItemMap.get(item.getId());

                if (redisItem == null) {
                    // 장바구니에 추가한적이 없는 아이템인 경우 happy case
                    redisProduct.getItems().add(item);
                } else {
                    // 추가한적이 있고 가격이 변동된 아이템인 경우 메시지 추가
                    if (!redisItem.getPrice().equals(item.getPrice())) {
                        cart.addMessage(redisProduct.getName() +
                                item.getName() +
                                "의 가격이 변경되었습니다. 확인 부탁드립니다.");
                    }
                    redisItem.setCount(redisItem.getCount() + item.getCount());
                }
            }
        } else {    // 추가 상품이 새로운 상품일경우 새로 추가
            Cart.Product product = Cart.Product.from(form);
            cart.getProducts().add(product);
        }
        redisClient.put(customerId, cart);  // 갱신
        return cart;
    }
}
