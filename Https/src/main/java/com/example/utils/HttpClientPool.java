package com.example.utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.MessageConstraints;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by mazhenhua on 2016/12/23.
 */

public class HttpClientPool {


    private String host;
    private String cookie;
    private String path;
    private int port;
    private SSLContext sslContext;
    private X509TrustManager tm;
    private HttpClient httpClient;
    private Registry<ConnectionSocketFactory> registry;
    private ConnectionSocketFactory plainSF;
    private LayeredConnectionSocketFactory sslSF;



    int                                       maxPerRoute              = 15;     // 每个路由允许活跃连接数
    int                                       maxTotal                 = 100;    // 所有路由总连接数上限
    int                                       maxLineLength            = 0;     // 每一个请求行的最大行长度(坑已踩)
    int                                       connectionRequestTimeout = 1000000000;  // 向连接池申请连接超时，单位：毫秒
    int                                       connectTimeout           = 6000;   // 建立连接超时，单位：毫秒
    int                                       socketTimeout            = 10000;  // 等待数据超时，单位：毫秒

    public HttpClientPool(String host, String cookie, String path, int port)
            throws Exception {
        this.host = host;
        this.cookie = cookie;
        this.path = path;
        this.port = port;
        sslContext = SSLContext.getInstance("TLS");
        tm = new MyX509TrustManager(); // 那个读取公钥的实现类
        sslContext.init(null, new TrustManager[] { tm },
                new java.security.SecureRandom());

        /* 不使用连接次
        httpClient = HttpClients.custom()
                .setSSLHostnameVerifier(new MyVerifyHostname())
                .setSSLContext(sslContext).build();*/


        /** 以下是连接池区域 具体的参数，可以根据情况自己调整**/
        sslSF = new SSLConnectionSocketFactory(sslContext, new MyVerifyHostname()); // 验证host的那个
        RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder
                .<ConnectionSocketFactory> create();
        plainSF = new PlainConnectionSocketFactory();
        registryBuilder.register("http", plainSF);
        registryBuilder.register("https", sslSF);
        registry = registryBuilder.build();
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(
                registry);
        connManager.setDefaultMaxPerRoute(maxPerRoute);
        connManager.setMaxTotal(maxTotal);
        ConnectionConfig connConfig = ConnectionConfig
                .custom()
                .setMessageConstraints(
                        MessageConstraints.custom().setMaxLineLength(maxLineLength).build()).build();
        connManager.setDefaultConnectionConfig(connConfig);

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(connectionRequestTimeout)
                .setConnectTimeout(connectTimeout).setSocketTimeout(socketTimeout).build();

        httpClient = HttpClients.custom().setConnectionManager(connManager)
                .setDefaultRequestConfig(requestConfig).build();
    }

    public String sendPost(String localPath, String filename)
            throws URISyntaxException, ClientProtocolException, IOException {

        URI uri = new URIBuilder().setScheme("https").setHost(host)
                .setPath(path).setParameter("filename", filename).setPort(port)
                .build();
        HttpPost httpPost = new HttpPost(uri);
        /**这个部分是支持带上传的*/
        FileEntity fileEntity = new FileEntity(new File(localPath));
        //fileEntity.setChunked(true);
        //httpPost.setEntity(fileEntity);
        httpPost.addHeader("Cookie", cookie);
        HttpResponse response = httpClient.execute(httpPost);
        HttpEntity httpEntity = response.getEntity();
        httpEntity = new BufferedHttpEntity(httpEntity);
        String resultString = EntityUtils.toString(httpEntity);
        return resultString;
    }

    public <T> T post(String uri, Map<String, String> postParam, String encoding,
                      ResponseHandler<T> handler) throws ClientProtocolException, IOException {
        List<NameValuePair> formParams = new ArrayList<NameValuePair>();
        for (Map.Entry<String, String> entry : postParam.entrySet()) {
            formParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        HttpPost request = new HttpPost(uri);
        request.setEntity(new UrlEncodedFormEntity(formParams, encoding));
        return httpClient.execute(request, handler);
    }

    public <T> T get(String uri, ResponseHandler<T> handler) throws IOException {
        HttpGet request = new HttpGet(uri);
        return httpClient.execute(request, handler);
    }


   /* public static void main(String[] args) {
        try {
            HttpClientPool test = new HttpClientPool(
                    "127.0.0.1",
                    null,
                    "/post/index.htm", 8443);
            String resultString = test.sendPost(null, null);
            System.out.println(resultString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

}
