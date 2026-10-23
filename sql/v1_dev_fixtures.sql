-- ============================================================
-- v1 开发测试数据（仅开发环境使用）
-- 1) 为超级管理员 uid=100 补充微信用户资料（便于用同一账号测试小程序端接口）
-- 2) 将 admin 密码重置为 admin123（BCrypt），便于本地登录调试
-- 执行：
--   mysql --user=root --host=127.0.0.1 --port=3307 --default-character-set=utf8mb4 fuudb < v1_dev_fixtures.sql
-- ============================================================

SET NAMES utf8mb4;

-- 1. 小程序用户资料（admin 兼作小程序端测试用户与跑腿员）
INSERT INTO `user_wx` (`uid`, `openid`, `avatar`, `nickname`, `phone`, `points`, `is_runner`, `can_order`, `can_take`, `school_id`, `realname`, `gender`, `credit_score`)
VALUES (100, 'dev-openid-100', '', '开发测试账号', '13800000000', 0, 1, 1, 1, 4, '测试用户', 1, 12)
ON DUPLICATE KEY UPDATE `phone` = VALUES(`phone`), `can_order` = 1, `can_take` = 1, `is_runner` = 1;

-- 2. 管理员密码重置为 admin123
--    （BCrypt 哈希，对应明文 admin123）
UPDATE `user_pc`
SET `password` = '$2a$10$ZRHGLKpQvraSkVwM8hZEEefZ8fS1yR5xBKCJ738O2cdikxz8NhE.G'
WHERE `username` = 'admin';

-- 3. 跑腿员测试账号 runner/admin123（uid=200，user_type=4 跑腿用户，绑定学校4，可接单）
INSERT INTO `user` (`uid`, `device_type`, `create_time`, `login_time`, `login_ip`, `login_region`, `user_type`, `create_id`, `update_time`, `update_id`)
VALUES (200, 0, NOW(), NOW(), '127.0.0.1', '本地', 4, 100, NOW(), 100)
ON DUPLICATE KEY UPDATE `user_type` = 4, `update_time` = NOW();

INSERT INTO `user_pc` (`uid`, `username`, `password`, `phone`, `name`, `sex`, `status`, `avatar`)
VALUES (200, 'runner', '$2a$10$ZRHGLKpQvraSkVwM8hZEEefZ8fS1yR5xBKCJ738O2cdikxz8NhE.G', '13900000000', '测试跑腿员', 1, 1, '')
ON DUPLICATE KEY UPDATE `password` = VALUES(`password`), `status` = 1;

INSERT INTO `user_wx` (`uid`, `openid`, `avatar`, `nickname`, `phone`, `points`, `is_runner`, `can_order`, `can_take`, `school_id`, `realname`, `gender`, `credit_score`)
VALUES (200, 'dev-openid-200', '', '测试跑腿员', '13900000000', 0, 1, 1, 1, 4, '跑腿测试', 1, 12)
ON DUPLICATE KEY UPDATE `can_take` = 1, `is_runner` = 1, `can_order` = 1;

INSERT INTO `wallet` (`uid`, `withdrawn`, `balance`, `create_time`, `update_time`)
VALUES (200, 0.00, 0.00, NOW(), NOW())
ON DUPLICATE KEY UPDATE `update_time` = NOW();

-- 4. 完成凭证用的假 OSS 记录（id=900001）
INSERT INTO `oss` (`id`, `file_size`, `file_name`, `type`, `original_name`, `file_suffix`, `url`, `create_time`, `create_id`, `service`)
VALUES (900001, 1024, 'dev-proof.png', 0, 'dev-proof.png', 'png', 'https://dev.local/proof.png', NOW(), '100', 'minio')
ON DUPLICATE KEY UPDATE `update_time` = NOW();

-- 5. 纠纷调解员账号 mediator/admin123（uid=300，user_type=6）
INSERT INTO `user` (`uid`, `device_type`, `create_time`, `login_time`, `login_ip`, `login_region`, `user_type`, `create_id`, `update_time`, `update_id`)
VALUES (300, 0, NOW(), NOW(), '127.0.0.1', '本地', 6, 100, NOW(), 100)
ON DUPLICATE KEY UPDATE `user_type` = 6, `update_time` = NOW();

INSERT INTO `user_pc` (`uid`, `username`, `password`, `phone`, `name`, `sex`, `status`, `avatar`)
VALUES (300, 'mediator', '$2a$10$ZRHGLKpQvraSkVwM8hZEEefZ8fS1yR5xBKCJ738O2cdikxz8NhE.G', '13700000000', '纠纷调解员', 2, 1, '')
ON DUPLICATE KEY UPDATE `password` = VALUES(`password`), `status` = 1;

INSERT INTO `wallet` (`uid`, `withdrawn`, `balance`, `create_time`, `update_time`)
VALUES (300, 0.00, 0.00, NOW(), NOW())
ON DUPLICATE KEY UPDATE `update_time` = NOW();

INSERT INTO `role_perm` (`role_id`, `perm_id`)
SELECT 6, `id` FROM `perm`
WHERE `perms` IN ('getInfo','order:appeal:view','order:appeal:list','order:appeal:get','order:appeal:edit','order:order:detail','order:order:phone','order:order:view','order:order:list:hall')
ON DUPLICATE KEY UPDATE `role_id` = `role_id`;
