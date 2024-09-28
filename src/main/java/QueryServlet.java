import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

public class QueryServlet extends HttpServlet{
    BackendAPI api = new BackendAPI();

    @SuppressWarnings("unchecked")
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException{
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String service = request.getParameter("service");
        System.out.println("Request arrived: " + request + ", service called: " + service);
        JSONObject responseJSON = new JSONObject();

        int userId = checkLogin(request, response);
        if (userId == -1){
            responseJSON.put("redirect", "index.html");
            response.getWriter().write(responseJSON.toJSONString());
            System.out.println("response sent: " + responseJSON.toJSONString());
            return;
        }
        int clubId;
        int newCommitment;
        String newEmail;
        String password;
        String newPassword;
        float twoK;
        String fName;
        String lName;
        int captainId;
        int crewId;
        int memberId;
        int adminId;
        int roleId;

        switch (service) {

            case "addAdmin":
                adminId = Integer.valueOf(request.getParameter("memberId"));
                responseJSON = api.addAdmin(userId, adminId);
                break;
        
            case "addCrewCaptain":
                captainId = Integer.valueOf(request.getParameter("memberId"));
                crewId = Integer.valueOf(request.getParameter("crewId"));
                responseJSON = api.addCrewCaptain(userId, captainId, crewId);
                break;
                
            case "addCrewMember":
                memberId = Integer.valueOf(request.getParameter("memberId"));
                crewId = Integer.valueOf(request.getParameter("crewId"));
                responseJSON = api.addCrewMember(userId, memberId, crewId);
                break;

            case "addRoleToMember":
                memberId = Integer.valueOf(request.getParameter("memberId"));
                crewId = Integer.valueOf(request.getParameter("crewId"));
                roleId = Integer.valueOf(request.getParameter("roleId"));
                responseJSON = api.addRoleToMember(userId, memberId, crewId, roleId);
                break;

            case "changeUserData":
                newEmail = request.getParameter("newEmail");
                password = request.getParameter("password");
                newPassword = request.getParameter("newPassword");
                System.out.println(newPassword);
                twoK = Float.valueOf(request.getParameter("newTwoK"));
                fName = request.getParameter("fName");
                lName = request.getParameter("lName");
                newCommitment = Integer.valueOf(request.getParameter("commitment"));
                System.out.println("API call");
                responseJSON = api.changeUserData(userId, newEmail, newPassword, twoK, fName, lName, newCommitment, password);
                break;

            case "getClubData":
                responseJSON = api.getClubData(userId);
                break;

            case "getClubMembers":
                responseJSON = api.getClubMembers(userId);
                break;

            case "getClubs":
                api.getClubs();
                break;

            case "getCrewData":
                crewId = Integer.valueOf(request.getParameter("crewId"));
                responseJSON = api.getCrewData(userId, crewId);
                break;

            case "getMemberAvailabilities":
                responseJSON = api.getMemberAvailabilities(userId);
                break;

            case "getUserData":
                responseJSON = api.getUserData(userId);
                break;
        
            case "joinClub":
                clubId = Integer.valueOf(request.getParameter("clubId"));
                responseJSON = api.addClubMember(userId, clubId);
                break;

            case "removeAdmin":
                adminId = Integer.valueOf(request.getParameter("adminId"));
                responseJSON = api.removeAdmin(userId, adminId);
                break;

            case "removeCrewCaptain":
                captainId = Integer.valueOf(request.getParameter("captainId"));
                crewId = Integer.valueOf(request.getParameter("crewId"));
                responseJSON = api.removeCrewCaptain(userId, captainId, crewId);
                break;

            case "removeCrewMember":
                memberId = Integer.valueOf(request.getParameter("memberId"));
                crewId = Integer.valueOf(request.getParameter("crewId"));
                responseJSON = api.removeCrewMember(userId, memberId, crewId);
                break;

            case "removeRoleFromMember":
                memberId = Integer.valueOf(request.getParameter("memberId"));
                crewId = Integer.valueOf(request.getParameter("crewId"));
                roleId = Integer.valueOf(request.getParameter("roleId"));
                responseJSON = api.removeRoleFromMember(userId, memberId, crewId, roleId);
                break;

            case "setMemberAvailabilities":
                String availabilities = request.getParameter("availabilities");
                responseJSON = api.setMemberAvailabilities(userId, availabilities);
                break;
        }
        response.getWriter().write(responseJSON.toJSONString());
        System.out.println("Response sent: " + responseJSON.toJSONString() + "\n");
        return;
    }


    private int checkLogin(HttpServletRequest request, HttpServletResponse response) throws IOException{
        int user = -1;
        Cookie cookies[] = request.getCookies();    //returns null in case of no cookies!!! (WHY???)

        if (cookies == null){
            return -1;
        }

        for(Cookie cookie: cookies ){
            if(cookie.getName().equals("user")){
                user = Integer.valueOf(cookie.getValue());
            }
        }

        // renew session
        if(user != -1){
            Cookie userCookie = new Cookie("user", Integer.toString(user));
            userCookie.setMaxAge(30 * 60);
            response.addCookie(userCookie);
        }

        return user;
    }
}