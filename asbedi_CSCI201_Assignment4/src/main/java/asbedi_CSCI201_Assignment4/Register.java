package asbedi_CSCI201_Assignment4; 

import java.util.*; 
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Register {
	public static void main(String [] args) {
		int rs = register("danwills", "weed420", "daniel@usc.edu"); 
		try {
			login("arjunsbedi", "123");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	public static int register(String username, String password, String email) {
		System.out.println("Registering " + username); 

		Connection conn = null;
		Statement st = null;
		ResultSet rs = null;
		int userID = -1;
		
		try {
			conn = DBConnector.connect();
			st = conn.createStatement();
			rs = st.executeQuery("SELECT * FROM users WHERE username='" + username + "'");
			if (!rs.next()) {	// no user with that username
				st = conn.createStatement();
				rs = st.executeQuery("SELECT * FROM users WHERE email='" + email + "'");
				if (!rs.next()) {
					rs.close();
					st.execute("INSERT INTO users (username, pass, email, balance) VALUES ('" + username + "','" + password + "', '" + email + "',50000)");
					rs = st.executeQuery("SELECT LAST_INSERT_ID()");
					rs.next(); 
					userID = rs.getInt(1); 
				}
				else {
					userID = -2;
				}
			}
			else {
				//System.out.println(rs.getInt(1)); 
			}
		} catch(SQLException sqle) {
			System.out.println("SQLException in registerUser. "); 
			sqle.printStackTrace();
		} finally {
			try {
				if (rs != null) {
					rs.close();	
				}
				if (st != null) {
					st.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException sqle) {
				System.out.println("sqle:" + sqle.getMessage());
			}
		}
		return userID; 
	}
	
	public static boolean login(String username, String password) throws SQLException {
		Connection conn = DBConnector.connect(); 
		Statement st = conn.createStatement();
		ResultSet rs = st.executeQuery("SELECT * FROM users WHERE username='" + username + "'");
		if (!rs.next()) {	// no user with that username
			System.out.println("Username not found.");
			return false; 
		}
		if (rs.getString("pass").equals(password) == false) {
			System.out.println("Incorrect password.");
			System.out.println("Expected: " + rs.getString("pass"));
			System.out.println("Received: " + password);
			return false; 
		}
		return true; 
	}
} 