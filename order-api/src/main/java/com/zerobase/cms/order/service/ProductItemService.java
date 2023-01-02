package com.zerobase.cms.order.service;

import com.zerobase.cms.order.domain.model.Product;
import com.zerobase.cms.order.domain.model.ProductItem;
import com.zerobase.cms.order.domain.product.AddProductItemForm;
import com.zerobase.cms.order.domain.product.ProductDto;
import com.zerobase.cms.order.domain.product.UpdateProductItemForm;
import com.zerobase.cms.order.domain.repository.ProductItemRepository;
import com.zerobase.cms.order.domain.repository.ProductRepository;
import com.zerobase.cms.order.exception.CustomException;
import com.zerobase.cms.order.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.zerobase.cms.order.exception.ErrorCode.NOT_FOUND_ITEM;

@Service
@RequiredArgsConstructor
public class ProductItemService {
    private final ProductItemRepository productItemRepository;
    private final ProductRepository productRepository;

    @Transactional
    public ProductItem getProductItem(Long id) {
        return productItemRepository.getById(id);
    }

    /**
     * 상품 아이템 추가
     */
    @Transactional
    public Product addProductItem(Long sellerId, AddProductItemForm form) {
        Product product = productRepository.findBySellerIdAndId(sellerId, form.getProductId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_PRODUCT));

        if (product.getProductItems().stream()
                .anyMatch(item -> item.getName().equals(form.getName()))) {
            throw new CustomException(ErrorCode.ALREADY_EXIST_ITEM);
        }

        ProductItem item = ProductItem.of(sellerId, form);
        product.getProductItems().add(item);

        return product;
    }

    /**
     * 상품 아이템 수정
     */
    @Transactional
    public ProductItem updateProductItem(Long sellerId, UpdateProductItemForm form) {
        ProductItem item = productItemRepository.findById(form.getId())
                .filter(pi -> pi.getSellerId().equals(sellerId))
                .orElseThrow(() -> new CustomException(NOT_FOUND_ITEM));

        item.updateItem(form);

        return item;
    }

    /**
     * 상품 아이템 삭제
     */
    @Transactional
    public void deleteProductItem(Long sellerId, Long productItemId) {
        ProductItem item = productItemRepository.findById(productItemId)
                .filter(pi -> pi.getSellerId().equals(sellerId))
                .orElseThrow(() -> new CustomException(NOT_FOUND_ITEM));

        productItemRepository.delete(item);
    }
}
