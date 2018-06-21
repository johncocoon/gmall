package com.atguigu.gmall1108.manage.mapper;

import com.atguigu.gmall1108.bean.SpuSaleAttr;
import com.atguigu.gmall1108.bean.SpuSaleAttrValue;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @param
 * @return
 */
public interface SpuSaleAttrMapper extends Mapper<SpuSaleAttr> {

    public  List<SpuSaleAttr> selectSpuSaleAttrList(Long id);

    public List<SpuSaleAttr>  selectSpuSaleAttrListCheckBySku(Long skuid,Long spuid);
}
