package com.dosimple.robot.util.http;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

@Slf4j
public class ResponseHandlers {
	public static final ResponseHandler<Response> REST = new ResponseHandler<Response>() {
		@Override
		public Response handleResponse(HttpResponse response) throws IOException {
			try{
				StatusLine status = response.getStatusLine();
				byte[] body = EntityUtils.toByteArray(response.getEntity());
				Response result = new Response(status.getStatusCode(), body);
				return result;
			}catch (Exception e){
				throw e;
			}finally {
				close(response);
			}
		}

		private void close(HttpResponse resp) {
			try {
				if(resp == null) return;
				if(CloseableHttpResponse.class.isAssignableFrom(resp.getClass())){
					((CloseableHttpResponse)resp).close();
				}
			} catch (IOException e) {
				log.error("关闭HttpResponse 失败 ",e);
			}
		}
	};
}
