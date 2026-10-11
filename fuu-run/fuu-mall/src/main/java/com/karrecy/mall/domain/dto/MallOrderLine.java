package com.karrecy.mall.domain.dto;

import com.karrecy.mall.domain.po.Goods;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 商城下单条目（商品 + 数量，内部使用）
 */
@Data
@AllArgsConstructor
public class MallOrderLine implements Serializable {

    private static final long serialVersionUID = 1L;

    private Goods goods;

    private Integer quantity;

}
