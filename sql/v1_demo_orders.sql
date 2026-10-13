-- ============================================================
-- 演示数据脚本：生成最近 30 天的订单/支付/商品快照数据
-- 用途：管理端"数据统计"页图表、订单列表分页、论文界面截图
-- 说明：
--   1) 可重复执行（幂等）：以 detail 前缀 '[演示]' 识别演示订单并重建
--   2) 只影响本地开发库 fuudb，不包含任何真实数据
--   3) 执行后可在管理端"数据统计"页点击"生成/补算统计"写 statistics_daily
-- 用法：
--   mysql --no-defaults --default-character-set=utf8mb4 --host=127.0.0.1 --port=3307 -uroot fuudb -e "source sql/v1_demo_orders.sql"
-- ============================================================
USE fuudb;

-- 1. 清理旧的演示数据（订单/支付/商品明细）
DELETE p FROM order_payment p
JOIN order_main o ON o.id = p.order_id
WHERE o.detail LIKE '[演示]%';

DELETE i FROM order_goods_item i
JOIN order_main o ON o.id = i.order_id
WHERE o.detail LIKE '[演示]%';

DELETE FROM order_main WHERE detail LIKE '[演示]%';

-- 2. 生成最近 30 天订单（每天 3 单，90 单；25% 为商城代购单）
INSERT INTO order_main
    (id, school_id, service_type, order_source, tag, detail, is_timed, auto_cancel_ttl, gender,
     estimated_price, goods_amount, total_amount, status, create_time, user_id, runner_id, end_address)
WITH RECURSIVE seq AS (
    SELECT 0 AS n UNION ALL SELECT n + 1 FROM seq WHERE n < 89
)
SELECT
    91000001 + n,
    4,
    MOD(n, 3),
    IF(MOD(n, 4) = 0, 1, 0),
    CASE MOD(n, 4) WHEN 0 THEN '商城代购' WHEN 1 THEN '快递' WHEN 2 THEN '外卖' ELSE '文件' END,
    CONCAT('[演示]代购订单-', LPAD(n, 2, '0')),
    0, 900, 2,
    ROUND(3 + MOD(n * 7, 35) + MOD(n, 3) * 0.5, 2),
    IF(MOD(n, 4) = 0, ROUND(5 + MOD(n * 3, 20), 2), 0),
    ROUND(3 + MOD(n * 7, 35) + MOD(n, 3) * 0.5 + IF(MOD(n, 4) = 0, 5 + MOD(n * 3, 20), 0), 2),
    CASE MOD(n, 20) WHEN 0 THEN 0 WHEN 1 THEN 5 WHEN 2 THEN 1 WHEN 3 THEN 1 WHEN 4 THEN 4 ELSE 10 END,
    LEAST(NOW(), TIMESTAMPADD(HOUR, 8 + MOD(n, 12), TIMESTAMPADD(DAY, -FLOOR(n / 3), CURDATE()))),
    226,
    CASE WHEN MOD(n, 20) IN (0, 1, 2, 3) THEN NULL ELSE 200 END,
    JSON_OBJECT('title', '学生公寓', 'detail', CONCAT('A', MOD(n, 9) + 1, '栋 5', MOD(n, 9), '室'))
FROM seq;

-- 3. 支付记录：待支付/已取消不支付，其余为已支付（支付时间=下单后3分钟）
INSERT INTO order_payment
    (order_id, additional_amount, actual_payment, payment_status, payment_time, is_couponed)
SELECT
    o.id, 0, o.total_amount,
    IF(o.status IN (0, 5), 0, 1),
    IF(o.status IN (0, 5), NULL, TIMESTAMPADD(MINUTE, 3, o.create_time)),
    0
FROM order_main o
WHERE o.detail LIKE '[演示]%';

-- 4. 商城代购单的商品明细快照（关联 1/2/3 号商品，即零食分类）
INSERT INTO order_goods_item
    (order_id, goods_id, goods_name, goods_image, price, quantity, purchase_fee, create_time)
SELECT
    o.id, g.id, g.name, g.image, g.price, 1, IFNULL(g.purchase_fee, 0), o.create_time
FROM order_main o
JOIN goods g ON g.id = 1 + MOD(o.id, 3)
WHERE o.detail LIKE '[演示]%' AND o.order_source = 1;

-- 5. 商品销量重排（确定性赋值，保证分类销量图表有梯度）
UPDATE goods SET sales = 8 + MOD(id * 7, 40) WHERE id <= 10;
