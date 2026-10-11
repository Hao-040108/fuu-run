package com.karrecy.admin.controller.mall;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.karrecy.common.core.controller.BaseController;
import com.karrecy.common.core.domain.PageQuery;
import com.karrecy.common.core.domain.R;
import com.karrecy.common.core.page.TableDataInfo;
import com.karrecy.common.exception.ServiceException;
import com.karrecy.mall.domain.dto.GoodsQuery;
import com.karrecy.mall.domain.po.Goods;
import com.karrecy.mall.domain.po.GoodsCategory;
import com.karrecy.mall.domain.vo.MallOrderDetailVO;
import com.karrecy.mall.service.IGoodsCategoryService;
import com.karrecy.mall.service.IGoodsService;
import com.karrecy.mall.service.IMallOrderService;
import com.karrecy.order.domain.dto.OrderQuery;
import com.karrecy.order.domain.po.OrderMain;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 代购商城（管理端接口）
 */
@RestController
@RequestMapping("/mall/admin")
@RequiredArgsConstructor
@Slf4j
public class MallAdminController extends BaseController {

    private final IGoodsCategoryService categoryService;
    private final IGoodsService goodsService;
    private final IMallOrderService mallOrderService;

    // ==================== 分类管理 ====================

    /**
     * 分类列表（含停用）
     */
    @GetMapping("/category/list")
    @SaCheckPermission("mall:category:list")
    public R<List<GoodsCategory>> categoryList() {
        return R.ok(categoryService.list(new LambdaQueryWrapper<GoodsCategory>()
                .orderByAsc(GoodsCategory::getSort)
                .orderByAsc(GoodsCategory::getId)));
    }

    /**
     * 新增分类
     */
    @PostMapping("/category/add")
    @SaCheckPermission("mall:category:add")
    public R<Void> categoryAdd(@RequestBody GoodsCategory category) {
        category.setId(null);
        category.setCreateTime(LocalDateTime.now());
        category.setCreateId(getUserId());
        return toAjax(categoryService.save(category));
    }

    /**
     * 修改分类
     */
    @PostMapping("/category/edit")
    @SaCheckPermission("mall:category:edit")
    public R<Void> categoryEdit(@RequestBody GoodsCategory category) {
        if (category.getId() == null) {
            throw new ServiceException("分类id不能为空");
        }
        category.setUpdateTime(LocalDateTime.now());
        category.setUpdateId(getUserId());
        return toAjax(categoryService.updateById(category));
    }

    /**
     * 删除分类（存在关联商品时禁止删除）
     */
    @PostMapping("/category/remove/{id}")
    @SaCheckPermission("mall:category:remove")
    public R<Void> categoryRemove(@PathVariable Long id) {
        long count = goodsService.count(new LambdaQueryWrapper<Goods>().eq(Goods::getCategoryId, id));
        if (count > 0) {
            throw new ServiceException("该分类下仍有商品，无法删除");
        }
        return toAjax(categoryService.removeById(id));
    }

    // ==================== 商品管理 ====================

    /**
     * 商品分页（含下架）
     */
    @GetMapping("/goods/list")
    @SaCheckPermission("mall:goods:list")
    public TableDataInfo<Goods> goodsList(GoodsQuery query, PageQuery pageQuery) {
        return goodsService.pageGoods(query, pageQuery);
    }

    /**
     * 新增商品
     */
    @PostMapping("/goods/add")
    @SaCheckPermission("mall:goods:add")
    public R<Void> goodsAdd(@RequestBody Goods goods) {
        goods.setId(null);
        if (goods.getStatus() == null) {
            goods.setStatus(1);
        }
        if (goods.getStock() == null) {
            goods.setStock(-1);
        }
        goods.setSales(0);
        goods.setCreateTime(LocalDateTime.now());
        goods.setCreateId(getUserId());
        return toAjax(goodsService.save(goods));
    }

    /**
     * 修改商品
     */
    @PostMapping("/goods/edit")
    @SaCheckPermission("mall:goods:edit")
    public R<Void> goodsEdit(@RequestBody Goods goods) {
        if (goods.getId() == null) {
            throw new ServiceException("商品id不能为空");
        }
        goods.setUpdateTime(LocalDateTime.now());
        goods.setUpdateId(getUserId());
        return toAjax(goodsService.updateById(goods));
    }

    /**
     * 删除商品
     */
    @PostMapping("/goods/remove/{id}")
    @SaCheckPermission("mall:goods:remove")
    public R<Void> goodsRemove(@PathVariable Long id) {
        return toAjax(goodsService.removeById(id));
    }

    /**
     * 上架/下架
     */
    @PostMapping("/goods/status")
    @SaCheckPermission("mall:goods:edit")
    public R<Void> goodsStatus(@RequestParam Long id, @RequestParam Integer status) {
        Goods goods = new Goods();
        goods.setId(id);
        goods.setStatus(status);
        goods.setUpdateTime(LocalDateTime.now());
        goods.setUpdateId(getUserId());
        return toAjax(goodsService.updateById(goods));
    }

    // ==================== 订单管理 ====================

    /**
     * 商城订单分页
     */
    @GetMapping("/order/list")
    @SaCheckPermission("mall:order:list")
    public TableDataInfo<OrderMain> orderList(OrderQuery query, PageQuery pageQuery) {
        return mallOrderService.pageMallOrders(query, pageQuery);
    }

    /**
     * 商城订单详情（订单 + 商品明细）
     */
    @GetMapping("/order/detail/{orderId}")
    @SaCheckPermission("mall:order:detail")
    public R<MallOrderDetailVO> orderDetail(@PathVariable Long orderId) {
        return R.ok(mallOrderService.mallOrderDetail(orderId));
    }

}
