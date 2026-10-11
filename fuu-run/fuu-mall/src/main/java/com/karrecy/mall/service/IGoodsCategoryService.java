package com.karrecy.mall.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.karrecy.mall.domain.po.GoodsCategory;

import java.util.List;

/**
 * 商品分类服务
 */
public interface IGoodsCategoryService extends IService<GoodsCategory> {

    /**
     * 启用中的分类列表（小程序端）
     */
    List<GoodsCategory> listEnabled();

}
