package com.ice.scheduler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ice.process.ApiFiter;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URL;

@Component
public class FetchApi {

    private static final Logger logger= LoggerFactory.getLogger(FetchApi.class);


    @Autowired
    ApiFiter apiFiter;

    public void processTask() throws IOException {
        logger.info("batch start");
        URL resource = FetchApi.class.getResource("/APISource.json");
        String path = resource.getPath();
        File file = new File(path);
        String fileContent= FileUtils.readFileToString(file,"utf-8");
        JSONArray array= JSON.parseArray(fileContent);
        for(int i=0;i<array.size();i++){
            JSONObject json = array.getJSONObject(i);
            try {
                String jsonResp = apiFiter.getApiContent(json.getString("url"), json.getString("clientIdKey"),
                        json.getString("clientId"), json.getString("secretKey"), json.getString("clientSecret"),
                        json.getString("method"), json.getString("para"),json.getString("reqType"));
                logger.info("response is {}", jsonResp);
            }catch (Exception e){
                logger.error("fail to fetch API: {}",json.getString("url"),e);
            }
        }
        logger.info("batch complete");
    }
}
