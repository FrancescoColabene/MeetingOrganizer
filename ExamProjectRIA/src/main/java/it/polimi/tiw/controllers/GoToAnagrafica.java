package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import it.polimi.tiw.beans.Meeting;
import it.polimi.tiw.beans.Person;
import it.polimi.tiw.dao.PersonDAO;
import it.polimi.tiw.utils.ConnectionHandler;

/**
 * Servlet implementation class GoToAnagrafica
 */
@WebServlet("/OpenAnagrafica")
@MultipartConfig
public class GoToAnagrafica extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	
    public GoToAnagrafica() {
        super();
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
		Person person = (Person) session.getAttribute("user");
		
		// values check
		String title = null;
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm");
		Date startDate = new Date();
		Date startHour = new Date();
		Date duration = new Date();
		Integer max = 0;
		try {
			title = StringEscapeUtils.escapeJava(request.getParameter("title"));
			startDate = (Date) sdf1.parse(request.getParameter("date"));
			startHour = (Date) sdf2.parse(request.getParameter("hour"));
			duration = (Date) sdf2.parse(request.getParameter("duration"));
			max = Integer.parseInt(request.getParameter("max"));
			if (title == null || startDate == null || startHour == null || startHour == null || title.isEmpty()) {
				throw new Exception("Missing or empty credential value");
			}
			if (max < 2) {
				throw new Exception("You can't create a meeting without people");
			}
			if (title.length() > 45) {
				throw new Exception("The title is too long, choose a shorter one");
			}
			Calendar c = Calendar.getInstance();
			
			startDate.setHours(startHour.getHours());
			startDate.setMinutes(startHour.getMinutes());
			if( duration.getHours() == 0 && duration.getMinutes() == 0) {
				throw new Exception("The duration is zero, please modify it");
			}
			if( startDate.before(c.getTime()) ) {
				throw new Exception("The selected date has already passed");
			}

			Meeting m = new Meeting(person.getId(), 0, title, startDate, startHour, duration, max);
			session.setAttribute(m.getTitle(), m);
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println(e.getMessage());
			return;
		}
		
		
		PersonDAO pDAO = new PersonDAO(connection);
		List<Person> people = new ArrayList<>();
		
		try {
			people = pDAO.getPeopleExcept(person);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Not possible to recover people");
			return;
		}
		
		session.setAttribute("people " + title, people);
		Map<Person,Boolean> checks = new HashMap<>();
		for(Person p : people) {
			checks.put(p, false);
		}
		Map<Person, Boolean> sortedMap = new TreeMap<>(
	             new Comparator<Person>() {
	                @Override
	                public int compare(Person o1, Person o2) {
	                    return o1.getId()-o2.getId();
	                }
	            }
	    );
		sortedMap.putAll(checks);
		
		Gson gson = new GsonBuilder().create();
		String json = gson.toJson(checks);
		
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(json);
	}

}

