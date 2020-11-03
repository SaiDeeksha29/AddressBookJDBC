package AddressBookJDBC;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;

import AddressBookJDBC.AddressBookService.IOService;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class AddressBookJsonServerTest {
	private static Logger log = Logger.getLogger(AddressBookJsonServerTest.class.getName());

	@Before
	public void setUp() {
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = 3000;
	}

	public Contact[] getContactList() {
		Response response = RestAssured.get("/contacts");
		log.info("Contact entries in JSON Server :\n" + response.asString());
		Contact[] arrayOfContacts = new Gson().fromJson(response.asString(), Contact[].class);
		return arrayOfContacts;
	}

	public Response addContactToJsonServer(Contact contactData) {
		String contactJson = new Gson().toJson(contactData);
		RequestSpecification request = RestAssured.given();
		request.header("Content-Type", "application/json");
		request.body(contactJson);
		return request.post("/contacts");
	}

	@Test
	public void givenEmployeeDataInJsonServer_WhenRetrived_ShouldMatchCount() {
		Contact[] arrayOfEmps = getContactList();
		AddressBookService employeePayrollService;
		employeePayrollService = new AddressBookService(Arrays.asList(arrayOfEmps));
		long entries = employeePayrollService.countEntries(IOService.REST_IO);
		Assert.assertEquals(3, entries);
	}

	@Test
	public void givenNewContact_WhenAdded__ShouldMatch() {
		AddressBookService addressBookService;
		Contact[] arrayOfContacts = getContactList();
		addressBookService = new AddressBookService(Arrays.asList(arrayOfContacts));
		Contact contactData = null;
		contactData = new Contact("Uma", "Rani", "Whitefield", "Bangalore", "Karnataka", 700012, 99084874,
				"umarani@gmail.com", "Casual", LocalDate.now());
		Response response = addContactToJsonServer(contactData);
		int statusCode = response.getStatusCode();
		Assert.assertEquals(201, statusCode);

		contactData = new Gson().fromJson(response.asString(), Contact.class);
		addressBookService.addContactToJSONServer(contactData, IOService.REST_IO);
		long entries = addressBookService.countEntries(IOService.REST_IO);
		Assert.assertEquals(3, entries);
	}
}
