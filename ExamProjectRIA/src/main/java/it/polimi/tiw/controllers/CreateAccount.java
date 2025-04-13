package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;

import it.polimi.tiw.dao.PersonDAO;
import it.polimi.tiw.utils.ConnectionHandler;

/**
 * Servlet implementation class CreateAccount
 */
@WebServlet("/CreateAccount")
@MultipartConfig
public class CreateAccount extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
   
    public CreateAccount() {
        super();
    }

    public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
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
			name = StringEscapeUtils.escapeJava(request.getParameter("nameReg"));
			surname = StringEscapeUtils.escapeJava(request.getParameter("surnameReg"));
			username = StringEscapeUtils.escapeJava(request.getParameter("userReg"));
			email = StringEscapeUtils.escapeJava(request.getParameter("emailReg"));
			password1 = StringEscapeUtils.escapeJava(request.getParameter("pwd1Reg"));
			password2 = StringEscapeUtils.escapeJava(request.getParameter("pwd2Reg"));
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
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println(e.getMessage());
			return;
		}
		
		try {
			if(pDAO.createPerson(name, surname, username, email, password1)) {
				response.setStatus(HttpServletResponse.SC_OK);
				response.getWriter().println("ok");
			}
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("It was not possible to create the account");
			return;
		}
		
	}

}
