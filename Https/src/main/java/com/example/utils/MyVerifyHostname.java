package com.example.utils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * Created by mazhenhua on 2016/12/23.
 */
public class MyVerifyHostname implements HostnameVerifier {

    @Override
    public boolean verify(String arg0, SSLSession arg1) {
        if (arg0.equals("127.0.0.1") || arg0.equals("mazhenhua.com"))
            return true;
        else
            return false;
    }
}
