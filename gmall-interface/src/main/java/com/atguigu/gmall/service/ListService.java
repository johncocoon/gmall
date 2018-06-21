/**
 * Copyright (C), 2015-2018, XXX有限公司
 * FileName: ListService
 * Author:   John
 * Date:     2018/4/18 16:34
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.atguigu.gmall.service;

import com.atguigu.gmall1108.bean.SkuLsParams;
import com.atguigu.gmall1108.bean.SkuLsResult;

/**
 * 〈一句话功能简述〉<br> 
 * 〈〉
 *
 * @author John
 * @create 2018/4/18
 * @since 1.0.0
 */
public interface ListService {
    public void saveSkuInfoById(String skuid);


    public SkuLsResult search(SkuLsParams skuLsParams);


    public void increment(String skuId);
}