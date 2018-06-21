/**
 * Copyright (C), 2015-2018, XXX有限公司
 * FileName: PaymentStatus
 * Author:   John
 * Date:     2018/4/27 20:42
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.atguigu.gmall1108.bean;

/**
 * 〈一句话功能简述〉<br> 
 * 〈〉
 *
 * @author John
 * @create 2018/4/27
 * @since 1.0.0
 */
public enum  PaymentStatus {
    UNPAID("支付中"),
    PAID("已支付"),
    PAY_FAIL("支付失败"),
    ClOSED("已关闭");

    private String name ;

    PaymentStatus(String name) {
        this.name=name;
    }
}