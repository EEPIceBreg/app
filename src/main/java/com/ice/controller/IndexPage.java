package com.ice.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;

import com.google.cloud.bigquery.*;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PushConfig;
import com.google.pubsub.v1.Subscription;
import com.ice.model.BigqueryModel;
import com.ice.model.RequestUrl;
import com.ice.model.SubModel;
import com.ice.process.ApiFiter;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class IndexPage {

    private static final Logger logger = LoggerFactory.getLogger(IndexPage.class);

    private static final String FILE_NAME = "/iceberg/apis.json";

    @Autowired
    ApiFiter apiFiter;

    @RequestMapping("/index")
    public String indexHtml(Model model) throws IOException {
        model.addAttribute("apiList", getApisList());
        model.addAttribute("requestUrl", new RequestUrl());
        return "index";
    }

    @RequestMapping("/dashboard")
    public String dashboardHtml(Model model) {
        model.addAttribute("bigqueryModel", new BigqueryModel());
        return "dashboard";
    }

    @RequestMapping("/fetchAPI")
    @ResponseBody
    public String fetchApi(@RequestBody RequestUrl requestUrl) {
        String jsonResp = apiFiter.getApiContent(requestUrl.getUrl(), requestUrl.getClientIdKey(),
                requestUrl.getClientId(), requestUrl.getSecretKey(), requestUrl.getClientSecret(),
                requestUrl.getMethod(), requestUrl.getPara(), requestUrl.getRequestType());
        logger.info("response is {}", jsonResp);
        return jsonResp;
    }

    @RequestMapping("/refreshApis")
    public ModelAndView refreshApis(@ModelAttribute RequestUrl requestUrl) throws IOException {
        String jsonResp = apiFiter.getApiContent(requestUrl.getUrl(), requestUrl.getClientIdKey(),
                requestUrl.getClientId(), requestUrl.getSecretKey(), requestUrl.getClientSecret(),
                requestUrl.getMethod(), requestUrl.getPara(), requestUrl.getRequestType());
        logger.info("response is {}", jsonResp);
        List<RequestUrl> apiList = getApisList();
        for (RequestUrl requestUrl1 : apiList) {
            if (requestUrl1.getUrl().equals(requestUrl.getUrl())) {
                requestUrl1.setResponse(jsonResp);
            }
        }
        File jsonFile = new File(FILE_NAME);
        FileUtils.writeStringToFile(jsonFile, JSON.toJSONString(apiList), "UTF-8");
        ModelAndView mv = new ModelAndView("redirect:/index");
        return mv;
    }

    @PostMapping("newApis")
    public ModelAndView addApis(@ModelAttribute RequestUrl requestUrl, Model model) throws IOException {
        File jsonFile = new File(FILE_NAME);
        String fileContent = FileUtils.readFileToString(jsonFile, "UTF-8");
        JSONArray array = JSON.parseArray(fileContent);
        String strJson = JSON.toJSONString(requestUrl);
        array.add(strJson);
        FileUtils.writeStringToFile(jsonFile, array.toJSONString(), "UTF-8");
        model.addAttribute("apiList", getApisList());
        model.addAttribute("requestUrl", new RequestUrl());
        ModelAndView mv = new ModelAndView("redirect:/index");
        return mv;
    }

    @PostMapping("/subData")
    public ModelAndView getGcpData(@ModelAttribute SubModel subModel, Model model) {
        try (SubscriptionAdminClient subscriptionAdminClient = SubscriptionAdminClient.create()) {
            ProjectSubscriptionName subName = ProjectSubscriptionName.of(subModel.getProject(), subModel.getSubScription());
            ProjectTopicName topic = ProjectTopicName.of(subModel.getProject(), subModel.getTop());
            PushConfig pushConfig = PushConfig.newBuilder().build();
            Subscription response = subscriptionAdminClient.createSubscription(subName, topic, pushConfig, 10);
            model.addAttribute("respText", response.toString());
            model.addAttribute("respMap",response.getLabelsMap());
        } catch (Exception e) {
            logger.error("fetch data from Pubsub fail", e);
        }
        ModelAndView mv = new ModelAndView("redirect:/subData");
        return mv;
    }

    @PostMapping("/bigQueryData")
    public String getbigQueryData(@ModelAttribute BigqueryModel bigqueryModel, Model model) throws InterruptedException {
        List respList=new ArrayList<String>();

        BigQuery bigquery= BigQueryOptions.getDefaultInstance().getService();
        String query="SELECT * '%s' ;";
        QueryJobConfiguration queryConfig= QueryJobConfiguration.newBuilder(String.format(query, bigqueryModel.getTable())).build();
        for(FieldValueList row: bigquery.query(queryConfig).iterateAll()){
            for(FieldValue val:row){
                respList.add(val.toString());
            }
        }
        model.addAttribute("respList",respList);
        return "bigQueryData";
    }

    @RequestMapping("/test")
    @ResponseBody
    public String test() {
        return "{\"json\":\"success\"}";
    }

    private List getApisList() throws IOException {
        List apiList = new ArrayList<RequestUrl>();
        File jsonFile = new File(FILE_NAME);
        String fileContent = FileUtils.readFileToString(jsonFile, "UTF-8");
        JSONArray array = JSON.parseArray(fileContent);
        for (int i = 0; i < array.size(); i++) {
            String json = array.getString(i);
            RequestUrl url = JSON.parseObject(json, RequestUrl.class);
            apiList.add(url);
        }
        return apiList;
    }


}
