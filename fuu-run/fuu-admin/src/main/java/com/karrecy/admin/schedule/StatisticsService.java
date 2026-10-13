package com.karrecy.admin.schedule;

import com.karrecy.common.constant.CacheNames;
import com.karrecy.common.utils.redis.RedisUtils;
import com.karrecy.order.service.IOrderAppealService;
import com.karrecy.order.service.IOrderMainService;
import com.karrecy.order.service.IOrderPaymentService;
import com.karrecy.payment.service.ICapitalFlowService;
import com.karrecy.common.core.domain.entity.StatisticsDaily;
import com.karrecy.system.mapper.StatisticsDailyMapper;
import com.karrecy.system.service.IUserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * 自动统计数据
 */
@Component
@RequiredArgsConstructor
public class StatisticsService {

    private final IUserService userService;
    private final IOrderMainService orderMainService;

    private final StatisticsDailyMapper statisticsDailyMapper;

    @Scheduled(cron = "0 0 1 * * ?") // 每天凌晨1点执行
    public void generateDailyStatistics() {
        LocalDate today = LocalDate.now();
        today = today.minusDays(1);
        LocalDate lastDay = today.minusDays(2);
        StatisticsDaily statistics = new StatisticsDaily();
        statistics.setCreateTime(lastDay);

        // 统计订单数据
        orderMainService.calculateOrderStatistics(statistics,lastDay,today);

        // 统计金额数据
        orderMainService.calculateFinancialStatistics(statistics,lastDay,today);

        // 统计访问数据
        calculateVisitStatistics(statistics,lastDay);

        // 统计用户数据
        userService.calculateUserStatistics(statistics,lastDay,today);

        // 保存统计结果
        statisticsDailyMapper.insert(statistics);
    }

    /**
     * 生成指定日期的统计行（存在则覆盖）
     * <p>按"当天 00:00 ~ 次日 00:00"口径统计订单/金额/用户数据，
     * 供管理端"数据统计"页补算历史数据或演示环境生成图表数据使用。</p>
     *
     * @param date 需要统计的日期
     */
    public void generateForDate(LocalDate date) {
        StatisticsDaily statistics = new StatisticsDaily();
        LocalDate nextDay = date.plusDays(1);

        // 统计订单数据
        orderMainService.calculateOrderStatistics(statistics, date, nextDay);
        // 统计金额数据
        orderMainService.calculateFinancialStatistics(statistics, date, nextDay);
        // 统计访问数据（Redis 中的当日活跃/访客计数）
        calculateVisitStatistics(statistics, date);
        // 统计用户数据
        userService.calculateUserStatistics(statistics, date, nextDay);

        // 覆盖式写入：先删除同日期旧数据
        statisticsDailyMapper.delete(new LambdaQueryWrapper<StatisticsDaily>()
                .eq(StatisticsDaily::getCreateTime, date));

        // 保存统计结果（createTime 记录统计归属日期）
        statistics.setCreateTime(date);
        statisticsDailyMapper.insert(statistics);
    }

    /**
     * 批量补算最近 days 天（含今天）的统计数据
     *
     * @param days 天数（1~31）
     * @return 实际生成的天数
     */
    public int generateRecentDays(int days) {
        int count = 0;
        LocalDate today = LocalDate.now();
        for (int i = days - 1; i >= 0; i--) {
            generateForDate(today.minusDays(i));
            count++;
        }
        return count;
    }

    private void calculateVisitStatistics(StatisticsDaily statistics, LocalDate lastDay) {
        long auCountRunner = RedisUtils.getAUCount(lastDay.toString(),CacheNames.DAILY_AU_RUNNER);
        long auCountUser = RedisUtils.getAUCount(lastDay.toString(),CacheNames.DAILY_AU_USER);

        long uvCount = RedisUtils.getUVCount(lastDay.toString());
        long totalVisits = RedisUtils.getAtomicValue(CacheNames.DAILY_TOTAL_VISITS);

        statistics.setActiveRunners((int) auCountRunner);
        statistics.setActiveUsers((int) auCountUser);
        statistics.setTotalVisits((int) totalVisits);
        statistics.setUniqueVisitors((int) uvCount);
    }

}
