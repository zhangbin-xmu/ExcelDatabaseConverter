package com.excel.database.converter.util;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *     HTTP工具类
 * </p>
 * @author zhangbin
 * @date 2019-05-21
 */
public class HttpUtil {

	private static final int CONNECT_TIMEOUT = 60;

	private static final int SOCKET_TIMEOUT = 60;

	public static String get(String url) throws IOException {
		return get(url, new HashMap<>(0));
	}

	public static String get(String url, @NotNull Map<String, String> headers) throws IOException {
		HttpGet httpGet = new HttpGet(url);
		httpGet.setConfig(RequestConfig.custom()
				.setConnectTimeout(1000 * CONNECT_TIMEOUT)
				.setSocketTimeout(1000 * SOCKET_TIMEOUT)
				.build());

		for (Map.Entry<String, String> entry : headers.entrySet()) {
			httpGet.addHeader(entry.getKey(), entry.getValue());
		}

		try (CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
			 CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpGet)) {
			HttpEntity httpEntity = closeableHttpResponse.getEntity();
			return EntityUtils.toString(httpEntity, StandardCharsets.UTF_8);
		}
	}

	public static String post(String url, String json) throws IOException {
		return post(url, json, new HashMap<>(0));
	}

	public static String post(String url, String json, @NotNull Map<String, String> headers) throws IOException {
		StringEntity stringEntity = new StringEntity(json);
		stringEntity.setContentEncoding("UTF-8");
		stringEntity.setContentType("application/json");

		HttpPost httpPost = new HttpPost(url);
		httpPost.addHeader(HTTP.CONTENT_TYPE, "application/json");
		httpPost.setEntity(stringEntity);
		httpPost.setConfig(RequestConfig.custom()
				.setConnectTimeout(1000 * CONNECT_TIMEOUT)
				.setSocketTimeout(1000 * SOCKET_TIMEOUT)
				.build());

		for (Map.Entry<String, String> entry : headers.entrySet()) {
			httpPost.addHeader(entry.getKey(), entry.getValue());
		}

		try (CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
			 CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpPost)) {
			HttpEntity httpEntity = closeableHttpResponse.getEntity();
			return EntityUtils.toString(httpEntity, StandardCharsets.UTF_8);
		}
	}

	public static String post(String url, List<NameValuePair> form) throws IOException {
		HttpPost httpPost = new HttpPost(url);
		httpPost.setEntity(new UrlEncodedFormEntity(form, StandardCharsets.UTF_8));
		httpPost.setConfig(RequestConfig.custom()
				.setConnectTimeout(1000 * CONNECT_TIMEOUT)
				.setSocketTimeout(1000 * SOCKET_TIMEOUT)
				.build());

		try (CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
			 CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpPost)) {
			HttpEntity httpEntity = closeableHttpResponse.getEntity();
			return EntityUtils.toString(httpEntity, StandardCharsets.UTF_8);
		}
	}

	@NotNull
	public static Map<String, Object> post(String url, @NotNull File file, @NotNull Map<String, Object> parameters) throws IOException {
		MultipartEntityBuilder builder = MultipartEntityBuilder.create()
				.setCharset(StandardCharsets.UTF_8)
				.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		builder.addPart(file.getName(), new FileBody(file));

		for (Map.Entry<String, Object> entry : parameters.entrySet()) {
			builder.addPart(entry.getKey(),
					new StringBody((String) entry.getValue(), ContentType.create("text/plain", Consts.UTF_8)));
		}

		HttpPost httpPost = new HttpPost(url);
		httpPost.setEntity(builder.build());
		httpPost.setConfig(RequestConfig.custom()
				.setConnectTimeout(1000 * CONNECT_TIMEOUT)
				.setSocketTimeout(1000 * SOCKET_TIMEOUT)
				.build());

		try (CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
			 CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpPost)) {
			Map<String, Object> result = new HashMap<>(2);
			result.put("statusCode", closeableHttpResponse.getStatusLine().getStatusCode());
			HttpEntity httpEntity = closeableHttpResponse.getEntity();
			if (null != httpEntity) {
				result.put("data", EntityUtils.toString(httpEntity, StandardCharsets.UTF_8));
			}
			EntityUtils.consume(httpEntity);
			return result;
		}
	}
}
