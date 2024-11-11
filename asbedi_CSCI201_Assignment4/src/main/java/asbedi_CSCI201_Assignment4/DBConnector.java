package asbedi_CSCI201_Assignment4;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBConnector {
    // You can adjust these constants according to your project needs
    private static final String URL = "jdbc:mysql://localhost:3306/assignment4";
    private static final String USER = "root";
    private static final String PASSWORD = "Bedi1007";

    public static Connection connect() throws SQLException {
        try {
            // Ensuring the driver class is loaded
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Creating a connection using the DriverManager
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            // This exception should be thrown if the driver is not found
            System.err.println("MySQL JDBC Driver not found.");
            e.printStackTrace();
            throw new SQLException("MySQL JDBC Driver not found.", e);
        }
    }
    
    public static Connection connectPortfolio() throws SQLException {
        try {
            // Ensuring the driver class is loaded
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Creating a connection using the DriverManager
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            // This exception should be thrown if the driver is not found
            System.err.println("MySQL JDBC Driver not found.");
            e.printStackTrace();
            throw new SQLException("MySQL JDBC Driver not found.", e);
        }
    }

    
    public static User getUserData(String username) throws SQLException {
        // Initialize variables
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        User user = new User(); 

        try {
            // Establish database connection
            connection = DBConnector.connect();
            
            // Prepare SQL statement to retrieve balance for the given username
            String sql = "SELECT * FROM Users WHERE username = ?";
            statement = connection.prepareStatement(sql);
            statement.setString(1, username);
            
            // Execute the query
            resultSet = statement.executeQuery();
            
            // Retrieve balance from the result set
            if (resultSet.next()) {
                user.username = resultSet.getString("username");
                user.password  = resultSet.getString("pass");
                user.email = resultSet.getString("email");
                user.balance = resultSet.getDouble("balance");
            } else {
                // User not found, handle appropriately
                throw new SQLException("User not found");
            }
        } finally {
            // Close resources in the finally block to ensure they are closed even if an exception occurs
            if (resultSet != null) resultSet.close();
            if (statement != null) statement.close();
            if (connection != null) connection.close();
        }
        return user;
    }
    
    public static void deductFromUserBalance(String username, double totalCost) throws SQLException {
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = DBConnector.connect();
            String sql = "UPDATE users SET balance = balance - ? WHERE username = ?";
            statement = connection.prepareStatement(sql);
            statement.setDouble(1, totalCost);
            statement.setString(2, username);
            statement.executeUpdate();
        } finally {
            if (statement != null) statement.close();
            if (connection != null) connection.close();
        }
    }
    
    public static int getUserIdByUsername(String username) throws SQLException {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        int userId = -1; // Default value if user not found

        try {
            connection = DBConnector.connect();
            String sql = "SELECT userID FROM Users WHERE username = ?";
            statement = connection.prepareStatement(sql);
            statement.setString(1, username);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                userId = resultSet.getInt("userID");
            }
        } finally {
            if (resultSet != null) resultSet.close();
            if (statement != null) statement.close();
            if (connection != null) connection.close();
        }

        return userId;
    }

    
    public static Trade getUserPortfolio(int user_id) throws SQLException {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        Trade trade = null; 

        try {
            connection = DBConnector.connect();
            String sql = "SELECT * FROM portfolio WHERE userID = ?";
            statement = connection.prepareStatement(sql);
            statement.setInt(1, user_id);
            resultSet = statement.executeQuery();
            
            while (resultSet.next()) {
                int tradeID = resultSet.getInt("tradeID");
                int userID = resultSet.getInt("userID");
                String ticker = resultSet.getString("ticker");
                int num = resultSet.getInt("num");
                double price = resultSet.getDouble("price");
                trade = new Trade(tradeID, userID, ticker, num, price);
            }
        } finally {
            if (resultSet != null) resultSet.close();
            if (statement != null) statement.close();
            if (connection != null) connection.close();
        }
        return trade;
    }
    
    public static void addTradeToPortfolio(String username, String ticker, int num, double price) throws SQLException {
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = DBConnector.connect();
            String sql = "INSERT INTO portfolio (userID, ticker, num, price) VALUES (?, ?, ?, ?)";
            statement = connection.prepareStatement(sql);
            
            // Get the user_id associated with the username
            int userId = getUserIdByUsername(username);
            
            statement.setInt(1, userId);
            statement.setString(2, ticker);
            statement.setInt(3, num);
            statement.setDouble(4, price);
            
            statement.executeUpdate();
        } finally {
            if (statement != null) statement.close();
            if (connection != null) connection.close();
        }
    }

}
