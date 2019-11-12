package com.ice.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;

import com.google.cloud.bigquery.*;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PushConfig;
import com.google.pubsub.v1.Subscription;
import com.ice.model.*;
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

    @RequestMapping("/bigQueryData")
    public String getbigQueryData(Model model) throws InterruptedException {
        List respList=new ArrayList<String>();
        List respListToken=new ArrayList<String>();

        BigQuery bigquery= BigQueryOptions.getDefaultInstance().getService();
        String tableName="eep-iceberg-project-257702.iceberg_poc.client_transactions";
        String query="SELECT * '%s' ;";
        QueryJobConfiguration queryConfig= QueryJobConfiguration.newBuilder(String.format(query, tableName)).build();
        for(FieldValueList row: bigquery.query(queryConfig).iterateAll()){
            ClientTrx trx=new ClientTrx();
            trx.setClientId(row.get("clientId").getStringValue());
            trx.setTransanctionInJson(row.get("transanctionInJson").getStringValue());
            respList.add(trx);
        }
        tableName="eep-iceberg-project-257702.iceberg_poc.client_id_accessTokens";
        queryConfig= QueryJobConfiguration.newBuilder(String.format(query, tableName)).build();
        for(FieldValueList row: bigquery.query(queryConfig).iterateAll()){
            ClientTokens token=new ClientTokens();
            token.setClientId(row.get("clientId").getStringValue());
            token.setBankAISP(row.get("bankAISP").getStringValue());
            token.setAccessToken(row.get("accessToken").getStringValue());
            token.setRefreshToken(row.get("refreshToken").getStringValue());
            token.setIdToken(row.get("idToken").getStringValue());
            token.setTokenType(row.get("tokenType").getStringValue());
            token.setExpiresIn(row.get("expiresIn").getStringValue());
            token.setCurrentTimeStamp(row.get("currentTimestamp").getStringValue());
            respListToken.add(token);
        }

        model.addAttribute("respListToken",respListToken);
        model.addAttribute("respList",respList);
        return "bigQueryData";
    }

    @RequestMapping("/filterData")
    public String getfilterData(Model model) throws InterruptedException {
        List respList=new ArrayList<String>();
        List respListToken=new ArrayList<String>();

        BigQuery bigquery= BigQueryOptions.getDefaultInstance().getService();
        String tableName="eep-iceberg-project-257702.iceberg_poc.client_transaction_filter_config";
        String query="SELECT * '%s' ;";
        QueryJobConfiguration queryConfig= QueryJobConfiguration.newBuilder(String.format(query, tableName)).build();
        for(FieldValueList row: bigquery.query(queryConfig).iterateAll()){
            FilterConfig config=new FilterConfig();
            config.setBankAISP(row.get("bankAISP").getStringValue());
            config.setFilter_Path(row.get("filter_Path").getStringValue());
            config.setFilter_Table_Schema(row.get("filter_Table_Schema").getStringValue());
            respList.add(config);
        }
        tableName="eep-iceberg-project-257702.iceberg_poc.client_transaction_filtered";
        queryConfig= QueryJobConfiguration.newBuilder(String.format(query, tableName)).build();
        for(FieldValueList row: bigquery.query(queryConfig).iterateAll()){
            Filtered filtered=new Filtered();
            filtered.setClientId(row.get("clientId").getStringValue());
            filtered.setCreditDebitIndicator(row.get("CreditDebitIndicator").getStringValue());
            filtered.setStatus(row.get("Status").getStringValue());
            filtered.setBookingDateTime(row.get("BookingDateTime").getStringValue());
            filtered.setAccountId(row.get("accountId").getStringValue());
            filtered.setAmountAmount(row.get("amount.Amount").getStringValue());
            filtered.setAmountCurrency(row.get("amount.currency").getStringValue());
            filtered.setTransactionInformation(row.get("TransactionInformation").getStringValue());

            respListToken.add(filtered);
        }

        model.addAttribute("respListToken",respListToken);
        model.addAttribute("respList",respList);
        return "filterData";
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
