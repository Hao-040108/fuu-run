package com.karrecy.mall.domain.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 修改购物车数量参数
 */
@Data
public class CartUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "购物车条目id不能为空")
    private Long id;

    @NotNull(message = "数量不能为空")
    private Integer quantity;

}
