package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.polimi.tiw.beans.Person;

public class PersonDAO {

	private Connection connection;

	public PersonDAO(Connection connection) {
		this.connection = connection;
	}
	
	public List<Person> getPeopleExcept(Person p) throws SQLException {
		List<Person> temp = new ArrayList<>();
		String query = "SELECT id, name, surname, email FROM db_project.account WHERE id != ?";
		try (PreparedStatement preparedStatement = connection.prepareStatement(query);) {
			preparedStatement.setInt(1, p.getId());
			try (ResultSet result = preparedStatement.executeQuery();) {
				if (!result.isBeforeFirst()) // in questo caso, true sse il risultato è vuoto
					return null;
				else {
					while(result.next()) {
						Person person = new Person();
						person.setId(result.getInt("id"));
						person.setName(result.getString("name"));
						person.setSurname(result.getString("surname"));
						person.setEmail(result.getString("email"));
						temp.add(person);
					}	
				}
			}
		}
		return temp;
	}
	
	public boolean checkEmail(String email) throws SQLException {
		String query = "SELECT id FROM db_project.account WHERE email = ?";
		try (PreparedStatement preparedStatement = connection.prepareStatement(query);) {
			preparedStatement.setString(1, email);
			try (ResultSet result = preparedStatement.executeQuery();) {
				return !result.isBeforeFirst();
				// true se il risultato è vuoto cioè posso usare l'email, viceversa è falso
			}
		}
	}
	
	public boolean checkUsername(String username) throws SQLException {
		String query = "SELECT id FROM db_project.account WHERE username = ?";
		try (PreparedStatement preparedStatement = connection.prepareStatement(query);) {
			preparedStatement.setString(1, username);
			try (ResultSet result = preparedStatement.executeQuery();) {
				return !result.isBeforeFirst();
				// true se il risultato è vuoto cioè posso usare l'email, viceversa è falso
			}
		}
	}
	
	
	public Person checkUser(String username, String password) throws SQLException {
		String query = "SELECT id, name, surname, email FROM db_project.account WHERE username = ? AND password = ? ";
		try (PreparedStatement preparedStatement = connection.prepareStatement(query);) {
			preparedStatement.setString(1, username);
			preparedStatement.setString(2, password);
			try (ResultSet result = preparedStatement.executeQuery();) {
				if (!result.isBeforeFirst()) // in questo caso, true sse il risultato è vuoto
					return null;
				else {
					result.next();
					Person person = new Person();
					person.setId(result.getInt("id"));
					person.setName(result.getString("name"));
					person.setSurname(result.getString("surname"));
					person.setEmail(result.getString("email"));
					person.setUsername(username);
					return person;
				}
			}
		}
	}
	
	public boolean createPerson(String name, String surname, String username, String email, String password) throws SQLException {
		String query = "INSERT INTO db_project.account (name,surname,username,email,password) VALUES (?,?,?,?,?)";
		try (PreparedStatement preparedStatement = connection.prepareStatement(query);) {
			preparedStatement.setString(1, name);
			preparedStatement.setString(2, surname);
			preparedStatement.setString(3, username);
			preparedStatement.setString(4, email);
			preparedStatement.setString(5, password);
			return preparedStatement.executeUpdate()>0;
		}
	}
	
	public String getFullNameFromId(int id) throws SQLException {
		String fullName = "";
		String query = "SELECT name, surname FROM db_project.account WHERE id = ?";
		try (PreparedStatement preparedStatement = connection.prepareStatement(query);) {
			preparedStatement.setInt(1, id);
			try (ResultSet result = preparedStatement.executeQuery();) {
				if (!result.isBeforeFirst()) // in questo caso, true sse il risultato è vuoto
					return null;
				else {
					result.next();
					fullName = result.getString("name") + " " + result.getString("surname");
				}
			}
		}
		return fullName;
	}
}
