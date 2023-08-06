package vip.zhguo.oneplus.Util;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
//import org.springframework.util.StringUtils;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * HttpClient 工具类
 */
@Slf4j
public class HttpClientUtil {
    public static final String APPLICATION_JSON_VALUE = "application/json";
    private static final Logger logger = log;
    private static final Integer CONN_TIME_OUT = 3000;// 超时时间豪秒
    private static final Integer SOCKET_TIME_OUT = 10000;

    /**
     * 每个路由的最大请求数，默认2
     */
    private static final Integer DEFAULT_MAX_PER_ROUTE = 1;

    /**
     * 最大连接数，默认20
     */
    private static final Integer MAX_TOTAL = 1;


    private static HttpClient httpClient;

    static {
        // 请求配置
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(CONN_TIME_OUT)
                .setConnectionRequestTimeout(CONN_TIME_OUT)
                .setSocketTimeout(SOCKET_TIME_OUT)
                .build();

        // 管理 http连接池
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setDefaultMaxPerRoute(DEFAULT_MAX_PER_ROUTE);
        cm.setMaxTotal(MAX_TOTAL);

        httpClient = HttpClients.custom().setConnectionManager(cm).setDefaultRequestConfig(requestConfig).build();
    }

    public static String requestGet(String url, Map<String, String> paramsMap) throws Exception {
        logger.info("GET request  url:{} params:{}", url, paramsMap);

        Long start = System.currentTimeMillis();

        List<NameValuePair> params = initParams(paramsMap);
        // Get请求
        HttpGet httpGet = new HttpGet(url);

        try {
            // 设置参数
            String str = EntityUtils.toString(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));
            String uriStr = StringUtils.isEmpty(str) ? httpGet.getURI().toString() : httpGet.getURI().toString() + "?" + str;
            httpGet.setURI(new URI(uriStr));
            // 发送请求
            HttpResponse response = httpClient.execute(httpGet);
            logger.info("GET request  url:{} response:{} time:{}",
                    url, response, System.currentTimeMillis() - start);
            // 获取返回数据
            return getSuccessRetFromResp(response, url, JSON.toJSONString(paramsMap));
        } finally {
            // 必须释放连接，负责连接用完后会阻塞
            httpGet.releaseConnection();
        }
    }


    /**
     * @param url
     * @param paramsMap
     * @return 响应结果
     */
    public static String requestPost(String url, Map<String, String> paramsMap) throws Exception {
        logger.info("POST request  url:{} params:{}", url, paramsMap);
        Long start = System.currentTimeMillis();

        List<NameValuePair> params = initParams(paramsMap);

        HttpPost httpPost = new HttpPost(url);

        try {

            httpPost.setEntity(new UrlEncodedFormEntity(params, Consts.UTF_8));

            HttpResponse response = httpClient.execute(httpPost);

            logger.info("POST request  url:{} response:{}  time:{}",
                    url, response, System.currentTimeMillis() - start);

            String retStr = getSuccessRetFromResp(response, url, JSON.toJSONString(paramsMap));


            return retStr;
        } finally {
            httpPost.releaseConnection();
        }
    }

    /**
     * POST json 格式数据
     */
    public static String requestPostJsonStr(String url, String json, String cookie) throws Exception {

        logger.info("POST request  url:{} params:{}", url, json);

        long start = System.currentTimeMillis();

        HttpPost httpPost = new HttpPost(url);

        try {
            StringEntity entity = new StringEntity(json, Consts.UTF_8);
            entity.setContentType(APPLICATION_JSON_VALUE);
            httpPost.setEntity(entity);

            HttpResponse response = httpClient.execute(httpPost);
//            HttpResponse response = httpClient.execute(httpPost, httpClientContext);

            logger.info("POST request  url:{} response:{}  time:{}", url, response, System.currentTimeMillis() - start);

            return getSuccessRetFromResp(response, url, json);
        } finally {
            // 资源释放
            httpPost.releaseConnection();
        }

    }

    /**
     * post json 格式数据
     *
     * @param url
     * @param obj
     * @return
     * @throws Exception
     */
    public static String requestPostJson(String url, Object obj, String cookie) throws Exception {
        String params = JSON.toJSONString(obj);
        return requestPostJsonStr(url, params, cookie);
    }

    private static String getSuccessRetFromResp(HttpResponse response, String url, String params) throws Exception {
        String retStr = "";
        // 检验状态码，如果成功接收数据
        int code = response.getStatusLine().getStatusCode();

        if (code == 200) {
            retStr = EntityUtils.toString(response.getEntity(), Consts.UTF_8);
        } else {
            throw new RuntimeException(String.format("Http request error:%s, url:%s, params:%s", response, url, params));
        }

        logger.info("Http request retStr:{}. url:{}", retStr, url);
        return retStr;
    }

    private static List<NameValuePair> initParams(Map<String, String> paramsMap) {

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        if (paramsMap == null)
            return params;

        for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
            params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }

        return params;
    }

    public static String requestPostString(String url, String data, String cookie) throws Exception {
        Long start = System.currentTimeMillis();
        //设置cookie
//        BasicCookieStore basicCookieStore = new BasicCookieStore();
//        BasicClientCookie cookie_ = new BasicClientCookie("cookie", cookie);
//        cookie_.setDomain("https://cloud.h2os.com/*");
////            cookie_.setPath("/");
//        basicCookieStore.addCookie(cookie_);
//        log.info("cookie_>>>>>>>>>>>>>" + cookie_.toString());
//        HttpClientContext httpClientContext = HttpClientContext.create();
//        httpClientContext.setCookieStore(basicCookieStore);
//        httpClient = HttpClients.custom().setProxy(new HttpHost("183.164.242.16",8089)).build();
//        httpClient = HttpClients.custom().build();
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("accept", "*/*");
        httpPost.setHeader("Accept-Encoding", "gzip, deflate, br");
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        httpPost.setHeader("cookie", cookie);
        httpPost.setHeader("connection", "Keep-Alive");
        httpPost.setHeader("host", "cloud.h2os.com");
        httpPost.setHeader("origin", "cloud.h2os.com");
        httpPost.setHeader("referer", "https://cloud.h2os.com/");
        httpPost.setHeader("X-Requested-With", "XMLHttpRequest");
        httpPost.setHeader("user-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/113.0.0.0 Safari/537.36 Edg/113.0.1774.50");
        try {

            httpPost.setEntity(new StringEntity(data.toString(), Consts.UTF_8));
            HttpResponse response = httpClient.execute(httpPost);

            logger.info("POST request  url:{} response:{}  time:{}",
                    url, response, System.currentTimeMillis() - start);

            String retStr = getSuccessRetFromResp(response, url, data);


            return retStr;
        } finally {
            httpPost.releaseConnection();
        }
    }
}
