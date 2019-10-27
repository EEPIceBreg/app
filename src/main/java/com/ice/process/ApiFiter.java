package com.ice.process;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;


@Service
public class ApiFiter {

    private static final Logger logger = LoggerFactory.getLogger(ApiFiter.class);

    private RestTemplate restTemplate = buildRestTemplate();

    public ApiFiter() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
    }

    public String getApiContent(String strUrl, String clientIdKey, String clientId,
                                String secretKey, String clientSecret, String method,
                                String para,String reqType) {
        String responseText;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type",reqType);
        if (null != clientIdKey) {
            headers.set(clientIdKey, clientId);
            headers.set(secretKey, clientSecret);
        }
        logger.info("start requesting {}", strUrl);
        if ("post".equals(method)) {
            HttpEntity<String> httpEntity = new HttpEntity<>(para, headers);
            responseText = restTemplate.exchange(strUrl, HttpMethod.POST, httpEntity, String.class).getBody();
        } else {
            StringBuffer uri = new StringBuffer(strUrl);
            uri.append(para);
            HttpEntity<String> httpEntity = new HttpEntity<>(headers);
            responseText = restTemplate.exchange(uri.toString(), HttpMethod.GET, httpEntity, String.class).getBody();
        }
        logger.info("complete requesting {}", strUrl);
        return responseText;
    }

    private RestTemplate buildRestTemplate() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        HttpComponentsClientHttpRequestFactory factory = new
                HttpComponentsClientHttpRequestFactory();
        // https
        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, (X509Certificate[] x509Certificates, String s) -> true);
        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(builder.build(), new String[]{"SSLv2Hello", "SSLv3", "TLSv1", "TLSv1.2"}, null, NoopHostnameVerifier.INSTANCE);
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", new PlainConnectionSocketFactory())
                .register("https", socketFactory).build();
        PoolingHttpClientConnectionManager phccm = new PoolingHttpClientConnectionManager(registry);
        phccm.setMaxTotal(200);
        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(socketFactory).setConnectionManager(phccm).setConnectionManagerShared(true).build();
        factory.setHttpClient(httpClient);
        RestTemplate restTemplate = new RestTemplate(factory);
        return restTemplate;
    }
}
