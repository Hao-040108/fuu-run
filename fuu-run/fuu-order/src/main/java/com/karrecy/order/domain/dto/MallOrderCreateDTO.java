package com.karrecy.order.domain.dto;

import com.karrecy.order.domain.vo.AddressVO;
import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 代购商城订单构建参数（由商城模块组装后传入订单域）
 */
@Data
public class MallOrderCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 学校id
     */
    @NotNull(message = "学校尚未初始化")
    private Long schoolId;

    /**
     * 收货地址
     */
    @NotNull(message = "请先选择收货地址")
    @Valid
    private AddressVO endAddress;

    /**
     * 标签（如：商城代购）
     */
    private String tag;

    /**
     * 订单描述（商品摘要）
     */
    private String detail;

    /**
     * 商品金额合计（元）
     */
    private BigDecimal goodsAmount;

    /**
     * 代购服务费合计（元）
     */
    private BigDecimal serviceFee;

    /**
     * 追加金额（元，可选）
     */
    private BigDecimal additionalAmount;

    /**
     * 未支付自动取消时长（秒）
     */
    private Integer autoCancelTtl;

}
