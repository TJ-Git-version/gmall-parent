package com.atguigu.gmall.weball.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.client.ListFeignClient;
import com.atguigu.gmall.model.list.SearchParam;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class SearchController {

    private final ListFeignClient listFeignClient;

    /**
     * 全文检索入口
     * @param searchParam
     * @param model
     * @return
     */
    @GetMapping("/list.html")
    public String list(SearchParam searchParam, Model model) {
        Result<Map<String, Object>> result = listFeignClient.searchList(searchParam);
        // 返回检索结果
        model.addAllAttributes(result.getData());
        // 返回searchParam，搜索框回填
        model.addAttribute("searchParam", searchParam);
        // 拼接url路径
        String urlParam = this.makeUrlParam(searchParam);
        model.addAttribute("urlParam", urlParam);
        // 封装排序规则
        Map<String, String> orderMap = this.makeOrderParam(searchParam);
        model.addAttribute("orderMap", orderMap);
        // 封装面包屑信息
        this.makeCrumbParam(searchParam, model);
        return "list/index";
    }

    private void makeCrumbParam(SearchParam searchParam, Model model) {
        // 品牌面包屑
        StringBuilder trademarkParam = new StringBuilder();
        String trademark = searchParam.getTrademark();
        if (StringUtils.isNotBlank(trademark)) {
            String[] trademarks = trademark.split(":");
            if (trademarks.length == 2) {
                trademarkParam
                        .append("品牌：")
                        .append("<span style='color:red'>")
                        .append(trademarks[1])
                        .append("</span>");
            }
        }
        model.addAttribute("trademarkParam", trademarkParam.toString());
        // 平台属性面包屑
        String[] props = searchParam.getProps();
        // 23:4G:运行内存
        List<Map<String, Object>> propsParamList = new ArrayList<>();
        if (props != null && props.length > 0) {
            propsParamList = Arrays.stream(props).map(prop -> {
                String[] propArr = prop.split(":");
                Map<String, Object> propMap = new HashMap<>();
                if (propArr.length == 3) {
                    propMap.put("attrId", propArr[0]);
                    propMap.put("attrValue", propArr[1]);
                    propMap.put("attrName", propArr[2]);
                }
                return propMap;
            }).collect(Collectors.toList());
        }
        model.addAttribute("propsParamList", propsParamList);
    }

    private Map<String, String> makeOrderParam(SearchParam searchParam) {
        String order = searchParam.getOrder();
        Map<String, String> orderMap = new HashMap<>(1);
        if (StringUtils.isNotBlank(order)) {
            String[] orders = order.split(":");
            if (orders.length == 2) {
                orderMap.put("type", orders[0]);
                orderMap.put("sort", orders[1]);
                return orderMap;
            }
        }
        orderMap.put("type", "1");
        orderMap.put("sort", "desc");
        return orderMap;
    }

    private String makeUrlParam(SearchParam searchParam) {
        StringBuilder urlParam = new StringBuilder();
        // 关键字查询和分类是互斥的，所以只要有关键字，就不用再添加分类参数

        // 关键字
        if (StringUtils.isNotBlank(searchParam.getKeyword())) {
            urlParam.append("keyword=").append(searchParam.getKeyword());
        }
        // 一级分类
        if (searchParam.getCategory1Id() != null && searchParam.getCategory1Id() > 0) {
            urlParam.append("category1Id=").append(searchParam.getCategory1Id());
        }
        // 二级分类
        if (searchParam.getCategory2Id() != null && searchParam.getCategory2Id() > 0) {
            urlParam.append("category2Id=").append(searchParam.getCategory2Id());
        }
        // 三级分类
        if (searchParam.getCategory3Id() != null && searchParam.getCategory3Id() > 0) {
            urlParam.append("category3Id=").append(searchParam.getCategory3Id());
        }

        // 必须要有关键字查询或者分类查询，才能进行其他条件查询
        if (urlParam.length() > 0) {
            // 品牌属性
            if (StringUtils.isNotBlank(searchParam.getTrademark())) {
                urlParam.append("&trademark=").append(searchParam.getTrademark());
            }

            // 平台属性 23:4G:运行内存
            String[] props = searchParam.getProps();
            if (props != null && props.length > 0) {
                Arrays.stream(props).forEach(prop -> urlParam.append("&props=").append(prop));
            }
            // 排序字段
            // if (StringUtils.isNotBlank(searchParam.getOrder())) {
            //     urlParam.append("&order=").append(searchParam.getOrder());
            // }
        }

        return "list.html?" + urlParam;
    }

}
