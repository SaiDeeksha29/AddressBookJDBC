package AddressBookJDBC;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Test;

import AddressBookJDBC.AddressBookService.IOService;

public class AddressBookTest {

	private static Logger log = Logger.getLogger(AddressBookService.class.getName());

	@Test
	public void contactsWhenRetrievedFromDB_ShouldMatchCount() {
		AddressBookService addressBookService = new AddressBookService();
		List<Contact> contactList = addressBookService.readContactData();
		Assert.assertEquals(4, contactList.size());
	}

	@Test
	public void givenNewSalaryForEmployee_WhenUpdatedUsingPreparedStatement_ShouldSyncWithDB() {
		AddressBookService addressBookService = new AddressBookService();
		List<Contact> contactList = addressBookService.readContactData();
		addressBookService.updateContactDetails("Deeksha", "Kalpakkam");
		boolean result = addressBookService.checkContactInSyncWithDB("Deeksha");
		Assert.assertTrue(result);
	}

	@Test
	public void givenDateRange_WhenRetrieved_ShouldMatchEmployeeCount() {
		AddressBookService addressBookService = new AddressBookService();
		addressBookService.readContactData();
		LocalDate startDate = LocalDate.of(2018, 01, 01);
		LocalDate endDate = LocalDate.now();
		List<Contact> contactList = addressBookService.readContactDataForGivenDateRange(startDate, endDate);
		Assert.assertEquals(6, contactList.size());
	}

	@Test
	public void givenContacts_RetrieveNumberOfContacts_ByCityOrState() {
		AddressBookService addressBookService = new AddressBookService();
		addressBookService.readContactData();
		Map<String, Integer> contactByCityOrStateMap = addressBookService.readContactByCityOrState();
		Assert.assertEquals(true, contactByCityOrStateMap.get("Hyderabad").equals(3));
		Assert.assertEquals(true, contactByCityOrStateMap.get("TamilNadu").equals(2));
	}

	@Test
	public void givenNewContact_WhenAdded_ShouldSyncWithDB() {
		AddressBookService addressBookService = new AddressBookService();
		addressBookService.readContactData();
		LocalDate date = LocalDate.of(2020, 02, 20);
		addressBookService.addContactToDatabase("Uma", "Rani", "Whitefield", "Bangalore", "Karnataka", 700012, 99084874,
				"umarani@gmail.com", "Personal", "Family", date);
		boolean result = addressBookService.checkContactInSyncWithDB("Uma");
		Assert.assertTrue(result);
	}

	@Test
	public void givenContacts_WhenAddedToDB_ShouldMatchEmployeeEntries() {
		Contact[] arrayOfEmployee = {
				new Contact("Mohana", "Kavya", "Sathupalli", "Khammam", "Telangana", 507012, 98654331,
						"mohanakavya@gmail.com", "Casual", LocalDate.now()),
				new Contact("Mounika", "Anne", "Miyapur", "Hyderabad", "Telangana", 500050, 96763129,
						"mounikaanne@gmail.com", "Personal", LocalDate.now()),
				new Contact("Sohail", "Syed", "SGNagar", "Kalpakkam", "TamilNadu", 600010, 87655433,
						"sohailsyed@gmail.com", "Corporate", LocalDate.now()) };
		AddressBookService addressBookService = new AddressBookService();
		addressBookService.readData(IOService.DB_IO);
		Instant start = Instant.now();
		addressBookService.addContact(Arrays.asList(arrayOfEmployee));
		Instant end = Instant.now();
		log.info("Duration without thread : " + Duration.between(start, end));
		Instant threadStart = Instant.now();
		addressBookService.addEmployeeToPayrollWithThreads(Arrays.asList(arrayOfEmployee));
		Instant threadEnd = Instant.now();
		log.info("Duartion with Thread : " + Duration.between(threadStart, threadEnd));
		Assert.assertEquals(10, addressBookService.countEntries(IOService.DB_IO));
	}
}