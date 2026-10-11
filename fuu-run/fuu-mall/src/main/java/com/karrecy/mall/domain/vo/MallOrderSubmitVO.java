package com.karrecy.mall.domain.vo;

import com.karrecy.payment.domain.vo.PayedVO;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商城下单结果（金额明细 + 支付参数）
 */
@Data
public class MallOrderSubmitVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long orderId;

    /**
     * 商品金额
     */
    private BigDecimal goodsAmount;

    /**
     * 代购服务费
     */
    private BigDecimal serviceFee;

    /**
     * 配送费（学校底价）
     */
    private BigDecimal deliveryFee;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 是否待支付（开发模式跳过真实预支付时为 true）
     */
    private Boolean payPending;

    /**
     * 微信支付参数
     */
    private PayedVO payInfo;

}
