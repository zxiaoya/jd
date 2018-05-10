package com.jd.utils;

import com.alibaba.fastjson.JSONObject;
import com.jd.model;
import com.jd.model.Pager;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


/**
 *
 * 描述：http请求工具类 
 * 
 * 作者： joke
 */
public class HttpUtilRequest {
	
	public static final String REQ_TYPE_POST = "post";
	public static final String REQ_TYPE_GET = "get";
	
	public static final int GET_STREAM = 1;
	public static final int GET_HTML = 0;
	
	public static final double REQ_PARAMS_ARRAY = 1;
	public static final double REQ_PARAMS_ENTITY = 0;
	
	public static final String CHARSET_UTF8 = "UTF-8";
	public static final String CHARSET_GBK = "GBK";
	public static final String CHARSET_GBK2312 = "gb2312";

	public static final String STRING_ENTITY_KEY = "content";
	
	
	// 设置 ssl context请求版本 默认为 SSLv3 ， 如果想修改通过 setSSLContextVersion 修改。
	public static String SSL_CONTEXT_VER = "SSLv3";
	public static String SSL_CONTEXT_VER_TLSv1 = "TLSv1";
	public static String SSL_CONTEXT_VER_TLSv1_1 = "TLSv1.1";
	public static String SSL_CONTEXT_VER_TLSv1_2 = "TLSv1.2";
	
	public static Logger logger = LoggerFactory.getLogger(HttpUtilRequest.class);
	
	public static Pager get(String url) {
		return req(commonHeaders(), null, REQ_TYPE_GET, null, url, GET_HTML, REQ_PARAMS_ARRAY, CHARSET_UTF8, false);
	}
	public static model.HttpUtilNewPage get(String url, List<Cookie> cookies) {
		return req(commonHeaders(), cookies, REQ_TYPE_GET, null, url, GET_HTML, REQ_PARAMS_ARRAY, CHARSET_UTF8, false);
	}
	public static List<Cookie> MergeCookies(List<Cookie> current_cookies, final List<Cookie> new_cookies)
	{
		if(new_cookies==null || new_cookies.isEmpty())
		{
			return current_cookies;
		}

		if(current_cookies==null || current_cookies.isEmpty())
		{
			return new_cookies;
		}

		for(Cookie c:new_cookies)
		{
			boolean already_exist = false;

			for(int i=0; i<current_cookies.size(); ++i)
			{
				if(current_cookies.get(i).getName().equals(c.getName()))
				{
					already_exist = true;
					current_cookies.set(i, c);
				}
			}

			if(!already_exist)
			{
				current_cookies.add(c);
			}
		}

		return current_cookies;
	}
	
	public static model.HttpUtilNewPage get(String url, boolean ignoreTrustCert) {
		return req(commonHeaders(), null, REQ_TYPE_GET, null, url, GET_HTML, REQ_PARAMS_ARRAY, CHARSET_UTF8, ignoreTrustCert);
	}
	
	public static model.HttpUtilNewPage get(String url, int retType, boolean ignoreTrustCert) {
		return req(null, null, REQ_TYPE_GET, null, url, retType, REQ_PARAMS_ARRAY, CHARSET_UTF8, ignoreTrustCert);
	}
	
	public static Pager get(Map<String, String> headers, String url) {
		return req(headers, null, REQ_TYPE_GET, null, url, GET_HTML, REQ_PARAMS_ARRAY, CHARSET_UTF8, false);
	}
	
	public static model.HttpUtilNewPage get(Map<String, String> headers, String url, boolean ignoreTrustCert) {
		return req(headers, null, REQ_TYPE_GET, null, url, GET_HTML, REQ_PARAMS_ARRAY, CHARSET_UTF8, ignoreTrustCert);
	}
	
	public static model.HttpUtilNewPage get(List<Cookie> cookies, String url, boolean ignoreTrustCert) {
		return req(commonHeaders(), cookies, REQ_TYPE_GET, null, url, GET_HTML, REQ_PARAMS_ARRAY, CHARSET_UTF8, ignoreTrustCert);
	}
	
	public static Pager get(Map<String, String> headers, String url, int retType) {
		return req(headers, null, REQ_TYPE_GET, null, url, retType, REQ_PARAMS_ARRAY, CHARSET_UTF8, false);
	}

	public static model.HttpUtilNewPage get(Map<String, String> headers, String url, int retType, boolean ignoreTrustCert) {
		return req(headers, null, REQ_TYPE_GET, null, url, retType, REQ_PARAMS_ARRAY, CHARSET_UTF8, ignoreTrustCert);
	}

	public static model.HttpUtilNewPage get(Map<String, String> headers, List<Cookie> cookies, String url, int retType, boolean ignoreTrustCert) {
		return req(headers, cookies, REQ_TYPE_GET, null, url, retType, REQ_PARAMS_ARRAY, CHARSET_UTF8, ignoreTrustCert);
	}
	
	public static model.HttpUtilNewPage get(NameValuePair[] nvps, String url) {
		return req(commonHeaders(), null, REQ_TYPE_GET, nvps, url, GET_HTML, REQ_PARAMS_ARRAY, CHARSET_UTF8, false);
	}
	
	public static model.HttpUtilNewPage get(Map<String, String> headers, NameValuePair[] nvps, String url) {
		return req(headers, null, REQ_TYPE_GET, nvps, url, GET_HTML, REQ_PARAMS_ARRAY, CHARSET_UTF8, false);
	}
	
	public static model.HttpUtilNewPage get(Map<String, String> headers, NameValuePair[] nvps, String url, boolean ignoreTrustCert) {
		return req(headers, null, REQ_TYPE_GET, nvps, url, GET_HTML, REQ_PARAMS_ARRAY, CHARSET_UTF8, ignoreTrustCert);
	}
	
	public static model.HttpUtilNewPage get(Map<String, String> headers, List<Cookie> cookies, String url) {
		return req(headers, cookies, REQ_TYPE_GET, null, url, GET_HTML, REQ_PARAMS_ARRAY, CHARSET_UTF8, false);
	}
	public static model.HttpUtilNewPage get(Map<String, String> headers, List<Cookie> cookies, String url, String charset) {
		return req(headers, cookies, REQ_TYPE_GET, null, url, GET_HTML, REQ_PARAMS_ARRAY, charset, false);
	}
	
	public static model.HttpUtilNewPage get(Map<String, String> headers, List<Cookie> cookies, String url, String charset, boolean ignoreAllCert) {
		return req(headers, cookies, REQ_TYPE_GET, null, url, GET_HTML, REQ_PARAMS_ARRAY, charset, ignoreAllCert);
	}
	
	public static model.HttpUtilNewPage get(Map<String, String> headers, List<Cookie> cookies, String url, double reqType, String charset, boolean ignoreAllCert) {
		return req(headers, cookies, REQ_TYPE_GET, null, url, GET_HTML, reqType, charset, ignoreAllCert);
	}
	
	public static model.HttpUtilNewPage get(Map<String, String> headers, List<Cookie> cookies, NameValuePair[] nvps, String url, double reqType, String charset, boolean ignoreAllCert) {
		return req(headers, cookies, REQ_TYPE_GET, nvps, url, GET_HTML, reqType, charset, ignoreAllCert);
	}
	
	public static model.HttpUtilNewPage getGB2312(Map<String, String> headers, List<Cookie> cookies, String url) {
		return req(headers, cookies, REQ_TYPE_GET, null, url, GET_HTML, REQ_PARAMS_ARRAY, CHARSET_GBK2312, false);
	}
	public static model.HttpUtilNewPage getGB2312(Map<String, String> headers, List<Cookie> cookies, String url, boolean ignoreAllCert) 	{
		return req(headers, cookies, REQ_TYPE_GET, null, url, GET_HTML, REQ_PARAMS_ARRAY, CHARSET_GBK2312, ignoreAllCert);
	}
	public static model.HttpUtilNewPage get(Map<String, String> headers, List<Cookie> cookies, String url, boolean ignoreTrustCert) {
		return req(headers, cookies, REQ_TYPE_GET, null, url, GET_HTML, REQ_PARAMS_ARRAY, CHARSET_UTF8, ignoreTrustCert);
	}
	
	public static model.HttpUtilNewPage get(Map<String, String> headers, List<Cookie> cookies, String url, int retType) {
		return req(headers, cookies, REQ_TYPE_GET, null, url, retType, REQ_PARAMS_ARRAY, CHARSET_UTF8, false);
	}
	
	public static model.HttpUtilNewPage get(Map<String, String> headers, List<Cookie> cookies, NameValuePair[] nvps, String url) {
		return req(headers, cookies, REQ_TYPE_GET, nvps, url, GET_HTML, REQ_PARAMS_ARRAY, CHARSET_UTF8, false);
	}
	
	public static model.HttpUtilNewPage get(Map<String, String> headers, List<Cookie> cookies, NameValuePair[] nvps, String url, int retType) {
		return req(headers, cookies, REQ_TYPE_GET, nvps, url, retType, REQ_PARAMS_ARRAY, CHARSET_UTF8, false);
	}
	
	public static model.HttpUtilNewPage post(String url) {
		return req(commonHeaders(), null, REQ_TYPE_POST, null, url, GET_HTML, REQ_PARAMS_ARRAY, CHARSET_UTF8, false);
	}
	
	public static model.HttpUtilNewPage post(String url, boolean ignoreAllCert) {
		return req(commonHeaders(), null, REQ_TYPE_POST, null, url, GET_HTML, REQ_PARAMS_ARRAY, CHARSET_UTF8, ignoreAllCert);
	}
	
	public static model.HttpUtilNewPage post(String url, NameValuePair[] nvps) {
		return req(commonHeaders(), null, REQ_TYPE_POST, nvps, url, GET_HTML, REQ_PARAMS_ARRAY, CHARSET_UTF8, false);
	}
	
	public static model.HttpUtilNewPage post(Map<String, String> headers, String url) {
		return req(headers, null, REQ_TYPE_POST, null, url, GET_HTML, REQ_PARAMS_ARRAY, CHARSET_UTF8, false);
	}
	
	public static model.HttpUtilNewPage post(Map<String, String> headers, List<Cookie> cookies, String url) {
		return req(headers, cookies, REQ_TYPE_POST, null, url, GET_HTML, REQ_PARAMS_ARRAY, CHARSET_UTF8, false);
	}
	public static model.HttpUtilNewPage post(Map<String, String> headers, List<Cookie> cookies, String url, boolean ignoreAllCert) {
		return req(headers, cookies, REQ_TYPE_POST, null, url, GET_HTML, REQ_PARAMS_ARRAY, CHARSET_UTF8, ignoreAllCert);
	}
	public static model.HttpUtilNewPage post(Map<String, String> headers, NameValuePair[] nvps, String url) {
		return req(headers, null, REQ_TYPE_POST, nvps, url, GET_HTML, REQ_PARAMS_ARRAY, CHARSET_UTF8, false);
	}
	
	public static model.HttpUtilNewPage post(Map<String, String> headers, List<Cookie> cookies, String url, int retType) {
		return req(headers, cookies, REQ_TYPE_POST, null, url, retType, REQ_PARAMS_ARRAY, CHARSET_UTF8, false);
	}
	
	public static model.HttpUtilNewPage post(Map<String, String> headers, NameValuePair[] nvps, String url, double reqParamType, boolean ignoreAllCert) {
		return req(headers, null, REQ_TYPE_POST, nvps, url, GET_HTML, reqParamType, CHARSET_UTF8, ignoreAllCert);
	}

	public static model.HttpUtilNewPage post(Map<String, String> headers, List<Cookie> cookies, NameValuePair[] nvps, String url) {
		return req(headers, cookies, REQ_TYPE_POST, nvps, url, GET_HTML, REQ_PARAMS_ARRAY, CHARSET_UTF8, false);
	}

	public static model.HttpUtilNewPage post(Map<String, String> headers, List<Cookie> cookies, NameValuePair[] nvps, String url, boolean ignoreAllCert) {
		return req(headers, cookies, REQ_TYPE_POST, nvps, url, GET_HTML, REQ_PARAMS_ARRAY, CHARSET_UTF8, ignoreAllCert);
	}

	public static model.HttpUtilNewPage postGBK(Map<String, String> headers, List<Cookie> cookies, NameValuePair[] nvps, String url) {
		return req(headers, cookies, REQ_TYPE_POST, nvps, url, GET_HTML, REQ_PARAMS_ARRAY, CHARSET_GBK, false);
	}

	public static model.HttpUtilNewPage postGBK(Map<String, String> headers, List<Cookie> cookies, NameValuePair[] nvps, String url, boolean ignoreAllCert) {
		return req(headers, cookies, REQ_TYPE_POST, nvps, url, GET_HTML, REQ_PARAMS_ARRAY, CHARSET_GBK, ignoreAllCert);
	}
	
	public static model.HttpUtilNewPage post(Map<String, String> headers, NameValuePair[] nvps, String url, double reqParamType) {
		return req(headers, null, REQ_TYPE_POST, nvps, url, GET_HTML, reqParamType, CHARSET_UTF8, false);
	}
	
	public static Pager post(Map<String, String> headers, List<Cookie> cookies, NameValuePair[] nvps, String url, double reqParamType) {
		return req(headers, cookies, REQ_TYPE_POST, nvps, url, GET_HTML, reqParamType, CHARSET_UTF8, false);
	}
	
	public static model.HttpUtilNewPage post(Map<String, String> headers, List<Cookie> cookies, NameValuePair[] nvps, String url, double reqParamType, boolean ignoreAllCert) {
		return req(headers, cookies, REQ_TYPE_POST, nvps, url, GET_HTML, reqParamType, CHARSET_UTF8, ignoreAllCert);
	}
	
	public static model.HttpUtilNewPage post(Map<String, String> headers, List<Cookie> cookies, NameValuePair[] nvps, String url, double reqParamType, String charset, boolean ignoreAllCert) {
		return req(headers, cookies, REQ_TYPE_POST, nvps, url, GET_HTML, reqParamType, charset, ignoreAllCert);
	}
	
	public static model.HttpUtilNewPage post(Map<String, String> headers, List<Cookie> cookies, NameValuePair[] nvps, String url, double reqParamType, String charset) {
		return req(headers, cookies, REQ_TYPE_POST, nvps, url, GET_HTML, reqParamType, charset, false);
	}

	public static model.HttpUtilNewPage postWithGB2312(Map<String, String> headers, List<Cookie> cookies, NameValuePair[] nvps, String url, double reqParamType) {
		return req(headers, cookies, REQ_TYPE_POST, nvps, url, GET_HTML, reqParamType, CHARSET_GBK2312, false);
	}

	
	public static model.HttpUtilNewPage post(Map<String, String> headers, NameValuePair[] nvps, String url, boolean ignoreTrustCert) {
		return req(headers, null, REQ_TYPE_POST, nvps, url, GET_HTML, REQ_PARAMS_ARRAY, CHARSET_UTF8, ignoreTrustCert);
	}
	public static model.HttpUtilNewPage post(Map<String, String> headers, NameValuePair[] nvps, String url, String charset, boolean ignoreTrustCert) {
		return req(headers, null, REQ_TYPE_POST, nvps, url, GET_HTML, REQ_PARAMS_ARRAY, charset, ignoreTrustCert);
	}
	public static model.HttpUtilNewPage get(Map<String, String> headers, String url, String charset, boolean ignoreAllCert) {
		return req(headers, null, REQ_TYPE_GET, null, url, GET_HTML, REQ_PARAMS_ARRAY, charset, ignoreAllCert);
	}
	public static model.HttpUtilNewPage req(Map<String, String> headers,
											List<Cookie> cookies, String reqType,
											NameValuePair[] nvps,
											String url, int retType,
											double reqParamType,
											String charset,
											boolean ignoreAllCert) {
		
		// 请求方式
		RequestBuilder req = RequestBuilder.get();
		if(reqType.equals(REQ_TYPE_POST)) {
			req = RequestBuilder.post();
		}
		
		// 请求url
		req.setUri(url);
		
		// 添加header
		if(headers != null && !headers.isEmpty()) {
			for(String key: headers.keySet()) {
				req.addHeader(key, headers.get(key));
			}
		}
		
		// 设置请求参数
		if(nvps != null) {
			if(reqParamType == REQ_PARAMS_ENTITY) {
				req.setEntity(new StringEntity(nvps[0].getValue(), "UTF-8"));
			} else {
				req.addParameters(nvps);
			}
		}
		
		// 设置超时时间
		int timeout = 20000;
		RequestConfig.Builder requestConfigBuilder = 
				RequestConfig.custom()
				.setConnectionRequestTimeout(timeout)
				.setSocketTimeout(timeout)
				.setConnectTimeout(timeout)
				.setRedirectsEnabled(false)
				.setCookieSpec(CookieSpecs.DEFAULT);
		req.setConfig(requestConfigBuilder.build());
		HttpUriRequest httpUriReq = req.build();
		
		
        CloseableHttpResponse httpResponse = null;
        try {
        	
        	HttpClientContext context = HttpClientContext.create();
        	CloseableHttpClient client = generateClient(cookies, ignoreAllCert);
        	
        	httpResponse = client.execute(httpUriReq, context);
        	
//        	BufferedReader br = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
//        	String line = br.readLine();
//        	int i = 0;
//        	while(i < 20) {
//        		System.out.println("我去: " + line);
//        		line = br.readLine();
//        		i ++;
//        	}
        	
        	model.HttpUtilNewPage ret = new model.HttpUtilNewPage();
        	ret.setHeaders(httpResponse.getAllHeaders());
        	ret.setCookies(context.getCookieStore().getCookies());
        	if(retType == GET_STREAM) {
        		ret.setBytes(EntityUtils.toByteArray(httpResponse.getEntity()));
        	} else {
        		
        		if(StringUtils.isEmpty(charset)) {
        			ret.setHtml(IOUtils.toString(httpResponse.getEntity().getContent(), "UTF-8"));
        		} else {
        			ret.setHtml(IOUtils.toString(httpResponse.getEntity().getContent(), charset));
        		}
        		
        	}
        	
        	int statusCode = httpResponse.getStatusLine().getStatusCode();
        	
        	ret.setStatusCode(statusCode);
        	if(statusCode == 302) {
        		ret.setLocation(httpResponse.getHeaders("Location")[0].getValue());
        	}
        	return ret;
            
        } catch (IOException e) {
        	
        	e.printStackTrace();
        	
        	logger.error("请求失败:", e);
        	
            return null;
        } finally {
            try {
                if (httpResponse != null) {
                    //ensure the connection is released back to pool
                    EntityUtils.consume(httpResponse.getEntity());
                }
            } catch (IOException e) {
                logger.warn("close response fail", e);
            }
        }
	}
	
	
	private static CloseableHttpClient generateClient(List<Cookie> cookies, boolean ignoreAllCert) {
		SSLConnectionSocketFactory sslFactory = null;
		if(ignoreAllCert) {
			sslFactory = createIgnoreVerifySSL();
		} else {
			sslFactory = SSLConnectionSocketFactory.getSocketFactory();
		}
		
		Registry<ConnectionSocketFactory> reg = RegistryBuilder.<ConnectionSocketFactory> create()
				.register("http", PlainConnectionSocketFactory.INSTANCE)
				.register("https", sslFactory)
				.build();
		
		PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(reg);
		connectionManager.setDefaultMaxPerRoute(100);

		HttpClientBuilder httpClientBuilder = HttpClients.custom().setConnectionManager(connectionManager);
		SocketConfig socketConfig = SocketConfig.custom().setSoKeepAlive(true).setTcpNoDelay(true).build();
		httpClientBuilder.setDefaultSocketConfig(socketConfig);
		httpClientBuilder.setRetryHandler(new DefaultHttpRequestRetryHandler(3, true));
		
		CookieStore cookieStore = new BasicCookieStore();
		if(cookies != null && !cookies.isEmpty()) {
			for (Cookie c: cookies) {
				cookieStore.addCookie(c);
			}
		}
		
		httpClientBuilder.setDefaultCookieStore(cookieStore);
		
		return httpClientBuilder.build();
	}
	
	public static SSLConnectionSocketFactory createSSLConnectionSocketFactory() {
		return new SSLConnectionSocketFactory(custom("src/main/java/com/test/moni/zxzq/wtid.cer", "tomcat"));
	}
	
	/**
	 * 设置信任自签名证书
	 *  
	 * @param keyStorePath      密钥库路径
	 * @param keyStorepass      密钥库密码
	 * @return
	 */
	public static SSLContext custom(String keyStorePath, String keyStorepass) {
		SSLContext sc = null;
		FileInputStream instream = null;
		KeyStore trustStore = null;
		try {
			keyStorepass = "MIIEyDCCA7CgAwIBAgIDASnpMA0GCSqGSIb3DQEBBQUAMDwxCzAJBgNVBAYTAlVTMRcwFQYDVQQKEw5HZW9UcnVzdCwgSW5jLjEUMBIGA1UEAxMLUmFwaWRTU0wgQ0EwHhcNMTEwMzI3MTcwNjI2WhcNMTIwMzI5MjMxNDExWjCB4TEpMCcGA1UEBRMgaldWdk9pVDZqNWl3bzNGVFRsRVZHT01taDFXVUpQNS0xCzAJBgNVBAYTAkNOMRYwFAYDVQQKEw13dC5jaXRpY3MuY29tMRMwEQYDVQQLEwpHVDc4MzE0MzcwMTEwLwYDVQQLEyhTZWUgd3d3LnJhcGlkc3NsLmNvbS9yZXNvdXJjZXMvY3BzIChjKTExMS8wLQYDVQQLEyZEb21haW4gQ29udHJvbCBWYWxpZGF0ZWQgLSBSYXBpZFNTTChSKTEWMBQGA1UEAxMNd3QuY2l0aWNzLmNvbTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBANN6oKe3vTPiAg7qtRW3OwMtK8ZoEJ4Sn+mR1Yi/AJKcmPYj0D4wrEMRDQakjUi5/eThvcCp8Y4N6fYT/m9EeFwb6nhpvsfyCE2c1skNVk634ewFNC2o8zBaeULD8Vmii6pCkxJ5ppe18V+JZ8PaTxOO1OU18DIjqMv0rB1lx/aoBx/N0jxw9/a27sWMuX1O/zZmBkWmKPxwRnWww0b2GbdFF6TkpaBOiuMhSic5FChifRf+qmuokMrSgU4XZ3ykX99hsrGPwEZsJONogUCGva4TqGNqXZzqPe1AhYQZ8LP02Wx0b0FWH0istA1niUCf4qbfYfwufNRva5mBdZc7EtcCAwEAAaOCASswggEnMB8GA1UdIwQYMBaAFGtpPWoYQkrdjwJlOf01JIZ4kRYwMA4GA1UdDwEB/wQEAwIFoDAdBgNVHSUEFjAUBggrBgEFBQcDAQYIKwYBBQUHAwIwGAYDVR0RBBEwD4INd3QuY2l0aWNzLmNvbTBDBgNVHR8EPDA6MDigNqA0hjJodHRwOi8vcmFwaWRzc2wtY3JsLmdlb3RydXN0LmNvbS9jcmxzL3JhcGlkc3NsLmNybDAdBgNVHQ4EFgQUd5CtX4AH4hs6kZS/mfLvDHx004wwDAYDVR0TAQH/BAIwADBJBggrBgEFBQcBAQQ9MDswOQYIKwYBBQUHMAKGLWh0dHA6Ly9yYXBpZHNzbC1haWEuZ2VvdHJ1c3QuY29tL3JhcGlkc3NsLmNydDANBgkqhkiG9w0BAQUFAAOCAQEAXrwTnxdZhE6t+WcqWPz+SwblJG3Q2ei6QnzDR2zVTTNF+Dpo/HvbGidEpNNMCg4at4et+oM1ghKnYbeQ1Uw65VILoeJ9Lb/D4L9dAUT6AjNDkP1eiLcoc2tLKUfKRYQVzBLRC2FwcUl+yaOA40fL9y5gWEPCddG9EWAipHIGb1Cd/PyHCHN+Q3EPDFiLJDHRD0TNmYtKKbD4VTx06aeq1SBGLPpkZH2v2PgzpuZQAoxxD7u6klaPmOF0Lscug210KDjbKuKHojzEHqu5UhPf7hEN5gvLpubkgT1NOLhDHrp/Y5UNrj0/QbeTrS4UJOj1UGFCRc8RT32ki5gIoD9wYQ==";
			trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			instream = new FileInputStream(new File(keyStorePath));
			trustStore.load(instream, keyStorepass.toCharArray());
			// 相信自己的CA和所有自签名的证书
			sc = SSLContexts.custom().loadTrustMaterial(trustStore, new TrustSelfSignedStrategy()).build();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				instream.close();
			} catch (IOException e) {
			}
		}
		return sc;
	}
	
	/**
	 */
	public static void configureHttpClient(HttpClientBuilder clientBuilder) {
		try {
			SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
				// 信任所有
				@Override
				public boolean isTrusted(X509Certificate[] chain, String authType) {
					return true;
				}
			}).build();

			clientBuilder.setSslcontext(sslContext);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static SSLConnectionSocketFactory createIgnoreVerifySSL() {
		try {
			SSLContext sc = SSLContext.getInstance(SSL_CONTEXT_VER);

			// 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法
			X509TrustManager trustManager = new X509TrustManager() {
				@Override
				public void checkClientTrusted(X509Certificate[] paramArrayOfX509Certificate,
						String paramString) {
				}

				@Override
				public void checkServerTrusted(X509Certificate[] paramArrayOfX509Certificate,
						String paramString) {
				}

				@Override
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			};

			sc.init(null, new TrustManager[] { trustManager }, null);
			return new SSLConnectionSocketFactory(sc, new NoopHostnameVerifier());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void setSSLContextVersion(String ver) {
		SSL_CONTEXT_VER = ver;
	}

	public static Map<String, String> commonHeaders() {
		Map<String, String> map = new TreeMap<String, String>();
		map.put("Accept", "text/html, application/xhtml+xml, application/x-ms-application, image/jpeg, application/xaml+xml, image/gif, image/pjpeg, application/x-ms-xbap, */*");
		map.put("Accept-Encoding", "gzip, deflate");
		map.put("Accept-Language", "zh-CN");
		map.put("Cache-Control", "no-cache");
		map.put("Connection", "Keep-Alive");
		map.put("Content-Type", "application/x-www-form-urlencoded");
		map.put("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; Win64; x64; Trident/4.0; .NET CLR 2.0.50727; SLCC2; .NET CLR 3.5.30729; .NET CLR 3.0.30729; .NET4.0C; .NET4.0E; InfoPath.3)");
		return map;
	}
	
	public static Map<String, String> commonAjaxHeaders() {
		Map<String, String> map = new TreeMap<String, String>();
		map.put("Accept", "application/x-ms-application, image/jpeg, application/json, text/javascript, application/xaml+xml, image/gif, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, image/pjpeg, application/x-ms-xbap, */*");
		map.put("Accept-Encoding", "gzip, deflate");
		map.put("Accept-Language", "zh-CN");
		map.put("Cache-Control", "no-cache");
		map.put("Connection", "Keep-Alive");
		map.put("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
	    map.put("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; Win64; x64; Trident/4.0; .NET CLR 2.0.50727; SLCC2; .NET CLR 3.5.30729; .NET CLR 3.0.30729; .NET4.0C; .NET4.0E; InfoPath.3)");
	    map.put("x-requested-with", "XMLHttpRequest");
		return map;
	}
	
	public static String createHeaderCookie(List<Cookie> cookies) {
		String curCookies = "";
		
		if(cookies == null || cookies.isEmpty()) {
			return curCookies;
		}
		
	    for(Cookie c: cookies) {
	    	curCookies += (c.getName() + "=" + c.getValue() + "; ");
	    }
	    return curCookies;
	}
	
	public static void addCookieFromSetCookieHeader(List<Cookie> ret, Header[] headers) {
		if(headers == null || headers.length <= 0) {
			return;
		}
		
		for(Header h: headers) {
			if(h.getName().equals("Set-Cookie")) {
				
				String path = null;
				HeaderElement eles = h.getElements()[0];
				NameValuePair[] nvs = eles.getParameters();
				for(NameValuePair nv: nvs) {
					if(nv.getName().equals("path")) {
						path = nv.getValue();
						break;
					}
				}
				
				BasicClientCookie b = new BasicClientCookie(eles.getName(), eles.getValue());
				if(path != null) {
					b.setPath(path);
				}
				ret.add(b);
			}
		}
		
	}
	
	public static void addCookieFromSetCookieHeader(JSONObject ret, Header[] headers) {
		if(headers == null || headers.length <= 0) {
			return;
		}
		
		for(Header h: headers) {
			if(h.getName().equals("Set-Cookie")) {
				HeaderElement eles = h.getElements()[0];
				ret.put(eles.getName(), eles.getValue());
			}
		}
		
	}
	
	public static JSONObject toMapFormCookieList(List<Cookie> cList) {
		
		JSONObject ret = new JSONObject();
		
		if(cList == null || cList.isEmpty()) {
			return ret;
		}
		
		for(Cookie c: cList) {
			ret.put(c.getName(), c.getValue());
		}
		
		return ret;
	}
	
}
