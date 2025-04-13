package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.lang.String;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import it.polimi.tiw.beans.Meeting;
import it.polimi.tiw.beans.Person;
import it.polimi.tiw.dao.MeetingDAO;
import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.utils.MeetingDateComparator;

@WebServlet("/GetInvitedMeetings")
public class GetInvitedMeetings extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public GetInvitedMeetings() {
		super();
	}

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		Person person = (Person) session.getAttribute("user");
		MeetingDAO meetingDAO = new MeetingDAO(connection);
		List<Meeting> invitedMeetings = new ArrayList<>();
		
		try {
			invitedMeetings = meetingDAO.findMeetingsByInvites(person.getId());
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to recover meetings");
			return;
		}
		MeetingDateComparator mdc = new MeetingDateComparator();
		invitedMeetings.sort(mdc);
		
		Gson gson = new GsonBuilder()
				   .setDateFormat("yyyy MMM dd").create();
		String json = gson.toJson(invitedMeetings);
		
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(json);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
