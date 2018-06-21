package com.atguigu.gmall1108.bean;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * @param
 * @return
 */
public class BaseAttrInfo implements Serializable {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column
    private String id;
    @Column
    private String attrName;
    @Column
    private String catalog3Id;
    @Column
    private String isEnabled;
    @Transient
    List<BaseAttrValue> attrValueList;





    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAttrName() {
        return attrName;
    }

    public void setAttrName(String attrName) {
        this.attrName = attrName;
    }

    public String getCatalog3Id() {
        return catalog3Id;
    }

    public void setCatalog3Id(String catalog3Id) {
        this.catalog3Id = catalog3Id;
    }

    public String getIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(String isEnabled) {
        this.isEnabled = isEnabled;
    }

    public List<BaseAttrValue> getAttrValueList() {
        return attrValueList;
    }

    public void setAttrValueList(List<BaseAttrValue> attrValueList) {
        this.attrValueList = attrValueList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseAttrInfo that = (BaseAttrInfo) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(attrName, that.attrName) &&
                Objects.equals(catalog3Id, that.catalog3Id) &&
                Objects.equals(isEnabled, that.isEnabled) &&
                Objects.equals(attrValueList, that.attrValueList);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, attrName, catalog3Id, isEnabled, attrValueList);
    }
}
