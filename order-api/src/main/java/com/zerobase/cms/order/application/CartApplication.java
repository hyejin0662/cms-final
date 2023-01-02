package com.zerobase.cms.order.application;

import com.zerobase.cms.order.domain.model.Product;
import com.zerobase.cms.order.domain.model.ProductItem;
import com.zerobase.cms.order.domain.product.AddProductCartForm;
import com.zerobase.cms.order.domain.redis.Cart;
import com.zerobase.cms.order.exception.CustomException;
import com.zerobase.cms.order.exception.ErrorCode;
import com.zerobase.cms.order.service.CartService;
import com.zerobase.cms.order.service.ProductSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.zerobase.cms.order.exception.ErrorCode.ITEM_COUNT_NOT_ENOUGH;
import static com.zerobase.cms.order.exception.ErrorCode.NOT_FOUND_PRODUCT;

@Service
@RequiredArgsConstructor
public class CartApplication {
    private final ProductSearchService productSearchService;
    private final CartService cartService;

    /**
     * 장바구니 추가
     */
    public Cart addCart(Long customerId, AddProductCartForm form){
        // 검사
        Product product = productSearchService.getByProductId(form.getId());
        if (product == null) {
            throw new CustomException(NOT_FOUND_PRODUCT);
        }
        Cart cart = cartService.getCart(customerId);

        if (!addAble(cart, product, form)) {
            throw new CustomException(ITEM_COUNT_NOT_ENOUGH);
        }

        return cartService.addCart(customerId, form);
    }

    /**
     * 장바구니 업데이트
     */
    public Cart updateCart(Long customerId, Cart cart) {
        // 장바구니를 새로 밀어넣고 검사 진행후 리턴
        cartService.putCart(customerId, cart);
        return getCart(customerId);
    }

    /**
     * 장바구니 조회
     */
    public Cart getCart(Long customerId) {
        Cart cart = refreshCart(cartService.getCart(customerId));
        cartService.putCart(cart.getCustomerId(), cart);

        Cart returnCart = new Cart();
        returnCart.setCustomerId(customerId);
        returnCart.setProducts(cart.getProducts());
        returnCart.setMessages(cart.getMessages());

        cart.setMessages(new ArrayList<>());
        // 빈 메세지의 Cart reids에 업데이트
        cartService.putCart(customerId, cart);

        return returnCart;
    }

    /**
     * 장바구니 내용 초기화
     */
    public void clearCart(Long customerId) {
        cartService.putCart(customerId, null);
    }

    /**
     * 장바구니에 담긴 상품 및 옵션 변동사항 체크
     * 변동된 상품 및 옵션에 대해서 메시지 추가
     */
    protected Cart refreshCart(Cart cart) {
        // 1. 상품이나 상품아이템의 정보, 가격, 수량이 변경되었는지 체크하고 그에 맞는 알람을 제공
        // 2. 상품의 수량이나 가격은 우리가 수정.

        // 장바구니의 상품 아이디를 토대로 가져온 상품 맵
        Map<Long, Product> productMap = productSearchService.getListByProductIds(
                cart.getProducts().stream().map(Cart.Product::getId)
                        .collect(Collectors.toList()))
                .stream()
                .collect(Collectors.toMap(Product::getId, product -> product));

        // 장바구니에 대한 상품의 변동사항 검사 시작 및 옵션 변경 시작
        for (int i = 0; i < cart.getProducts().size(); i++) {
            // 상품 검사
            Cart.Product cartProduct = cart.getProducts().get(i);

            Product p = productMap.get(cartProduct.getId());
            if (p == null) {    // 상품이 존재하지 않을 경우
                cart.getProducts().remove(cartProduct);
                i--;
                cart.addMessage(cartProduct.getName() + " 상품이 삭제되었습니다.");
                continue;
            }

            // 상품 아이템들 검사
            Map<Long, ProductItem> productItemMap = p.getProductItems().stream()
                    .collect(Collectors.toMap(ProductItem::getId, pi -> pi));

            List<String> tmpMessage = new ArrayList<>();
            for (int j = 0; j < cartProduct.getItems().size(); j++) {
                Cart.ProductItem cartProductItem = cartProduct.getItems().get(j);
                ProductItem pi = productItemMap.get(cartProductItem.getId());
                if (pi == null) {
                    cartProduct.getItems().remove(cartProductItem);
                    j--;
                    tmpMessage.add(cartProductItem.getName() + " 옵션이 삭제되었습니다.");
                    continue;
                }

                boolean isPriceChanged = false;
                boolean isCountNotEnough  = false;

                if (!cartProductItem.getPrice().equals(pi.getPrice())) {
                    isPriceChanged = true;
                    cartProductItem.setPrice(pi.getPrice());
                }
                if (cartProductItem.getCount() > pi.getCount()) {
                    isCountNotEnough = true;
                    cartProductItem.setCount(pi.getCount());
                }

                if (isPriceChanged && isCountNotEnough) {
                    tmpMessage.add(cartProductItem.getName() + " 가격과 수량이 변동되었습니다.");
                } else if (isPriceChanged) {
                    tmpMessage.add(cartProductItem.getName() + " 가격이 변동되었습니다.");
                } else if (isCountNotEnough) {
                    tmpMessage.add(cartProductItem.getName() + " 수량이 변동되었습니다.");
                }
            }

            // 현재 상품의 옵션이 없어진 경우 장바구니에서 삭제
            if (cartProduct.getItems().size() == 0) {
                cart.getProducts().remove(cartProduct);
                i--;
                cart.addMessage(cartProduct.getName() + " 상품의 옵션이 소진되어 구매가 불가능합니다.");
                continue;
            } else if (tmpMessage.size() > 0) {
                StringBuilder builder = new StringBuilder();
                builder.append(cartProduct.getName() + " 상품의 변동상황 : ");
                for (String message : tmpMessage) {
                    builder.append(message);
                    builder.append(", ");
                }
                cart.addMessage(builder.toString());
            }
        }
        return cart;
    }

    /**
     * 아이템 재고량 검사
     */
    private boolean addAble(Cart cart, Product product, AddProductCartForm form) {
        Cart.Product cartProduct = cart.getProducts().stream()
                .filter(p -> p.getId().equals(form.getId()))
                .findFirst()
                .orElse(Cart.Product.builder()
                        .id(product.getId())
                        .items(Collections.emptyList())
                        .build()
                );

        // 상품의 아이템 목록에서 키 : 아이템 id, value : 아이템 수량 으로 map 타입으로 변경(검색 속도 차이)
        Map<Long, Integer> cartItemCount = cartProduct.getItems().stream()
                .collect(Collectors.toMap(Cart.ProductItem::getId, Cart.ProductItem::getCount));

        Map<Long, Integer> currentItemCount = product.getProductItems().stream()
                .collect(Collectors.toMap(ProductItem::getId, ProductItem::getCount));

        return form.getItems().stream().noneMatch(
                formItem -> {
                    Integer cartCount = cartItemCount.get(formItem.getId());
                    if (cartCount == null) {
                        cartCount = 0;
                    }
                    Integer currentCount = currentItemCount.get(formItem.getId());
                    return formItem.getCount() + cartCount > currentCount;
                });
    }
}
