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
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

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
	private TemplateEngine templateEngine;

    public CreateMeeting() {
        super();
        // TODO Auto-generated constructor stub
    }

    public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
		ServletContext servletContext = getServletContext();
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String loginpath = getServletContext().getContextPath() + "/index.html";
		HttpSession session = request.getSession();
		if (session.isNew() || session.getAttribute("user") == null) {
			response.sendRedirect(loginpath);
			return;
		}
		
		String title = StringEscapeUtils.escapeJava(request.getParameter("title"));
		if(title == null) {
			// ? ? ? ? ? ?
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Don't touch hidden form values! And please Undo from browser");
		}
		
		List<Person> people = (List<Person>) session.getAttribute("people " + title);
		
		String[] invites = request.getParameterValues("invited");
		if(invites == null) {
			Map<Person, Boolean> checks = new HashMap<>();
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
			request.setAttribute("checks", sortedMap);
			getServletContext().getRequestDispatcher("/GoToAnagrafica").forward(request, response);
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
				String path = "/WEB-INF/Cancellazione.html";
				ServletContext servletContext = getServletContext();
				final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
				templateEngine.process(path, ctx, response.getWriter());
				return;
			}
			
			Map<Person, Boolean> checks = new HashMap<>();
			for (int i = 0; i < people.size(); i++) {
				for(int j = 0; j < invites.length ; j++) {
					if(i == Integer.parseInt(invites[j])) {
						checks.put(people.get(i), true);
						break;
					}
					else checks.put(people.get(i), false);
				}
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
			request.setAttribute("above", over);
			request.setAttribute("checks", sortedMap);
			getServletContext().getRequestDispatcher("/GoToAnagrafica").forward(request, response);
			return;
		}
		else {
			
			// posso creare la riunione
			
			List<Integer> invitedPeople = new ArrayList<>();
			for (int i = 0; i < people.size(); i++) {
				for(int j = 0; j < invites.length ; j++) {
					if(i == Integer.parseInt(invites[j])) {
						invitedPeople.add(people.get(i).getId());
					}
				}
			}
			
			MeetingDAO meetingDAO = new MeetingDAO(connection);
			
			try {
				session.removeAttribute(title);
				session.removeAttribute("people " + title);
				if(!meetingDAO.createMeeting(m.getIdHost(), title, m.getDataStart(), m.getHourStart(), m.getDuration(), m.getMax(), invitedPeople)) {
					throw new Exception();
				}
			} catch (Exception e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "It was not possible to create a meeting");
				return;
			}
			response.sendRedirect(getServletContext().getContextPath() + "/GoToHomePage");
		}
	}
}
