package rpc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import db.DBConnection;
import db.DBConnectionFactory;

/**
 * Servlet implementation class Login
 */
@WebServlet("/login")
public class Login extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Login() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		DBConnection connection =  DBConnectionFactory.getConnection();
		HttpSession session = request.getSession(false);
		JSONObject result = new JSONObject();
		try {
			if ( session != null) {
				String userId = session.getAttribute("user_id").toString();
				result.put("status", "OK")
				.put("user_id", userId)
				.put("name", connection.getFullname(userId));
			}else {
				result.put("result", "Invalid Session");
				response.setStatus(403);
			}
			RpcHelper.writeJsonObject(response, result);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		DBConnection connection =  DBConnectionFactory.getConnection();
		JSONObject result = new JSONObject();
		try {
			JSONObject input = RpcHelper.readJSONObject(request);
			String userId = input.getString("user_id");
			String password = input.getString("password");
	  		if (connection.verifyLogin(userId, password)) {
	  			HttpSession session = request.getSession();
	  			session.setAttribute("user_id", userId);
	  			session.setMaxInactiveInterval(10000000);
	  			result.put("status", "OK")
	  			.put("user_id", userId)
	  			.put("name", connection.getFullname(userId));
	  			System.out.println("ssss");
	  		}else {
	  			result.put("result", "No right combination");
	  			response.setStatus(401);
	  		}
	  		
	  		RpcHelper.writeJsonObject(response, result);
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
	  		 connection.close();
		}
	}

}
