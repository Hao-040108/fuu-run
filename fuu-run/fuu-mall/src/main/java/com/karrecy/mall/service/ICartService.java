package com.karrecy.mall.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.karrecy.mall.domain.dto.CartAddDTO;
import com.karrecy.mall.domain.dto.CartUpdateDTO;
import com.karrecy.mall.domain.po.CartItem;
import com.karrecy.mall.domain.vo.CartItemVO;

import java.util.List;

/**
 * 购物车服务
 */
public interface ICartService extends IService<CartItem> {

    /**
     * 加入购物车（已存在则累加数量）
     */
    void add(CartAddDTO dto);

    /**
     * 修改数量
     */
    void updateQuantity(CartUpdateDTO dto);

    /**
     * 删除条目
     */
    void remove(Long id);

    /**
     * 清空当前用户购物车
     */
    void clear();

    /**
     * 当前用户购物车列表（含商品信息）
     */
    List<CartItemVO> listItems();

}
