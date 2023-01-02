package com.zerobase.cms.order.domain.redis;

import com.zerobase.cms.order.domain.product.AddProductCartForm;
import lombok.*;
import org.springframework.data.redis.core.RedisHash;

import javax.persistence.Id;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@RedisHash("cart")
// 객체 선언후 @RedisHash 사용시 redis에 저장할 자료구조 객체를 정의. value옵션 : keyspace
public class Cart {

    @Id // @Id 어노테이션이 붙은 필드가 Redis Key값. 레디스에 저장될 최종 키 값은 keyspace : id
    private Long customerId;
    private List<Product> products = new ArrayList<>(); // 상품리스트
    private List<String> messages = new ArrayList<>();

    public Cart(Long customerId) {
        this.customerId = customerId;
    }

    public void addMessage(String message) {
        this.messages.add(message);
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Product {   // ProductEntity와의 중복방지
        private Long id;
        private Long sellerId;
        private String name;
        private String description;
        private List<ProductItem> items = new ArrayList<>();

        public static Product from(AddProductCartForm form) {
            return Product.builder()
                    .id(form.getId())
                    .sellerId(form.getSellerId())
                    .name(form.getName())
                    .description(form.getDescription())
                    .items(form.getItems().stream()
                            .map(ProductItem::from).collect(Collectors.toList()))
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductItem {
        private Long id;
        private String name;
        private Integer price;
        private Integer count;

        public static ProductItem from(AddProductCartForm.ProductItem form) {
            return ProductItem.builder()
                    .id(form.getId())
                    .name(form.getName())
                    .count(form.getCount())
                    .price(form.getPrice())
                    .build();
        }
    }

}
