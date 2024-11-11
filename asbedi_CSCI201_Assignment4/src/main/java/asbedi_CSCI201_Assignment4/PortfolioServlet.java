package asbedi_CSCI201_Assignment4;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/PortfolioServlet")
public class PortfolioServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        String action = request.getParameter("action");
        if (action == null) {
            out.println("{\"error\":\"No action specified\"}");
            return;
        }

        switch (action) {
            case "fetch":
                fetchPortfolio(request, response);
                break;
            case "buy":
                buyStock(request, response);
                break;
            case "sell":
                sellStock(request, response);
                break;
            default:
                out.println("{\"error\":\"Unknown action\"}");
                break;
        }
    }


    private void fetchPortfolio(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession();
        String username = session.getAttribute("username").toString();

        try (Connection connection = DBConnector.connect();
             PreparedStatement statement = connection.prepareStatement(
                 "SELECT * FROM portfolio WHERE userID = ?")) {
            statement.setInt(1, DBConnector.getUserIdByUsername(username));
            ResultSet resultSet = statement.executeQuery();

            List<Trade> trades = new ArrayList<>();
            while (resultSet.next()) {
                Trade trade = new Trade();
                trade.tradeID = resultSet.getInt("tradeID");
                trade.userID = resultSet.getInt("userID");
                trade.ticker = resultSet.getString("ticker");
                trade.num = resultSet.getInt("num");
                trade.price = resultSet.getDouble("price");
                trades.add(trade); 
            }
            resultSet.close();

            String json = convertTradesToJson(trades);
            out.println(json);
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("{\"error\":\"Error retrieving portfolio data.\"}");
        }
    }

    private void buyStock(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        String username = session.getAttribute("username").toString();
        String ticker = request.getParameter("ticker");
        int quantity = Integer.parseInt(request.getParameter("quantity"));
        double price = CompanyStocks.getStock(ticker).getC();  // Assuming this fetches the current price correctly

        Connection connection = null;
        PreparedStatement checkBalance = null;
        PreparedStatement updatePortfolio = null;
        PreparedStatement updateBalance = null;
        ResultSet rs = null;

        try {
            connection = DBConnector.connect();
            connection.setAutoCommit(false);  // Start transaction

            checkBalance = connection.prepareStatement("SELECT balance FROM users WHERE username = ?");
            checkBalance.setString(1, username);
            rs = checkBalance.executeQuery();

            if (rs.next() && rs.getDouble("balance") >= quantity * price) {
                updatePortfolio = connection.prepareStatement("UPDATE portfolio SET num = num + ? WHERE ticker = ? AND userID = ? ORDER BY tradeID DESC LIMIT 1");
                updatePortfolio.setInt(1, quantity);
                updatePortfolio.setString(2, ticker);
                updatePortfolio.setInt(3, DBConnector.getUserIdByUsername(username));
                updatePortfolio.executeUpdate();

                updateBalance = connection.prepareStatement("UPDATE users SET balance = balance - ? WHERE userID = ?");
                updateBalance.setDouble(1, quantity * price);
                updateBalance.setInt(2, DBConnector.getUserIdByUsername(username));
                updateBalance.executeUpdate();

                connection.commit();  // Commit transaction
                response.getWriter().println("{\"success\":\"Bought " + quantity + " " + ticker + " stocks.\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().println("{\"error\":\"Insufficient funds.\"}");
            }
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();  // Rollback transaction on error
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("{\"error\":\"Error processing buy transaction.\"}");
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (checkBalance != null) try { checkBalance.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (updatePortfolio != null) try { updatePortfolio.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (updateBalance != null) try { updateBalance.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (connection != null) try { connection.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }


    private void sellStock(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession();
        String username = session.getAttribute("username").toString();
        int quantity = Integer.parseInt(request.getParameter("quantity"));
        String ticker = request.getParameter("ticker");
        double price = CompanyStocks.getStock(ticker).getC();

        try (Connection connection = DBConnector.connect();
             PreparedStatement checkStocks = connection.prepareStatement(
                 "SELECT num FROM portfolio WHERE ticker = ? AND userID = ?");
             PreparedStatement updatePortfolio = connection.prepareStatement(
                 "UPDATE portfolio SET num = num - ? WHERE ticker = ? AND userID = ? ORDER BY num DESC LIMIT 1");
        	 PreparedStatement updateBalance = connection.prepareStatement(
        	     "UPDATE users SET balance = balance + ? WHERE userID = ?");) {
            
            checkStocks.setString(1, ticker);
            checkStocks.setInt(2, DBConnector.getUserIdByUsername(username));
            ResultSet rs = checkStocks.executeQuery();
            if (rs.next() && rs.getInt("num") >= quantity) {
                updatePortfolio.setInt(1, quantity);
                updatePortfolio.setString(2, ticker);
                updatePortfolio.setInt(3, DBConnector.getUserIdByUsername(username));
                updatePortfolio.executeUpdate();
                updateBalance.setDouble(1, price*quantity);
                updateBalance.setInt(2, DBConnector.getUserIdByUsername(username));
                updateBalance.execute(); 
                out.println("{\"success\":\"Sold " + quantity + " " + ticker + " stocks.\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("{\"error\":\"Insufficient stock quantity.\"}");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("{\"error\":\"Error processing sell transaction.\"}");
        }
    }

    // Utility method to convert trades list to JSON
    private String convertTradesToJson(List<Trade> trades) {
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("[");
        for (int i = 0; i < trades.size(); i++) {
            Trade trade = trades.get(i);
            jsonBuilder.append("{");
            jsonBuilder.append("\"tradeID\":").append(trade.tradeID).append(",");
            jsonBuilder.append("\"userID\":").append(trade.userID).append(",");
            jsonBuilder.append("\"ticker\":\"").append(trade.ticker).append("\",");
            jsonBuilder.append("\"num\":").append(trade.num).append(",");
            jsonBuilder.append("\"price\":").append(trade.price);
            jsonBuilder.append("}");
            if (i < trades.size() - 1) {
                jsonBuilder.append(",");
            }
        }
        jsonBuilder.append("]");
        return jsonBuilder.toString();
    }
}

