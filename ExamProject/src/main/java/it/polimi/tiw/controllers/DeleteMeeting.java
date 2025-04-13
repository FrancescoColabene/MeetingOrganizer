package it.polimi.tiw.controllers;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


@WebServlet("/DeleteMeeting")
public class DeleteMeeting extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public DeleteMeeting() {
        super();
    }


	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String loginpath = getServletContext().getContextPath() + "/index.html";
		HttpSession session = request.getSession();
		if (session.isNew() || session.getAttribute("user") == null) {
			response.sendRedirect(loginpath);
			return;
		}
		
		String title = request.getParameter("title");
		
		if( title == null) {
			// ? ? ? ? ? ? ? ?
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Don't touch the query string! And please Undo from browser");
		} else {
			session.removeAttribute(title);
			session.removeAttribute("people " + title);
			response.sendRedirect(getServletContext().getContextPath() + "/GoToHomePage");
		}
	}

}
