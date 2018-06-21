/**
 * Copyright (C), 2015-2018, XXX有限公司
 * FileName: SkuLsParams
 * Author:   John
 * Date:     2018/4/18 21:23
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.atguigu.gmall1108.bean;

import java.io.Serializable;

/**
 * 〈一句话功能简述〉<br> 
 * 〈用于查询的请求参数〉
 *
 * @author John
 * @create 2018/4/18
 * @since 1.0.0
 */
public class SkuLsParams implements Serializable {


    private String catalog3Id;
    private String [] valueId;
    private String keyword;
    private int pageNo=1;
    private int pageSize=20;



    public String getCatalog3Id() {
        return catalog3Id;
    }

    public void setCatalog3Id(String catalog3Id) {
        this.catalog3Id = catalog3Id;
    }

    public String[] getValueId() {
        return valueId;
    }

    public void setValueId(String[] valueId) {
        this.valueId = valueId;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}