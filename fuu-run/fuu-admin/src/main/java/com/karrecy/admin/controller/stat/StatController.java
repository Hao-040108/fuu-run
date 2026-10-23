package com.karrecy.admin.controller.stat;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.karrecy.admin.schedule.StatisticsService;
import com.karrecy.common.core.controller.BaseController;
import com.karrecy.common.core.domain.R;
import com.karrecy.common.core.domain.entity.StatisticsDaily;
import com.karrecy.common.core.domain.entity.UserWx;
import com.karrecy.common.enums.OrderStatus;
import com.karrecy.common.enums.Status;
import com.karrecy.order.domain.po.OrderAppeal;
import com.karrecy.order.domain.po.OrderMain;
import com.karrecy.order.mapper.OrderAppealMapper;
import com.karrecy.order.mapper.OrderMainMapper;
import com.karrecy.system.mapper.StatisticsDailyMapper;
import com.karrecy.system.mapper.UserWxMapper;
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
 * 管理端任务统计（统一任务域：代拿 / 代购 / 其他服务）
 */
@RestController
@RequestMapping("/order/admin/stat")
@RequiredArgsConstructor
@Slf4j
public class StatController extends BaseController {

    private final StatisticsService statisticsService;
    private final OrderMainMapper orderMainMapper;
    private final OrderAppealMapper orderAppealMapper;
    private final UserWxMapper userWxMapper;
    private final StatisticsDailyMapper statisticsDailyMapper;

    /**
     * 实时概览（顶部卡片）
     */
    @GetMapping("/summary")
    @SaCheckPermission("order:order:list:hall")
    public R<Map<String, Object>> summary() {
        Map<String, Object> result = new LinkedHashMap<>();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        Map<String, Object> today = firstRow(orderMainMapper.selectMaps(new QueryWrapper<OrderMain>()
                .select("count(*) as cnt", "ifnull(sum(total_amount),0) as amt")
                .ge("create_time", todayStart)));
        result.put("todayTasks", toLong(today.get("cnt")));
        result.put("todayAmount", toDecimal(today.get("amt")));

        Map<String, Object> all = firstRow(orderMainMapper.selectMaps(new QueryWrapper<OrderMain>()
                .select("count(*) as cnt", "ifnull(sum(total_amount),0) as amt")));
        long totalTasks = toLong(all.get("cnt"));
        result.put("totalTasks", totalTasks);
        result.put("totalAmount", toDecimal(all.get("amt")));

        long completed = orderMainMapper.selectCount(new LambdaQueryWrapper<OrderMain>()
                .eq(OrderMain::getStatus, OrderStatus.COMPLETED.getCode()));
        result.put("completedTasks", completed);
        result.put("completionRate", totalTasks == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(completed * 100.0 / totalTasks).setScale(1, RoundingMode.HALF_UP));

        long pendingAccept = orderMainMapper.selectCount(new LambdaQueryWrapper<OrderMain>()
                .eq(OrderMain::getStatus, OrderStatus.PENDING_ACCEPTANCE.getCode()));
        result.put("pendingAccept", pendingAccept);

        long disputePending = orderAppealMapper.selectCount(new LambdaQueryWrapper<OrderAppeal>()
                .eq(OrderAppeal::getAppealStatus, 2));
        result.put("disputePending", disputePending);

        long runnerCount = userWxMapper.selectCount(new LambdaQueryWrapper<UserWx>()
                .eq(UserWx::getIsRunner, Status.OK.getCode()));
        result.put("runnerCount", runnerCount);
        return R.ok(result);
    }

    /**
     * 图表数据：日趋势 + 任务类型分布 + 跑腿员接单榜
     */
    @GetMapping("/charts")
    @SaCheckPermission("order:order:list:hall")
    public R<Map<String, Object>> charts(@RequestParam(value = "days", defaultValue = "14") Integer days) {
        int d = Math.max(7, Math.min(days, 30));
        LocalDate from = LocalDate.now().minusDays(d - 1L);
        Map<String, Object> result = new LinkedHashMap<>();

        List<StatisticsDaily> daily = statisticsDailyMapper.selectList(new LambdaQueryWrapper<StatisticsDaily>()
                .ge(StatisticsDaily::getCreateTime, from)
                .orderByAsc(StatisticsDaily::getCreateTime));
        List<Map<String, Object>> trend = new ArrayList<>();
        for (StatisticsDaily s : daily) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", s.getCreateTime() == null ? null : s.getCreateTime().toString());
            item.put("totalTasks", s.getTotalOrders());
            item.put("completedTasks", s.getCompletedOrders());
            item.put("totalPayment", s.getTotalPayment() == null ? BigDecimal.ZERO : s.getTotalPayment());
            item.put("newUsers", s.getNewUsers());
            item.put("completionRate", s.getCompletionRate() == null ? BigDecimal.ZERO
                    : s.getCompletionRate().multiply(BigDecimal.valueOf(100)).setScale(1, RoundingMode.HALF_UP));
            trend.add(item);
        }
        result.put("daily", trend);

        // 任务类型分布（0 代拿 / 1 代购 / 2 其他服务）
        String[] typeNames = {"代拿", "代购", "其他服务"};
        List<Map<String, Object>> typeSplit = new ArrayList<>();
        for (int type = 0; type < 3; type++) {
            long count = orderMainMapper.selectCount(new LambdaQueryWrapper<OrderMain>()
                    .eq(OrderMain::getServiceType, type));
            typeSplit.add(itemOf(typeNames[type], count));
        }
        result.put("typeSplit", typeSplit);

        // 跑腿员接单榜（已完成任务数）
        List<Map<String, Object>> rows = orderMainMapper.selectMaps(new QueryWrapper<OrderMain>()
                .select("runner_id", "count(*) as task_count")
                .isNotNull("runner_id")
                .eq("status", OrderStatus.COMPLETED.getCode())
                .groupBy("runner_id")
                .orderByDesc("task_count")
                .last("limit 8"));
        List<Long> runnerIds = rows.stream().map((r) -> toLong(r.get("runner_id"))).collect(Collectors.toList());
        Map<Long, String> runnerNames = Collections.emptyMap();
        if (!runnerIds.isEmpty()) {
            List<UserWx> wxUsers = userWxMapper.selectList(new LambdaQueryWrapper<UserWx>()
                    .in(UserWx::getUid, runnerIds));
            runnerNames = wxUsers.stream().collect(Collectors.toMap(
                    UserWx::getUid,
                    (u) -> u.getNickname() == null ? ("跑腿员" + u.getUid()) : u.getNickname(),
                    (a, b) -> a));
        }
        List<Map<String, Object>> topRunners = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Long runnerId = toLong(row.get("runner_id"));
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("uid", runnerId);
            item.put("name", runnerNames.getOrDefault(runnerId, "跑腿员" + runnerId));
            item.put("value", toLong(row.get("task_count")));
            topRunners.add(item);
        }
        result.put("topRunners", topRunners);
        return R.ok(result);
    }

    /**
     * 生成/补算统计数据（最近 days 天，含今天）
     */
    @PostMapping("/generate")
    @SaCheckPermission("order:order:list:hall")
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
