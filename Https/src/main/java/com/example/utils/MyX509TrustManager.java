package com.example.utils;

import javax.net.ssl.X509TrustManager;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * Created by mazhenhua on 2016/12/23.
 */
public class MyX509TrustManager implements X509TrustManager {
    private Certificate cert = null;

    public MyX509TrustManager() {
        try {
            FileInputStream fis = new FileInputStream(
                    "C:\\Users\\mazhenhua\\Desktop\\cert\\test.cer");
            BufferedInputStream bis = new BufferedInputStream(fis);

            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            while (bis.available() > 0) {
                cert = cf.generateCertificate(bis);
				System.out.println(cert.toString());
            }
            bis.close();
        } catch (Exception e) {

        }
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
    }

    @Deprecated
    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        for (X509Certificate cert : chain) {
            if (cert.toString().equals(this.cert.toString()))
                return;
        }
        throw new CertificateException("certificate is illegal");
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[] { (X509Certificate) cert };
    }
}
