package com.karrecy.admin.controller.mall;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.karrecy.admin.schedule.StatisticsService;
import com.karrecy.common.core.controller.BaseController;
import com.karrecy.common.core.domain.R;
import com.karrecy.common.core.domain.entity.StatisticsDaily;
import com.karrecy.common.enums.OrderStatus;
import com.karrecy.common.enums.Status;
import com.karrecy.mall.domain.po.Goods;
import com.karrecy.mall.domain.po.GoodsCategory;
import com.karrecy.mall.mapper.GoodsCategoryMapper;
import com.karrecy.mall.mapper.GoodsMapper;
import com.karrecy.order.domain.po.OrderMain;
import com.karrecy.order.mapper.OrderMainMapper;
import com.karrecy.system.mapper.StatisticsDailyMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理端数据统计
 * <p>summary：实时概览（订单/金额/来源分布/商品）；
 * charts：日趋势（statistics_daily 聚合表）+ 来源分布 + 分类销量 + 热销商品；
 * generate：按日补算统计数据（含今天），供演示/论文截图准备数据。</p>
 */
@RestController
@RequestMapping("/mall/admin/stat")
@RequiredArgsConstructor
@Slf4j
public class StatController extends BaseController {

    private final StatisticsService statisticsService;
    private final OrderMainMapper orderMainMapper;
    private final GoodsMapper goodsMapper;
    private final GoodsCategoryMapper goodsCategoryMapper;
    private final StatisticsDailyMapper statisticsDailyMapper;

    /**
     * 实时概览（顶部卡片）
     */
    @GetMapping("/summary")
    @SaCheckPermission("mall:stat:list")
    public R<Map<String, Object>> summary() {
        Map<String, Object> result = new LinkedHashMap<>();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        // 今日订单量与金额
        Map<String, Object> today = firstRow(orderMainMapper.selectMaps(new QueryWrapper<OrderMain>()
                .select("count(*) as cnt", "ifnull(sum(total_amount),0) as amt")
                .ge("create_time", todayStart)));
        result.put("todayOrders", toLong(today.get("cnt")));
        result.put("todayAmount", toDecimal(today.get("amt")));

        // 累计订单量与金额
        Map<String, Object> all = firstRow(orderMainMapper.selectMaps(new QueryWrapper<OrderMain>()
                .select("count(*) as cnt", "ifnull(sum(total_amount),0) as amt")));
        long totalOrders = toLong(all.get("cnt"));
        result.put("totalOrders", totalOrders);
        result.put("totalAmount", toDecimal(all.get("amt")));

        // 来源分布
        long mallOrders = orderMainMapper.selectCount(new LambdaQueryWrapper<OrderMain>()
                .eq(OrderMain::getOrderSource, 1));
        result.put("mallOrders", mallOrders);
        result.put("hallOrders", Math.max(0, totalOrders - mallOrders));

        // 完单率（%）
        long completed = orderMainMapper.selectCount(new LambdaQueryWrapper<OrderMain>()
                .eq(OrderMain::getStatus, OrderStatus.COMPLETED.getCode()));
        result.put("completedOrders", completed);
        result.put("completionRate", totalOrders == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(completed * 100.0 / totalOrders).setScale(1, RoundingMode.HALF_UP));

        // 商品侧
        long goodsCount = goodsMapper.selectCount(new LambdaQueryWrapper<Goods>());
        long onSale = goodsMapper.selectCount(new LambdaQueryWrapper<Goods>()
                .eq(Goods::getStatus, Status.OK.getCode()));
        long stockWarning = goodsMapper.selectCount(new LambdaQueryWrapper<Goods>()
                .isNotNull(Goods::getStock)
                .ge(Goods::getStock, 0)
                .le(Goods::getStock, 5));
        result.put("goodsCount", goodsCount);
        result.put("goodsOnSale", onSale);
        result.put("stockWarning", stockWarning);
        return R.ok(result);
    }

    /**
     * 图表数据：日趋势 + 来源分布 + 分类销量 + 热销商品
     */
    @GetMapping("/charts")
    @SaCheckPermission("mall:stat:list")
    public R<Map<String, Object>> charts(@RequestParam(value = "days", defaultValue = "14") Integer days) {
        int d = Math.max(7, Math.min(days, 30));
        LocalDate from = LocalDate.now().minusDays(d - 1L);
        Map<String, Object> result = new LinkedHashMap<>();

        // 1) 日趋势（statistics_daily 由定时任务/补算写入）
        List<StatisticsDaily> daily = statisticsDailyMapper.selectList(new LambdaQueryWrapper<StatisticsDaily>()
                .ge(StatisticsDaily::getCreateTime, from)
                .orderByAsc(StatisticsDaily::getCreateTime));
        List<Map<String, Object>> trend = new ArrayList<>();
        for (StatisticsDaily s : daily) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", s.getCreateTime() == null ? null : s.getCreateTime().toString());
            item.put("totalOrders", s.getTotalOrders());
            item.put("completedOrders", s.getCompletedOrders());
            item.put("totalPayment", s.getTotalPayment() == null ? BigDecimal.ZERO : s.getTotalPayment());
            item.put("newUsers", s.getNewUsers());
            item.put("completionRate", s.getCompletionRate() == null ? BigDecimal.ZERO
                    : s.getCompletionRate().multiply(BigDecimal.valueOf(100)).setScale(1, RoundingMode.HALF_UP));
            trend.add(item);
        }
        result.put("daily", trend);

        // 2) 来源分布（实时）
        long mall = orderMainMapper.selectCount(new LambdaQueryWrapper<OrderMain>()
                .eq(OrderMain::getOrderSource, 1));
        long hall = orderMainMapper.selectCount(new LambdaQueryWrapper<OrderMain>()
                .ne(OrderMain::getOrderSource, 1));
        List<Map<String, Object>> sourceSplit = new ArrayList<>();
        sourceSplit.add(itemOf("需求大厅", hall));
        sourceSplit.add(itemOf("代购商城", mall));
        result.put("sourceSplit", sourceSplit);

        // 3) 分类销量（goods 表聚合）
        Map<Long, String> categoryNames = goodsCategoryMapper.selectList(null).stream()
                .collect(Collectors.toMap(GoodsCategory::getId, GoodsCategory::getName, (a, b) -> a));
        List<Map<String, Object>> rows = goodsMapper.selectMaps(new QueryWrapper<Goods>()
                .select("category_id", "ifnull(sum(sales),0) as total_sales", "count(*) as goods_count")
                .groupBy("category_id"));
        List<Map<String, Object>> categorySales = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Long categoryId = toLong(row.get("category_id"));
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", categoryNames.getOrDefault(categoryId, "未分类"));
            item.put("value", toLong(row.get("total_sales")));
            item.put("goodsCount", toLong(row.get("goods_count")));
            categorySales.add(item);
        }
        categorySales.sort((a, b) -> Long.compare((Long) b.get("value"), (Long) a.get("value")));
        result.put("categorySales", categorySales);

        // 4) 热销商品 Top8（实时）
        List<Goods> topGoods = goodsMapper.selectList(new LambdaQueryWrapper<Goods>()
                .select(Goods::getId, Goods::getName, Goods::getSales, Goods::getStock, Goods::getPrice, Goods::getCategoryId)
                .orderByDesc(Goods::getSales)
                .last("limit 8"));
        List<Map<String, Object>> topList = new ArrayList<>();
        for (Goods g : topGoods) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", g.getId());
            item.put("name", g.getName());
            item.put("sales", g.getSales());
            item.put("stock", g.getStock());
            item.put("price", g.getPrice());
            topList.add(item);
        }
        result.put("topGoods", topList);
        return R.ok(result);
    }

    /**
     * 生成/补算统计数据（最近 days 天，含今天；覆盖已有同日数据）
     */
    @PostMapping("/generate")
    @SaCheckPermission("mall:stat:list")
    public R<String> generate(@RequestParam(value = "days", defaultValue = "7") Integer days) {
        int d = Math.max(1, Math.min(days, 31));
        int count = statisticsService.generateRecentDays(d);
        return R.ok("已生成最近 " + count + " 天统计数据（" + LocalDate.now().minusDays(d - 1L) + " ~ " + LocalDate.now() + "）");
    }

    private static Map<String, Object> firstRow(List<Map<String, Object>> rows) {
        return rows == null || rows.isEmpty() ? Collections.emptyMap() : rows.get(0);
    }

    private static Map<String, Object> itemOf(String name, Object value) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("name", name);
        item.put("value", value);
        return item;
    }

    private static Long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(value.toString());
    }

    private static BigDecimal toDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        if (value instanceof Number number) {
            return new BigDecimal(number.toString());
        }
        return new BigDecimal(value.toString());
    }
}
