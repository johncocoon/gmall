/**
 * Copyright (C), 2015-2018, XXX有限公司
 * FileName: OrderStatus
 * Author:   John
 * Date:     2018/4/25 19:59
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
 * @create 2018/4/25
 * @since 1.0.0
 */
public enum OrderStatus {
    UNPAID("未支付"),
    PAID("已支付" ),
    WAITING_DELEVER("待发货"),
    DELEVERED("已发货"),
    CLOSED("已关闭"),
    FINISHED("已完结") ,
    SPLIT("订单已拆分");

    private String comment ;


    OrderStatus(String comment ){
        this.comment=comment;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }


}