package asbedi_CSCI201_Assignment4;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import java.io.IOException;

@WebServlet("/api/profile")
public class CompanyProfileServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        String ticker = req.getParameter("ticker");
        System.out.println("Fetching profile for ticker: " + ticker);
        if (ticker != null && !ticker.isEmpty()) {
            try {
                Profile profile = CompanyStocks.getProfile(ticker);
                Gson gson = new Gson();
                String jsonResponse = gson.toJson(profile);
                resp.getWriter().write(jsonResponse);
            } catch (Exception e) {
                e.printStackTrace();
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("{\"error\": \"Error fetching company profile information.\"}");
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"No ticker provided.\"}");
        }
    }
}
