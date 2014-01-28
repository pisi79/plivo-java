package com.plivo.helper.api.client;

//Exceptions
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.protocol.HTTP;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.plivo.helper.api.response.account.Account;
import com.plivo.helper.api.response.account.SubAccount;
import com.plivo.helper.api.response.account.SubAccountFactory;
import com.plivo.helper.api.response.application.Application;
import com.plivo.helper.api.response.application.ApplicationFactory;
import com.plivo.helper.api.response.call.CDR;
import com.plivo.helper.api.response.call.CDRFactory;
import com.plivo.helper.api.response.call.Call;
import com.plivo.helper.api.response.call.LiveCall;
import com.plivo.helper.api.response.call.LiveCallFactory;
import com.plivo.helper.api.response.carrier.IncomingCarrier;
import com.plivo.helper.api.response.carrier.IncomingCarrierFactory;
import com.plivo.helper.api.response.carrier.OutgoingCarrier;
import com.plivo.helper.api.response.carrier.OutgoingCarrierCreatedResponse;
import com.plivo.helper.api.response.carrier.OutgoingCarrierFactory;
import com.plivo.helper.api.response.carrier.OutgoingCarrierRouting;
import com.plivo.helper.api.response.carrier.OutgoingCarrierRoutingCreatedResponse;
import com.plivo.helper.api.response.carrier.OutgoingCarrierRoutingFactory;
import com.plivo.helper.api.response.conference.Conference;
import com.plivo.helper.api.response.conference.LiveConferenceFactory;
import com.plivo.helper.api.response.endpoint.Endpoint;
import com.plivo.helper.api.response.endpoint.EndpointFactory;
import com.plivo.helper.api.response.message.Message;
import com.plivo.helper.api.response.message.MessageFactory;
import com.plivo.helper.api.response.message.MessageResponse;
import com.plivo.helper.api.response.number.NumberGroupFactory;
import com.plivo.helper.api.response.number.NumberResponse;
import com.plivo.helper.api.response.number.NumberSearchFactory;
import com.plivo.helper.api.response.pricing.PlivoPricing;
import com.plivo.helper.api.response.response.GenericResponse;
import com.plivo.helper.api.response.response.Record;
import com.plivo.helper.exception.PlivoException;
import com.plivo.helper.util.HtmlEntity;
// Plivo resources
// Authentication for HTTP resources
// Handle HTTP requests
//Add pay load to POST request 
// Handle JSON response
// Handle unicode characters

public class RestAPI {

	private static final String PLIVO_PROTOCOL = "https://";
	private static final String PLIVO_HOST = "api.plivo.com";
	private static final int PLIVO_PORT = 443;
	private static final String PLIVO_VERSION = "v1";

	public String authId;
	private String authToken;
	private String baseURI;
	private DefaultHttpClient client;
	private Gson gson = new Gson();

	public RestAPI() {

	}

	public RestAPI(String auth_id, String auth_token, String version) {
		this.authId = auth_id;
		this.authToken = auth_token;
		if (version == null || version.trim().isEmpty())
			version = PLIVO_VERSION;
		this.baseURI = String.format("%s/%s/Account/%s", PLIVO_PROTOCOL
				+ PLIVO_HOST, version, authId);
	}

	public RestAPI(String auth_id, String auth_token) {
		this.authId = auth_id;
		this.authToken = auth_token;

		this.baseURI = String.format("%s/%s/Account/%s", PLIVO_PROTOCOL
				+ PLIVO_HOST, PLIVO_VERSION, authId);
	}

	private void initClient() {
		this.client = new DefaultHttpClient();
		this.client.getCredentialsProvider().setCredentials(
				new AuthScope(PLIVO_HOST, PLIVO_PORT),
				new UsernamePasswordCredentials(authId, authToken));
	}

	public String request(String method, String resource,
			LinkedHashMap<String, String> parameters) throws PlivoException {
		HttpResponse response = new BasicHttpResponse(new ProtocolVersion(
				"HTTP", 1, 1), HttpStatus.SC_OK, "OK");
		initClient();
		try {
			if ("GET".equals(method)) {
				// Prepare a String with GET parameters
				StringBuffer getparams = new StringBuffer("?");
				for (Entry<String, String> pair : parameters.entrySet())
					getparams.append(pair.getKey()).append("=")
							.append(pair.getValue()).append("&");
				// remove the trailing '&'
				// getparams = getparams.substring(0, getparams.length() - 1);

				HttpGet httpget = new HttpGet(this.baseURI
						+ resource
						+ getparams.toString().substring(0,
								getparams.toString().length() - 1));
				response = this.client.execute(httpget);
			} else if ("POST".equals(method)) {
				HttpPost httpost = new HttpPost(this.baseURI + resource);
				Gson gson = new GsonBuilder().serializeNulls().create();
				// Create a String entity with the POST parameters
				StringEntity se = new StringEntity(gson.toJson(parameters),
						"utf-8");
				se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,
						"application/json"));
				// Now, attach the pay load to the request
				httpost.setEntity(se);
				response = this.client.execute(httpost);
			} else if ("DELETE".equals(method)) {
				HttpDelete httpdelete = new HttpDelete(this.baseURI + resource);
				response = this.client.execute(httpdelete);
			}

			Integer serverCode = response.getStatusLine().getStatusCode();

			if (response.getEntity() != null) {
				return this.convertStreamToString(
						response.getEntity().getContent()).replaceFirst(
						"\\{",
						String.format("{ \"server_code\": %s, ",
								serverCode.toString()));
			} else {
				// dummy response
				return String
						.format("{\"message\":\"no response\",\"api_id\":\"unknown\", \"server_code\":%s}",
								serverCode.toString());
			}

		} catch (ClientProtocolException e) {
			throw new PlivoException(e.getLocalizedMessage());
		} catch (IOException e) {
			throw new PlivoException(e.getLocalizedMessage());
		} catch (IllegalStateException e) {
			throw new PlivoException(e.getLocalizedMessage());
		} finally {
			this.client.getConnectionManager().shutdown();
		}

	}

	private String convertStreamToString(InputStream istream)
			throws IOException {
		BufferedReader breader = new BufferedReader(new InputStreamReader(
				istream));
		StringBuilder responseString = new StringBuilder();
		String line = "";
		while ((line = breader.readLine()) != null) {
			responseString.append(line);
		}
		breader.close();
		return responseString.toString();
	}

	private String getKeyValue(LinkedHashMap<String, String> params, String key)
			throws PlivoException {
		String value = "";
		if (params.containsKey(key)) {
			value = params.get(key);
			params.remove(key);
		} else {
			throw new PlivoException(String.format(
					"Missing mandatory parameter %s.", key));
		}
		return value;
	}

	// Internal methods
	public String getAuthId() {
		return this.authId;
	}

	public void setAuthId(String authId) {
		this.authId = authId;
	}

	public String getAuthToken() {
		return this.authToken;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

	public String getBaseURI() {
		return this.baseURI;
	}

	public void setBaseURI(String baseURI) {
		this.baseURI = baseURI;
	}

	// Account
	public Account getAccount() throws PlivoException {
		return this.gson.fromJson(
				request("GET", "/", new LinkedHashMap<String, String>()),
				Account.class);
	}

	public GenericResponse editAccount(LinkedHashMap<String, String> parameters)
			throws PlivoException {
		return this.gson.fromJson(request("POST", "/", parameters),
				GenericResponse.class);
	}

	public SubAccountFactory getSubaccounts() throws PlivoException {
		return this.gson.fromJson(
				request("GET", "/Subaccount/",
						new LinkedHashMap<String, String>()),
				SubAccountFactory.class);
	}

	public SubAccount getSubaccount(LinkedHashMap<String, String> parameters)
			throws PlivoException {
		String subauth_id = this.getKeyValue(parameters, "subauth_id");
		return this.gson.fromJson(
				request("GET", String.format("/Subaccount/%s/", subauth_id),
						parameters), SubAccount.class);
	}

	public GenericResponse createSubaccount(
			LinkedHashMap<String, String> parameters) throws PlivoException {
		return this.gson.fromJson(request("POST", "/Subaccount/", parameters),
				GenericResponse.class);
	}

	public GenericResponse editSubaccount(
			LinkedHashMap<String, String> parameters) throws PlivoException {
		return this.gson.fromJson(request("POST", "/Subaccount/", parameters),
				GenericResponse.class);
	}

	public GenericResponse deleteSubaccount(
			LinkedHashMap<String, String> parameters) throws PlivoException {
		String subauth_id = this.getKeyValue(parameters, "subauth_id");
		return this.gson.fromJson(
				request("DELETE", String.format("/Subaccount/%s/", subauth_id),
						parameters), GenericResponse.class);
	}

	// Application
	public ApplicationFactory getApplications() throws PlivoException {
		return this.gson.fromJson(
				request("GET", "/Application/",
						new LinkedHashMap<String, String>()),
				ApplicationFactory.class);
	}

	public Application getApplication(LinkedHashMap<String, String> parameters)
			throws PlivoException {
		String app_id = this.getKeyValue(parameters, "app_id");
		return this.gson
				.fromJson(
						request("GET",
								String.format("/Application/%s/", app_id),
								new LinkedHashMap<String, String>()),
						Application.class);
	}

	public GenericResponse createApplication(
			LinkedHashMap<String, String> parameters) throws PlivoException {
		return this.gson.fromJson(request("POST", "/Application/", parameters),
				GenericResponse.class);
	}

	public GenericResponse editApplication(
			LinkedHashMap<String, String> parameters) throws PlivoException {
		String app_id = this.getKeyValue(parameters, "app_id");
		return this.gson.fromJson(
				request("POST", String.format("/Application/%s/", app_id),
						parameters), GenericResponse.class);
	}

	public GenericResponse deleteApplication(
			LinkedHashMap<String, String> parameters) throws PlivoException {
		String app_id = this.getKeyValue(parameters, "app_id");
		return this.gson.fromJson(
				request("DELETE", String.format("/Application/%s/", app_id),
						parameters), GenericResponse.class);
	}

	// Call
	public CDRFactory getCDRs(LinkedHashMap<String, String> parameters)
			throws PlivoException {
		return this.gson.fromJson(request("GET", "/Call/", parameters),
				CDRFactory.class);
	}

	public CDR getCDR(LinkedHashMap<String, String> parameters)
			throws PlivoException {
		String record_id = getKeyValue(parameters, "record_id");
		return this.gson.fromJson(
				request("GET", String.format("/Call/%s/", record_id),
						new LinkedHashMap<String, String>()), CDR.class);
	}

	public LiveCallFactory getLiveCalls() throws PlivoException {
		LinkedHashMap<String, String> parameters = new LinkedHashMap<String, String>();
		parameters.put("status", "live");
		return this.gson.fromJson(request("GET", "/Call/", parameters),
				LiveCallFactory.class);
	}

	public LiveCall getLiveCall(LinkedHashMap<String, String> parameters)
			throws PlivoException {
		String call_uuid = getKeyValue(parameters, "call_uuid");
		parameters.put("status", "live");
		return this.gson.fromJson(
				request("GET", String.format("/Call/%s/", call_uuid),
						parameters), LiveCall.class);
	}

	public Call makeCall(LinkedHashMap<String, String> parameters)
			throws PlivoException {
		return this.gson.fromJson(request("POST", "/Call/", parameters),
				Call.class);
	}

	public GenericResponse hangupAllCalls() throws PlivoException {
		return this.gson
				.fromJson(
						request("DELETE", "/Call/",
								new LinkedHashMap<String, String>()),
						GenericResponse.class);
	}

	public GenericResponse hangupCall(LinkedHashMap<String, String> parameters)
			throws PlivoException {
		String call_uuid = getKeyValue(parameters, "call_uuid");
		return this.gson.fromJson(
				request("DELETE", String.format("/Call/%s/", call_uuid),
						new LinkedHashMap<String, String>()),
				GenericResponse.class);
	}

	public GenericResponse transferCall(LinkedHashMap<String, String> parameters)
			throws PlivoException {
		String call_uuid = getKeyValue(parameters, "call_uuid");
		return this.gson.fromJson(
				request("POST", String.format("/Call/%s/", call_uuid),
						parameters), GenericResponse.class);
	}

	public Record record(LinkedHashMap<String, String> parameters)
			throws PlivoException {
		String call_uuid = getKeyValue(parameters, "call_uuid");
		return this.gson.fromJson(
				request("POST", String.format("/Call/%s/Record/", call_uuid),
						parameters), Record.class);
	}

	public GenericResponse stopRecord(LinkedHashMap<String, String> parameters)
			throws PlivoException {
		String call_uuid = getKeyValue(parameters, "call_uuid");
		return this.gson.fromJson(
				request("DELETE", String.format("/Call/%s/Record/", call_uuid),
						new LinkedHashMap<String, String>()),
				GenericResponse.class);
	}

	public GenericResponse play(LinkedHashMap<String, String> parameters)
			throws PlivoException {
		String call_uuid = getKeyValue(parameters, "call_uuid");
		return this.gson.fromJson(
				request("POST", String.format("/Call/%s/Play/", call_uuid),
						parameters), GenericResponse.class);
	}

	public GenericResponse stopPlay(LinkedHashMap<String, String> parameters)
			throws PlivoException {
		String call_uuid = getKeyValue(parameters, "call_uuid");
		return this.gson.fromJson(
				request("DELETE", String.format("/Call/%s/Play/", call_uuid),
						new LinkedHashMap<String, String>()),
				GenericResponse.class);
	}

	public GenericResponse speak(LinkedHashMap<String, String> parameters)
			throws PlivoException {
		String text = HtmlEntity.convert(getKeyValue(parameters, "text"));
		parameters.put("text", text);
		String call_uuid = getKeyValue(parameters, "call_uuid");
		return this.gson.fromJson(
				request("POST", String.format("/Call/%s/Speak/", call_uuid),
						parameters), GenericResponse.class);
	}

	public GenericResponse stopSpeak(LinkedHashMap<String, String> parameters)
			throws PlivoException {
		String call_uuid = getKeyValue(parameters, "call_uuid");
		return this.gson.fromJson(
				request("DELETE", String.format("/Call/%s/Speak/", call_uuid),
						new LinkedHashMap<String, String>()),
				GenericResponse.class);
	}

	public GenericResponse sendDigits(LinkedHashMap<String, String> parameters)
			throws PlivoException {
		String call_uuid = getKeyValue(parameters, "call_uuid");
		return this.gson.fromJson(
				request("POST", String.format("/Call/%s/DTMF/", call_uuid),
						parameters), GenericResponse.class);
	}

	// Conference
	public LiveConferenceFactory getLiveConferences() throws PlivoException {
		return this.gson.fromJson(
				request("GET", "/Conference/",
						new LinkedHashMap<String, String>()),
				LiveConferenceFactory.class);
	}

	public GenericResponse hangupAllConferences() throws PlivoException {
		return this.gson.fromJson(
				request("DELETE", "/Conference/",
						new LinkedHashMap<String, String>()),
				GenericResponse.class);
	}

	public Conference getLiveConference(LinkedHashMap<String, String> parameters)
			throws PlivoException {
		String conference_name = getKeyValue(parameters, "conference_name");
		return this.gson.fromJson(
				request("GET",
						String.format("/Conference/%s/", conference_name),
						new LinkedHashMap<String, String>()), Conference.class);
	}

	public GenericResponse hangupConference(
			LinkedHashMap<String, String> parameters) throws PlivoException {
		String conference_name = getKeyValue(parameters, "conference_name");
		return this.gson.fromJson(
				request("DELETE",
						String.format("/Conference/%s/", conference_name),
						new LinkedHashMap<String, String>()),
				GenericResponse.class);
	}

	public GenericResponse hangupMember(LinkedHashMap<String, String> parameters)
			throws PlivoException {
		String conference_name = getKeyValue(parameters, "conference_name");
		String member_id = getKeyValue(parameters, "member_id");
		return this.gson.fromJson(
				request("DELETE", String.format(
						"/Conference/%1$s/Member/%2$s/", conference_name,
						member_id), new LinkedHashMap<String, String>()),
				GenericResponse.class);
	}

	public GenericResponse playMember(LinkedHashMap<String, String> parameters)
			throws PlivoException {
		String conference_name = getKeyValue(parameters, "conference_name");
		String member_id = getKeyValue(parameters, "member_id");
		return this.gson.fromJson(
				request("POST", String.format(
						"/Conference/%1$s/Member/%2$s/Play/", conference_name,
						member_id), new LinkedHashMap<String, String>()),
				GenericResponse.class);
	}

	public GenericResponse stopPlayMember(
			LinkedHashMap<String, String> parameters) throws PlivoException {
		String conference_name = getKeyValue(parameters, "conference_name");
		String member_id = getKeyValue(parameters, "member_id");
		return this.gson.fromJson(
				request("DELETE", String.format(
						"/Conference/%1$s/Member/%2$s/Play/", conference_name,
						member_id), new LinkedHashMap<String, String>()),
				GenericResponse.class);
	}

	public GenericResponse speakMember(LinkedHashMap<String, String> parameters)
			throws PlivoException {
		String text = HtmlEntity.convert(getKeyValue(parameters, "text"));
		parameters.put("text", text);
		String conference_name = getKeyValue(parameters, "conference_name");
		String member_id = getKeyValue(parameters, "member_id");
		return this.gson.fromJson(
				request("POST", String.format(
						"/Conference/%1$s/Member/%2$s/Speak/", conference_name,
						member_id), new LinkedHashMap<String, String>()),
				GenericResponse.class);
	}

	public GenericResponse deafMember(LinkedHashMap<String, String> parameters)
			throws PlivoException {
		String conference_name = getKeyValue(parameters, "conference_name");
		String memberId = getKeyValue(parameters, "member_id");
		return this.gson.fromJson(
				request("POST", String.format(
						"/Conference/%1$s/Member/%2$s/Deaf/", conference_name,
						memberId), new LinkedHashMap<String, String>()),
				GenericResponse.class);
	}

	public GenericResponse undeafMember(LinkedHashMap<String, String> parameters)
			throws PlivoException {
		String conference_name = getKeyValue(parameters, "conference_name");
		String memberId = getKeyValue(parameters, "member_id");
		return this.gson.fromJson(
				request("DELETE", String.format(
						"/Conference/%1$s/Member/%2$s/Deaf/", conference_name,
						memberId), new LinkedHashMap<String, String>()),
				GenericResponse.class);
	}

	public GenericResponse muteMember(LinkedHashMap<String, String> parameters)
			throws PlivoException {
		String conference_name = getKeyValue(parameters, "conference_name");
		String member_id = getKeyValue(parameters, "member_id");
		return this.gson.fromJson(
				request("POST", String.format(
						"/Conference/%1$s/Member/%2$s/Mute/", conference_name,
						member_id), new LinkedHashMap<String, String>()),
				GenericResponse.class);
	}

	public GenericResponse unmuteMember(LinkedHashMap<String, String> parameters)
			throws PlivoException {
		String conference_name = getKeyValue(parameters, "conference_name");
		String member_id = getKeyValue(parameters, "member_id");
		return this.gson.fromJson(
				request("DELETE", String.format(
						"/Conference/%1$s/Member/%2$s/Mute/", conference_name,
						member_id), new LinkedHashMap<String, String>()),
				GenericResponse.class);
	}

	public GenericResponse kickMember(LinkedHashMap<String, String> parameters)
			throws PlivoException {
		String conference_name = getKeyValue(parameters, "conference_name");
		String member_id = getKeyValue(parameters, "member_id");
		return this.gson.fromJson(
				request("POST", String.format(
						"/Conference/%1$s/Member/%2$s/Kick/", conference_name,
						member_id), new LinkedHashMap<String, String>()),
				GenericResponse.class);
	}

	public Record recordConference(LinkedHashMap<String, String> parameters)
			throws PlivoException {
		String conference_name = getKeyValue(parameters, "conference_name");
		return this.gson.fromJson(
				request("POST", String.format("/Conference/%s/Record/",
						conference_name), parameters), Record.class);
	}

	public GenericResponse stopRecordConference(
			LinkedHashMap<String, String> parameters) throws PlivoException {
		String conference_name = getKeyValue(parameters, "conference_name");
		return this.gson.fromJson(
				request("DELETE", String.format("/Conference/%s/Record/",
						conference_name), new LinkedHashMap<String, String>()),
				GenericResponse.class);
	}

	// Endpoint
	public EndpointFactory getEndpoints(LinkedHashMap<String, String> parameters)
			throws PlivoException {
		return this.gson.fromJson(request("GET", "/Endpoint/", parameters),
				EndpointFactory.class);
	}

	public GenericResponse createEndpoint(
			LinkedHashMap<String, String> parameters) throws PlivoException {
		return this.gson.fromJson(request("POST", "/Endpoint/", parameters),
				GenericResponse.class);
	}

	public Endpoint getEndpoint(LinkedHashMap<String, String> parameters)
			throws PlivoException {
		String endpoint_id = getKeyValue(parameters, "endpoint_id");
		return this.gson.fromJson(
				request("GET", String.format("/Endpoint/%s/", endpoint_id),
						new LinkedHashMap<String, String>()), Endpoint.class);
	}

	public GenericResponse editEndpoint(LinkedHashMap<String, String> parameters)
			throws PlivoException {
		String endpoint_id = getKeyValue(parameters, "endpoint_id");
		return this.gson.fromJson(
				request("POST", String.format("/Endpoint/%s/", endpoint_id),
						parameters), GenericResponse.class);
	}

	public GenericResponse deleteEndpoint(
			LinkedHashMap<String, String> parameters) throws PlivoException {
		String endpoint_id = getKeyValue(parameters, "endpoint_id");
		return this.gson.fromJson(
				request("DELETE", String.format("/Endpoint/%s/", endpoint_id),
						new LinkedHashMap<String, String>()),
				GenericResponse.class);
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
		String number = getKeyValue(parameters, "number");
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
		String groupId = getKeyValue(parameters, "group_id");
		return this.gson.fromJson(
				request("POST",
						String.format("/AvailableNumberGroup/%s/", groupId),
						parameters), NumberResponse.class);
	}

	public GenericResponse unRentNumber(LinkedHashMap<String, String> parameters)
			throws PlivoException {
		String number = getKeyValue(parameters, "number");
		return this.gson.fromJson(
				request("DELETE", String.format("/Number/%s/", number),
						parameters), GenericResponse.class);
	}

	public GenericResponse linkApplicationNumber(
			LinkedHashMap<String, String> parameters) throws PlivoException {
		String number = getKeyValue(parameters, "number");
		return this.gson.fromJson(
				request("POST", String.format("/Number/%s/", number),
						parameters), GenericResponse.class);
	}

	public GenericResponse unlinkApplicationNumber(
			LinkedHashMap<String, String> parameters) throws PlivoException {
		String number = getKeyValue(parameters, "number");
		parameters.put("app_id", "");
		return this.gson.fromJson(
				request("POST", String.format("/Number/%s/", number),
						parameters), GenericResponse.class);
	}

	// Message
	public MessageResponse sendMessage(LinkedHashMap<String, String> parameters)
			throws PlivoException {
		return this.gson.fromJson(request("POST", "/Message/", parameters),
				MessageResponse.class);
	}

	public Message getMessage(LinkedHashMap<String, String> parameters)
			throws PlivoException {
		String record_id = getKeyValue(parameters, "record_id");
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

	// Incoming Carrier
	public IncomingCarrierFactory getIncomingCarriers(
			LinkedHashMap<String, String> parameters) throws PlivoException {
		return this.gson.fromJson(
				request("GET", "/IncomingCarrier/", parameters),
				IncomingCarrierFactory.class);
	}

	public IncomingCarrier getIncomingCarrier(
			LinkedHashMap<String, String> parameters) throws PlivoException {
		String carrier = getKeyValue(parameters, "carrier_id");
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
		String carrier = getKeyValue(parameters, "carrier_id");
		return this.gson.fromJson(
				request("POST", String.format("/IncomingCarrier/", carrier),
						parameters), GenericResponse.class);
	}

	public GenericResponse dropIncomingCarrier(
			LinkedHashMap<String, String> parameters) throws PlivoException {
		String carrier = getKeyValue(parameters, "carrier_id");
		return this.gson.fromJson(
				request("DELETE",
						String.format("/IncomingCarrier/%s/", carrier),
						parameters), GenericResponse.class);
	}

	// Outgoing Carrier
	public OutgoingCarrierFactory getOutgoingCarriers(
			LinkedHashMap<String, String> parameters) throws PlivoException {
		return this.gson.fromJson(
				request("GET", "/OutgoingCarrier/", parameters),
				OutgoingCarrierFactory.class);
	}

	public OutgoingCarrier getOutgoingCarrier(
			LinkedHashMap<String, String> parameters) throws PlivoException {
		String carrier = getKeyValue(parameters, "carrier_id");
		return this.gson.fromJson(
				request("GET", String.format("/OutgoingCarrier/%s/", carrier),
						parameters), OutgoingCarrier.class);
	}

	public OutgoingCarrierCreatedResponse addOutgoingCarrier(
			LinkedHashMap<String, String> parameters) throws PlivoException {
		return this.gson.fromJson(
				request("POST", "/OutgoingCarrier/", parameters),
				OutgoingCarrierCreatedResponse.class);
	}

	public GenericResponse editOutgoingCarrier(
			LinkedHashMap<String, String> parameters) throws PlivoException {
		String carrier = getKeyValue(parameters, "carrier_id");
		return this.gson.fromJson(
				request("POST", String.format("/OutgoingCarrier/%s/", carrier),
						parameters), GenericResponse.class);
	}

	public GenericResponse dropOutgoingCarrier(
			LinkedHashMap<String, String> parameters) throws PlivoException {
		String carrier = getKeyValue(parameters, "carrier_id");
		return this.gson.fromJson(
				request("DELETE",
						String.format("/OutgoingCarrier/%s/", carrier),
						parameters), GenericResponse.class);
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
		String carrier = getKeyValue(parameters, "routing_id");
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
		String routing_id = getKeyValue(parameters, "routing_id");
		return this.gson.fromJson(
				request("POST", String.format("/OutgoingCarrierRouting/%s/",
						routing_id), parameters), GenericResponse.class);
	}

	public GenericResponse dropOutgoingCarrierRouting(
			LinkedHashMap<String, String> parameters) throws PlivoException {
		String routing_id = getKeyValue(parameters, "routing_id");
		return this.gson.fromJson(
				request("DELETE", String.format("/OutgoingCarrierRouting/%s/",
						routing_id), parameters), GenericResponse.class);
	}

	// Pricing
	public PlivoPricing getPricing(LinkedHashMap<String, String> parameters)
			throws PlivoException {
		return this.gson.fromJson(request("GET", "/Pricing/", parameters),
				PlivoPricing.class);
	}
}