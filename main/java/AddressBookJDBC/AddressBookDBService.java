package AddressBookJDBC;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Logger;

public class AddressBookDBService {

	private static AddressBookDBService addressBookDBService;
	private static Logger log = Logger.getLogger(AddressBookDBService.class.getName());
	private PreparedStatement ContactDataStatement;

	private AddressBookDBService() {
	}

	public static AddressBookDBService getInstance() {
		if (addressBookDBService == null)
			addressBookDBService = new AddressBookDBService();
		return addressBookDBService;
	}

	public List<Contact> readData() {
		String sql = "SELECT c.firstName, c.lastName,c.Address_Book_Name,c.Address,c.City,"
				+ "c.State,c.Zip,c.Phone_Number,c.Email,a.Address_Book_Type "
				+ "from contacts c inner join Address_Book_Dictionary a "
				+ "on c.Address_Book_Name=a.Address_Book_Name; ";
		return this.getContactDetailsUsingSqlQuery(sql);
	}

	private List<Contact> getContactDetailsUsingSqlQuery(String sql) {
		List<Contact> ContactList = null;
		try (Connection connection = addressBookDBService.getConnection();) {
			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			ResultSet result = preparedStatement.executeQuery(sql);
			ContactList = this.getAddressBookData(result);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ContactList;
	}

	private List<Contact> getAddressBookData(ResultSet resultSet) {
		List<Contact> contactList = new ArrayList<>();
		try {
			while (resultSet.next()) {
				String firstName = resultSet.getString("firstName");
				String lastName = resultSet.getString("lastName");
				String addressBookName = resultSet.getString("Address_Book_Name");
				String address = resultSet.getString("Address");
				String city = resultSet.getString("City");
				String state = resultSet.getString("State");
				int zip = resultSet.getInt("zip");
				int phoneNumber = resultSet.getInt("Phone_Number");
				String email = resultSet.getString("email");
				String addressBookType = resultSet.getString("Address_Book_Type");
				contactList.add(new Contact(firstName, lastName, address, city, state, zip, phoneNumber, email,
						addressBookName, addressBookType));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return contactList;
	}

	public int updateEmployeeData(String name, String address) {
		return this.updateContactDataUsingPreparedStatement(name, address);
	}

	private int updateContactDataUsingPreparedStatement(String firstName, String address) {
		try (Connection connection = addressBookDBService.getConnection();) {
			String sql = "update contacts set Address=? where firstName=?;";
			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, address);
			preparedStatement.setString(2, firstName);
			int status = preparedStatement.executeUpdate();
			return status;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public List<Contact> getContactDataByName(String name) {
		List<Contact> contactList = null;
		if (this.ContactDataStatement == null)
			this.prepareStatementForContactData();
		try {
			ContactDataStatement.setString(1, name);
			ResultSet resultSet = ContactDataStatement.executeQuery();
			contactList = this.getAddressBookData(resultSet);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return contactList;
	}

	public List<Contact> getContactForGivenDateRange(LocalDate startDate, LocalDate endDate) {
		String sql = String.format(
				"SELECT c.firstName, c.lastName,c.Address_Book_Name,c.Address,c.City,"
						+ "c.State,c.Zip,c.Phone_Number,c.Email,a.Address_Book_Type "
						+ "from contacts c inner join Address_Book_Dictionary a "
						+ "on c.Address_Book_Name=a.Address_Book_Name WHERE startDate BETWEEN '%s' AND '%s'; ",
				Date.valueOf(startDate), Date.valueOf(endDate));
		return this.getContactDetailsUsingSqlQuery(sql);
	}

	public Map<String, Integer> getContactsByCityOrState() {
		Map<String, Integer> contactByCityOrStateMap = new HashMap<>();
		ResultSet resultSet;
		String sqlCity = "SELECT city, count(firstName) as count from contacts group by City; ";
		String sqlState = "SELECT state, count(firstName) as count from contacts group by State; ";
		try (Connection connection = addressBookDBService.getConnection()) {
			Statement statement = connection.createStatement();
			resultSet = statement.executeQuery(sqlCity);
			while (resultSet.next()) {
				String city = resultSet.getString("city");
				Integer count = resultSet.getInt("count");
				contactByCityOrStateMap.put(city, count);
			}
			resultSet = statement.executeQuery(sqlState);
			while (resultSet.next()) {
				String state = resultSet.getString("state");
				Integer count = resultSet.getInt("count");
				contactByCityOrStateMap.put(state, count);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return contactByCityOrStateMap;
	}

	public Contact addContact(String firstName, String lastName, String address, String city, String state, int zip,
			int phone, String email, String addressBookName, LocalDate startDate) {
		Connection connection = null;
		try {
			connection = this.getConnection();
			connection.setAutoCommit(false);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		try {
			Statement statement = connection.createStatement();
			String sql = String.format(
					"insert into contacts(firstName,lastName,Address_Book_Name,Address,City,State,Zip,Phone_Number,Email,startDate) values ('%s','%s','%s','%s','%s','%s','%s','%s','%s','%s')",
					firstName, lastName, addressBookName, address, city, state, zip, phone, email, startDate);
			statement.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (SQLException e1) {
				e.printStackTrace();
			}
		}

		try {
			connection.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return new Contact(firstName, lastName, address, city, state, zip, phone, email, addressBookName, startDate);
	}

	private void prepareStatementForContactData() {
		try {
			Connection connection = addressBookDBService.getConnection();
			String sql = "SELECT c.firstName, c.lastName,c.Address_Book_Name,c.Address,c.City,"
					+ "c.State,c.Zip,c.Phone_Number,c.Email,a.Address_Book_Type "
					+ "from contacts c inner join Address_Book_Dictionary a "
					+ "on c.Address_Book_Name=a.Address_Book_Name WHERE firstName=?; ";
			ContactDataStatement = connection.prepareStatement(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private Connection getConnection() throws SQLException {
		String jdbcURL = "jdbc:mysql://localhost:3306/address_book_service?useSSL=false";
		String userName = "root";
		String password = "satyasai1";
		Connection connection;
		log.info("Connecting to database: " + jdbcURL);
		connection = DriverManager.getConnection(jdbcURL, userName, password);
		log.info("Connection successful: " + connection);
		return connection;
	}
}