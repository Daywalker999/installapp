package com.bigbrassband.util.atlas.installapp;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.http.Header;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.json.JSONObject;

import java.io.File;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;

// https://community.atlassian.com/t5/Answers-Developer-Questions/Install-plugin-into-Jira-using-UPM-REST-API/qaq-p/486119

public class Main {
    private static final String PLUGINS_PATH = "/rest/plugins/1.0/";

    static public void main(String args[]) throws Exception {

        if (args.length != 1) {
            System.err.println("USAGE");
            System.err.println("java -jar installapp.jar config.json");
            System.exit(1);
        }


        JSONObject jsonConfig = new JSONObject(FileUtils.readFileToString(new File(args[0]), StandardCharsets.UTF_8));

        Main.performUpload(jsonConfig);

    }

    public static void performUpload(JSONObject jsonConfig) throws Exception
	{
		String baseUrl = jsonConfig.getString("baseUrl");
		String username = jsonConfig.getString("username");
		String password = jsonConfig.getString("password");
		File appFile = new File(jsonConfig.getString("appFile"));
		boolean ignoreCertificateErrors = jsonConfig.optBoolean("ignoreCertificateErrors", false);
		int timeoutMilliseconds = jsonConfig.optInt("timeoutMilliseconds", 10000);

		try (final CloseableHttpClient httpClient = createHttpClient(ignoreCertificateErrors, timeoutMilliseconds, username, password)) {
			final BasicHeader authHeader = new BasicHeader("authorization", "Basic " + Base64.encodeBase64String((username + ":" + password).getBytes(StandardCharsets.UTF_8)));

			String token = getToken(baseUrl, authHeader, httpClient);
			uploadFile(baseUrl, authHeader, httpClient, appFile, token);
		}
	}

    private static void uploadFile(String baseUrl, BasicHeader authHeader, CloseableHttpClient httpClient, File file, String token) throws Exception {
        HttpPost request = new HttpPost();
        request.setHeader(authHeader);
        request.setURI(new URI(baseUrl + PLUGINS_PATH + "?token=" + token));
        request.setEntity(MultipartEntityBuilder.create().addBinaryBody("plugin", file)
                .build());

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            if (response.getStatusLine().getStatusCode() != 202)
                throw new Exception(response.getStatusLine().toString());
        }
    }

    private static String getToken(String baseUrl, BasicHeader authHeader, CloseableHttpClient httpClient) throws Exception {
        HttpHead request = new HttpHead();
        request.setURI(new URI(baseUrl + PLUGINS_PATH + "?os_authType=basic"));
        request.setHeader(authHeader);

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            final Header[] headers = response.getHeaders("upm-token");
            if (headers.length == 0)
                throw new Exception("Could not find upm-token");

            return headers[0].getValue();
        }
    }

    private static CloseableHttpClient createHttpClient(boolean ignoreCertificateErrors, int timeoutMilliseconds, String username, String password) {
        HttpClientBuilder httpClientBuilder = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setSocketTimeout(timeoutMilliseconds)
                        .setConnectTimeout(timeoutMilliseconds)
                        .setConnectionRequestTimeout(timeoutMilliseconds)
                        .build());

        if (ignoreCertificateErrors) { //disable certificate error?
            httpClientBuilder.setSSLHostnameVerifier(SslFixer.getNullVerifier())
                    .setSSLContext(SslFixer.getInsecureSslContext());
        }

        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(username, password));

        httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);

        return httpClientBuilder.build();
    }

    private static void enableWireLogging() {
        ConsoleAppender console = new ConsoleAppender();
        console.setWriter(new OutputStreamWriter(System.out));
        console.setLayout(new PatternLayout("%d [%t] %-5p %c -  %m%n"));
        Logger rootLogger = Logger.getRootLogger();
        rootLogger.addAppender(console);

        Logger logger = Logger.getLogger("log4j.logger.org.apache.http.wire");
        logger.setLevel(Level.DEBUG);
    }
}