package com.dosimple.robot.util.http;

import com.dosimple.robot.util.GsonHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

@Slf4j
public class RestClient {
	private static RestClient instance = new RestClient(new HTTP(new HTTPConfig()));

	private HTTP httpClient;

	public RestClient(HTTP httpClient) {
		this.httpClient = httpClient;
	}

	public static RestClient getDefault() {
		return instance;
	}



	enum Method {
		POST, PUT, PATCH, GET, DELETE, HEAD, OPTIONS, TRACE
	}

	public Response postByJson(String url, Map<String, ? extends CharSequence> headers, List<File> files)
			throws IOException {
		return uploadFile(Method.POST, url, null, headers, files);
	}

	public Response postByJson(String url, Map<String, ? extends CharSequence> headers, String json)
			throws IOException {
		return reqWithEntity(Method.POST, url, null, headers, json);
	}

	public Response postByJson(String url, Map<String, ? extends CharSequence> headers, Map<String, Object> data)
			throws IOException {
		return reqWithEntity(Method.POST, url, null, headers, data);
	}

	public Response postByJson(String url, Map<String, Object> data) throws IOException {
		return reqWithEntity(Method.POST, url, null, null, data);
	}

	public Response postByJson(String url, String json) throws IOException {
		return reqWithEntity(Method.POST, url, null, null, json);
	}

	public Response postByForm(String url, Map<String, String> params, Map<String, String> headers) throws IOException {
		return reqWithSSLFormEntity(url, params, headers);
	}
	private Response reqWithSSLFormEntity(String url, Map<String, String> params, Map<String, String> headers) {
//		return new Response(HttpStatus.SC_OK, "".getBytes());
		HttpPost httpPost = new HttpPost(url);
		HttpResponse httpResponse = null;
		if (headers != null) {
			Set<String> keys = headers.keySet();
			for (String key : keys) {
				httpPost.addHeader(key, headers.get(key));
			}
		}
		try {
			httpPost.setConfig(HTTPConfig.getReqConfig());
			List<NameValuePair> pairList = new ArrayList<>(params.size());
			for (Entry<String, String> entry : params.entrySet()) {
				NameValuePair pair = new BasicNameValuePair(entry.getKey(), entry.getValue());
				pairList.add(pair);
			}
			httpPost.setEntity(new UrlEncodedFormEntity(pairList, Consts.UTF_8));
			httpResponse = httpClient.execute(httpPost);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if (statusCode != HttpStatus.SC_OK) {
				return new Response(statusCode, httpResponse.getStatusLine().getReasonPhrase().getBytes());
			}
			HttpEntity entity = httpResponse.getEntity();
			if (entity == null) {
				return new Response(HttpStatus.SC_OK, null);
			}
			String httpStr = EntityUtils.toString(entity, "utf-8");
			return new Response(HttpStatus.SC_OK, httpStr.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("调用异常");
		} finally {
			if (httpResponse != null) {
				try {
					EntityUtils.consume(httpResponse.getEntity());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public Response putByJson(String url, Map<String, ? extends CharSequence> headers, String json)
			throws IOException {
		return reqWithEntity(Method.PUT, url, null, headers, json);
	}

	public Response putByJson(String url, Map<String, ? extends CharSequence> headers, Map<String, Object> data)
			throws IOException {
		return reqWithEntity(Method.PUT, url, null, headers, data);
	}

	public Response putByJson(String url, String json) throws IOException {
		return reqWithEntity(Method.PUT, url, null, null, json);
	}

	public Response putByJson(String url, Map<String, Object> data) throws IOException {
		return reqWithEntity(Method.PUT, url, null, null, data);
	}

	public Response get(String url, Map<String, String> headers)
			throws IOException {
		return reqNoEntity(Method.GET, url, null, headers);
	}

	public Response get(String url, Map<String, String> headers, Map<String, String> params)
			throws IOException {
		return reqNoEntity(Method.GET, url, params, headers);
	}

	public Response get(String url) throws IOException {
		return reqNoEntity(Method.GET, url, null, null);
	}

	public Response delete(String url, Map<String, String> headers)
			throws IOException {
		return reqNoEntity(Method.DELETE, url, null, headers);
	}

	public Response delete(String url) throws IOException {
		return reqNoEntity(Method.DELETE, url, null, null);
	}

	public static String buildUri(String url, Map<String, ?> params) {
		try {
			URIBuilder ub = new URIBuilder(url);
			if (params != null) {
				for (Entry<String, ?> entry : params.entrySet()) {
					if (entry.getKey() != null && entry.getValue() != null) {
						ub.addParameter(entry.getKey(), entry.getValue().toString());
					}
				}
			}
			return ub.build().toString();
		} catch (URISyntaxException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private void setHeaders(HttpUriRequest request, Map<String, ? extends CharSequence> headers) {
		if (headers != null) {
			for (Entry<String, ? extends CharSequence> entry : headers.entrySet()) {
				if (entry.getKey() != null && entry.getValue() != null) {
					request.addHeader(entry.getKey(), entry.getValue().toString());
				}
			}
		}
	}

	private Response uploadFile(Method method, String url, Map<String, ? extends CharSequence> params,
                                                                Map<String, ? extends CharSequence> headers, List<File> list) throws IOException {
		HttpEntityEnclosingRequestBase request;
		if (method == Method.POST) {
			request = new HttpPost(buildUri(url, params));
		} else {
			request = new HttpPut(buildUri(url, params));
		}
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

		for (int i = 0; i < list.size(); i++) {
			FileBody fileBody = new FileBody(list.get(i));
			builder.addPart("file" + i, fileBody);
		}

		request.setEntity(builder.build());

		return httpClient.execute(request, ResponseHandlers.REST);
	}

	private Response reqWithEntity(Method method, String url, Map<String, ? extends CharSequence> params,
                                                                   Map<String, ? extends CharSequence> headers, String json) throws IOException {
		HttpEntityEnclosingRequestBase request;
		if (method == Method.POST) {
			request = new HttpPost(buildUri(url, params));
		} else {
			request = new HttpPut(buildUri(url, params));
		}

		setHeaders(request, headers);

		if (StringUtils.isNotBlank(json)) {
			request.setEntity(new StringEntity(json, "utf-8"));
			if (null != headers && headers.get(HttpHeaders.CONTENT_TYPE) != null) {
				request.addHeader(HttpHeaders.CONTENT_TYPE, headers.get(HttpHeaders.CONTENT_TYPE).toString());
			} else {
				request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
			}
		}

		return httpClient.execute(request, ResponseHandlers.REST);
	}

	private Response reqWithEntity(Method method, String url, Map<String, ? extends CharSequence> params,
                                                                   Map<String, ? extends CharSequence> headers, Map<String, Object> data)
			throws IOException {
		String json = null;
		if (data != null && !data.isEmpty()) {
			json = GsonHelper.toJson(data);
		}
		return reqWithEntity(method, url, params, headers, json);
	}

	private Response reqNoEntity(Method method, String url, Map<String, ? extends CharSequence> params,
                                                                 Map<String, ? extends CharSequence> headers) throws IOException {
		HttpUriRequest request;
		if (method == Method.GET) {
			request = new HttpGet(buildUri(url, params));
		} else if (method == Method.DELETE) {
			request = new HttpDelete(buildUri(url, params));
		} else if (method == Method.HEAD) {
			request = new HttpHead(buildUri(url, params));
		} else if (method == Method.OPTIONS) {
			request = new HttpOptions(buildUri(url, params));
		} else {
			request = new HttpTrace(buildUri(url, params));
		}

		setHeaders(request, headers);
		return httpClient.execute(request, ResponseHandlers.REST);

	}

	public void close() throws IOException {
		httpClient.close();
	}

	private static HttpClient wrapClient(String host) {
		HttpClient httpClient = HttpClientBuilder.create().build();
		if (host.startsWith("https://")) {
			sslClient(httpClient);
		}
		return httpClient;
	}

	private static void sslClient(HttpClient httpClient) {
		try {
			SSLContext ctx = SSLContext.getInstance("TLS");
			X509TrustManager tm = new X509TrustManager() {
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
				public void checkClientTrusted(X509Certificate[] xcs, String str) {

				}
				public void checkServerTrusted(X509Certificate[] xcs, String str) {

				}
			};
			ctx.init(null, new TrustManager[] { tm }, null);
			SSLSocketFactory ssf = new SSLSocketFactory(ctx);
			ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			ClientConnectionManager ccm = httpClient.getConnectionManager();
			SchemeRegistry registry = ccm.getSchemeRegistry();
			registry.register(new Scheme("https", 443, ssf));
		} catch (KeyManagementException ex) {
			throw new RuntimeException(ex);
		} catch (NoSuchAlgorithmException ex) {
			throw new RuntimeException(ex);
		}
	}
}
