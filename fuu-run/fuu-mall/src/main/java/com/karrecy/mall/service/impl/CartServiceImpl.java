package com.karrecy.mall.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.karrecy.common.enums.Status;
import com.karrecy.common.exception.ServiceException;
import com.karrecy.common.helper.LoginHelper;
import com.karrecy.mall.domain.dto.CartAddDTO;
import com.karrecy.mall.domain.dto.CartUpdateDTO;
import com.karrecy.mall.domain.po.CartItem;
import com.karrecy.mall.domain.po.Goods;
import com.karrecy.mall.domain.vo.CartItemVO;
import com.karrecy.mall.mapper.CartItemMapper;
import com.karrecy.mall.mapper.GoodsMapper;
import com.karrecy.mall.service.ICartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 购物车服务实现
 */
@Service
@RequiredArgsConstructor
public class CartServiceImpl extends ServiceImpl<CartItemMapper, CartItem> implements ICartService {

    private final GoodsMapper goodsMapper;

    @Override
    public void add(CartAddDTO dto) {
        Long uid = LoginHelper.getUserId();
        Goods goods = goodsMapper.selectById(dto.getGoodsId());
        if (goods == null || !ObjectUtil.equals(goods.getStatus(), Status.OK.getCode())) {
            throw new ServiceException("商品不存在或已下架");
        }
        int quantity = ObjectUtil.defaultIfNull(dto.getQuantity(), 1);
        if (quantity <= 0) {
            quantity = 1;
        }
        CartItem exist = getOne(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUid, uid)
                .eq(CartItem::getGoodsId, dto.getGoodsId()));
        if (exist == null) {
            save(new CartItem()
                    .setUid(uid)
                    .setGoodsId(dto.getGoodsId())
                    .setQuantity(quantity)
                    .setCreateTime(LocalDateTime.now())
                    .setUpdateTime(LocalDateTime.now()));
        } else {
            exist.setQuantity(exist.getQuantity() + quantity);
            exist.setUpdateTime(LocalDateTime.now());
            updateById(exist);
        }
    }

    @Override
    public void updateQuantity(CartUpdateDTO dto) {
        Long uid = LoginHelper.getUserId();
        CartItem item = getById(dto.getId());
        if (item == null || !ObjectUtil.equals(item.getUid(), uid)) {
            throw new ServiceException("购物车条目不存在");
        }
        int quantity = ObjectUtil.defaultIfNull(dto.getQuantity(), 1);
        if (quantity <= 0) {
            throw new ServiceException("数量必须大于 0");
        }
        item.setQuantity(quantity);
        item.setUpdateTime(LocalDateTime.now());
        updateById(item);
    }

    @Override
    public void remove(Long id) {
        Long uid = LoginHelper.getUserId();
        CartItem item = getById(id);
        if (item == null || !ObjectUtil.equals(item.getUid(), uid)) {
            throw new ServiceException("购物车条目不存在");
        }
        removeById(id);
    }

    @Override
    public void clear() {
        Long uid = LoginHelper.getUserId();
        remove(new LambdaQueryWrapper<CartItem>().eq(CartItem::getUid, uid));
    }

    @Override
    public List<CartItemVO> listItems() {
        Long uid = LoginHelper.getUserId();
        List<CartItem> items = list(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUid, uid)
                .orderByDesc(CartItem::getCreateTime));
        if (ObjectUtil.isEmpty(items)) {
            return new ArrayList<>();
        }
        List<Long> goodsIds = items.stream().map(CartItem::getGoodsId).collect(Collectors.toList());
        Map<Long, Goods> goodsMap = goodsMapper.selectBatchIds(goodsIds).stream()
                .collect(Collectors.toMap(Goods::getId, Function.identity()));
        List<CartItemVO> result = new ArrayList<>();
        for (CartItem item : items) {
            Goods goods = goodsMap.get(item.getGoodsId());
            if (goods == null) {
                continue;
            }
            BigDecimal quantity = BigDecimal.valueOf(item.getQuantity());
            CartItemVO vo = new CartItemVO();
            vo.setId(item.getId());
            vo.setGoodsId(goods.getId());
            vo.setName(goods.getName());
            vo.setImage(goods.getImage());
            vo.setUnit(goods.getUnit());
            vo.setPrice(goods.getPrice());
            vo.setPurchaseFee(goods.getPurchaseFee());
            vo.setQuantity(item.getQuantity());
            vo.setStock(goods.getStock());
            vo.setGoodsStatus(goods.getStatus());
            vo.setSubtotal(goods.getPrice().multiply(quantity));
            vo.setFeeSubtotal(ObjectUtil.defaultIfNull(goods.getPurchaseFee(), BigDecimal.ZERO).multiply(quantity));
            result.add(vo);
        }
        return result;
    }

}
