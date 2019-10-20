package com.ice.controller;

import com.ice.model.RequestUrl;
import com.ice.process.ApiFiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class IndexPage {

    private static final Logger logger= LoggerFactory.getLogger(IndexPage.class);

    @Autowired
    ApiFiter apiFiter;

    @RequestMapping("/index")
    public String indexHtml(){
        return "index";
    }

    @RequestMapping("/fetchAPI")
    @ResponseBody
    public String fetchApi(@RequestBody RequestUrl requestUrl){
        String jsonResp = apiFiter.getApiContent(requestUrl.getUrl(), requestUrl.getClientIdKey(),
                requestUrl.getClientId(), requestUrl.getSecretKey(), requestUrl.getClientSecret(),
                requestUrl.getMethod(), requestUrl.getPara());
        logger.info("response is {}", jsonResp);
        return jsonResp;
    }

    @RequestMapping("/test")
    @ResponseBody
    public String test(){
        return "{\"json\":\"success\"}";
    }
}
