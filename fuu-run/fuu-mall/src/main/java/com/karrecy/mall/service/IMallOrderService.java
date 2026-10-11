package com.karrecy.mall.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.karrecy.common.core.domain.PageQuery;
import com.karrecy.common.core.page.TableDataInfo;
import com.karrecy.mall.domain.dto.MallOrderSubmitDTO;
import com.karrecy.mall.domain.po.OrderGoodsItem;
import com.karrecy.mall.domain.vo.MallOrderDetailVO;
import com.karrecy.mall.domain.vo.MallOrderSubmitVO;
import com.karrecy.order.domain.dto.OrderQuery;
import com.karrecy.order.domain.po.OrderMain;

/**
 * 商城订单服务（编排：商品校验 → 订单域建单 → 明细/库存/购物车 → 发起支付）
 */
public interface IMallOrderService extends IService<OrderGoodsItem> {

    /**
     * 提交商城订单
     */
    MallOrderSubmitVO submitMall(MallOrderSubmitDTO dto);

    /**
     * 商城订单分页（管理端）
     */
    TableDataInfo<OrderMain> pageMallOrders(OrderQuery query, PageQuery pageQuery);

    /**
     * 商城订单详情（订单主表 + 商品明细）
     */
    MallOrderDetailVO mallOrderDetail(Long orderId);

}
