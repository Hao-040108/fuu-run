package com.karrecy.admin.controller.dev;

import cn.hutool.core.util.ObjectUtil;
import com.karrecy.common.config.FuuConfig;
import com.karrecy.common.constant.QueueNames;
import com.karrecy.common.core.controller.BaseController;
import com.karrecy.common.core.domain.R;
import com.karrecy.common.enums.OrderStatus;
import com.karrecy.common.utils.redis.QueueUtils;
import com.karrecy.order.domain.po.OrderMain;
import com.karrecy.order.service.IOrderMainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 开发调试接口（仅在 fuu.mock-pay-enabled=true 时可用）
 */
@RestController
@RequestMapping("/dev")
@RequiredArgsConstructor
@Slf4j
public class DevMockController extends BaseController {

    private final FuuConfig fuuConfig;
    private final IOrderMainService orderMainService;

    /**
     * 模拟支付成功：走与微信支付回调完全一致的队列链路
     */
    @PostMapping("/mockPay/{orderId}")
    public R<Void> mockPay(@PathVariable Long orderId) {
        if (!ObjectUtil.equals(Boolean.TRUE, fuuConfig.getMockPayEnabled())) {
            return R.fail("模拟支付未开启（fuu.mock-pay-enabled != true）");
        }
        OrderMain order = orderMainService.getById(orderId);
        if (order == null) {
            return R.fail("订单不存在");
        }
        if (!ObjectUtil.equals(order.getStatus(), OrderStatus.PENDING_PAYMENT.getCode())) {
            return R.fail("订单当前状态不可支付（status=" + order.getStatus() + "）");
        }
        QueueUtils.addBoundedQueueObject(QueueNames.ORDER_PAY_SUCCESS, String.valueOf(orderId));
        log.info("[DEV] 已触发模拟支付成功：orderId={}", orderId);
        return R.ok("已触发模拟支付成功，订单将进入待接单");
    }

}
