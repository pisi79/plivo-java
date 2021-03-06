package com.plivo.test.number;

import junit.framework.Assert;

import org.junit.Test;

import com.plivo.helper.api.response.number.NumberGroupFactory;
import com.plivo.helper.exception.PlivoException;
import com.plivo.test.common.AbstractTest;

public class SearchAvailableNumbersTest extends AbstractTest {

	@Test
	public void test() {
		NumberGroupFactory numbers = new NumberGroupFactory();

		// Mandatory Parameter - country_iso
		getParameters().put("country_iso", "BE");

		// Optional Parameters - number_type, prefix, region, services, limit,
		// offset
		/*
		 * getParameters().put("number_type", "BE"); getParameters().put("prefix", "12345");
		 * getParameters().put("region", "California"); getParameters().put("services", "voice");
		 * getParameters().put("limit", "10"); getParameters().put("offset", "705");
		 */

		try {
			numbers = getRestApi().searchNumberGroups(getParameters());
			Assert.assertNotNull(numbers.groupList.get(0).groupId);
		} catch (PlivoException plivoException) {
			System.out.println(plivoException.getMessage());
		}
	}

}
