import java.sql.*;

public class DBConnector {
    private String url;
    private String user;
    private String password;
    private Connection connection;

    public DBConnector() {
        this.url = "jdbc:mysql://localhost:3306/testdb?useUnicode=true&characterEncoding=UTF-8";
        this.user = "root";
        this.password = "password";
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Connection could not be made: " + e);
        }
    }



    private ResultSet queryPreparedStatement(PreparedStatement statement){
        try{
            ResultSet resultSet = statement.executeQuery();

            return resultSet;
        } catch (SQLException e){
            System.err.println("Error in querying DB: " + e);
        }

        return null;
    }


    private boolean updateQueryPreparedStatement(PreparedStatement statement){
        try{
            statement.executeUpdate();
            return true;
        } catch (SQLException e){
            System.err.println("Error in update query: " + e);
            return false;
        }
    }


    ResultSet query(String sqlStatement, Object[] values) {
        try {
            PreparedStatement dbQuery = connection.prepareStatement(sqlStatement);
            
            for(int i = 0; i < values.length; i++){
                dbQuery.setObject(i + 1, values[i]);
            }
    
            return queryPreparedStatement(dbQuery);
        } catch (SQLException e){
            System.err.println("Problem with query: " + sqlStatement);
            System.err.println(e);
            return null;
        }
    }


    boolean updateQuery(String sqlStatement, Object[] values) {
        try {
            PreparedStatement dbQuery = connection.prepareStatement(sqlStatement);
            
            for(int i = 0; i < values.length; i++){
                dbQuery.setObject(i + 1, values[i]);
            }
    
            return updateQueryPreparedStatement(dbQuery);
        } catch (SQLException e){
            System.err.println("Problem with query: " + sqlStatement);
            System.err.println(e);
            return false;
        }
    }
}
