package com.atguigu.gmall.list.controller;

import com.atguigu.gmall.common.execption.GmallException;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.mapper.GoodsRepository;
import com.atguigu.gmall.model.list.Goods;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/list")
@RequiredArgsConstructor
@Slf4j
public class ListApiController {

    private final ElasticsearchRestTemplate esRestTemplate;


    /**
     * 初始化mapping结构到es中
     * @return
     */
    @GetMapping("/createIndex")
    public Result<Void> createIndex() {
        try {
            // 创建索引
            boolean flag = esRestTemplate.indexOps(Goods.class).create();
            // 创建映射
            // Document document = esRestTemplate.indexOps(Goods.class).createMapping();
            // boolean document = esRestTemplate.putMapping(Goods.class);
            boolean document = esRestTemplate.createIndex(Goods.class);
            // 刷新索引
            esRestTemplate.indexOps(Goods.class).refresh();
            log.info("goods 创建索引成功：{}", flag);
            log.info("goods 创建映射成功：{}", document);
        } catch (Exception e) {
            log.error("创建索引失败：{}", e.getMessage());
            throw new GmallException("该索引已存在，请勿重复创建");
        }
        return Result.ok();
    }

    @GetMapping("/deleteIndex")
    public Result<Void> deleteIndex(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            boolean flag = esRestTemplate.indexOps(clazz).delete();
            esRestTemplate.indexOps(clazz).refresh();
            log.info("goods 删除索引成功：{}",flag);
        } catch (Exception e) {
            log.error("删除索引失败：{}", e.getMessage());
            throw new GmallException("删除索引失败，请检查className是否正确");
        }
        return Result.ok();
    }
}
