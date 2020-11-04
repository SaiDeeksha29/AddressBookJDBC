package AddressBookJDBC;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import com.google.gson.Gson;
import AddressBookJDBC.AddressBookService.IOService;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

@FixMethodOrder(MethodSorters.JVM)
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
	public void givenNewListOfContacts_WhenAdded_ShouldMatch() {
		AddressBookService addressBookService;
		Contact[] arrayOfContacts = getContactList();
		addressBookService = new AddressBookService(Arrays.asList(arrayOfContacts));
		Contact[] contactArrays = {
				new Contact("Monica", "Geller", "Manhanttan", "NYC", "NYC", 73410091, 90991212,
						"mounikageller@gmail.com", "Casual", LocalDate.now()),
				new Contact("Mohana", "Kavya", "Bhadrachalam", "Khammam", "Telangana", 510119, 99797878,
						"mohanakavya@gmail.com", "corporate", LocalDate.now()) };
		for (Contact contactData : contactArrays) {
			Response response = addContactToJsonServer(contactData);
			int statusCode = response.getStatusCode();
			Assert.assertEquals(201, statusCode);
			contactData = new Gson().fromJson(response.asString(), Contact.class);
			addressBookService.addContactToAddressBook(contactData, IOService.REST_IO);
		}
		long entries = addressBookService.countEntries(IOService.REST_IO);
		Assert.assertEquals(5, entries);
	}

	@Test
	public void givenContactDataInJsonServer_WhenRetrived_ShouldMatchCount() {
		Contact[] arrayOfContacts = getContactList();
		AddressBookService addressBookService;
		addressBookService = new AddressBookService(Arrays.asList(arrayOfContacts));
		long entries = addressBookService.countEntries(IOService.REST_IO);
		Assert.assertEquals(5, entries);
	}

	@Test
	public void givenNewContact_WhenUpdated_ShouldMatch200Response() {
		AddressBookService addressBookService;
		Contact[] arrayOfContacts = getContactList();
		addressBookService = new AddressBookService(Arrays.asList(arrayOfContacts));
		addressBookService.updateContactJsonServer("Uma", "Time Square", IOService.REST_IO);
		Contact contactData = addressBookService.getContactData("Uma");
		String contactJson = new Gson().toJson(contactData);
		RequestSpecification request = RestAssured.given();
		request.header("Content-Type", "application/json");
		request.body(contactJson);
		Response response = request.put("/contacts/" + contactData.id);
		int statusCode = response.getStatusCode();
		Assert.assertEquals(200, statusCode);
	}

	@Test
	public void givenNewContactName_WhenRemoved_ShouldMatch200ResponseAndCount() {
		AddressBookService addressBookService;
		Contact[] arrayOfContacts = getContactList();
		addressBookService = new AddressBookService(Arrays.asList(arrayOfContacts));
		Contact contactData = addressBookService.getContactData("Monica");
		RequestSpecification request = RestAssured.given();
		request.header("Content-Type", "application/json");
		Response response = request.delete("/contacts/" + contactData.id);
		int statusCode = response.getStatusCode();
		Assert.assertEquals(200, statusCode);
		addressBookService.deleteContact(contactData.firstName, IOService.REST_IO);
		long entries = addressBookService.countEntries(IOService.REST_IO);
		Assert.assertEquals(4, entries);
	}
}
