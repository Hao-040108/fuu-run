package com.karrecy.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.karrecy.common.enums.Status;
import com.karrecy.mall.domain.po.GoodsCategory;
import com.karrecy.mall.mapper.GoodsCategoryMapper;
import com.karrecy.mall.service.IGoodsCategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品分类服务实现
 */
@Service
public class GoodsCategoryServiceImpl extends ServiceImpl<GoodsCategoryMapper, GoodsCategory> implements IGoodsCategoryService {

    @Override
    public List<GoodsCategory> listEnabled() {
        return list(new LambdaQueryWrapper<GoodsCategory>()
                .eq(GoodsCategory::getStatus, Status.OK.getCode())
                .orderByAsc(GoodsCategory::getSort)
                .orderByAsc(GoodsCategory::getId));
    }

}
