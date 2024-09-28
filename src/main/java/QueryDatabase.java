import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class QueryDatabase {

    private DBConnector dbConnector;

    public QueryDatabase() {
        this.dbConnector = new DBConnector();
    }
    

    boolean addAdmin(int user_id, int club_id){
        String queryString = "INSERT INTO admins (user_id, club_id) VALUES (?, ?);";
        Object[] inputValues = {user_id, club_id};
        return dbConnector.updateQuery(queryString, inputValues);
    }


    boolean addClubMember(int user_id, int club_id){
        if(user_id == 0){
            System.err.println("User does not exist");
            return false;
        }
        String queryString = "UPDATE users SET club_id = ? WHERE user_id = ?";
        Object[] inputValues = {club_id, user_id};
        return dbConnector.updateQuery(queryString, inputValues);
    }


    boolean addCrewCaptain(int user_id, int crew_id){
        String queryString = "INSERT INTO captains (user_id, crew_id) VALUES (?, ?);";
        Object[] inputValues = {user_id, crew_id};
        return dbConnector.updateQuery(queryString, inputValues);
    }


    boolean addCrewMember(int user_id, int crew_id){
        String queryString = "INSERT INTO active_memberships (user_id, crew_id) VALUES (?, ?)";
        Object[] inputValues = {user_id, crew_id};

        return dbConnector.updateQuery(queryString, inputValues);
    }


    boolean addRoleToMember(int user_id, int crew_id, int role_id){
        String queryString = "INSERT INTO active_roles (user_id, crew_id, role_id) VALUES (?, ?, ?)";
        Object[] inputValues = {user_id, crew_id, role_id};
        
        return dbConnector.updateQuery(queryString, inputValues);
    }


    boolean changeDetails(int user_id, String newEmail_address, String password, float twoK, String first_name, String last_name, int max_outings){
        String queryString = "UPDATE users SET ";
        ArrayList<Object> inputValuesList = new ArrayList<Object>();
   
        if (newEmail_address != null) {
            queryString = queryString + "email_address = ?, ";
            inputValuesList.add(newEmail_address);
        }
        if (password != null && password.length() > 0) {
            queryString = queryString + "password = ?, ";
            inputValuesList.add(password);
        }
        if (twoK != 0) {
            queryString = queryString + "twoK = ?, ";
            inputValuesList.add(twoK);
        }
        if (first_name != null) {
            queryString = queryString + "first_name = ?, ";
            inputValuesList.add(first_name);
        }
        if (last_name != null) {
            queryString = queryString + "last_name = ?, ";
            inputValuesList.add(last_name);
        }
        if (max_outings != 0) {
            queryString = queryString + "max_outings = ?, ";
            inputValuesList.add(max_outings);
        }
        queryString = queryString.substring(0, queryString.length() - 2) + " WHERE user_id = ?";
        inputValuesList.add(user_id);

        System.out.println("twoK: " + twoK);
        System.out.println(queryString);

        Object[] inputValues = inputValuesList.toArray();

        return dbConnector.updateQuery(queryString, inputValues);
    }


    boolean createCrew (int crew_id, int club_id, String crew_name){
        String queryString = "INSERT INTO clubs (crew_id, club_id, crew_name) VALUES (?, ?, ?)";
        Object[] inputValues = {crew_id, club_id, crew_name};
        return dbConnector.updateQuery(queryString, inputValues);
    }


    boolean createUser(String email_address, String password, String first_name, String last_name){
        if (emailTaken(email_address)){
            return false;
        }
    
        int newID = getMaxUserID() + 1;
        String queryString = "INSERT INTO users (user_id, club_id, first_name, last_name, password, email_address, twoK, max_outings) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        Object[] inputValues = {newID, null, first_name, last_name, password, email_address, null, null};
        return dbConnector.updateQuery(queryString, inputValues);
    }


    boolean deleteMemberAvailabilities(int user_id){
        String queryString = "DELETE FROM availabilities WHERE user_id = ?";
        Object[] inputValues = {user_id};
        boolean result = dbConnector.updateQuery(queryString, inputValues);
        return result;
    }


    boolean emailTaken(String email_address){
        try {
            String queryString = "SELECT * FROM users WHERE users.email_address = ?";
            Object[] inputValues = {email_address};
            ResultSet result = dbConnector.query(queryString, inputValues);
            return result.next();
        } catch (SQLException e) {
            return false;
        }
    }


    boolean existsClub(String club_name){
        try{
            String queryString = "SELECT * FROM clubs WHERE club_name = ?";
            Object[] inputValues = {club_name};
            ResultSet result = dbConnector.query(queryString, inputValues);
            return result.next();
        } catch (SQLException e) {
            return false;
        }
    }


    boolean createClub(String club_name){
        if(existsClub(club_name)){
            return false;
        }

        int newID = getMaxClubID() + 1;
        String queryString = "INSERT INTO clubs (club_id, club_name) VALUES (?, ?)";
        Object[] inputValues = {newID, club_name};
        boolean result = dbConnector.updateQuery(queryString, inputValues);
        return result;
    }


    ResultSet getAdmins(int club_id){
        String queryString = "SELECT users.first_name, users.last_name, users.user_id FROM admins\n" + //
                            "JOIN users ON users.user_id = admins.user_id\n" + //
                            "WHERE admins.club_id = ?;";
        Object[] inputValues = {club_id};
        ResultSet result = dbConnector.query(queryString, inputValues);

        return result;
}


    ResultSet getCaptainedCrews(int user_id){
        String queryString = "SELECT crews.crew_id, crew_name FROM crews \n" + //
                            "JOIN captains ON crews.crew_id = captains.crew_id \n" + //
                            "JOIN users ON captains.user_id = users.user_id \n" + //
                            "WHERE users.user_id = ?";
        Object[] inputValues = {user_id};
        ResultSet result = dbConnector.query(queryString, inputValues);

        return result;
    }


    ResultSet getClubMembers(int club_id){

        String queryString = "SELECT * FROM users WHERE club_id = ?";
        Object[] inputValues = {club_id};
        ResultSet result = dbConnector.query(queryString, inputValues);

        return result;
    }


    ResultSet getClubs(){
        String queryString = "SELECT club_id, club_name FROM clubs";
        Object[] inputValues = {};
        ResultSet result = dbConnector.query(queryString, inputValues);

        return result;
    }


    int getClubOfCrew(int crew_id) {
        try{
            String queryString = "SELECT club_id FROM crews WHERE crew_id = ?";
            Object[] inputValues = {crew_id};
            ResultSet result = dbConnector.query(queryString, inputValues);
            if (result.next()){
                return result.getInt("club_id");
            }
            return 0;
        } catch (SQLException e) {
            System.err.println("Error in getting user id: " + e);
            return 0;
        }

    }


    int getClubOfUser(int user_id) {
        try{
            String queryString = "SELECT club_id FROM users WHERE user_id = ?";
            Object[] inputValues = {user_id};
            ResultSet result = dbConnector.query(queryString, inputValues);

            if (result.next()){
                return result.getInt("club_id");
            }
            return 0;
        } catch (SQLException e) {
            return 0;
        }
    }


    ResultSet getCrewCaptains(int crew_id){
        String queryString = "SELECT * FROM crews\n" + //
                            "JOIN captains ON crews.crew_id = captains.crew_id\n" + //
                            "JOIN users ON captains.user_id = users.user_id\n" + //
                            "WHERE crews.crew_id = ?;";
        Object[] inputValues = {crew_id};
        ResultSet result = dbConnector.query(queryString, inputValues);

        return result;
    }


    ResultSet getCrewMembers(int crew_id){
        String queryString = "SELECT * FROM crews\n" + //
                            "JOIN active_memberships ON crews.crew_id = active_memberships.crew_id\n" + //
                            "JOIN users ON active_memberships.user_id = users.user_id\n" + //
                            "WHERE crews.crew_id = ?;";
        Object[] inputValues = {crew_id};
        ResultSet result = dbConnector.query(queryString, inputValues);
        
        return result;
    }


    ResultSet getCrewRoles(int crew_id){
        String queryString = "SELECT * FROM active_roles \n" + //
                            "WHERE crew_id = ?;";
        Object[] inputValues = {crew_id};
        ResultSet result = dbConnector.query(queryString, inputValues);

        return result;
    }
    

    ResultSet getCrews(int club_id){
        String queryString = "SELECT * FROM crews\n" + //
                            "WHERE club_id = ?;";
        Object[] inputValues = {club_id};
        ResultSet result = dbConnector.query(queryString, inputValues);
        return result;
    }


    String getEmailOfUser(int user_id){
        try{
            String queryString = "SELECT email_address FROM users WHERE user_id = ?";
            Object[] inputValues = {user_id};
            ResultSet result = dbConnector.query(queryString, inputValues);

            if(result.next()){
                return result.getString("email_address");
            }
            return null;
        } catch (SQLException e){
            System.err.println("Database problem in getEmailOfUser: " + e);
            return null;
        }
    }


    int getMaxClubID() {
        try {
            String queryString = "SELECT MAX(club_id) AS max FROM clubs";
            Object[] inputValues = {};
            ResultSet result = dbConnector.query(queryString, inputValues);
            if (result.next()){
                return result.getInt("max");
            }
            return -1;
        } catch (SQLException e) {
            return -1;
        }
    }


    int getMaxCrewID() {
        try{
            String queryString = "SELECT MAX(crew_id) AS max FROM crews";
            Object[] inputValues = {};
            ResultSet result = dbConnector.query(queryString, inputValues);
            if (result.next()){
                return result.getInt("max");
            }
            return -1;
        }catch (SQLException e) {
            return -1;
        }
    }


    int getMaxUserID() {
        try {
            String queryString = "SELECT MAX(user_id) AS max FROM users";
            Object[] inputValues = {};
            ResultSet result = dbConnector.query(queryString, inputValues);
            if (result.next()){
                return result.getInt("max");
            }
            return -1;
        } catch (SQLException e) {
            return -1;
        }
    }


    ResultSet getMemberAvailabilities(int user_id) {
        String queryString = "SELECT * FROM availabilities WHERE user_id = ?;";
        Object[] inputValues = {user_id};
        ResultSet result = dbConnector.query(queryString, inputValues);
        
        return result;
    }


    ResultSet getRoleMap(){
        String queryString = "SELECT * FROM roles";
        Object[] inputValues = {};
        ResultSet result = dbConnector.query(queryString, inputValues);

        return result;
    }


    int getUserCommitment(int user_id) {
        try{
            String queryString = "SELECT max_outings FROM users WHERE email_address = ?";
            Object[] inputValues = {};
            ResultSet result = dbConnector.query(queryString, inputValues);
            if (result.next()){
                return result.getInt("max_outings");
            }
            return -1;
        } catch (SQLException e){
            return -1;
        }
    }


    ResultSet getUserData(int user_id) {
        String queryString = "SELECT * FROM users WHERE user_id = ?";
        Object[] inputValues = {user_id};
        ResultSet result = dbConnector.query(queryString, inputValues);
        return result;
    }


    int getUserIDByEmail(String email_address) {
        try{
            String queryString = "SELECT user_id FROM users WHERE email_address = ?";
            Object[] inputValues = {email_address};
            ResultSet result = dbConnector.query(queryString, inputValues);
            if (result.next()){
                return result.getInt("user_id");
            }
            return -1;
        } catch (SQLException e){
            return -1;
        }
    }


    boolean isAdmin(int user_id, int club_id){
        try{
            String queryString = "SELECT * FROM admins WHERE user_id = ? AND club_id = ?;";
            Object[] inputValues = {user_id, club_id};
            ResultSet result = dbConnector.query(queryString, inputValues);
            
            boolean isAdmin = result.next();
            return isAdmin;

        } catch (SQLException e) {
            System.err.println("API error in isAdmin: " + e);
            return false;
        }
    }
    

    boolean isCaptain(int user_id, int crew_id){
        try{
            String queryString = "SELECT * FROM captains WHERE user_id = ? AND crew_id = ?;";
            Object[] inputValues = {user_id, crew_id};
            ResultSet result = dbConnector.query(queryString, inputValues);
            
            boolean isCaptain = result.next();
            return isCaptain;

        } catch (SQLException e) {
            System.err.println("API error in isAdmin: " + e);
            return false;
        }
    }


    String queryPassword(int user_id) {
        try{
            String queryString = "SELECT password FROM users WHERE users.user_id = ?";
            Object[] inputValues = {user_id};
            ResultSet result = dbConnector.query(queryString, inputValues);
            if (result.next()){
                return result.getString("password");
            }
            return null;
        } catch (SQLException e) {
            return null;
        }
    }

   
    boolean removeAdmin(int user_id, int club_id){
        String queryString = "DELETE FROM admins WHERE club_id = ? AND user_id = ?;";
        Object[] inputValues = {club_id, user_id};
        return dbConnector.updateQuery(queryString, inputValues);   
    }

   
    boolean removeCrewCaptain(int user_id, int crew_id){
        String queryString = "DELETE FROM captains WHERE crew_id = ? AND user_id = ?;";
        Object[] inputValues = {crew_id, user_id};
        return dbConnector.updateQuery(queryString, inputValues);   
    }


    boolean removeCrewMember(int user_id, int crew_id){
        if(!removeMemberFromRoles(user_id, crew_id)){
            return false;
        }

        String queryString = "DELETE FROM active_memberships WHERE crew_id = ? AND user_id = ?;";
        Object[] inputValues = {crew_id, user_id};
        return dbConnector.updateQuery(queryString, inputValues);
    }


    boolean removeMemberFromRoles(int user_id, int crew_id){
        String queryString = "DELETE FROM active_roles WHERE crew_id = ? AND user_id = ?;";
        Object[] inputValues = {crew_id, user_id};
        return dbConnector.updateQuery(queryString, inputValues);   
    }
    

    boolean removeRoleFromMember(int user_id, int crew_id, int role_id){
        String queryString = "DELETE FROM active_roles WHERE user_id = ? AND crew_id = ? AND role_id = ?;";
        Object[] inputValues = {user_id, crew_id, role_id};
        
        return dbConnector.updateQuery(queryString, inputValues);
    }


    boolean setMemberAvailability(int user_id, String start_time, String finish_time){
        String queryString = "INSERT INTO availabilities (user_id, start_time, finish_time) VALUES (?, ?, ?);";
        Object[] inputValues = {user_id, start_time, finish_time};
        
        return dbConnector.updateQuery(queryString, inputValues);
    }
}