package com.plivo.helper.api.client.simple;

import java.util.LinkedHashMap;

import com.google.gson.Gson;
import com.plivo.helper.api.common.CommonRestApi;
import com.plivo.helper.api.response.carrier.OutgoingCarrierRouting;
import com.plivo.helper.api.response.carrier.OutgoingCarrierRoutingCreatedResponse;
import com.plivo.helper.api.response.carrier.OutgoingCarrierRoutingFactory;
import com.plivo.helper.api.response.response.GenericResponse;
import com.plivo.helper.exception.PlivoException;
import com.plivo.helper.util.HttpUtils;

public class OutgoingCarrierRoutingRestAPI extends CommonRestApi {

	private Gson gson = new Gson();

	public OutgoingCarrierRoutingRestAPI() {

	}

	public OutgoingCarrierRoutingRestAPI(String authId, String authToken,
			String version) {
		this.authId = authId;
		this.authToken = authToken;
		if (version == null || version.trim().isEmpty())
			version = LATEST_PLIVO_VERSION;
		this.baseURI = String.format("%s/%s/Account/%s", PLIVO_PROTOCOL
				+ PLIVO_HOST, version, authId);
	}

	public OutgoingCarrierRoutingRestAPI(String authId, String authToken) {
		this.authId = authId;
		this.authToken = authToken;

		this.baseURI = String.format("%s/%s/Account/%s", PLIVO_PROTOCOL
				+ PLIVO_HOST, LATEST_PLIVO_VERSION, authId);
	}

	// Outgoing Carrier Routing
	public OutgoingCarrierRoutingFactory getOutgoingCarrierRoutings(
			LinkedHashMap<String, String> parameters) throws PlivoException {
		return this.gson.fromJson(
				request("GET", "/OutgoingCarrierRouting/", parameters),
				OutgoingCarrierRoutingFactory.class);
	}

	public OutgoingCarrierRouting getOutgoingCarrierRouting(
			LinkedHashMap<String, String> parameters) throws PlivoException {
		String carrier = HttpUtils.getKeyValue(parameters, "routing_id");
		return this.gson.fromJson(
				request("GET",
						String.format("/OutgoingCarrierRouting/%s/", carrier),
						parameters), OutgoingCarrierRouting.class);
	}

	public OutgoingCarrierRoutingCreatedResponse addOutgoingCarrierRouting(
			LinkedHashMap<String, String> parameters) throws PlivoException {
		return this.gson.fromJson(
				request("POST", "/OutgoingCarrierRouting/", parameters),
				OutgoingCarrierRoutingCreatedResponse.class);
	}

	public GenericResponse editOutgoingCarrierRouting(
			LinkedHashMap<String, String> parameters) throws PlivoException {
		String routing_id = HttpUtils.getKeyValue(parameters, "routing_id");
		return this.gson.fromJson(
				request("POST", String.format("/OutgoingCarrierRouting/%s/",
						routing_id), parameters), GenericResponse.class);
	}

	public GenericResponse dropOutgoingCarrierRouting(
			LinkedHashMap<String, String> parameters) throws PlivoException {
		String routing_id = HttpUtils.getKeyValue(parameters, "routing_id");
		return this.gson.fromJson(
				request("DELETE", String.format("/OutgoingCarrierRouting/%s/",
						routing_id), parameters), GenericResponse.class);
	}

}
