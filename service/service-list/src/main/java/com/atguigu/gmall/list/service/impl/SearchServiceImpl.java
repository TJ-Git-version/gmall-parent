package com.atguigu.gmall.list.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.list.repository.SearchRepository;
import com.atguigu.gmall.list.service.SearchService;
import com.atguigu.gmall.model.list.*;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.TotalHits;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.AggregatorFactories;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.profile.ProfileShardResult;
import org.elasticsearch.search.sort.SortOrder;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
@Slf4j
@SuppressWarnings("all")
public class SearchServiceImpl implements SearchService {

    @Autowired
    private SearchRepository searchRepository;

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * 商品搜索功能
     *
     * @param searchParam
     * @return
     */
    @Override
    @SneakyThrows
    public SearchResponseVo searchList(SearchParam searchParam) {
        // 封装查询条件
        SearchRequest searchRequest = this.buildSearchQueryDsl(searchParam);

        // 执行查询
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        // 封装响应实体类信息
        SearchResponseVo searchResponseVo = this.buildSearchResponseVo(searchResponse);
        // 当前页
        searchResponseVo.setPageNo(searchParam.getPageNo());
        // 每页大小
        searchResponseVo.setPageSize(searchParam.getPageSize());
        // 总页数
        searchResponseVo.setTotalPages((searchResponseVo.getTotal() + searchParam.getPageSize() - 1) / searchParam.getPageSize());

        return searchResponseVo;
    }

    /**
     * 封装列表查询条件
     *
     * @param searchParam
     * @return
     */
    private SearchRequest buildSearchQueryDsl(SearchParam searchParam) {
        SearchRequest searchRequest = new SearchRequest("goods");
        SearchSourceBuilder searchBuilder = new SearchSourceBuilder();
        // 查询条件
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (StringUtils.isNotBlank(searchParam.getKeyword())) {
            MatchQueryBuilder keywordQueryBuilder = QueryBuilders.matchQuery("title", searchParam.getKeyword()).operator(Operator.AND);
            boolQueryBuilder.must(keywordQueryBuilder);
        }
        // 过滤查询-分类（一级、二级和三级分类）
        if (searchParam.getCategory1Id() != null) {
            TermQueryBuilder category1IdQueryBuilder = QueryBuilders.termQuery("category1Id", searchParam.getCategory1Id());
            boolQueryBuilder.filter().add(category1IdQueryBuilder);
        }
        if (searchParam.getCategory2Id() != null) {
            TermQueryBuilder category2IdQueryBuilder = QueryBuilders.termQuery("category2Id", searchParam.getCategory2Id());
            boolQueryBuilder.filter().add(category2IdQueryBuilder);
        }
        if (searchParam.getCategory3Id() != null) {
            TermQueryBuilder category3IdQueryBuilder = QueryBuilders.termQuery("category3Id", searchParam.getCategory3Id());
            boolQueryBuilder.filter().add(category3IdQueryBuilder);
        }
        // 过滤查询-商品品牌 2:华为 品牌id:品牌名称
        String trademark = searchParam.getTrademark();
        if (StringUtils.isNotBlank(trademark)) {
            String[] trademarkArr = trademark.split(",");
            if (trademarkArr != null && trademarkArr.length == 2) {
                boolQueryBuilder.filter().add(QueryBuilders.termQuery("tmId", trademarkArr[0]));
                boolQueryBuilder.filter().add(QueryBuilders.termQuery("tmName", trademarkArr[1]));
            }
        }
        // 嵌套查询-平台属性值   prop=23:4G:运行内存    平台属性Id 平台属性值名称 平台属性名
        String[] props = searchParam.getProps();
        if (props != null && props.length > 0) {
            for (String prop : props) {
                String[] propArr = prop.split(":");
                if (propArr != null && propArr.length == 3) {
                    // 构建嵌套查询
                    BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
                    // 构建子查询中的过滤条件
                    BoolQueryBuilder suQueryBuilder = QueryBuilders.boolQuery();
                    suQueryBuilder.must(QueryBuilders.termQuery("attrId", propArr[0]));
                    suQueryBuilder.must(QueryBuilders.termQuery("attrName", propArr[1]));
                    suQueryBuilder.must(QueryBuilders.termQuery("attrValueId", propArr[2]));
                    // 构造nested查询
                    queryBuilder.must().add(QueryBuilders.nestedQuery("attrs", suQueryBuilder, ScoreMode.None));
                    // 添加到整个过滤对象中
                    boolQueryBuilder.filter().add(queryBuilder);
                }
            }
        }
        searchBuilder.query(boolQueryBuilder);

        // 聚合条件
        // 聚合商品品牌数据
        TermsAggregationBuilder tmAggregationBuilder = AggregationBuilders.terms("tmIdAgg").field("tmId")
                // 子聚合-品牌名称和商品logo
                .subAggregation(AggregationBuilders.terms("tmNameAgg").field("tmName"))
                .subAggregation(AggregationBuilders.terms("tmLogoUrlAgg").field("tmLogoUrl"));
        searchBuilder.aggregation(tmAggregationBuilder);
        NestedAggregationBuilder nestedAggregationBuilder = AggregationBuilders.nested("attrsAgg", "attrs")
                .subAggregation(AggregationBuilders.terms("attrIdAgg").field("attrs.attrId").size(10));
        // 聚合平台属性数据
        TermsAggregationBuilder attrIdAggregationBuilder = AggregationBuilders.terms("attrIdAgg").field("attrs.attrId").size(10);
        NestedAggregationBuilder attrsAggregationBuilder = AggregationBuilders.nested("attrsAgg", "attrs")
                .subAggregation(attrIdAggregationBuilder);
        attrIdAggregationBuilder.subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName").size(10))
                .subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue").size(10));
        searchBuilder.aggregation(attrsAggregationBuilder);
        //  根据商品的热度和价格进行排序
        // 排序规则
        // 1:hotScore 2:price   1：综合排序/热度  2：价格  order=1:desc
        if (searchParam.getOrder() != null) {
            String[] orderArr = searchParam.getOrder().split(":");
            if (orderArr != null && orderArr.length == 2) {
                String sortOrder = "";
                switch (orderArr[0]) {
                    case "1":
                        sortOrder = "hotScore";
                        break;
                    case "2":
                        sortOrder = "price";
                        break;
                }
                searchBuilder.sort(sortOrder, SortOrder.DESC.equals(orderArr[1]) ? SortOrder.DESC : SortOrder.ASC);
            }
        } else {
            searchBuilder.sort("hotScore", SortOrder.DESC);
        }

        // 查询结果高亮显示
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title")
                .preTags("<span style='color:red'>")
                .postTags("</span>");
        searchBuilder.highlighter(highlightBuilder);
        // 分页查询
        int pageNum = (searchParam.getPageNo() - 1) * searchParam.getPageSize();
        searchBuilder.from(pageNum);
        searchBuilder.size(searchParam.getPageSize());
        // 列表数据-过滤多余字段
        String[] excludes = {"id", "title", "defaultImg", "price", "hotScore"};
        searchBuilder.fetchSource(excludes, null);
        searchRequest.source(searchBuilder);
        System.out.println("dsl:" + searchBuilder.toString());
        return searchRequest;
    }

    /**
     * 封装列表响应实体类信息
     *
     * @param searchResponse
     * @return
     */
    private SearchResponseVo buildSearchResponseVo(SearchResponse searchResponse) {
        SearchResponseVo searchResponseVo = new SearchResponseVo();
        // 聚合后的品牌列表
        Map<String, Aggregation> aggregationMap = searchResponse.getAggregations().getAsMap();
        ParsedStringTerms tmTerms = (ParsedStringTerms) aggregationMap.get("tmIdAgg");
        if (CollectionUtils.isNotEmpty(tmTerms.getBuckets())) {
            List<SearchResponseTmVo> searchResponseTmVoList = tmTerms.getBuckets().stream().map(term -> {
                SearchResponseTmVo searchResponseTmVo = new SearchResponseTmVo();
                // 获取品牌id
                String tmIdAgg = term.getKeyAsString();
                searchResponseTmVo.setTmId(Long.valueOf(tmIdAgg));
                // 获取品牌名称
                Map<String, Aggregation> subAggregationsAsMap = term.getAggregations().getAsMap();
                ParsedStringTerms tmNameTermsAgg = (ParsedStringTerms) subAggregationsAsMap.get("tmNameAgg");
                String tmName = tmNameTermsAgg.getBuckets().get(0).getKeyAsString();
                searchResponseTmVo.setTmName(tmName);
                // 获取品牌logoUrl
                ParsedStringTerms tmLogoUrlTermsAgg = (ParsedStringTerms) subAggregationsAsMap.get("tmLogoUrlAgg");
                String tmLogoUrl = tmLogoUrlTermsAgg.getBuckets().get(0).getKeyAsString();
                searchResponseTmVo.setTmLogoUrl(tmLogoUrl);
                return searchResponseTmVo;
            }).collect(Collectors.toList());
            searchResponseVo.setTrademarkList(searchResponseTmVoList);
        }


        //  聚合后的平台属性列表
        ParsedNested attrParsedNested = (ParsedNested) aggregationMap.get("attrsAgg");
        Terms attrIdTermsAgg = attrParsedNested.getAggregations().get("attrIdAgg");
        if (CollectionUtils.isNotEmpty(attrIdTermsAgg.getBuckets())) {
            List<SearchResponseAttrVo> searchAttrVoList = attrIdTermsAgg.getBuckets().stream().map(term -> {
                SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();
                String attrId = term.getKeyAsString();
                searchResponseAttrVo.setAttrId(Long.valueOf(attrId));
                // 获取平台属性名称
                Map<String, Aggregation> subAggregationsAsMap = term.getAggregations().getAsMap();
                ParsedStringTerms attrNameAgg = (ParsedStringTerms) subAggregationsAsMap.get("attrNameAgg");
                String attrName = attrNameAgg.getBuckets().get(0).getKeyAsString();
                searchResponseAttrVo.setAttrName(attrName);
                // 获取平台属性值
                ParsedStringTerms attrValueAgg = (ParsedStringTerms) subAggregationsAsMap.get("attrValueAgg");
                List<String> attrValueList = attrValueAgg.getBuckets().stream().map(MultiBucketsAggregation.Bucket::getKeyAsString).collect(Collectors.toList());
                searchResponseAttrVo.setAttrValueList(attrValueList);
                return searchResponseAttrVo;
            }).collect(Collectors.toList());
            searchResponseVo.setAttrsList(searchAttrVoList);
        }

        // 商品列表
        SearchHits hits = searchResponse.getHits();
        SearchHit[] hitsHits = hits.getHits();
        if (hitsHits != null && hitsHits.length > 0) {
            List<Goods> goodsList = Arrays.stream(hitsHits).map(hitsHit -> {
                Goods goods = JSON.parseObject(hitsHit.getSourceAsString(), Goods.class);
                // 高亮显示
                Map<String, HighlightField> highlightFields = hitsHit.getHighlightFields();
                if (highlightFields != null && highlightFields.size() > 0) {
                    HighlightField highlightField = highlightFields.get("title");
                    goods.setTitle(highlightField.getFragments()[0].toString());
                }
                return goods;
            }).collect(Collectors.toList());
            searchResponseVo.setGoodsList(goodsList);
        }
        // 总记录数
        searchResponseVo.setTotal(hits.getTotalHits().value);
        return searchResponseVo;
    }


    /**
     * 商品热度排名添加
     *
     * @param skuId
     */
    @Override
    public void incrHotScore(Long skuId) {
        // 商品热度排名添加
        Double hostScore = redisTemplate.opsForZSet().incrementScore(RedisConst.SKU_HOT_SCORE_KEY, RedisConst.SKU_INFO_KEY_PREFIX + skuId, 1);
        // 判断商品热度排名是否达到了阈值，达到则添加到es中
        if (hostScore % RedisConst.SKU_HOT_SCORE_THRESHOLD == 0) {
            // 查询elasticsearch商品信息
            Goods goods = searchRepository.findById(skuId).get();
            goods.setHotScore(Math.round(hostScore));
            // 同步商品信息到elasticsearch中
            searchRepository.save(goods);
        }
    }

    /**
     * 商品下架功能
     *
     * @param skuId
     */
    @Override
    public void lowerGoods(Long skuId) {
        searchRepository.deleteById(skuId);
    }


    /**
     * 商品上架功能
     * 使用CompletableFuture异步编排优化以下代码
     *
     * @param skuId
     * @return
     */
    @Override
    public Goods upperGoods(Long skuId) {
        // 使用布隆过滤器判断商品是否存在
        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(RedisConst.SKU_BLOOM_FILTER);
        if (!bloomFilter.contains(skuId)) {
            return new Goods();
        }

        // 封装Goods对象
        Goods goods = new Goods();

        // 查询sku商品信息
        CompletableFuture<SkuInfo> skuInfoCompletableFuture = CompletableFuture.supplyAsync(() -> {
            SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
            goods.setId(skuInfo.getId());
            goods.setTitle(skuInfo.getSkuName());
            goods.setPrice(skuInfo.getPrice().doubleValue());
            goods.setDefaultImg(skuInfo.getSkuDefaultImg());
            goods.setTmId(skuInfo.getTmId());
            goods.setCreateTime(skuInfo.getCreateTime());
            return skuInfo;
        }, threadPoolExecutor);

        CompletableFuture<Void> attrListCompletableFuture = CompletableFuture.runAsync(() -> {
            // 查询sku的规格参数信息
            List<BaseAttrInfo> attrList = productFeignClient.getAttrList(skuId);
            // 封装sku的规格参数信息
            List<SearchAttr> searchAttrList = attrList.stream().map(arr -> {
                SearchAttr searchAttr = new SearchAttr();
                searchAttr.setAttrId(arr.getId());
                searchAttr.setAttrName(arr.getAttrName());
                BaseAttrValue baseAttrValue = arr.getAttrValueList().stream().findFirst().orElse(new BaseAttrValue());
                searchAttr.setAttrValue(baseAttrValue.getValueName());
                return searchAttr;
            }).collect(Collectors.toList());
            goods.setAttrs(searchAttrList);
        }, threadPoolExecutor);

        // 查询sku的品牌信息
        CompletableFuture<Void> trademarkCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            BaseTrademark skuTrademark = productFeignClient.getTrademark(skuInfo.getTmId());
            // 封装sku品牌信息
            goods.setTmName(skuTrademark.getTmName());
            goods.setTmLogoUrl(skuTrademark.getLogoUrl());
        }, threadPoolExecutor);

        // 查询sku的分类信息
        CompletableFuture<Void> ategoryViewCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
            // 封装sku分类信息
            BeanUtils.copyProperties(categoryView, goods);
            goods.setId(skuInfo.getId());
        }, threadPoolExecutor);

        CompletableFuture.allOf(
                skuInfoCompletableFuture, trademarkCompletableFuture,
                ategoryViewCompletableFuture, attrListCompletableFuture
        ).join();
        // 同步商品信息到elasticsearch中
        return searchRepository.save(goods);
    }

}
