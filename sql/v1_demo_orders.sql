-- ============================================================
-- 演示数据脚本：生成最近 30 天的校园互助任务与支付记录
-- 三类任务：0 代拿 / 1 代购 / 2 其他服务
-- 说明：
--   1) 可重复执行（幂等）：以 detail 前缀 '[演示]' 识别并重建
--   2) 只影响本地开发库 fuudb，不包含任何真实数据
--   3) 执行后可在管理端"数据统计"页补算 statistics_daily
-- 用法：
--   mysql --no-defaults --default-character-set=utf8mb4 --host=127.0.0.1 --port=3307 -uroot fuudb -e "source sql/v1_demo_orders.sql"
-- ============================================================
USE fuudb;

-- 1. 清理旧的演示任务与支付记录
DELETE p FROM order_payment p
JOIN order_main o ON o.id = p.order_id
WHERE o.detail LIKE '[演示]%';

DELETE FROM order_main WHERE detail LIKE '[演示]%';

-- 2. 最近 30 天任务（每天 3 条，共 90 条；代拿/代购/其他服务各三分之一）
INSERT INTO order_main
    (id, school_id, service_type, tag, detail, is_timed, auto_cancel_ttl, gender,
     estimated_price, goods_amount, total_amount, status, create_time, user_id, runner_id, end_address)
WITH RECURSIVE seq AS (
    SELECT 0 AS n UNION ALL SELECT n + 1 FROM seq WHERE n < 89
)
SELECT
    91000001 + n,
    4,
    MOD(n, 3),
    CASE MOD(n, 3) WHEN 0 THEN '快递' WHEN 1 THEN '代购' ELSE '打印' END,
    CONCAT('[演示]任务-', LPAD(n, 2, '0')),
    0, 900, 2,
    ROUND(3 + MOD(n * 7, 35) + MOD(n, 3) * 0.5, 2),
    IF(MOD(n, 3) = 1, ROUND(5 + MOD(n * 3, 20), 2), 0),
    ROUND(3 + MOD(n * 7, 35) + MOD(n, 3) * 0.5 + IF(MOD(n, 3) = 1, 5 + MOD(n * 3, 20), 0), 2),
    CASE MOD(n, 20) WHEN 0 THEN 0 WHEN 1 THEN 5 WHEN 2 THEN 1 WHEN 3 THEN 1 WHEN 4 THEN 4 ELSE 10 END,
    LEAST(NOW(), TIMESTAMPADD(HOUR, 8 + MOD(n, 12), TIMESTAMPADD(DAY, -FLOOR(n / 3), CURDATE()))),
    100,
    CASE WHEN MOD(n, 20) IN (0, 1, 2, 3) THEN NULL ELSE 200 END,
    JSON_OBJECT('title', '学生公寓', 'detail', CONCAT('A', MOD(n, 9) + 1, '栋 5', MOD(n, 9), '室'))
FROM seq;

-- 3. 支付记录：待支付/已取消不支付，其余已支付（支付时间=任务创建后3分钟）
INSERT INTO order_payment
    (order_id, additional_amount, actual_payment, payment_status, payment_time, is_couponed)
SELECT
    o.id, 0, o.total_amount,
    IF(o.status IN (0, 5), 0, 1),
    IF(o.status IN (0, 5), NULL, TIMESTAMPADD(MINUTE, 3, o.create_time)),
    0
FROM order_main o
WHERE o.detail LIKE '[演示]%';
