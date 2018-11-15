package cn.creditease.fso.kratos.utils;

import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author:duangduangda
 * 
 */
public class HttpClientProxy {

    /**
     * 全局连接池对象
     */
    private static final PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();

    private static final int HTTP_DEFAULT_TIMEOUT = 6000;

    private static final int HTTP_MAX_RETRY = 5;

    /**
     * 静态代码块配置连接池信息
     */
    static {

        // 设置最大连接数
        connManager.setMaxTotal(200);
        // 设置每个连接的路由数
        connManager.setDefaultMaxPerRoute(20);

    }

    /**
     * 获取Http客户端连接对象
     *
     * @param timeOut 超时时间
     * @return Http客户端连接对象
     */
    private static CloseableHttpClient getHttpClient(Integer timeOut) {
        // 创建Http请求配置参数
        RequestConfig requestConfig = RequestConfig.custom()
                // 获取连接超时时间
                .setConnectionRequestTimeout(timeOut)
                // 请求超时时间
                .setConnectTimeout(timeOut)
                // 响应超时时间
                .setSocketTimeout(timeOut)
                .build();


        // 创建httpClient
        return HttpClients.custom()
                // 把请求相关的超时信息设置到连接客户端
                .setDefaultRequestConfig(requestConfig)
                // 把请求重试设置到连接客户端
                .setRetryHandler(retryHandler())
                // 配置连接池管理对象
                .setConnectionManager(connManager)
                // 创建实例
                .build();

    }

    /**
     * 超时重试机制，如果直接放回false,不重试
     *
     * @return
     */
    private static HttpRequestRetryHandler retryHandler() {
        return (IOException exception, int executionCount, HttpContext context) -> {
            // 如果已经重试了HTTP_MAX_RETRY次，就放弃
            if (executionCount >= HTTP_MAX_RETRY) {
                return false;
            }

            // 如果服务器丢掉了连接，那么就重试
            if (exception instanceof NoHttpResponseException) {
                return true;
            }

            // 不要重试SSL握手异常
            if (exception instanceof SSLHandshakeException) {
                return false;
            }

            // 超时
            if (exception instanceof InterruptedIOException) {
                return true;
            }

            // 目标服务器不可达
            if (exception instanceof UnknownHostException) {
                return false;
            }

            // 连接被拒绝
            if (exception instanceof ConnectTimeoutException) {
                return false;
            }

            // ssl握手异常
            if (exception instanceof SSLException) {
                return false;
            }

            HttpClientContext clientContext = HttpClientContext.adapt(context);
            HttpRequest request = clientContext.getRequest();
            // 如果请求是幂等的，就再次尝试
            if (!(request instanceof HttpEntityEnclosingRequest)) {
                return true;
            }
            return false;
        };
    }

    /**
     * 发送HttpGet请求
     *
     * @param url
     * @return
     */
    public static String sendGet(String url) {
        return sendGet(url, HTTP_DEFAULT_TIMEOUT);
    }

    /**
     * 发送HttpGet请求
     *
     * @param url
     * @param timeout
     * @return
     */
    public static String sendGet(String url, int timeout) {
        HttpGet httpget = new HttpGet(url);
        return handleRequest(httpget, timeout);
    }

    /**
     * 发送HttpPost请求
     *
     * @param url
     * @return
     */
    public static String sendPost(String url) {
        return sendPost(url, null, HTTP_DEFAULT_TIMEOUT);
    }

    /**
     * 发送HttpPost请求
     *
     * @param url
     * @param timeout
     * @return
     */
    public static String sendPost(String url, int timeout) {
        return sendPost(url, null, timeout);
    }

    /**
     * 发送HttpPost请求，参数为map
     *
     * @param url     请求url
     * @param map     请求参数
     * @param timeout 超时时间
     * @return
     */
    public static String sendPost(String url, Map<String, String> map, int timeout) {
        List<NameValuePair> formparams = new ArrayList<>(10);
	if(null != map){
        for (Map.Entry<String, String> entry : map.entrySet()) {
            formparams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
	}
        UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
        HttpPost httppost = new HttpPost(url);
        httppost.setEntity(urlEncodedFormEntity);
        return handleRequest(httppost, timeout);
    }

    /**
     * 发送HttpPost请求，参数为json字符串
     *
     * @param url
     * @param paramJson
     * @return
     */
    public static String sendPost(String url, String paramJson) {
        StringEntity entity = new StringEntity(paramJson, Consts.UTF_8);
        entity.setContentEncoding(Consts.UTF_8.toString());
        entity.setContentType("application/json");
        HttpPost httppost = new HttpPost(url);
        httppost.setEntity(entity);
        return handleRequest(httppost, HTTP_DEFAULT_TIMEOUT);
    }

    /**
     * 发送HttpPost请求，参数为json字符串,附带cookie参数
     *
     * @param url
     * @param paramJson
     * @return
     */
    public static String sendPostWithCookies(String url, String paramJson, Map<String, Object> cookiesParams) {
        CookieStore cookieStore = new BasicCookieStore();
        CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();

        StringEntity entity = new StringEntity(paramJson, "utf-8");
        entity.setContentEncoding("UTF-8");
        entity.setContentType("application/json");
        HttpPost httppost = new HttpPost(url);
        httppost.setEntity(entity);
        CloseableHttpResponse response = null;
        String result = null;
        try {
            String domain = (String) cookiesParams.get("domain");
            String path = (String) cookiesParams.get("path");
            Map<String, Object> cookieDataMap = (Map<String, Object>) cookiesParams.get("cookieData");
            BasicClientCookie cookie = null;
            HttpEntity httpEntity = response.getEntity();

            for (Map.Entry entry : cookieDataMap.entrySet()) {
                cookie = new BasicClientCookie((String) entry.getKey(), (String) entry.getValue());
                cookie.setDomain(domain);
                cookie.setPath(path);
                cookieStore.addCookie(cookie);
            }
            response = httpclient.execute(httppost);
            result = EntityUtils.toString(httpEntity, Charset.defaultCharset());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                response.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 发送HttpPost请求，参数为json字符串,Content-type：application/x-www-form-urlencoded
     *
     * @param url
     * @param map
     * @return
     */
    public static String sendPostByForm(String url, Map<String, String> map) {
        List<NameValuePair> formparams = new ArrayList<>(10);
        for (Map.Entry<String, String> entry : map.entrySet()) {
            formparams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
        HttpPost httppost = new HttpPost(url);
        httppost.setEntity(urlEncodedFormEntity);
        httppost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        return handleRequest(httppost, HTTP_DEFAULT_TIMEOUT);
    }


    /**
     * 执行http请求
     *
     * @param httpRequestBase 请求类型get,post,put,delete
     * @param timeout         请求超时时间
     * @return
     */
    private static String handleRequest(HttpRequestBase httpRequestBase, int timeout) {
        CloseableHttpClient httpclient = getHttpClient(timeout);
        String result = null;
        CloseableHttpResponse response = null;
        try {
            response = httpclient.execute(httpRequestBase);
            int status = response.getStatusLine().getStatusCode();
            if (status >= HttpStatus.SC_OK && status < HttpStatus.SC_MULTIPLE_CHOICES) {
                HttpEntity httpEntity = response.getEntity();
                result = EntityUtils.toString(httpEntity, Charset.defaultCharset());
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
        return result;
    }

}
