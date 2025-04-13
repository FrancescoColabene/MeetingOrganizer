package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;

import it.polimi.tiw.beans.Meeting;
import it.polimi.tiw.beans.Person;
import it.polimi.tiw.dao.MeetingDAO;
import it.polimi.tiw.utils.ConnectionHandler;

/**
 * Servlet implementation class CreateMeeting
 */
@WebServlet("/CreateMeeting")
@MultipartConfig
public class CreateMeeting extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

    public CreateMeeting() {
        super();
        // TODO Auto-generated constructor stub
    }

    public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String loginpath = getServletContext().getContextPath() + "/index.html";
		HttpSession session = request.getSession();
		if (session.isNew() || session.getAttribute("user") == null) {
			response.sendRedirect(loginpath);
			return;
		}
		
		String title = null;
		title = StringEscapeUtils.escapeJava(request.getParameter("hiddenTitle"));
		if(title == null) {
			// ? ? ? ? ? ?
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Don't touch hidden form values! And please Undo from browser");
			return;
		}
		
		List<Person> people = (List<Person>) session.getAttribute("people " + title);
		
		String[] invites = request.getParameterValues("invited");
		if(invites == null) {
			response.setStatus(HttpServletResponse.SC_OK);
			response.setCharacterEncoding("UTF-8");
			response.getWriter().print(-2);
			return;
		}
		
		// come ritrovo il titolo della riunione da qua?
		// oppure, come discrimino due pagine del browser dal punto di vista della servlet?
		// come mi ricordo a quale riunione fa riferimento una pagina o l'altra?
		
		Meeting m = (Meeting) session.getAttribute(title);
		int over = invites.length+1 - m.getMax();
		if( over > 0 ) {
			m.setCounter(m.getCounter()+1);
			if(m.getCounter() == 3) {
				session.removeAttribute(title);
				session.removeAttribute("people " + title);
				response.setStatus(HttpServletResponse.SC_OK);
				response.setCharacterEncoding("UTF-8");
				response.getWriter().print(-1);
				return;
			}
			response.setStatus(HttpServletResponse.SC_OK);
			response.setCharacterEncoding("UTF-8");
			response.getWriter().print(over);
			return;
		}
		else {
			
			// posso creare la riunione
			
			List<Integer> invitedPeople = new ArrayList<>();
			for (int i = 0; i < invites.length; i++) {
				invitedPeople.add(Integer.parseInt(invites[i]));
			}
			
			MeetingDAO meetingDAO = new MeetingDAO(connection);
			
			try {
				session.removeAttribute(title);
				session.removeAttribute("people " + title);
				if(!meetingDAO.createMeeting(m.getIdHost(), title, m.getDataStart(), m.getHourStart(), m.getDuration(), m.getMax(), invitedPeople)) {
					throw new Exception();
				}
				response.setStatus(HttpServletResponse.SC_OK);
				response.setCharacterEncoding("UTF-8");
				response.getWriter().print(0);
			} catch (Exception e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.setCharacterEncoding("UTF-8");
				response.getWriter().print(-1);
				return;
			}
		}
	}
}
