package com.ice.process;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
public class ApiFiter {

    private static final Logger logger= LoggerFactory.getLogger(ApiFiter.class);

    private RestTemplate restTemplate=new RestTemplate();

    public String getApiContent(String strUrl, String clientIdKey, String clientId,
                                    String secretKey, String clientSecret, String method,
                                    String para){
        String responseText;
        HttpHeaders headers=new HttpHeaders();
        headers.set(clientIdKey,clientId);
        headers.set(secretKey,clientSecret);
        logger.info("start requesting {}",strUrl);
        if("post".equals(method)){
            HttpEntity<String> httpEntity=new HttpEntity<>(para,headers);
            responseText =restTemplate.exchange(strUrl, HttpMethod.POST,httpEntity,String.class).getBody();
        }else{
            StringBuffer uri=new StringBuffer(strUrl);
            uri.append(para);
            HttpEntity<String> httpEntity=new HttpEntity<>(headers);
            responseText =restTemplate.exchange(uri.toString(), HttpMethod.GET,httpEntity,String.class).getBody();
        }
        logger.info("complete requesting {}",strUrl);
        return responseText;
    }
}
