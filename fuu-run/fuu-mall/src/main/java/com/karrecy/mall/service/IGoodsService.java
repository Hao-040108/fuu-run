package com.karrecy.mall.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.karrecy.common.core.domain.PageQuery;
import com.karrecy.common.core.page.TableDataInfo;
import com.karrecy.mall.domain.dto.GoodsQuery;
import com.karrecy.mall.domain.po.Goods;

/**
 * 商品服务
 */
public interface IGoodsService extends IService<Goods> {

    /**
     * 商品分页（管理端，含下架商品）
     */
    TableDataInfo<Goods> pageGoods(GoodsQuery query, PageQuery pageQuery);

    /**
     * 商品分页（小程序端，仅上架商品）
     */
    TableDataInfo<Goods> pageAppGoods(GoodsQuery query, PageQuery pageQuery);

}
