package com.karrecy.admin.controller.mall;

import com.karrecy.common.core.controller.BaseController;
import com.karrecy.common.core.domain.PageQuery;
import com.karrecy.common.core.domain.R;
import com.karrecy.common.core.page.TableDataInfo;
import com.karrecy.common.exception.ServiceException;
import com.karrecy.mall.domain.dto.CartAddDTO;
import com.karrecy.mall.domain.dto.CartUpdateDTO;
import com.karrecy.mall.domain.dto.GoodsQuery;
import com.karrecy.mall.domain.dto.MallOrderSubmitDTO;
import com.karrecy.mall.domain.po.Goods;
import com.karrecy.mall.domain.po.GoodsCategory;
import com.karrecy.mall.domain.vo.CartItemVO;
import com.karrecy.mall.domain.vo.MallOrderSubmitVO;
import com.karrecy.mall.service.ICartService;
import com.karrecy.mall.service.IGoodsCategoryService;
import com.karrecy.mall.service.IGoodsService;
import com.karrecy.mall.service.IMallOrderService;
import com.karrecy.order.domain.dto.OrderQuery;
import com.karrecy.order.domain.po.OrderMain;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 代购商城（小程序端接口）
 */
@RestController
@RequestMapping("/mall")
@RequiredArgsConstructor
@Slf4j
public class MallController extends BaseController {

    private final IGoodsCategoryService categoryService;
    private final IGoodsService goodsService;
    private final ICartService cartService;
    private final IMallOrderService mallOrderService;

    /**
     * 商品分类列表（仅启用）
     */
    @GetMapping("/category/list")
    public R<List<GoodsCategory>> categoryList() {
        return R.ok(categoryService.listEnabled());
    }

    /**
     * 商品分页（仅上架）
     */
    @GetMapping("/goods/list")
    public TableDataInfo<Goods> goodsList(GoodsQuery query, PageQuery pageQuery) {
        return goodsService.pageAppGoods(query, pageQuery);
    }

    /**
     * 商品详情
     */
    @GetMapping("/goods/detail/{id}")
    public R<Goods> goodsDetail(@PathVariable Long id) {
        Goods goods = goodsService.getById(id);
        if (goods == null) {
            throw new ServiceException("商品不存在");
        }
        return R.ok(goods);
    }

    /**
     * 购物车列表
     */
    @GetMapping("/cart/list")
    public R<List<CartItemVO>> cartList() {
        return R.ok(cartService.listItems());
    }

    /**
     * 加入购物车
     */
    @PostMapping("/cart/add")
    public R<Void> cartAdd(@Validated @RequestBody CartAddDTO dto) {
        cartService.add(dto);
        return R.ok();
    }

    /**
     * 修改购物车数量
     */
    @PostMapping("/cart/update")
    public R<Void> cartUpdate(@Validated @RequestBody CartUpdateDTO dto) {
        cartService.updateQuantity(dto);
        return R.ok();
    }

    /**
     * 删除购物车条目
     */
    @PostMapping("/cart/remove/{id}")
    public R<Void> cartRemove(@PathVariable Long id) {
        cartService.remove(id);
        return R.ok();
    }

    /**
     * 清空购物车
     */
    @PostMapping("/cart/clear")
    public R<Void> cartClear() {
        cartService.clear();
        return R.ok();
    }

    /**
     * 提交商城订单（购物车结算 / 立即购买）
     */
    @PostMapping("/order/submit")
    public R<MallOrderSubmitVO> orderSubmit(@Validated @RequestBody MallOrderSubmitDTO dto) {
        return R.ok(mallOrderService.submitMall(dto));
    }

    /**
     * 我的商城订单分页
     */
    @GetMapping("/order/list")
    public TableDataInfo<OrderMain> orderList(OrderQuery query, PageQuery pageQuery) {
        query.setOrderUid(getUserId());
        return mallOrderService.pageMallOrders(query, pageQuery);
    }

}
