-- ============================================================
-- v1 校园代购平台 —— 数据库变更脚本（代购商城模块）
-- 库：fuudb（MySQL 8.0+）
-- 执行：
--   mysql --user=root --host=127.0.0.1 --port=3307 --default-character-set=utf8mb4 fuudb < v1_campus_proxy.sql
-- 说明：本脚本为一次性增量脚本（ALTER 语句不可重复执行）
-- ============================================================

SET NAMES utf8mb4;

-- ------------------------------------------------------------
-- 1. 代购商品分类
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `goods_category`;
CREATE TABLE `goods_category` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `school_id` bigint NULL DEFAULT NULL COMMENT '所属学校（NULL 表示全平台通用）',
  `name` varchar(50) NOT NULL COMMENT '分类名称',
  `icon` varchar(255) NULL DEFAULT NULL COMMENT '分类图标',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序（越小越靠前）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态 0 停用 1 启用',
  `create_time` datetime NOT NULL,
  `create_id` bigint NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  `update_id` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '代购商品分类' ROW_FORMAT = DYNAMIC;

-- ------------------------------------------------------------
-- 2. 代购商品（平台维护的代购目录）
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `goods`;
CREATE TABLE `goods` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `category_id` bigint NOT NULL COMMENT '分类id',
  `name` varchar(100) NOT NULL COMMENT '商品名称',
  `image` varchar(255) NULL DEFAULT NULL COMMENT '商品图片',
  `price` decimal(10, 2) NOT NULL COMMENT '商品参考价（元）',
  `unit` varchar(20) NULL DEFAULT NULL COMMENT '规格/单位',
  `purchase_fee` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '单件代购服务费（元）',
  `stock` int NOT NULL DEFAULT -1 COMMENT '库存（-1 表示不限）',
  `sales` int NOT NULL DEFAULT 0 COMMENT '销量',
  `description` varchar(500) NULL DEFAULT NULL COMMENT '商品描述',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态 0 下架 1 上架',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime NOT NULL,
  `create_id` bigint NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  `update_id` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_category_id`(`category_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '代购商品' ROW_FORMAT = DYNAMIC;

-- ------------------------------------------------------------
-- 3. 购物车
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `cart_item`;
CREATE TABLE `cart_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `uid` bigint NOT NULL COMMENT '用户id',
  `goods_id` bigint NOT NULL COMMENT '商品id',
  `quantity` int NOT NULL DEFAULT 1 COMMENT '数量',
  `create_time` datetime NOT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_uid_goods`(`uid` ASC, `goods_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '购物车' ROW_FORMAT = DYNAMIC;

-- ------------------------------------------------------------
-- 4. 订单商品明细（下单时快照，防止商品改价影响历史订单）
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `order_goods_item`;
CREATE TABLE `order_goods_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL COMMENT '订单id',
  `goods_id` bigint NULL DEFAULT NULL COMMENT '商品id',
  `goods_name` varchar(100) NOT NULL COMMENT '商品名称（快照）',
  `goods_image` varchar(255) NULL DEFAULT NULL COMMENT '商品图片（快照）',
  `price` decimal(10, 2) NOT NULL COMMENT '单价（快照）',
  `quantity` int NOT NULL COMMENT '数量',
  `purchase_fee` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '单件代购服务费（快照）',
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单商品明细' ROW_FORMAT = DYNAMIC;

-- ------------------------------------------------------------
-- 5. 订单主表扩展：订单来源 + 商品金额
-- ------------------------------------------------------------
ALTER TABLE `order_main`
  ADD COLUMN `order_source` tinyint NOT NULL DEFAULT 0 COMMENT '订单来源 0 需求大厅 1 代购商城' AFTER `service_type`,
  ADD COLUMN `goods_amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '商品金额（代购商城）' AFTER `estimated_price`;

-- ------------------------------------------------------------
-- 6. 演示种子数据（分类 + 商品）
-- ------------------------------------------------------------
DELETE FROM `cart_item`;
DELETE FROM `goods`;
DELETE FROM `goods_category`;

INSERT INTO `goods_category` (`id`, `name`, `icon`, `sort`, `status`, `create_time`) VALUES
  (1, '零食饮料', NULL, 1, 1, NOW()),
  (2, '水果生鲜', NULL, 2, 1, NOW()),
  (3, '文具用品', NULL, 3, 1, NOW()),
  (4, '生活用品', NULL, 4, 1, NOW());

INSERT INTO `goods` (`category_id`, `name`, `image`, `price`, `unit`, `purchase_fee`, `stock`, `sales`, `description`, `status`, `sort`, `create_time`) VALUES
  (1, '康师傅冰红茶 500ml', NULL, 3.50, '瓶', 0.50, -1, 0, '经典柠檬味，冰镇更好喝', 1, 1, NOW()),
  (1, '可口可乐 500ml', NULL, 3.50, '瓶', 0.50, -1, 0, '快乐水，常温/冰镇可选', 1, 2, NOW()),
  (1, '乐事薯片 原味 70g', NULL, 6.00, '袋', 0.80, -1, 0, '原味经典款', 1, 3, NOW()),
  (1, '达利园小面包 400g', NULL, 12.00, '箱', 1.00, -1, 0, '早餐/夜宵好搭档', 1, 4, NOW()),
  (2, '红富士苹果 500g', NULL, 5.50, '斤', 1.00, -1, 0, '脆甜多汁', 1, 1, NOW()),
  (2, '香蕉 500g', NULL, 4.00, '斤', 1.00, -1, 0, '进口香蕉', 1, 2, NOW()),
  (3, '得力中性笔 黑 0.5mm', NULL, 2.00, '支', 0.30, -1, 0, '考试刷题必备', 1, 1, NOW()),
  (3, 'A4笔记本 60页', NULL, 5.00, '本', 0.50, -1, 0, '线圈笔记本', 1, 2, NOW()),
  (4, '抽纸 3层 100抽', NULL, 4.50, '包', 0.50, -1, 0, '宿舍常备', 1, 1, NOW()),
  (4, '洗衣液 500g', NULL, 9.90, '瓶', 1.00, -1, 0, '薰衣草香型', 1, 2, NOW());
