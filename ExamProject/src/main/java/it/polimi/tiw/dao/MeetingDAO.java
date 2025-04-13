package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import it.polimi.tiw.beans.*;

public class MeetingDAO {

	private Connection connection;

	public MeetingDAO(Connection connection) {
		this.connection = connection;
	}
	
	public List<Meeting> findMeetingsByCreator(int id) throws SQLException {
		List<Meeting> meetings = new ArrayList<>();
		String query = "SELECT title, dataStart, hourStart, duration FROM meeting WHERE idHost = ?";
		try (PreparedStatement preparedStatement = connection.prepareStatement(query);) {
			preparedStatement.setInt(1, id);
			try (ResultSet result = preparedStatement.executeQuery();) {
				if (!result.isBeforeFirst()) // in questo caso, true sse il risultato è vuoto
					return meetings;
				else {
					while(result.next()) {
						Meeting meeting = new Meeting();
						meeting.setTitle(result.getString("title"));
						meeting.setDataStart(result.getDate("dataStart"));
						meeting.setHourStart(result.getTime("hourStart"));
						meeting.setDuration(result.getTime("duration"));
						// check the date and starting hour of the meeting
						LocalDateTime actualDate = LocalDateTime.now();
						LocalDateTime desiredDate = new java.util.Date(meeting.getDataStart().getTime()).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
						desiredDate = desiredDate.with(LocalTime.of(meeting.getHourStart().getHours(), meeting.getHourStart().getMinutes()));
						if( actualDate.isBefore(desiredDate))
						{
							meetings.add(meeting);
						}
						
						//if(meeting.getDataStart().after(new Date(Calendar.getInstance().getTime().getTime())) ||
						//		meeting.getHourStart().after(new Date(Calendar.getInstance().getTime().getTime())))
						//{
						//	meetings.add(meeting);
						//}
					}
				return meetings;
				}
			}
		}
	}
	
	public List<Meeting> findMeetingsByInvites(int id) throws SQLException {
		List<Meeting> meetings = new ArrayList<>();
		String query = "SELECT idHost, title, dataStart, hourStart, duration"
				+ " FROM (account as a INNER JOIN meeting as m) INNER JOIN bridge as b"
				+ " WHERE m.id = b.idMeeting AND a.id = b.idPerson AND a.id =  ?";
		try (PreparedStatement preparedStatement = connection.prepareStatement(query);) {
			preparedStatement.setInt(1, id);
			try (ResultSet result = preparedStatement.executeQuery();) {
				if (!result.isBeforeFirst()) // in questo caso, true sse il risultato è vuoto
					return meetings;
				else {
					while(result.next()) {
						Meeting meeting = new Meeting();
						meeting.setIdHost(result.getInt("idHost"));
						meeting.setTitle(result.getString("title"));
						meeting.setDataStart(result.getDate("dataStart"));
						meeting.setHourStart(result.getTime("hourStart"));
						meeting.setDuration(result.getTime("duration"));
						
						if(meeting.getDataStart().after(new Date(Calendar.getInstance().getTime().getTime())) ||
								meeting.getHourStart().after(new Date(Calendar.getInstance().getTime().getTime())))
						{
							meetings.add(meeting);
						}
						
					}
				
				}
			}
		}
		PersonDAO pDAO = new PersonDAO(connection);
		for(Meeting m : meetings) {
			m.setFullNameHost(pDAO.getFullNameFromId(m.getIdHost()));
		}
		return meetings;
	}
	
	public boolean createMeeting(int idHost, String title, java.util.Date dataStart,
												Time hourStart, Time duration, int max, List<Integer> idPeople) throws SQLException {
		//qui devo fare una transazione! ricordati di castare date in java.sql.date
		
		String query = "INSERT INTO db_project.meeting (id, idHost, title, dataStart, hourStart, duration, max) VALUES (?,?,?,?,?,?,?)";
		BridgeDAO bDAO = new BridgeDAO(connection);
		
		int id = findLastId();
		id++;
		// TODO se id=0 ed esiste già una riunione con quell'id semplicemente 
		// lancierà una sqlexception e non salverà la riunione
		// devo gestirla ulteriormente?
		
		connection.setAutoCommit(false);
		try(PreparedStatement preparedStatement = connection.prepareStatement(query);){
			preparedStatement.setInt(1, id);
			preparedStatement.setInt(2, idHost);
			preparedStatement.setString(3, title);
			preparedStatement.setDate(4, new java.sql.Date(dataStart.getTime()));
			preparedStatement.setTime(5, hourStart);
			preparedStatement.setTime(6, duration);
			preparedStatement.setInt(7, max);
			preparedStatement.executeUpdate();
			
			bDAO.createInvites(id, idPeople);
			
			connection.commit();
			
		} catch (Exception e) {
			connection.rollback();
			connection.setAutoCommit(true);
			return false;
		}
		connection.setAutoCommit(true);
		return true;
	}
	
	private int findLastId() throws SQLException {
		String query = "SELECT id FROM db_project.meeting WHERE id >= all (SELECT id FROM db_project.meeting)";
		try (PreparedStatement preparedStatement = connection.prepareStatement(query);) {
			try (ResultSet result = preparedStatement.executeQuery();) {
				if (!result.isBeforeFirst()) // in questo caso, true sse il risultato è vuoto
				{
					return -1;
				}
				else {
					result.next();
					return result.getInt("id");
				}
			}
		}
	}
}
