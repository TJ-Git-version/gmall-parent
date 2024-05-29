package com.atguigu.gmall.model.list;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;
import java.util.List;

// Index = goods , Type = info  es 7.8.0 逐渐淡化type！  修改！

/**
 * 创建索引 goods
 * @Document：注解，声明这是一个文档类，并指定索引名称、类型名称、分片数、副本数等信息。
 *    indexName：索引名称，默认为类名小写。
 *    shards：分片数，集群下生效，默认为5。
 *    replicas：副本数，集群下生效，默认为1。
 *    refreshInterval：刷新间隔，默认为1s。
 *    type：类型名称，默认为类名小写。
 *    versionType：版本类型，默认为INTERNAL。
 *  @Id：注解，声明文档的唯一标识，默认使用@Id注解的字段作为文档的主键。
 *  @Field：注解，声明文档字段，包括字段类型、是否索引、是否分词、是否存储等信息。
 *      type：字段类型，包括Text、Keyword、Long、Double、Date等。
 *      FieldType：
 *          Text：分词字段，支持模糊查询。
 *          keyword：不分词字段，支持精确查询。
 *          Long、Double：数值字段。
 *          Date：日期字段，支持范围查询。
 *          Nested：支持嵌套文档。
 *      analyzer：分词器，默认为standard。中文分词器：ik_max_word（细粒度）和ik_smart（粗粒度）。
 *      searchAnalyzer：搜索分词器，默认为standard。中文建议使用ik_smart（粗粒度）。
 *          ik_max_word：最细粒度的中文分词器，适合索引和搜索长文本。
 *          ik_smart：最粗粒度的中文分词器，适合索引短文本。
 *      store：是否存储，默认为true。
 *      index：是否索引，默认为true。
 */
@Data
@Document(indexName = "goods" , shards = 3,replicas = 2)
public class Goods {
    // 商品Id skuId
    @Id
    private Long id;

    @Field(type = FieldType.Keyword, index = false)
    private String defaultImg;

    //  es 中能分词的字段，这个字段数据类型必须是 text！keyword 不分词！
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String title;

    @Field(type = FieldType.Double)
    private Double price;

    //  @Field(type = FieldType.Date)   6.8.1
    @Field(type = FieldType.Date,format = DateFormat.custom,pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime; // 新品

    @Field(type = FieldType.Long)
    private Long tmId; // 品牌id

    @Field(type = FieldType.Keyword)
    private String tmName; // 品牌名称

    @Field(type = FieldType.Keyword)
    private String tmLogoUrl; // 品牌logo

    @Field(type = FieldType.Long)
    private Long category1Id;

    @Field(type = FieldType.Keyword)
    private String category1Name;

    @Field(type = FieldType.Long)
    private Long category2Id;

    @Field(type = FieldType.Keyword)
    private String category2Name;

    @Field(type = FieldType.Long)
    private Long category3Id;

    @Field(type = FieldType.Keyword)
    private String category3Name;

    //  商品的热度！ 我们将商品被用户点查看的次数越多，则说明热度就越高！
    @Field(type = FieldType.Long)
    private Long hotScore = 0L;

    // 平台属性集合对象
    // Nested 支持嵌套查询
    @Field(type = FieldType.Nested)
    private List<SearchAttr> attrs;

}
