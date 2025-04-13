package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.dao.PersonDAO;
import it.polimi.tiw.utils.ConnectionHandler;

/**
 * Servlet implementation class CreateAccount
 */
@WebServlet("/CreateAccount")
public class CreateAccount extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
   
    public CreateAccount() {
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
    

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String name = null;
		String surname = null;
		String username = null;
		String email = null;
		String password1 = null;
		String password2 = null;
		
		PersonDAO pDAO = new PersonDAO(connection);

		try {
			name = StringEscapeUtils.escapeJava(request.getParameter("name"));
			surname = StringEscapeUtils.escapeJava(request.getParameter("surname"));
			username = StringEscapeUtils.escapeJava(request.getParameter("username1"));
			email = StringEscapeUtils.escapeJava(request.getParameter("email1"));
			password1 = StringEscapeUtils.escapeJava(request.getParameter("password1"));
			password2 = StringEscapeUtils.escapeJava(request.getParameter("password2"));
			if (name == null || surname == null || email == null || password1 == null || password2 == null || 
				name.isEmpty() || surname.isEmpty() || email.isEmpty() || password1.isEmpty() || password2.isEmpty() ) {
				throw new Exception("Missing or empty credential value");
			}
			Pattern temp = Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");
			if(!temp.matcher(email).matches()) {
				throw new Exception("Insert a valid email");
			}
			if(!password1.equals(password2)) {
				throw new Exception("Passwords do not match");
			}
			if(!pDAO.checkUsername(username)) {
				throw new Exception("This username is already taken");
			}
			if(!pDAO.checkEmail(email)) {
				throw new Exception("This email is already used");
			}
		} catch (Exception e) {
			// for debugging only e.printStackTrace();
			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
			ctx.setVariable("message", e.getMessage());
			String path = "/index.html";
			templateEngine.process(path, ctx, response.getWriter());
			return;
		}
		
		try {
			if(pDAO.createPerson(name, surname, username, email, password1)) {
				ServletContext servletContext = getServletContext();
				final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
				ctx.setVariable("message", "The creation of the account was successful");
				String path = "/index.html";
				templateEngine.process(path, ctx, response.getWriter());
			}
			else {
				ServletContext servletContext = getServletContext();
				final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
				ctx.setVariable("message", "The creation of the account was not successful");
				String path = "/index.html";
				templateEngine.process(path, ctx, response.getWriter());
			}
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to create the account");
			return;
		}
		
	}

}
