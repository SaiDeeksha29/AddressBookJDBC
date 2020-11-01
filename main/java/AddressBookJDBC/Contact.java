package AddressBookJDBC;

public class Contact {
	public String firstName;
	public String lastName;
	public String address;
	public String city;
	public String state;
	public int zip;
	public int phoneNumber;
	public String email;
	public String addressBookName;
	public String addressBookType;

	public Contact(String firstName, String lastName, String address, String city, String state, int zip,
			int phoneNumber, String email, String addressBookName, String addressBookType) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.address = address;
		this.city = city;
		this.state = state;
		this.zip = zip;
		this.phoneNumber = phoneNumber;
		this.email = email;
		this.addressBookName = addressBookName;
		this.addressBookType = addressBookType;
	}
}
