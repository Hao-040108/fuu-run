package com.karrecy.mall.domain.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 商品查询条件
 */
@Data
public class GoodsQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分类id
     */
    private Long categoryId;

    /**
     * 关键字（商品名称模糊匹配）
     */
    private String keyword;

    /**
     * 状态 0 下架 1 上架（后台查询用）
     */
    private Integer status;

}
