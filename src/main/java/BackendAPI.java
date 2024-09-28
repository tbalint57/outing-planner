import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


public class BackendAPI {
    private QueryDatabase database;
    // TODO: Add salt to passwords

    public BackendAPI(){
        database = new QueryDatabase();
    }
    

    @SuppressWarnings("unchecked")
    public JSONObject addAdmin(int userId, int adminId){
        JSONObject json = new JSONObject();
        int clubId = database.getClubOfUser(userId);
        boolean hasRight = database.isAdmin(userId, clubId);

        if(!hasRight){
            json.put("success", false);
            json.put("errorMessage", "You do not have rights for this action!");
            return json;
        }
        
        json.put("success", database.addAdmin(adminId, clubId));

        return json;
    }


    @SuppressWarnings("unchecked")
    public JSONObject addClubMember(int userId, int clubId){
        JSONObject json = new JSONObject();
        boolean success = database.addClubMember(userId, clubId);

        if (success){
            json.put("success", true);
            json.put("redirect", "time.html");
        } else {
            json.put("errorMessage", "Could not join the club, please try again later!");
        }
        return json;
    }


    @SuppressWarnings("unchecked")
    public JSONObject addCrewCaptain(int userId, int captainId, int crewId){
        JSONObject json = new JSONObject();
        int clubId = database.getClubOfUser(userId);
        boolean hasRight = database.isAdmin(userId, clubId) || database.isCaptain(userId, crewId);

        if(!hasRight){
            json.put("success", false);
            json.put("errorMessage", "You do not have rights for this action!");
            return json;
        }
        
        json.put("success", database.addCrewCaptain(captainId, crewId));

        return json;
    }


    @SuppressWarnings("unchecked")
    public JSONObject addCrewMember(int userId, int memberId, int crewId){
        JSONObject json = new JSONObject();
        int clubId = database.getClubOfUser(userId);
        boolean hasRight = database.isAdmin(userId, clubId) || database.isCaptain(userId, crewId);

        if(!hasRight){
            json.put("success", false);
            json.put("errorMessage", "You do not have rights for this action!");
            return json;
        }
        
        json.put("success", database.addCrewMember(memberId, crewId));
        
        return json;
    }


    @SuppressWarnings("unchecked")
    public JSONObject addRoleToMember(int userId, int memberId, int crewId, int roleId){
        JSONObject json = new JSONObject();
        boolean hasRight = database.isCaptain(userId, crewId);

        if(!hasRight){
            json.put("success", false);
            json.put("errorMessage", "You do not have rights for this action!");
            return json;
        }

        json.put("success", database.addRoleToMember(memberId, crewId, roleId));
        
        return json;
    }


    @SuppressWarnings("unchecked")
    public JSONObject changeUserData(int userId, String newEmail, String newPassword, float twoK, String fName, String lName, int commitment, String password){
        JSONObject json = new JSONObject();
        boolean emailTaken = database.emailTaken(newEmail);
        String oldEmail = database.getEmailOfUser(userId);

        if(!login(userId, password)){
            json.put("success", false);
            json.put("errorMessage", "Wrong password");
            return json;
        }

        if(newPassword != null && newPassword.length() > 0){
            newPassword = hashString(newPassword);
        }

        if(emailTaken && !oldEmail.equals(newEmail)){
            json.put("success", false);
            json.put("errorMessage", "This email address is already taken, please pick another one");
            return json;
        }

        boolean success = database.changeDetails(userId, newEmail, newPassword, twoK, fName, lName, commitment);
        json.put("success", success);
        if (!success){
            json.put("errorMessage", "Something went wong, please try again later!");
        }
        return json;
    }

    
    @SuppressWarnings("unchecked")
    public JSONObject checkLogin(int userId, String password){
        JSONObject json = new JSONObject();
        String actualPassword = database.queryPassword(userId);

        json.put("success", password.equals(actualPassword));
        return json;
    }


    boolean createUser(String email, String password, String fName, String lName){
        return database.createUser(email, password, fName, lName);
    }

    
    @SuppressWarnings("unchecked")
    public JSONObject getClubs(){
        JSONObject json = new JSONObject();
        ResultSet resultSet = database.getClubs();
        
        String[] resultSetFields = {"club_id", "club_name"};
        String[] jsonNames = {"id", "name"};
        JSONArray clubs = convertResultSetToJSONArray(resultSet, resultSetFields, jsonNames);

        json.put("clubs", clubs);
        return json;
    }


    @SuppressWarnings("unchecked")
    public JSONObject getClubData(int userId){
        JSONObject json = new JSONObject();
        int clubId = database.getClubOfUser(userId);
        boolean hasRight = database.isAdmin(userId, clubId);

        if(!hasRight){
            json.put("errorMessage", "You do not have rights for this action!");
            return json;
        }

        // crews

        JSONArray crews = new JSONArray();
        ResultSet crewsResultSet = database.getCrews(clubId);
        try{
            while (crewsResultSet.next()) {
                int crewId = crewsResultSet.getInt("crew_id");
                String crewName = crewsResultSet.getString("crew_name");
    
                ResultSet membersResultSet = database.getCrewMembers(crewId);
                ResultSet captainsResultSet = database.getCrewCaptains(crewId);
    
                String[] resultSetFields = {"user_id", "first_name", "last_name"};
                String[] jsonNames = {"id", "fName", "lName"};
                JSONArray members = convertResultSetToJSONArray(membersResultSet, resultSetFields, jsonNames);
                JSONArray captains = convertResultSetToJSONArray(captainsResultSet, resultSetFields, jsonNames);
                
                JSONObject crew = new JSONObject();
                crew.put("captains", captains);
                crew.put("members", members);
                crew.put("crew_name", crewName);
                crew.put("crew_id", crewId);
                crews.add(crew);
            }
    
            json.put("crews", crews);
    
            // admins
    
            ResultSet adminsResultSet = database.getAdmins(clubId);
    
            String[] resultSetFields = {"user_id", "first_name", "last_name"};
            String[] jsonNames = {"id", "fName", "lName"};
            JSONArray admins = convertResultSetToJSONArray(adminsResultSet, resultSetFields, jsonNames);
    
            json.put("admins", admins);

            //members
        
            ResultSet membersResultSet = database.getClubMembers(userId);
            String[] membersResultSetFields = {"user_id", "first_name", "last_name"};
            String[] membersJSONNames = {"userId", "fName", "lName"};

            JSONArray members = convertResultSetToJSONArray(membersResultSet, membersResultSetFields, membersJSONNames);
            json.put("members", members);
    
            return json;
        } catch (SQLException e){
            System.out.println("API problem in getClubData: " + e);
            return null;
        }
    }

    
    public int getClubOfUser(int userId){
        return database.getClubOfUser(userId);
    }



    @SuppressWarnings("unchecked")
    public JSONObject getUserData(int userId){
        JSONObject json = new JSONObject();

        ResultSet resultSet = database.getUserData(userId);
        String[] resultSetFields = {"user_id", "club_id", "first_name", "last_name", "email_address", "twoK", "max_outings"};
        String[] jsonNames = {"userId", "clubId", "fName", "lName", "email", "twoK", "commitment"};
        json = convertResultSetToJSONObject(resultSet, resultSetFields, jsonNames);
        
        resultSet = database.getCaptainedCrews(userId);
        String[] resultSetFieldsCrews = {"crew_id", "crew_name"};
        String[] jsonNamesCrews = {"id", "name"};
        JSONArray crews = convertResultSetToJSONArray(resultSet, resultSetFieldsCrews, jsonNamesCrews);
        json.put("captainedCrews", crews);

        int clubId = database.getClubOfUser(userId);
        json.put("isAdmin", database.isAdmin(userId, clubId));

        return json;
    }
    

    public int getUserIDByEmail(String email) {
        return database.getUserIDByEmail(email);
    }

    
    @SuppressWarnings("unchecked")
    public JSONObject getClubMembers(int userId){
        JSONObject json = new JSONObject();
        int clubId = database.getClubOfUser(userId);
        boolean hasRight = database.isAdmin(userId, clubId);

        if(!hasRight){
            json.put("errorMessage", "You are not admin!");
            return json;
        }

        ResultSet resultSet = database.getClubMembers(userId);
        String[] resultSetFields = {"user_id", "first_name", "last_name"};
        String[] jsonNames = {"userId", "fName", "lName"};

        JSONArray userData = convertResultSetToJSONArray(resultSet, resultSetFields, jsonNames);
        json.put("members", userData);

        return json;
    }


    @SuppressWarnings("unchecked")
    public JSONObject getCrewData(int userId, int crewId){
        try {
            JSONObject json = new JSONObject();
            boolean hasRight = database.isCaptain(userId, crewId);
    
            if (!hasRight){
                json.put("success", false);
                json.put("errorMessage", "You do not have rights for this action!");
                return json;
            }

            // members    
            ResultSet membersResultSet = database.getCrewMembers(crewId);
            ResultSet rolesResultSet = database.getCrewRoles(crewId);
    
            HashMap<Integer, ArrayList<Integer>> roles = new HashMap<Integer, ArrayList<Integer>>();

            while (rolesResultSet.next()) {
                int roleId = rolesResultSet.getInt("role_id");
                int memberId = rolesResultSet.getInt("user_id");

                if(!roles.containsKey(memberId)){
                    roles.put(memberId, new ArrayList<Integer>());
                }
                roles.get(memberId).add(roleId);
            }

            
            JSONArray members = new JSONArray();
    
            while (membersResultSet.next()) {
                JSONObject entry = new JSONObject();
                entry.put("id",  membersResultSet.getObject("user_id"));
                entry.put("fName",  membersResultSet.getObject("first_name"));
                entry.put("lName",  membersResultSet.getObject("last_name"));

                JSONArray memberRoles = new JSONArray();
                ArrayList<Integer> roleList = roles.get(membersResultSet.getInt("user_id"));
                if (roleList != null){
                    for (Integer role : roleList){
                        memberRoles.add(role);
                    }
                }
                members.add(entry);

                entry.put("roles", memberRoles);
            }

            json.put("members", members);

            // captains
            ResultSet captainsResultSet = database.getCrewCaptains(crewId);
    
            String[] captainsResultSetFields = {"user_id", "first_name", "last_name"};
            String[] captainsJSONNames = {"id", "fName", "lName"};
            JSONArray captains = convertResultSetToJSONArray(captainsResultSet, captainsResultSetFields, captainsJSONNames);
            json.put("captains", captains);

            // role map
            ResultSet roleMapResultSet = database.getRoleMap();
    
            String[] roleMapResultSetFields = {"role_id", "role_name"};
            String[] roleMapJSONNames = {"id", "name"};
            JSONArray roleMap = convertResultSetToJSONArray(roleMapResultSet, roleMapResultSetFields, roleMapJSONNames);
            json.put("roleMap", roleMap);
            
            return json;            
        } catch (Exception e) {
            System.err.println("API problem getCrewData: " + e);
            return null;
        }
    }


    @SuppressWarnings("unchecked")
    public JSONObject getMemberAvailabilities(int userId){
        try {
            JSONObject json = new JSONObject();
            ResultSet availabilitiesResultSet = database.getMemberAvailabilities(userId);
            JSONArray intervals = new JSONArray();

            while (availabilitiesResultSet.next()){
                LocalTime startTime = availabilitiesResultSet.getTime("start_time").toLocalTime();
                LocalTime endTime = availabilitiesResultSet.getTime("finish_time").toLocalTime();
                LocalDate date = availabilitiesResultSet.getDate("start_time").toLocalDate();
                
                JSONObject interval = new JSONObject();

                interval.put("year", date.getYear());
                interval.put("month", date.getMonthValue());
                interval.put("day", date.getDayOfMonth());
                interval.put("startHour", startTime.getHour());
                interval.put("startMin", startTime.getMinute());
                interval.put("endHour", endTime.getHour());
                interval.put("endMin", endTime.getMinute());

                intervals.add(interval);
            }
            json.put("intervals", intervals);

            return json;
        } catch (Exception e) {
            System.err.println("API error in getMemberAvailabilities: " + e);
        }
        return null;
    }


    public boolean login(int userId, String password){
        String actualPassword = database.queryPassword(userId);
        password = hashString(password);
        return password.equals(actualPassword);
    }


    @SuppressWarnings("unchecked")
    public JSONObject removeAdmin(int userId, int adminId){
        JSONObject json = new JSONObject();
        int clubId = database.getClubOfUser(userId);
        boolean hasRight = database.isAdmin(userId, clubId);

        if(!hasRight){
            json.put("success", false);
            json.put("errorMessage", "You do not have rights for this action!");
            return json;
        }

        json.put("success", database.removeAdmin(adminId, clubId));
        
        return json;
    }


    @SuppressWarnings("unchecked")
    public JSONObject removeCrewCaptain(int userId, int captainId, int crewId){
        JSONObject json = new JSONObject();
        int clubId = database.getClubOfUser(userId);
        boolean hasRight = database.isAdmin(userId, clubId) || database.isCaptain(userId, crewId);

        if(!hasRight){
            json.put("success", false);
            json.put("errorMessage", "You do not have rights for this action!");
            return json;
        }
        
        json.put("success", database.removeCrewCaptain(captainId, crewId));

        return json;
    }


    @SuppressWarnings("unchecked")
    public JSONObject removeCrewMember(int userId, int memberId, int crewId){
        JSONObject json = new JSONObject();
        int clubId = database.getClubOfUser(userId);
        boolean hasRight = database.isAdmin(userId, clubId) || database.isCaptain(userId, crewId);

        if(!hasRight){
            json.put("success", false);
            json.put("errorMessage", "You do not have rights for this action!");
            return json;
        }

        json.put("success", database.removeCrewMember(memberId, crewId));
        
        return json;
    }


    @SuppressWarnings("unchecked")
    public JSONObject removeRoleFromMember(int userId, int memberId, int crewId, int roleId){
        JSONObject json = new JSONObject();
        boolean hasRight = database.isCaptain(userId, crewId);

        if(!hasRight){
            json.put("success", false);
            json.put("errorMessage", "You do not have rights for this action!");
            return json;
        }

        json.put("success", database.removeRoleFromMember(memberId, crewId, roleId));
        
        return json;
    }


    @SuppressWarnings("unchecked")
    public JSONObject setMemberAvailabilities(int userId, String availabilities){
        JSONObject json = new JSONObject();

        try {
            JSONParser parser = new JSONParser();
            JSONArray availabilitiesArray = (JSONArray) parser.parse(availabilities);

            database.deleteMemberAvailabilities(userId);

            for (int i = 0; i < availabilitiesArray.size(); i++){
                JSONObject daysAvailabilities = (JSONObject) availabilitiesArray.get(i);

                String year = daysAvailabilities.get("year").toString();
                String month = daysAvailabilities.get("month").toString();
                String day = daysAvailabilities.get("day").toString();
                String startHour = daysAvailabilities.get("startHour").toString();
                String startMin = daysAvailabilities.get("startMin").toString();
                String endHour = daysAvailabilities.get("endHour").toString();
                String endMin = daysAvailabilities.get("endMin").toString();

                String startTime = String.valueOf(year + "-" + month + "-" + day + " " + startHour + ":" + startMin);
                String endTime = String.valueOf(year + "-" + month + "-" + day + " " + endHour + ":" + endMin);
                database.setMemberAvailability(userId, startTime, endTime);
            }
            json.put("success", true);
            return json;
        } catch (Exception e) {
            json.put("success", false);
            System.err.println("API error in setMemberAvailabilities: " + e);
            return json;
        }
    }


    @SuppressWarnings("unchecked")
    private static JSONArray convertResultSetToJSONArray(ResultSet resultSet, String[] resultSetFields, String[] jsonNames){
        try{
            JSONArray jsonArray = new JSONArray();
    
            while (resultSet.next()) {
                JSONObject entry = new JSONObject();
                for(int i = 0; i < resultSetFields.length; i++){
                    entry.put(jsonNames[i],  resultSet.getObject(resultSetFields[i]));
                }
                jsonArray.add(entry);
            }
            return jsonArray;
        } catch (SQLException e){
            System.err.println("API error in convertResultSetToJSONArray: " + e);
            return null;
        }
    }


    @SuppressWarnings("unchecked")
    private static JSONObject convertResultSetToJSONObject(ResultSet resultSet, String[] resultSetFields, String[] jsonNames){
        try{
            JSONObject jsonObject = new JSONObject();
    
            if (resultSet.next()) {
                for(int i = 0; i < resultSetFields.length; i++){
                    jsonObject.put(jsonNames[i],  resultSet.getObject(resultSetFields[i]));
                }
            }
            return jsonObject;
        } catch (SQLException e){
            System.err.println("API error in convertResultSetToJSONObject: " + e);
            return null;
        }
    }


    private static String hashString(String string) {
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(string.getBytes(StandardCharsets.UTF_8));
    
            StringBuilder hexString = new StringBuilder(2 * hash.length);
            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e){
            System.err.println("hashing error: " + e);
            return null;
        }
    }
}
