import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RegisterServlet extends HttpServlet{
    BackendAPI api = new BackendAPI();

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException{
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        String email = request.getParameter("email");
        String password = request.getParameter("pwd");
        String passwordConfirm = request.getParameter("pwdConfirm");
        String fName = request.getParameter("fName");
        String lName = request.getParameter("lName");


        boolean passwordsMatch = password.equals(passwordConfirm);

        if (!passwordsMatch){
            response.getWriter().write("Passwords do not match!");
            return;
        }
        
        boolean userCreated = api.createUser(email, password, fName, lName);
        if (userCreated){
            response.getWriter().write("success");
        }

        response.getWriter().write("Something went wrong, please try again later, or with another email address (it may be taken)!");
    }
}