package com.bigbrassband.util.atlas.installapp;

import javax.net.ssl.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

public class SslFixer {

    private static final TrustManager[] trustAllCerts = {new X509TrustManager() {
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public void checkClientTrusted(X509Certificate[] certs, String authType) {
        }

        public void checkServerTrusted(X509Certificate[] certs, String authType) {
        }
    }};
    private static final HostnameVerifier nullVerifier = (s, sslSession) -> true;

    public static SSLContext getInsecureSslContext() {
        try {
            SSLContext e = SSLContext.getInstance("SSL");
            e.init(null, trustAllCerts, new SecureRandom());
            return e;
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new java.lang.RuntimeException(e);
        }
    }

    public static HostnameVerifier getNullVerifier() {
        return nullVerifier;
    }
}
