package com.atguigu.gmall.service;

import com.atguigu.gmall1108.bean.*;

import java.util.List;

/**
 * @param
 * @return
 */
public interface ManageService {


    public List<BaseCatalog1> getCatalog1();

    public List<BaseCatalog2> getCatalog2(String catalog1Id);

    public List<BaseCatalog3> getCatalog3(String catalog2Id);

    public List<BaseAttrInfo> getAttrList(String catalog3Id);

    public List<BaseAttrInfo> getAttrList(List<String> attrValueIdList);

    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) ;


    public BaseAttrInfo getAttrInfo(String id) ;

    public List<SpuInfo> getSpuInfoList(SpuInfo spuInfo);

    public List<BaseSaleAttr> getBaseSaleAttrList();

    public  void saveSpuInfo(SpuInfo spuInfo);

    public List<SpuImage> getSpuImageList(String spuId);

    public  List<SpuSaleAttr> getSpuSaleAttrList(String spuId);

    public void saveSkuInfo(SkuInfo skuInfo);

    public SpuInfo getSpuInfoById(String id);

    public SkuInfo getSkuInfoById(String skuId);

    public List<SkuImage> getSkuImage(String skuId);

    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(long skuid,long spuid);

    public List<SkuSaleAttrValue> getSkuSaleAttrValueBySku(Long skuid);


    SkuInfo getSkuInfo(String skuId);


    public List<SkuInfo>   getSkuInfoBySpuId(String spuId);
}
