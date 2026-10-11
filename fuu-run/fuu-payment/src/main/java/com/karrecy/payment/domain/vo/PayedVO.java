package com.karrecy.payment.domain.vo;

import lombok.Data;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

/**
 * 请求支付后的回调参数
 */
@Data
public class PayedVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long orderId;
    /**
     * 是否为"待支付"状态（开发模式跳过真实预支付时为 true，客户端可稍后调用继续支付）
     */
    private Boolean payPending;
    private String appId;
    private String timeStamp;
    private String nonceStr;
    private String packageValue;
    private String signType;
    private String paySign;

}
