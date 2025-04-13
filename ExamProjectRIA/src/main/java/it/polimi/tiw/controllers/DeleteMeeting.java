package it.polimi.tiw.controllers;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;


@WebServlet("/DeleteMeeting")
@MultipartConfig
public class DeleteMeeting extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public DeleteMeeting() {
        super();
    }


	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String loginpath = getServletContext().getContextPath() + "/index.html";
		HttpSession session = request.getSession();
		if (session.isNew() || session.getAttribute("user") == null) {
			response.sendRedirect(loginpath);
			return;
		}
		
		String title = StringEscapeUtils.escapeJava(request.getParameter("title"));
		
		if( title == null) {
			// ? ? ? ? ? ? ? ?
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Problems with deleting the meeting");
			System.out.println("Problems with deleting the meeting");
			
		} else {
			session.removeAttribute(title);
			session.removeAttribute("people " + title);
			response.setStatus(HttpServletResponse.SC_OK);
			response.setCharacterEncoding("UTF-8");
			response.getWriter().println("ok");
		}
	}

}
