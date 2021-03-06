package com.plivo.helper.api.client.simple;

import java.util.LinkedHashMap;

import com.google.gson.Gson;
import com.plivo.helper.api.common.CommonRestApi;
import com.plivo.helper.api.response.carrier.IncomingCarrier;
import com.plivo.helper.api.response.carrier.IncomingCarrierFactory;
import com.plivo.helper.api.response.response.GenericResponse;
import com.plivo.helper.exception.PlivoException;
import com.plivo.helper.util.HttpUtils;

public class IncomingCarrierRestAPI extends CommonRestApi {

	private Gson gson = new Gson();

	public IncomingCarrierRestAPI() {

	}

	public IncomingCarrierRestAPI(String authId, String authToken,
			String version) {
		this.authId = authId;
		this.authToken = authToken;
		if (version == null || version.trim().isEmpty())
			version = LATEST_PLIVO_VERSION;
		this.baseURI = String.format("%s/%s/Account/%s", PLIVO_PROTOCOL
				+ PLIVO_HOST, version, authId);
	}

	public IncomingCarrierRestAPI(String authId, String authToken) {
		this.authId = authId;
		this.authToken = authToken;

		this.baseURI = String.format("%s/%s/Account/%s", PLIVO_PROTOCOL
				+ PLIVO_HOST, LATEST_PLIVO_VERSION, authId);
	}

	// Incoming Carrier
	public IncomingCarrierFactory getIncomingCarriers(
			LinkedHashMap<String, String> parameters) throws PlivoException {
		return this.gson.fromJson(
				request("GET", "/IncomingCarrier/", parameters),
				IncomingCarrierFactory.class);
	}

	public IncomingCarrier getIncomingCarrier(
			LinkedHashMap<String, String> parameters) throws PlivoException {
		String carrier = HttpUtils.getKeyValue(parameters, "carrier_id");
		return this.gson.fromJson(
				request("GET", String.format("/IncomingCarrier/%s/", carrier),
						parameters), IncomingCarrier.class);
	}

	public GenericResponse addIncomingCarrier(
			LinkedHashMap<String, String> parameters) throws PlivoException {
		return this.gson.fromJson(
				request("POST", "/IncomingCarrier/", parameters),
				GenericResponse.class);
	}

	public GenericResponse editIncomingCarrier(
			LinkedHashMap<String, String> parameters) throws PlivoException {
		String carrier = HttpUtils.getKeyValue(parameters, "carrier_id");
		return this.gson.fromJson(
				request("POST", String.format("/IncomingCarrier/", carrier),
						parameters), GenericResponse.class);
	}

	public GenericResponse dropIncomingCarrier(
			LinkedHashMap<String, String> parameters) throws PlivoException {
		String carrier = HttpUtils.getKeyValue(parameters, "carrier_id");
		return this.gson.fromJson(
				request("DELETE",
						String.format("/IncomingCarrier/%s/", carrier),
						parameters), GenericResponse.class);
	}

}
