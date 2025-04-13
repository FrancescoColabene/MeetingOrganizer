package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.utils.MeetingDateComparator;
import it.polimi.tiw.dao.MeetingDAO;
import it.polimi.tiw.beans.*;

/**
 * Servlet implementation class GoToHomePage
 */
@WebServlet("/GoToHomePage")
public class GoToHomePage extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GoToHomePage() {
        super();
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
    
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String loginpath = getServletContext().getContextPath() + "/index.html";
		HttpSession session = request.getSession();
		if (session.isNew() || session.getAttribute("user") == null) {
			response.sendRedirect(loginpath);
			return;
		}
		Person person = (Person) request.getSession().getAttribute("user");
		MeetingDAO meetingDAO = new MeetingDAO(connection);
		List<Meeting> hostedMeetings = new ArrayList<>();
		List<Meeting> invitedMeetings = new ArrayList<>();
		
		try {
			hostedMeetings = meetingDAO.findMeetingsByCreator(person.getId());
			invitedMeetings = meetingDAO.findMeetingsByInvites(person.getId());
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to recover meetings");
			return;
		}
		MeetingDateComparator mdc = new MeetingDateComparator();
		hostedMeetings.sort(mdc);
		invitedMeetings.sort(mdc);

		String path = "/WEB-INF/HomePage.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("name", person.getName());
		ctx.setVariable("hostedMeetings", hostedMeetings);
		ctx.setVariable("invitedMeetings", invitedMeetings);
		templateEngine.process(path, ctx, response.getWriter());
	}

}
