package com.plivo.helper.api.client.simple;

import java.util.LinkedHashMap;

import com.google.gson.Gson;
import com.plivo.helper.api.common.CommonRestApi;
import com.plivo.helper.api.response.number.NumberGroupFactory;
import com.plivo.helper.api.response.number.NumberResponse;
import com.plivo.helper.api.response.number.NumberSearchFactory;
import com.plivo.helper.api.response.response.GenericResponse;
import com.plivo.helper.exception.PlivoException;
import com.plivo.helper.util.HttpUtils;

public class NumberRestAPI extends CommonRestApi {

	private Gson gson = new Gson();

	public NumberRestAPI() {

	}

	public NumberRestAPI(String authId, String authToken, String version) {
		this.authId = authId;
		this.authToken = authToken;
		if (version == null || version.trim().isEmpty())
			version = LATEST_PLIVO_VERSION;
		this.baseURI = String.format("%s/%s/Account/%s", PLIVO_PROTOCOL
				+ PLIVO_HOST, version, authId);
	}

	public NumberRestAPI(String authId, String authToken) {
		this.authId = authId;
		this.authToken = authToken;

		this.baseURI = String.format("%s/%s/Account/%s", PLIVO_PROTOCOL
				+ PLIVO_HOST, LATEST_PLIVO_VERSION, authId);
	}

	// Number
	public NumberSearchFactory getNumbers() throws PlivoException {
		return this.gson
				.fromJson(
						request("GET", "/Number/",
								new LinkedHashMap<String, String>()),
						NumberSearchFactory.class);
	}

	public NumberSearchFactory getNumbers(
			LinkedHashMap<String, String> parameters) throws PlivoException {
		return this.gson.fromJson(request("GET", "/Number/", parameters),
				NumberSearchFactory.class);
	}

	@Deprecated
	public NumberSearchFactory searchNumbers(
			LinkedHashMap<String, String> parameters) throws PlivoException {
		return this.gson.fromJson(
				request("GET", "/AvailableNumber/", parameters),
				NumberSearchFactory.class);
	}

	@Deprecated
	public GenericResponse rentNumber(LinkedHashMap<String, String> parameters)
			throws PlivoException {
		String number = HttpUtils.getKeyValue(parameters, "number");
		return this.gson.fromJson(
				request("POST", String.format("/AvailableNumber/%s/", number,
						parameters), new LinkedHashMap<String, String>()),
				GenericResponse.class);
	}

	public NumberGroupFactory searchNumberGroups(
			LinkedHashMap<String, String> parameters) throws PlivoException {
		return this.gson.fromJson(
				request("GET", "/AvailableNumberGroup/", parameters),
				NumberGroupFactory.class);
	}

	public NumberResponse rentNumbers(LinkedHashMap<String, String> parameters)
			throws PlivoException {
		String groupId = HttpUtils.getKeyValue(parameters, "group_id");
		return this.gson.fromJson(
				request("POST",
						String.format("/AvailableNumberGroup/%s/", groupId),
						parameters), NumberResponse.class);
	}

	public GenericResponse unRentNumber(LinkedHashMap<String, String> parameters)
			throws PlivoException {
		String number = HttpUtils.getKeyValue(parameters, "number");
		return this.gson.fromJson(
				request("DELETE", String.format("/Number/%s/", number),
						parameters), GenericResponse.class);
	}

	public GenericResponse linkApplicationNumber(
			LinkedHashMap<String, String> parameters) throws PlivoException {
		String number = HttpUtils.getKeyValue(parameters, "number");
		return this.gson.fromJson(
				request("POST", String.format("/Number/%s/", number),
						parameters), GenericResponse.class);
	}

	public GenericResponse unlinkApplicationNumber(
			LinkedHashMap<String, String> parameters) throws PlivoException {
		String number = HttpUtils.getKeyValue(parameters, "number");
		parameters.put("app_id", "");
		return this.gson.fromJson(
				request("POST", String.format("/Number/%s/", number),
						parameters), GenericResponse.class);
	}

}
