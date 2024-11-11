package asbedi_CSCI201_Assignment4;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;

/**
 * Servlet implementation class LoggedStockServlet
 */
@WebServlet("/LoggedStockServlet")
public class LoggedStockServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Stock stockData = null;
    	String action = request.getParameter("action");
        if ("fetch".equals(action)) {
            String ticker = request.getParameter("ticker");
            if (ticker != null && !ticker.isEmpty()) {
                // Assuming getStockData returns an object of Stock data
                stockData = CompanyStocks.getStock(ticker);
                sendJsonResponse(response, stockData);
                HttpSession session = request.getSession();
                session.setAttribute("ticker", ticker);
                session.setAttribute("price", stockData.getC());
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"Ticker is required\"}");
            }
        } else if ("buy".equals(action)) {
            try {
				handleBuyRequest(request, response);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }

    private void sendJsonResponse(HttpServletResponse response, Stock stockData) throws IOException {
        Gson gson = new Gson();
        String jsonResponse = gson.toJson(stockData);
        response.setContentType("application/json");
        response.getWriter().write(jsonResponse);
    }

    private void handleBuyRequest(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
    	HttpSession session = request.getSession(false); // 'false' means don't create a new session if it doesn't exist
    	String username; 
    	String ticker; 
    	double price; 
    	if (session != null) {
    	    username = (String) session.getAttribute("username");
    	    ticker = (String) session.getAttribute("ticker");
    	    price = (float) session.getAttribute("price"); 
    	    if (username != null) {
    	        // Use the username as needed
    	    } else {
    	        // Username not found in session
    	    	System.out.println("Username not found in session");
    	    	return; 
    	    }
    	} else {
    	    // Session doesn't exist
    		System.out.println("Session doesn't exist");
    		return; 
    	}
    	
    	User user = DBConnector.getUserData(username); 
    	double balance = user.balance;    	
    	
        int quantity;
        try {
            quantity = Integer.parseInt(request.getParameter("quantity"));
        } catch (NumberFormatException e) {
            response.getWriter().write("{\"error\": \"Invalid quantity\"}");
            return;
        }
        if (quantity < 1) {
            response.getWriter().write("{\"error\": \"FAILED: Purchase not possible\"}");
            return;
        }
        // Assume you check if the user has enough cash here
        if (price*quantity > balance) {
        	System.out.println("Price = " + price + " Qauntity = " + quantity + " Balance = " + balance);
            response.getWriter().write("{\"error\": \"FAILED: Purchase not possible\"}");
        } else {
            // Assume buying stock is successful
            response.getWriter().write("{\"success\": \"Executed purchase\"}");
            DBConnector.deductFromUserBalance(username, quantity*price);
            DBConnector.addTradeToPortfolio(username, ticker, quantity, price);
           
        }
    }
}

