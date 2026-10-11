package com.karrecy.mall.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 购物车条目视图（含商品信息）
 */
@Data
public class CartItemVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long goodsId;

    private String name;

    private String image;

    private String unit;

    private BigDecimal price;

    private BigDecimal purchaseFee;

    private Integer quantity;

    private Integer stock;

    /**
     * 商品状态 0 下架 1 上架
     */
    private Integer goodsStatus;

    /**
     * 小计（单价 × 数量）
     */
    private BigDecimal subtotal;

    /**
     * 代购服务费小计
     */
    private BigDecimal feeSubtotal;

}
