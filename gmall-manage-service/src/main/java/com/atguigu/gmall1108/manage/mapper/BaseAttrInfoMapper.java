package com.atguigu.gmall1108.manage.mapper;

import com.atguigu.gmall1108.bean.BaseAttrInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @param
 * @return
 */
public interface BaseAttrInfoMapper extends Mapper<BaseAttrInfo> {

    List<BaseAttrInfo> getBaseAttrInfoListByCatalog3Id(Long catalog3Id);

    List<BaseAttrInfo> getBaseAttrInfoListById(@Param("attrValueIdListString") String attrValueIdListString);
}
