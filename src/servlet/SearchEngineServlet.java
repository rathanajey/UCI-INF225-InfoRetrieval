package servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class SearchEngineServlet
 */
@WebServlet("/SearchEngineServlet")
public class SearchEngineServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String query;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SearchEngineServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		response.setContentType("text/html");
		int resultsPerPage = 20;
		Ranker ranked;

		if (!(request.getParameter("q") == null)){
			query = request.getParameter("q");
			ranked = new Ranker(query, resultsPerPage);
		}
		else {
				ranked = new Ranker(query, resultsPerPage);
		}
		
	    int page = 1;
	    if(request.getParameter("page") != null)
            page = Integer.parseInt(request.getParameter("page"));
	    List<String> listData = null;
	    
	    try {  listData = ranked.search(page); // however you get the data*/
	    } catch (SQLException e){
	    	e.printStackTrace();
	    }
	    
	    int numOfPages = (int) Math.ceil(ranked.numOfDocs * 1.0 / resultsPerPage);
	    int listStartNumber = (page-1)*resultsPerPage + 1;
	    // set the attributes in the request to access it on the JSP
	    request.setAttribute("query", query);
	    request.setAttribute("listData", listData);
	    request.setAttribute("numOfPages", numOfPages);
	    request.setAttribute("listStart", listStartNumber);
	    request.setAttribute("currentPage", page);
	    RequestDispatcher rd = getServletContext().getRequestDispatcher("/results.jsp");
	    rd.forward(request, response);

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
	}

}
