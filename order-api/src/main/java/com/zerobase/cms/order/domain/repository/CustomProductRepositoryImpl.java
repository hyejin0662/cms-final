package com.zerobase.cms.order.domain.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.zerobase.cms.order.domain.model.Product;
import com.zerobase.cms.order.domain.model.QProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CustomProductRepositoryImpl implements CustomProductRepository{

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Product> searchByName(String name) {
        String search = "%" + name + "%";

        QProduct qProduct = QProduct.product;

        return queryFactory.selectFrom(qProduct)
                .where(qProduct.name.like(search))
                .fetch();
    }
}
