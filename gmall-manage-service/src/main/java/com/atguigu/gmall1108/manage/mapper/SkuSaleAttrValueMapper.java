package com.atguigu.gmall1108.manage.mapper;

import com.atguigu.gmall1108.bean.SkuSaleAttrValue;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @param
 * @return
 */
public interface SkuSaleAttrValueMapper extends Mapper<SkuSaleAttrValue> {

    public List<SkuSaleAttrValue> selectSkuSaleAttrValueBySku(Long spuId);

}
