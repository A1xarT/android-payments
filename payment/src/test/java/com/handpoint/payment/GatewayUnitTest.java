package com.handpoint.payment;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Test cases for Gateway class.
 */
@Config(manifest=Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class GatewayUnitTest {

	private final Gateway handpointDirect = new Gateway("https://commerce-api.handpoint.com/direct/", "155928", "m3rch4nts1gn4tur3k3y");
	private final Gateway handpointHosted = new Gateway("https://commerce-api.handpoint.com/hosted/", "155928", "m3rch4nts1gn4tur3k3y");

	@Test
	public void direct_request() throws Exception {

		final Map<String, String> request = new HashMap<>();

		request.put("action", "SALE");
		request.put("amount", "1000");
		request.put("cardCVV", "356");
		request.put("cardExpiryMonth", "12");
		request.put("cardExpiryYear", "15");
		request.put("cardNumber", "4929421234600821");
		request.put("countryCode", "826"); // GB
		request.put("currencyCode", "826"); // GBP
		request.put("customerAddress", "6347 Test Card Street");
		request.put("customerName", "Handpoint");
		request.put("customerPhone", "+44 (0) 8450099575");
		request.put("customerPostCode", "17T ST8");
		request.put("type", "1"); // E-commerce

		final Map<String, String> response = this.handpointDirect.directRequest(request);

		assertEquals(Integer.parseInt(response.get("responseCode")), Gateway.RC_SUCCESS);
		assertEquals(response.get("amountReceived"), request.get("amount"));
		assertEquals(response.get("state"), "captured");

	}

	@Test
	public void hosted_request() throws Exception {

		final Map<String, String> request = new HashMap<>();

		request.put("action", "SALE");
		request.put("amount", "2691");
		request.put("cardExpiryDate", "1213");
		request.put("cardNumber", "4929 4212 3460 0821");
		request.put("countryCode", "826"); // GB
		request.put("currencyCode", "826"); // GBP
		request.put("orderRef", "Signature Test");
		request.put("transactionUnique", "55f025addd3c2");
		request.put("type", "1"); // E-commerce

		final Map<String, String> options = new HashMap<>();

		options.put("submitText", "Confirm & Pay");

		final String html = this.handpointHosted.hostedRequest(request, options);

		assertEquals(html, "<form method=\"post\"  action=\"https://commerce-api.handpoint.com/hosted/\">\n"
				+ "<input type=\"hidden\" name=\"action\" value=\"SALE\" />\n"
				+ "<input type=\"hidden\" name=\"amount\" value=\"2691\" />\n"
				+ "<input type=\"hidden\" name=\"cardExpiryDate\" value=\"1213\" />\n"
				+ "<input type=\"hidden\" name=\"cardNumber\" value=\"4929 4212 3460 0821\" />\n"
				+ "<input type=\"hidden\" name=\"countryCode\" value=\"826\" />\n"
				+ "<input type=\"hidden\" name=\"currencyCode\" value=\"826\" />\n"
				+ "<input type=\"hidden\" name=\"merchantID\" value=\"100001\" />\n"
				+ "<input type=\"hidden\" name=\"orderRef\" value=\"Signature Test\" />\n"
				+ "<input type=\"hidden\" name=\"signature\" value=\"d3e967258981e5e96724d51165b9332d18dfa20e3fed83ff2cb63b2ca208d513b8dd20d71610c3202f418f29cebf27c07c1ae614d07cc3ee2c3f3af28e688fd4|action,amount,cardExpiryDate,cardNumber,countryCode,currencyCode,merchantID,orderRef,transactionUnique,type\" />\n"
				+ "<input type=\"hidden\" name=\"transactionUnique\" value=\"55f025addd3c2\" />\n"
				+ "<input type=\"hidden\" name=\"type\" value=\"1\" />\n"
				+ "<input  type=\"submit\" value=\"Confirm &amp; Pay\">\n"
				+ "</form>\n");

	}

}