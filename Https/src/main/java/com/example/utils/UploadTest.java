package com.example.utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class UploadTest {
    private String host;
    private String cookie;
    private String path;
    private int port;
    private SSLContext sslContext;
    private X509TrustManager tm;
    private HttpClient httpClient;

    public UploadTest(String host, String cookie, String path, int port)
            throws Exception {
        this.host = host;
        this.cookie = cookie;
        this.path = path;
        this.port = port;
        sslContext = SSLContext.getInstance("TLS");
        tm = new MyX509TrustManager();
        sslContext.init(null, new TrustManager[] { tm },
                new java.security.SecureRandom());
        httpClient = HttpClients.custom()
                .setSSLHostnameVerifier(new MyVerifyHostname())
                .setSSLContext(sslContext).build();
    }

    public String uploadFile(String localPath, String filename)
            throws URISyntaxException, ClientProtocolException, IOException {

        URI uri = new URIBuilder().setScheme("https").setHost(host)
                .setPath(path).setParameter("filename", filename).setPort(port)
                .build();
        HttpPost httpPost = new HttpPost(uri);
        FileEntity fileEntity = new FileEntity(new File(localPath));
        fileEntity.setChunked(true);
        httpPost.setEntity(fileEntity);
        httpPost.addHeader("Cookie", cookie);
        HttpResponse response = httpClient.execute(httpPost);
        HttpEntity httpEntity = response.getEntity();
        httpEntity = new BufferedHttpEntity(httpEntity);
        String resultString = EntityUtils.toString(httpEntity);
        return resultString;
    }

    /*public static void main(String[] args) {
        try {
            UploadTest test = new UploadTest(
                    "127.0.0.1",
                    null,
                    "/get/index.htm", 8443);
            String resultString = test.uploadFile(
                    "/Users/justyoung/Desktop/upload/test.java", "mytest1");
            System.out.println(resultString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
}
