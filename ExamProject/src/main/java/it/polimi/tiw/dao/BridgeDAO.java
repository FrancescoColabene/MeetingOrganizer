package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class BridgeDAO {

	private Connection connection;

	public BridgeDAO(Connection connection) {
		this.connection = connection;
	}
	
	public void createInvites(int idMeeting, List<Integer> idPeople) throws SQLException {
		
		String query = "INSERT INTO db_project.bridge (idPerson, idMeeting) VALUES (?,?)";
		
		try (PreparedStatement preparedStatement = connection.prepareStatement(query);) {
			for(Integer x : idPeople) {
				preparedStatement.setInt(1, x);
				preparedStatement.setInt(2, idMeeting);
				preparedStatement.executeUpdate();
				preparedStatement.clearParameters();
			}
		}
	}
}
