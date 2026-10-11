package com.karrecy.mall.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 代购商品
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("goods")
public class Goods implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 分类id
     */
    private Long categoryId;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品图片
     */
    private String image;

    /**
     * 商品参考价（元）
     */
    private BigDecimal price;

    /**
     * 规格/单位
     */
    private String unit;

    /**
     * 单件代购服务费（元）
     */
    private BigDecimal purchaseFee;

    /**
     * 库存（-1 表示不限）
     */
    private Integer stock;

    /**
     * 销量
     */
    private Integer sales;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 状态 0 下架 1 上架
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer sort;

    private LocalDateTime createTime;

    private Long createId;

    private LocalDateTime updateTime;

    private Long updateId;

}
