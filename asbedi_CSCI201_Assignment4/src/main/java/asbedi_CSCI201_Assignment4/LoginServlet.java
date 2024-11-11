package asbedi_CSCI201_Assignment4;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public LoginServlet() {
        super();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        // Retrieve the parameters from the request
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // Here you would typically check these credentials against your users store
        boolean isAuthenticated = false; 
        try {
			isAuthenticated = authenticateUser(username, password);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        // Create JSON response with the parameters
        String jsonData = String.format("{\"username\": \"%s\", \"authenticated\": %b}", 
                                        escapeJson(username), 
                                        isAuthenticated);
        
        HttpSession session = request.getSession();
        session.setAttribute("username", username); // assuming 'username' is the variable holding the username

        out.print(jsonData);
        out.flush();
        out.close();
    }

    // Method to authenticate user (simplified version)
    private boolean authenticateUser(String username, String password) throws SQLException {
        boolean rs = Register.login(username, password); 
        if (rs == false) return false; 
        else return true; 
    }

    // Simple method to escape JSON special characters in user input
    private String escapeJson(String data) {
        if (data == null) {
            return "";
        }
        return data.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\b", "\\b")
                   .replace("\f", "\\f")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}
