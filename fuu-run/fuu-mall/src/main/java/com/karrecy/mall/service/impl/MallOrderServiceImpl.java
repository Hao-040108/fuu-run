package com.karrecy.mall.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.karrecy.common.core.domain.PageQuery;
import com.karrecy.common.core.page.TableDataInfo;
import com.karrecy.common.enums.Status;
import com.karrecy.common.exception.ServiceException;
import com.karrecy.common.helper.LoginHelper;
import com.karrecy.common.utils.StringUtils;
import com.karrecy.mall.domain.dto.MallOrderLine;
import com.karrecy.mall.domain.dto.MallOrderSubmitDTO;
import com.karrecy.mall.domain.po.CartItem;
import com.karrecy.mall.domain.po.Goods;
import com.karrecy.mall.domain.po.OrderGoodsItem;
import com.karrecy.mall.domain.vo.MallOrderDetailVO;
import com.karrecy.mall.domain.vo.MallOrderSubmitVO;
import com.karrecy.mall.mapper.CartItemMapper;
import com.karrecy.mall.mapper.GoodsMapper;
import com.karrecy.mall.mapper.OrderGoodsItemMapper;
import com.karrecy.mall.service.IMallOrderService;
import com.karrecy.order.domain.dto.MallOrderCreateDTO;
import com.karrecy.order.domain.dto.OrderQuery;
import com.karrecy.order.domain.po.OrderMain;
import com.karrecy.order.service.IOrderMainService;
import com.karrecy.payment.domain.vo.PayedVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 商城订单服务实现
 * <p>编排流程：收集下单条目 → 订单域建单（来源=代购商城）→ 写入商品明细快照/扣库存/加销量
 * → 清理购物车 → 发起支付（开发模式返回待支付占位）</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MallOrderServiceImpl extends ServiceImpl<OrderGoodsItemMapper, OrderGoodsItem> implements IMallOrderService {

    private final GoodsMapper goodsMapper;
    private final CartItemMapper cartItemMapper;
    private final IOrderMainService orderMainService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MallOrderSubmitVO submitMall(MallOrderSubmitDTO dto) {
        Long uid = LoginHelper.getUserId();

        // —— 1. 收集下单条目（购物车结算 / 立即购买）——
        List<MallOrderLine> lines = new ArrayList<>();
        boolean fromCart = ObjectUtil.isNotEmpty(dto.getCartItemIds());
        if (fromCart) {
            List<CartItem> cartItems = cartItemMapper.selectBatchIds(dto.getCartItemIds());
            if (ObjectUtil.isEmpty(cartItems)) {
                throw new ServiceException("购物车条目不存在");
            }
            for (CartItem cartItem : cartItems) {
                if (!ObjectUtil.equals(cartItem.getUid(), uid)) {
                    throw new ServiceException("购物车条目不属于当前用户");
                }
                Goods goods = goodsMapper.selectById(cartItem.getGoodsId());
                validateGoods(goods, cartItem.getQuantity());
                lines.add(new MallOrderLine(goods, cartItem.getQuantity()));
            }
        } else if (ObjectUtil.isNotNull(dto.getGoodsId())) {
            int quantity = ObjectUtil.defaultIfNull(dto.getQuantity(), 1);
            if (quantity <= 0) {
                quantity = 1;
            }
            Goods goods = goodsMapper.selectById(dto.getGoodsId());
            validateGoods(goods, quantity);
            lines.add(new MallOrderLine(goods, quantity));
        } else {
            throw new ServiceException("请选择要购买的商品");
        }

        // —— 2. 金额计算 ——
        BigDecimal goodsAmount = BigDecimal.ZERO;
        BigDecimal serviceFee = BigDecimal.ZERO;
        for (MallOrderLine line : lines) {
            BigDecimal quantity = BigDecimal.valueOf(line.getQuantity());
            goodsAmount = goodsAmount.add(line.getGoods().getPrice().multiply(quantity));
            serviceFee = serviceFee.add(ObjectUtil.defaultIfNull(line.getGoods().getPurchaseFee(), BigDecimal.ZERO).multiply(quantity));
        }
        BigDecimal additionalAmount = ObjectUtil.defaultIfNull(dto.getAdditionalAmount(), BigDecimal.ZERO);
        int autoCancelTtl = ObjectUtil.defaultIfNull(dto.getAutoCancelTtl(), 900);

        // —— 3. 订单域建单 ——
        MallOrderCreateDTO createDTO = new MallOrderCreateDTO();
        createDTO.setSchoolId(dto.getSchoolId());
        createDTO.setEndAddress(dto.getEndAddress());
        createDTO.setTag("商城代购");
        createDTO.setDetail(buildDetail(lines, dto.getRemark()));
        createDTO.setGoodsAmount(goodsAmount);
        createDTO.setServiceFee(serviceFee);
        createDTO.setAdditionalAmount(additionalAmount);
        createDTO.setAutoCancelTtl(autoCancelTtl);
        Long orderId = orderMainService.createMallOrder(createDTO);

        // —— 4. 商品明细快照 + 库存/销量 ——
        LocalDateTime now = LocalDateTime.now();
        for (MallOrderLine line : lines) {
            Goods goods = line.getGoods();
            save(new OrderGoodsItem()
                    .setOrderId(orderId)
                    .setGoodsId(goods.getId())
                    .setGoodsName(goods.getName())
                    .setGoodsImage(goods.getImage())
                    .setPrice(goods.getPrice())
                    .setQuantity(line.getQuantity())
                    .setPurchaseFee(ObjectUtil.defaultIfNull(goods.getPurchaseFee(), BigDecimal.ZERO))
                    .setCreateTime(now));
            if (goods.getStock() != null && goods.getStock() >= 0) {
                goods.setStock(Math.max(0, goods.getStock() - line.getQuantity()));
            }
            goods.setSales(ObjectUtil.defaultIfNull(goods.getSales(), 0) + line.getQuantity());
            goodsMapper.updateById(goods);
        }

        // —— 5. 清理购物车 ——
        if (fromCart) {
            cartItemMapper.deleteBatchIds(dto.getCartItemIds());
        }

        // —— 6. 发起支付（开发模式返回待支付占位）——
        OrderMain order = orderMainService.getById(orderId);
        PayedVO payedVO = orderMainService.payForOrder(orderId, "商城代购", order.getTotalAmount());

        MallOrderSubmitVO vo = new MallOrderSubmitVO();
        vo.setOrderId(orderId);
        vo.setGoodsAmount(order.getGoodsAmount());
        vo.setServiceFee(serviceFee);
        vo.setTotalAmount(order.getTotalAmount());
        vo.setDeliveryFee(order.getTotalAmount()
                .subtract(ObjectUtil.defaultIfNull(order.getGoodsAmount(), BigDecimal.ZERO))
                .subtract(serviceFee)
                .subtract(additionalAmount));
        vo.setPayPending(ObjectUtil.defaultIfNull(payedVO.getPayPending(), false));
        vo.setPayInfo(payedVO);
        log.info("商城订单创建成功：orderId={}, goodsAmount={}, serviceFee={}, totalAmount={}", orderId, goodsAmount, serviceFee, order.getTotalAmount());
        return vo;
    }

    @Override
    public TableDataInfo<OrderMain> pageMallOrders(OrderQuery query, PageQuery pageQuery) {
        LambdaQueryWrapper<OrderMain> lqw = new LambdaQueryWrapper<OrderMain>()
                .eq(OrderMain::getOrderSource, 1)
                .orderByDesc(OrderMain::getCreateTime)
                .eq(ObjectUtil.isNotNull(query.getId()), OrderMain::getId, query.getId())
                .eq(ObjectUtil.isNotNull(query.getStatus()), OrderMain::getStatus, query.getStatus())
                .eq(ObjectUtil.isNotNull(query.getOrderUid()), OrderMain::getUserId, query.getOrderUid())
                .eq(ObjectUtil.isNotNull(query.getTakerUid()), OrderMain::getRunnerId, query.getTakerUid())
                .between(ObjectUtil.isNotNull(query.getBeginTime()), OrderMain::getCreateTime, query.getBeginTime(), query.getEndTime());
        Page<OrderMain> page = orderMainService.page(pageQuery.build(), lqw);
        return TableDataInfo.build(page);
    }

    @Override
    public MallOrderDetailVO mallOrderDetail(Long orderId) {
        OrderMain order = orderMainService.getById(orderId);
        if (ObjectUtil.isEmpty(order)) {
            throw new ServiceException("订单不存在");
        }
        List<OrderGoodsItem> items = list(new LambdaQueryWrapper<OrderGoodsItem>()
                .eq(OrderGoodsItem::getOrderId, orderId)
                .orderByAsc(OrderGoodsItem::getId));
        MallOrderDetailVO vo = new MallOrderDetailVO();
        vo.setOrder(order);
        vo.setItems(items);
        return vo;
    }

    /**
     * 商品可用性校验（存在、上架、库存充足）
     */
    private void validateGoods(Goods goods, Integer quantity) {
        if (goods == null) {
            throw new ServiceException("商品不存在");
        }
        if (!ObjectUtil.equals(goods.getStatus(), Status.OK.getCode())) {
            throw new ServiceException("商品已下架：" + goods.getName());
        }
        if (goods.getStock() != null && goods.getStock() >= 0 && goods.getStock() < quantity) {
            throw new ServiceException("库存不足：" + goods.getName());
        }
    }

    /**
     * 生成订单摘要描述（order_main.detail 上限 100 字符）
     */
    private String buildDetail(List<MallOrderLine> lines, String remark) {
        StringBuilder sb = new StringBuilder("商城代购：");
        List<String> names = new ArrayList<>();
        for (MallOrderLine line : lines) {
            names.add(line.getGoods().getName() + "×" + line.getQuantity());
        }
        sb.append(String.join("、", names));
        if (StringUtils.isNotBlank(remark)) {
            sb.append("；备注：").append(remark);
        }
        String detail = sb.toString();
        return detail.length() > 100 ? detail.substring(0, 100) : detail;
    }

}
