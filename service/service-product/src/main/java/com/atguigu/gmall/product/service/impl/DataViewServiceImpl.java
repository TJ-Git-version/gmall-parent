package com.atguigu.gmall.product.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.cache.GmallCache;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.product.mapper.BaseCategoryViewMapper;
import com.atguigu.gmall.product.service.DataViewService;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("all")
public class DataViewServiceImpl implements DataViewService {

    @Autowired
    private BaseCategoryViewMapper baseCategoryViewMapper;

    /**
     * 根据三级分类id查询一二三级分类
     * @param category3Id
     * @return
     */
    @Override
    @GmallCache(prefix = "categoryView:")
    public BaseCategoryView getBaseCategoryView(Long category3Id) {
        return baseCategoryViewMapper.selectById(category3Id);
    }

    @Override
    @GmallCache(prefix = "categoryViewList:")
    public List<JSONObject> getBaseCategoryList() {
        List<BaseCategoryView> categoryViewList = baseCategoryViewMapper.selectList(Wrappers.emptyWrapper());
        List<JSONObject> list = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(categoryViewList)) {
            Map<Long, List<BaseCategoryView>> category1ViewMap = categoryViewList.stream().collect(Collectors.groupingByConcurrent(BaseCategoryView::getCategory1Id));
            AtomicInteger index = new AtomicInteger(1);
            // for (Map.Entry<Long, List<BaseCategoryView>> entry1 : category1ViewMap.entrySet()) {
            category1ViewMap.entrySet().forEach((entry1) -> {
                // 一级分类
                JSONObject category1Json = new JSONObject();
                BaseCategoryView CategoryView1 = entry1.getValue().get(0);
                category1Json.put("index", index.getAndIncrement());
                category1Json.put("categoryName", CategoryView1.getCategory1Name());
                category1Json.put("categoryId", CategoryView1.getCategory1Id());
                List<JSONObject> categoryChild2List = new ArrayList<>();
                // 二级分类
                if (CollectionUtils.isNotEmpty(entry1.getValue())) {
                    List<BaseCategoryView> category2Views = entry1.getValue();
                    if (CollectionUtils.isNotEmpty(category2Views)) {
                        Map<Long, List<BaseCategoryView>> category2ViewMap = category2Views.stream().collect(Collectors.groupingByConcurrent(BaseCategoryView::getCategory2Id));
                        // for (Map.Entry<Long, List<BaseCategoryView>> entry2 : category2ViewMap.entrySet()) {
                        category2ViewMap.entrySet().forEach((entry2) -> {
                            JSONObject category2Json = new JSONObject();
                            BaseCategoryView categoryView2 = entry2.getValue().get(0);
                            category2Json.put("categoryName", categoryView2.getCategory2Name());
                            category2Json.put("categoryId", categoryView2.getCategory2Id());
                            // 三级分类
                            if (CollectionUtils.isNotEmpty(entry2.getValue())) {
                                List<JSONObject> categoryChild3List = new ArrayList<>();
                                entry2.getValue().forEach(baseCategory3 -> {
                                    JSONObject category3Json = new JSONObject();
                                    category3Json.put("categoryName", baseCategory3.getCategory3Name());
                                    category3Json.put("categoryId", baseCategory3.getCategory3Id());
                                    categoryChild3List.add(category3Json);
                                });
                                // 二级分类添加三级分类
                                category2Json.put("categoryChild", categoryChild3List);
                            }
                            categoryChild2List.add(category2Json);
                        });
                    }
                }
                // 一级分类添加二级分类
                category1Json.put("categoryChild", categoryChild2List);
                list.add(category1Json);
            });

        }
        return list;
    }
}
