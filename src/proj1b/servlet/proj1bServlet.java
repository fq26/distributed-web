package proj1b.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import proj1b.ssm.*;

/**
 * Servlet implementation class proj1bServlet
 */
@WebServlet("/proj1bServlet")
public class proj1bServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static SessionManager ssm = SessionManager.getInstance();
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public proj1bServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
//		response.getWriter().append("Served at: ").append(request.getContextPath());
		
		
		
		// TODO set cookie domain, see instruction P7
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
