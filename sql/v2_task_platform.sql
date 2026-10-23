-- ============================================================
-- v2 迁移：校园互助任务众包平台（取消商城 / 四类角色 / 两方分账）
-- 适用：已执行过 v1_campus_proxy.sql 的开发库
-- 幂等：可重复执行
-- 用法：
--   mysql --no-defaults --default-character-set=utf8mb4 --host=127.0.0.1 --port=3307 -uroot fuudb -e "source sql/v2_task_platform.sql"
-- ============================================================
USE fuudb;

-- 1. 删除商城相关表
DROP TABLE IF EXISTS `order_goods_item`;
DROP TABLE IF EXISTS `cart_item`;
DROP TABLE IF EXISTS `goods`;
DROP TABLE IF EXISTS `goods_category`;

-- 2. 删除订单来源字段（先判断是否存在，保证幂等）
SET @has_order_source := (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = 'fuudb' AND table_name = 'order_main' AND column_name = 'order_source'
);
SET @drop_order_source := IF(@has_order_source > 0,
    'ALTER TABLE `order_main` DROP COLUMN `order_source`',
    'SELECT 1');
PREPARE stmt_drop_source FROM @drop_order_source;
EXECUTE stmt_drop_source;
DEALLOCATE PREPARE stmt_drop_source;

-- 3. 两方分账：原代理分成并入平台，学校不再绑定代理
UPDATE `school`
SET `profit_plat` = `profit_plat` + IFNULL(`profit_agent`, 0),
    `profit_agent` = 0,
    `belong_uid` = 100
WHERE `profit_agent` IS NOT NULL;

-- 4. 移除校区代理角色权限（role_id=1 对应 user_type=1）
DELETE FROM `role_perm` WHERE `role_id` = 1;

-- 5. 纠纷调解员权限（role_id=6 对应 user_type=6）
INSERT INTO `role_perm` (`role_id`, `perm_id`)
SELECT 6, `id` FROM `perm`
WHERE `perms` IN ('getInfo','order:appeal:view','order:appeal:list','order:appeal:get','order:appeal:edit','order:order:detail','order:order:phone','order:order:view','order:order:list:hall')
ON DUPLICATE KEY UPDATE `role_id` = `role_id`;

-- 6. 调解员账号 mediator/admin123（uid=300）
INSERT INTO `user` (`uid`, `device_type`, `create_time`, `login_time`, `login_ip`, `login_region`, `user_type`, `create_id`, `update_time`, `update_id`)
VALUES (300, 0, NOW(), NOW(), '127.0.0.1', '本地', 6, 100, NOW(), 100)
ON DUPLICATE KEY UPDATE `user_type` = 6, `update_time` = NOW();

INSERT INTO `user_pc` (`uid`, `username`, `password`, `phone`, `name`, `sex`, `status`, `avatar`)
SELECT 300, 'mediator', `password`, '13700000000', '纠纷调解员', 2, 1, ''
FROM `user_pc` WHERE `username` = 'admin'
ON DUPLICATE KEY UPDATE `password` = VALUES(`password`), `name` = '纠纷调解员', `status` = 1;

INSERT INTO `wallet` (`uid`, `withdrawn`, `balance`, `create_time`, `update_time`)
VALUES (300, 0.00, 0.00, NOW(), NOW())
ON DUPLICATE KEY UPDATE `update_time` = NOW();

-- 7. 历史商城标签统一为"代购"
UPDATE `order_main` SET `tag` = '代购' WHERE `tag` = '商城代购';
