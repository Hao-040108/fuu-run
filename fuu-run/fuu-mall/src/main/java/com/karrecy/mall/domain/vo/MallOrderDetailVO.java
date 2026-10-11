package com.karrecy.mall.domain.vo;

import com.karrecy.mall.domain.po.OrderGoodsItem;
import com.karrecy.order.domain.po.OrderMain;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 商城订单详情（订单主表 + 商品明细）
 */
@Data
public class MallOrderDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private OrderMain order;

    private List<OrderGoodsItem> items;

}
