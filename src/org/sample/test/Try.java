package org.sample.test;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path; 
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType; 
import org.json.JSONException;
import org.json.JSONObject;

@Path("/try") 

public class Try {
	//connect DB
	public Connection ConnectDB() throws ClassNotFoundException, SQLException {
		Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		Connection conn = DriverManager.getConnection("jdbc:sqlserver://10.10.6.111;"
		            + "databaseName=Test;user=sa;password=Sc2014sb06;");
		return conn;
	}
	//login fuction
	public ResultSet UserLogin(int Iaccount, String pwd, Connection conn) throws SQLException{
		String loginSql = "SELECT * from EMPLOYEE WHERE Id = " + Iaccount + " and Password = '" + pwd + "'" + " and Authority <> 2";
		Statement stmt = null;
		stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(loginSql);
		return rs;
	}
	
	@Context private HttpServletRequest request;
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public String Login(String message) throws SQLException, ClassNotFoundException {
		JSONObject j;
		try {
			j = new JSONObject(message);
			Object jsonObA = j.getJSONObject("LoginInfo").get("Account");
			Object jsonObP = j.getJSONObject("LoginInfo").get("Password");
			if((jsonObA.toString() == null) || jsonObA.toString().equals("") || (jsonObP.toString() == null) || jsonObP.toString().equals("")) {
				return new JSONObject().put("Login", "The field cannot be blank.").toString();
			}
			else {
				try {
					String session, result, authority;
					Connection conn = ConnectDB();
					int tmpAccount = Integer.parseInt(jsonObA.toString());
					ResultSet rs = UserLogin(tmpAccount, jsonObP.toString(), conn);
					JSONObject tmpJson = new JSONObject();
					if(rs.next()) {result = "Success";session = request.getSession().getId();authority =  rs.getString("Authority");}
					else {result = "Failed";session = "";authority = "";}
					tmpJson.put("Login", result);
					tmpJson.put("SessionId", session);
					tmpJson.put("Authority", authority);
					return tmpJson.toString();
				}catch(Exception e){
					e.printStackTrace();
					return new JSONObject().put("Login", "Please only use numbers.").toString();
				}
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
			return "error";
		}
	}
}
