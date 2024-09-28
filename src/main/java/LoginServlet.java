import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

public class LoginServlet extends HttpServlet{
    BackendAPI api = new BackendAPI();

    @SuppressWarnings("unchecked")
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException{
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        String email = request.getParameter("email");
        String password = request.getParameter("pwd");
        int userId = api.getUserIDByEmail(email);

        boolean loginSuccessful = api.login(userId, password);
        JSONObject json = new JSONObject();
        
        if (loginSuccessful){
            //TODO: Make this shit safe
            Cookie userCookie = new Cookie("user", Integer.toString(userId));
            userCookie.setMaxAge(30 * 60);
            response.addCookie(userCookie);

            String nextPage = api.getClubOfUser(userId) == 0 ? "clubselection.html" : "time.html";
            json.put("redirect", nextPage);
            json.put("login", "success");

            System.out.println(json.toJSONString());

        } else {
            json.put("errorMessage", "Incorrect email or password, please try again!");
            json.put("login", "failure");
        }

        response.getWriter().write(json.toJSONString());
    }
}