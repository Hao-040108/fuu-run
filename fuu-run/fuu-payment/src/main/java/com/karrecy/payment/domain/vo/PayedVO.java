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
    private String appId;
    private String timeStamp;
    private String nonceStr;
    private String packageValue;
    private String signType;
    private String paySign;

}
