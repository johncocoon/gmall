/**
 * Copyright (C), 2015-2018, XXX有限公司
 * FileName: CartInfoMapper
 * Author:   John
 * Date:     2018/4/24 18:32
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.atguigu.gmall1108.mapper;

import com.atguigu.gmall1108.bean.CartInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * 〈一句话功能简述〉<br> 
 * 〈〉
 *
 * @author John
 * @create 2018/4/24
 * @since 1.0.0
 */
public interface CartInfoMapper extends Mapper<CartInfo> {

    List<CartInfo> selectCartListWithCurPrice(String userId);
}