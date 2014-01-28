package com.plivo.test.conference;

import java.util.LinkedHashMap;

import com.plivo.helper.api.client.RestAPI;
import com.plivo.helper.exception.PlivoException;
import com.plivo.helper.api.response.response.GenericResponse;

public class StopPlayMember {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		RestAPI restAPI = new RestAPI("<AUTH_ID>", "<AUTH_TOKEN>", "v1");
		
		LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
		
		params.put("conference_name", "1234");
		params.put("member_id", "1,2");                                /* single member_id or multiple or 'all' */
		
		GenericResponse response = new GenericResponse();
		
		try 
		{
			response = restAPI.stopPlayMember(params);
			System.out.println(response.apiId);
		}
		catch (PlivoException plivoException) {
			
			plivoException.printStackTrace();
		}
		
	}

}
