package com.atguigu.gmall1108.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.service.ManageService;
import com.atguigu.gmall1108.bean.*;
import com.atguigu.gmall1108.config.RedisConst;
import com.atguigu.gmall1108.config.RedisUtil;
import com.atguigu.gmall1108.manage.mapper.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.ArrayList;
import java.util.List;

/**
 * @param
 * @return
 */
@Service
public class ManageServiceImpl implements ManageService {

    @Autowired
    BaseCatalog1Mapper baseCatalog1Mapper;

    @Autowired
    BaseCatalog2Mapper baseCatalog2Mapper;

    @Autowired
    BaseCatalog3Mapper baseCatalog3Mapper;

    @Autowired
    BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    BaseAttrValueMapper baseAttrValueMapper;

    @Autowired
    SpuInfoMapper spuInfoMapper;

    @Autowired
    BaseSaleAttrMapper baseSaleAttrMapper;

    @Autowired
    SpuImageMapper spuImageMapper;

    @Autowired
    SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Autowired
    SkuInfoMapper skuInfoMapper;
    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    SkuSaleAttrValueMapper skuSaleAttrValueMapper;
    @Autowired
    SkuImageMapper skuImageMapper;


    @Autowired
    RedisUtil redisUtil;

    @Override
    public List<BaseCatalog1> getCatalog1() {
        List<BaseCatalog1> baseCatalog1List = baseCatalog1Mapper.selectAll();
        return baseCatalog1List;
    }

    @Override
    public List<BaseCatalog2> getCatalog2(String catalog1Id) {
        BaseCatalog2 baseCatalog2 = new BaseCatalog2();
        baseCatalog2.setCatalog1Id(catalog1Id);

        List<BaseCatalog2> baseCatalog2List = baseCatalog2Mapper.select(baseCatalog2);
        return baseCatalog2List;
    }

    @Override
    public List<BaseCatalog3> getCatalog3(String catalog2Id) {
        BaseCatalog3 baseCatalog3 = new BaseCatalog3();
        baseCatalog3.setCatalog2Id(catalog2Id);

        List<BaseCatalog3> baseCatalog3List = baseCatalog3Mapper.select(baseCatalog3);
        return baseCatalog3List;
    }

    @Override
    public List<BaseAttrInfo> getAttrList(String catalog3Id) {
        List<BaseAttrInfo> baseAttrInfoList = baseAttrInfoMapper.getBaseAttrInfoListByCatalog3Id(Long.parseLong(catalog3Id));
        return baseAttrInfoList;
    }

    public List<BaseAttrInfo> getAttrList(List<String> attrValueIdList){
        String attrValueIdListString = StringUtils.join(attrValueIdList, ",");
        List<BaseAttrInfo> baseAttrInfoList = baseAttrInfoMapper.getBaseAttrInfoListById(attrValueIdListString);
        return baseAttrInfoList;
    }


    @Override
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {

        //如果有主键就进行更新，如果没有就插入
        if (baseAttrInfo.getId() != null && baseAttrInfo.getId().length() > 0) {
            baseAttrInfoMapper.updateByPrimaryKey(baseAttrInfo);
        } else {
            //防止主键被赋上一个空字符串
            if (baseAttrInfo.getId().length() == 0) {
                baseAttrInfo.setId(null);
            }
            baseAttrInfoMapper.insertSelective(baseAttrInfo);
        }
        //把原属性值全部清空
        BaseAttrValue baseAttrValue4Del = new BaseAttrValue();
        baseAttrValue4Del.setAttrId(baseAttrInfo.getId());
        baseAttrValueMapper.delete(baseAttrValue4Del);

        //重新插入属性
        if (baseAttrInfo.getAttrValueList() != null && baseAttrInfo.getAttrValueList().size() > 0) {
            for (BaseAttrValue attrValue : baseAttrInfo.getAttrValueList()) {
                //防止主键被赋上一个空字符串
                if (attrValue.getId() != null && attrValue.getId().length() == 0) {
                    attrValue.setId(null);
                }
                attrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insertSelective(attrValue);
            }
        }
    }


    /**
     * 点击编辑时加载
     *
     * @param id
     * @return
     */
    @Override
    public BaseAttrInfo getAttrInfo(String id) {
        //查询属性基本信息
        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectByPrimaryKey(id);

        //查询属性对应的属性值
        BaseAttrValue baseAttrValue4Query = new BaseAttrValue();
        baseAttrValue4Query.setAttrId(baseAttrInfo.getId());
        List<BaseAttrValue> baseAttrValueList = baseAttrValueMapper.select(baseAttrValue4Query);

        baseAttrInfo.setAttrValueList(baseAttrValueList);
        return baseAttrInfo;
    }


    public List<SpuInfo> getSpuInfoList(SpuInfo spuInfo) {
        List<SpuInfo> spuInfoList = spuInfoMapper.select(spuInfo);
        return spuInfoList;
    }


    public List<BaseSaleAttr> getBaseSaleAttrList() {
        List<BaseSaleAttr> baseSaleAttrList = baseSaleAttrMapper.selectAll();
        return baseSaleAttrList;
    }


    public void saveSpuInfo(SpuInfo spuInfo) {
        //如果有主键 update 没主键insert
        if (spuInfo.getId() != null && spuInfo.getId().length() == 0) {
            spuInfo.setId(null);
        }
        spuInfoMapper.insertSelective(spuInfo);

        //执行删除 再保存
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        for (SpuImage spuImage : spuImageList) {
            spuImage.setSpuId(spuInfo.getId());
            spuImageMapper.insertSelective(spuImage);
        }

        //执行删除 再保存
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
            spuSaleAttr.setSpuId(spuInfo.getId());
            spuSaleAttrMapper.insertSelective(spuSaleAttr);
            List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
            for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {
                spuSaleAttrValue.setSpuId(spuInfo.getId());
                spuSaleAttrValueMapper.insertSelective(spuSaleAttrValue);
            }
        }

    }


    public List<SpuImage> getSpuImageList(String spuId) {
        SpuImage spuImageQuery = new SpuImage();
        spuImageQuery.setSpuId(spuId);

        List<SpuImage> spuImageList = spuImageMapper.select(spuImageQuery);
        return spuImageList;
    }


    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId) {

        List<SpuSaleAttr> spuSaleAttrList = spuSaleAttrMapper.selectSpuSaleAttrList(Long.parseLong(spuId));
        return spuSaleAttrList;

    }


    public void saveSkuInfo(SkuInfo skuInfo) {
        if (skuInfo.getId() != null && skuInfo.getId().length() == 0) {
            skuInfo.setId(null);
        }
        skuInfoMapper.insertSelective(skuInfo);

        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        for (SkuImage skuImage : skuImageList) {
            skuImage.setSkuId(skuInfo.getId());

            skuImageMapper.insertSelective(skuImage);
        }

        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        for (SkuAttrValue skuAttrValue : skuAttrValueList) {
            skuAttrValue.setSkuId(skuInfo.getId());
            skuAttrValueMapper.insertSelective(skuAttrValue);
        }

        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {

            skuSaleAttrValue.setSkuId(skuInfo.getId());
            skuSaleAttrValueMapper.insertSelective(skuSaleAttrValue);
        }

    }

    @Override
    public SpuInfo getSpuInfoById(String id) {
        SpuInfo spuInfoQuery = spuInfoMapper.selectByPrimaryKey(id);
        //1 图片list
        SpuImage spuImage = new SpuImage();
        spuImage.setSpuId(spuInfoQuery.getId());
        List<SpuImage> spuImageList = spuImageMapper.select(spuImage);
        spuInfoQuery.setSpuImageList(spuImageList);
        //2 销售属性 和销售属性值
        List<SpuSaleAttr> spuSaleAttrList = spuSaleAttrMapper.selectSpuSaleAttrList(Long.parseLong(spuInfoQuery.getId()));
        spuInfoQuery.setSpuSaleAttrList(spuSaleAttrList);

        return spuInfoQuery;
    }


    public  SkuInfo getSkuInfo(String skuId) {

        try{
            Jedis jedis = redisUtil.getJedis();
            String skuKey = RedisConst.SKU_PREFIX + skuId + RedisConst.SKU_SUFFIX;
            String lockKey = RedisConst.SKU_PREFIX + skuId + RedisConst.LOCK_SUFFIX;
            String redisKey = jedis.get(skuKey);
            try {
                Thread.sleep(1000);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName()+"开启");
            if (redisKey == null) {

                //需要加锁   用来解决  由于redis在高并发的情况下 过期  这时  加上分布式锁  对于第一个请求的锁进行处理
                //让后通过查询出来之后删除锁key这时  下一个锁进行查询
                String lock = jedis.set(lockKey, "1", "nx", "px", 10 * 1000);
                System.out.println(Thread.currentThread().getName()+"加锁");
                if("ok".equalsIgnoreCase(lock)){
                    //需要去数据库查询
                    try {
                        Thread.sleep(10000);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println(Thread.currentThread().getName()+"到数据库查询");
                    SkuInfo skuInfoById = getSkuInfoById(skuId);


                    if(skuInfoById==null){
                        System.out.println(Thread.currentThread().getName()+"这个id是不存在的");
                        jedis.setex(skuKey, 24 * 60, "empty");
                        jedis.del(lockKey);
                        jedis.close();
                        return null;
                    }

                    String skustr = JSON.toJSONString(skuInfoById);
                    jedis.set(skuKey, skustr, "nx", "ex", 24 * 60 * 60);
                    jedis.del(lockKey);
                    jedis.close();
                    return skuInfoById;
                }else {
                    getSkuInfo(skuId);   //如果是前面有锁在  旋转等待
                }
            }else if("empty".equalsIgnoreCase(redisKey)){
                    return null;
            } else {
                SkuInfo skuInfo = JSON.parseObject(redisKey, SkuInfo.class);
                System.out.println(Thread.currentThread().getName()+" 结束   获取到数据");
                jedis.close();
                return skuInfo;
            }

        }catch (JedisConnectionException ex){
            ex.printStackTrace();

        }

        return getSkuInfoById(skuId);
    }


    @Override
    public SkuInfo getSkuInfoById(String skuId) {

        SkuInfo skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);
        List<SkuImage> skuImage = getSkuImage(skuId);
        skuInfo.setSkuImageList(skuImage);
        SkuSaleAttrValue skuSaleAttrValue = new SkuSaleAttrValue();
        skuSaleAttrValue.setSkuId(skuId);
        List<SkuSaleAttrValue> skuSaleAttrValueList =   skuSaleAttrValueMapper.select(skuSaleAttrValue) ;
        skuInfo.setSkuSaleAttrValueList(skuSaleAttrValueList);
        SkuAttrValue skuAttrValue   = new SkuAttrValue();
        skuAttrValue.setSkuId(skuId);
        List<SkuAttrValue> skuAttrValueList = skuAttrValueMapper.select(skuAttrValue);
        skuInfo.setSkuAttrValueList(skuAttrValueList);


        return skuInfo;
    }

    @Override
    public List<SkuImage> getSkuImage(String skuId) {
        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuId);
        return skuImageMapper.select(skuImage);
    }

    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(long skuid, long spuid) {
        return spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(skuid, spuid);
    }

    public List<SkuSaleAttrValue> getSkuSaleAttrValueBySku(Long spuid) {
        return skuSaleAttrValueMapper.selectSkuSaleAttrValueBySku(spuid);
    }


    //用来查询spu下的所有sku
    public List<SkuInfo>   getSkuInfoBySpuId(String spuId){
        SkuInfo skuInfoQuery = new SkuInfo();
        skuInfoQuery.setSpuId(spuId);
        List<SkuInfo> skuInfoList = skuInfoMapper.select(skuInfoQuery);
        List<SkuInfo> skuInfoListReult=new ArrayList<>(skuInfoList.size());
        for (SkuInfo skuInfo : skuInfoList) {
            SkuInfo skuInfoById = getSkuInfoById(skuInfo.getId());
            skuInfoListReult.add(skuInfoById);
        }

        return skuInfoListReult;


    }









}
