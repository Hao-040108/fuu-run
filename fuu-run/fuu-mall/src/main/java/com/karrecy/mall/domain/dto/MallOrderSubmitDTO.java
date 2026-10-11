package com.karrecy.mall.domain.dto;

import com.karrecy.order.domain.vo.AddressVO;
import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 商城下单参数
 * <p>两种方式二选一：购物车结算（cartItemIds）或立即购买（goodsId + quantity）</p>
 */
@Data
public class MallOrderSubmitDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "学校尚未初始化")
    private Long schoolId;

    /**
     * 收货地址
     */
    @NotNull(message = "请先选择收货地址")
    @Valid
    private AddressVO endAddress;

    /**
     * 备注（可选）
     */
    private String remark;

    /**
     * 购物车条目id集合（购物车结算）
     */
    private List<Long> cartItemIds;

    /**
     * 商品id（立即购买）
     */
    private Long goodsId;

    /**
     * 数量（立即购买，默认 1）
     */
    private Integer quantity;

    /**
     * 追加金额（可选）
     */
    private BigDecimal additionalAmount;

    /**
     * 未支付自动取消时长（秒，默认 900）
     */
    private Integer autoCancelTtl;

}
