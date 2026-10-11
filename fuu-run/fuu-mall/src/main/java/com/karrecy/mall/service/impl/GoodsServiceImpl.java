package com.karrecy.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.karrecy.common.core.domain.PageQuery;
import com.karrecy.common.core.page.TableDataInfo;
import com.karrecy.common.enums.Status;
import com.karrecy.common.utils.StringUtils;
import com.karrecy.mall.domain.dto.GoodsQuery;
import com.karrecy.mall.domain.po.Goods;
import com.karrecy.mall.mapper.GoodsMapper;
import com.karrecy.mall.service.IGoodsService;
import org.springframework.stereotype.Service;

/**
 * 商品服务实现
 */
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements IGoodsService {

    @Override
    public TableDataInfo<Goods> pageGoods(GoodsQuery query, PageQuery pageQuery) {
        LambdaQueryWrapper<Goods> lqw = buildWrapper(query)
                .eq(query.getStatus() != null, Goods::getStatus, query.getStatus());
        Page<Goods> page = baseMapper.selectPage(pageQuery.build(), lqw);
        return TableDataInfo.build(page);
    }

    @Override
    public TableDataInfo<Goods> pageAppGoods(GoodsQuery query, PageQuery pageQuery) {
        LambdaQueryWrapper<Goods> lqw = buildWrapper(query)
                .eq(Goods::getStatus, Status.OK.getCode());
        Page<Goods> page = baseMapper.selectPage(pageQuery.build(), lqw);
        return TableDataInfo.build(page);
    }

    private LambdaQueryWrapper<Goods> buildWrapper(GoodsQuery query) {
        return new LambdaQueryWrapper<Goods>()
                .eq(query.getCategoryId() != null, Goods::getCategoryId, query.getCategoryId())
                .like(StringUtils.isNotBlank(query.getKeyword()), Goods::getName, query.getKeyword())
                .orderByAsc(Goods::getSort)
                .orderByDesc(Goods::getId);
    }

}
