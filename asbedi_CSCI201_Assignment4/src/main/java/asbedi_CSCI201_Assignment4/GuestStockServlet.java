package asbedi_CSCI201_Assignment4;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import java.io.IOException;

@WebServlet("/api/stock")
public class GuestStockServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
	    resp.setContentType("application/json");
	    String ticker = req.getParameter("ticker");
	    System.out.println("Ticker: " + ticker);
	    if (ticker != null && !ticker.isEmpty()) {
	        try {
	            Stock stock = CompanyStocks.getStock(ticker);
	            Gson gson = new Gson();
	            String jsonResponse = gson.toJson(stock);
	            resp.getWriter().write(jsonResponse);
	        } catch (Exception e) {
	            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	            resp.getWriter().write("{\"error\": \"Error fetching stock information.\"}");
	        }
	    } else {
	        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	        resp.getWriter().write("{\"error\": \"No ticker provided.\"}");
	    }
	}


}
