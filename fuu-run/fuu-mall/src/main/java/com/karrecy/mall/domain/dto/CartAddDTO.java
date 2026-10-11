package com.karrecy.mall.domain.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 加入购物车参数
 */
@Data
public class CartAddDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "商品id不能为空")
    private Long goodsId;

    /**
     * 数量（默认 1）
     */
    private Integer quantity;

}
