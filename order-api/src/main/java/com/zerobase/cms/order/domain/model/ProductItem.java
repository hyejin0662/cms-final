package com.zerobase.cms.order.domain.model;

import com.zerobase.cms.order.domain.product.AddProductItemForm;
import com.zerobase.cms.order.domain.product.UpdateProductItemForm;
import lombok.*;
import org.hibernate.envers.AuditOverride;
import org.hibernate.envers.Audited;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Audited    // 값이 변경되면 자동 저장
@AuditOverride(forClass = BaseEntity.class)
public class ProductItem extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long sellerId;

    @Audited
    private String name;

    @Audited
    private Integer price;

    private Integer count;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    public static ProductItem of(Long sellerId, AddProductItemForm form) {
        return ProductItem.builder()
                .sellerId(sellerId)
                .name(form.getName())
                .price(form.getPrice())
                .count(form.getCount())
                .build();
    }

    public void updateItem(UpdateProductItemForm form) {
        this.name = form.getName();
        this.price = form.getPrice();
        this.count = form.getCount();
    }
}
