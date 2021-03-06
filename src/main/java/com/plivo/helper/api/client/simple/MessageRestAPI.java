package com.plivo.helper.api.client.simple;

import java.util.LinkedHashMap;

import com.google.gson.Gson;
import com.plivo.helper.api.common.CommonRestApi;
import com.plivo.helper.api.response.message.Message;
import com.plivo.helper.api.response.message.MessageFactory;
import com.plivo.helper.api.response.message.MessageResponse;
import com.plivo.helper.exception.PlivoException;
import com.plivo.helper.util.HttpUtils;

public class MessageRestAPI extends CommonRestApi {

	private Gson gson = new Gson();

	public MessageRestAPI() {

	}

	public MessageRestAPI(String authId, String authToken, String version) {
		this.authId = authId;
		this.authToken = authToken;
		if (version == null || version.trim().isEmpty())
			version = LATEST_PLIVO_VERSION;
		this.baseURI = String.format("%s/%s/Account/%s", PLIVO_PROTOCOL
				+ PLIVO_HOST, version, authId);
	}

	public MessageRestAPI(String authId, String authToken) {
		this.authId = authId;
		this.authToken = authToken;

		this.baseURI = String.format("%s/%s/Account/%s", PLIVO_PROTOCOL
				+ PLIVO_HOST, LATEST_PLIVO_VERSION, authId);
	}

	// Message
	public MessageResponse sendMessage(LinkedHashMap<String, String> parameters)
			throws PlivoException {
		return this.gson.fromJson(request("POST", "/Message/", parameters),
				MessageResponse.class);
	}

	public Message getMessage(LinkedHashMap<String, String> parameters)
			throws PlivoException {
		String record_id = HttpUtils.getKeyValue(parameters, "record_id");
		return this.gson.fromJson(
				request("GET", String.format("/Message/%s/", record_id),
						new LinkedHashMap<String, String>()), Message.class);
	}

	public MessageFactory getMessages() throws PlivoException {
		return this.gson
				.fromJson(
						request("GET", "/Message/",
								new LinkedHashMap<String, String>()),
						MessageFactory.class);
	}

	public MessageFactory getMessages(LinkedHashMap<String, String> parameters)
			throws PlivoException {
		return this.gson.fromJson(request("GET", "/Message/", parameters),
				MessageFactory.class);
	}

}
