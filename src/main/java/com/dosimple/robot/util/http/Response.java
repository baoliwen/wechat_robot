package com.dosimple.robot.util.http;


import com.dosimple.robot.util.GsonHelper;

import java.io.UnsupportedEncodingException;
import java.util.Map;

public class Response {

	private int code;

	private byte[] body;

	public Response(int code, byte[] body) {
		this.code = code;
		this.body = body;
	}

	public int getCode() {
		return code;
	}

	public byte[] getBodyAsBytes() {
		return body;
	}

	public String getBody() {
		try {
			return new String(body, "utf-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public String getText() {
		return getBody();
	}

	public Map<String, Object> toMap() {
		return GsonHelper.toMap(getBody());
	}

	public boolean isSuccess() {
		return code >= 200 && code < 300;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(this.code);
		str.append("\n");
		str.append(getBody());
		return str.toString();
	}
}
