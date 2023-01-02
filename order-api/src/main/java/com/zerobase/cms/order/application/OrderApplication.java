package com.zerobase.cms.order.application;

import com.zerobase.cms.order.client.UserClient;
import com.zerobase.cms.order.client.user.ChangeBalanceForm;
import com.zerobase.cms.order.client.user.CustomerDto;
import com.zerobase.cms.order.domain.model.ProductItem;
import com.zerobase.cms.order.domain.redis.Cart;
import com.zerobase.cms.order.exception.CustomException;
import com.zerobase.cms.order.service.ProductItemService;
import com.zerobase.domain.config.JwtAuthenticationProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.IntStream;

import static com.zerobase.cms.order.exception.ErrorCode.ORDER_FAIL_CHECK_CART;
import static com.zerobase.cms.order.exception.ErrorCode.ORDER_FAIL_NO_MONEY;

@Service
@RequiredArgsConstructor
public class OrderApplication {

    private final CartApplication cartApplication;
    private final UserClient userClient;
    private final ProductItemService productItemService;

    @Transactional
    public void order(String token, Cart cart) {
        Cart orderCart = cartApplication.refreshCart(cart);
        if (orderCart.getMessages().size() > 0) {
            throw new CustomException(ORDER_FAIL_CHECK_CART);
        }

        // 잔액 충분한지 확인
        CustomerDto customerDto = userClient.getCustomerInfo(token).getBody();

        int totalPrice = getTotalPrice(cart);
        if (customerDto.getBalance() < totalPrice) {
            throw new CustomException(ORDER_FAIL_NO_MONEY);
        }

        // 결제
        ChangeBalanceForm form = ChangeBalanceForm.builder()
                                        .from("USER")
                                        .message("order")
                                        .money(-totalPrice)
                                        .build();

        userClient.changeBalance(token, form);

        // 상품 재고 관리
        for (Cart.Product product : orderCart.getProducts()) {
            for (Cart.ProductItem item : product.getItems()) {
                ProductItem productItem =
                        productItemService.getProductItem(item.getId());
                productItem.setCount(productItem.getCount() - item.getCount());
            }
        }


    }

    // 1. 물건들이 전부 주문 가능한 상태인지 확인
    // 2. 가격 변동이 있었는지에 대해 확인
    // 3. 고객의 잔액이 충분한지
    // 4. 결제 & 상품의 재고 관리

    /**
     * 총 결제 가격 계산
     */
    private Integer getTotalPrice(Cart cart) {
        return cart.getProducts().stream().flatMapToInt(product ->
                product.getItems().stream().flatMapToInt(productItem ->
                        IntStream.of(productItem.getPrice() * productItem.getCount())))
                        .sum();
    }

}
