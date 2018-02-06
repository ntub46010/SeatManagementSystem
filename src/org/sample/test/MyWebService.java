package org.sample.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.microsoft.sqlserver.jdbc.SQLServerException;

//http://10.10.6.112:8080/SCSBSeatMgt/api/xxx

@Path("/")
public class MyWebService {
	private String server = "localhost";
	private String dbName = "SCSBSeatMgt";
	private String user = "user";
	private String pwd = "Sc2014sb06";

	public Connection ConnectDB() throws ClassNotFoundException, SQLException {
		Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		Connection conn = DriverManager.getConnection("jdbc:sqlserver://" + server
	        	  + ";databaseName=" + dbName + ";user=" + user + ";password=" + pwd);
		return conn;
	}	
	
	@Context private HttpServletRequest request;
	@POST
	@Path("/login")
	@Produces(MediaType.APPLICATION_JSON)
	public String sayHello(String message) throws SQLException, ClassNotFoundException {
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
					Connection conn = ConnectDB();
					String session, auth;
					int tmpAccount = Integer.parseInt(jsonObA.toString());
					String[] result = UserLogin(tmpAccount, jsonObP.toString(), conn);
					if(result[0].equals("Success")){
						session = request.getSession().getId();
						auth = result[1];
					}else {
						session = "";
						auth = "";
					}
					JSONObject tmpJson = new JSONObject();
					tmpJson.put("Login", result[0]);
					tmpJson.put("SessionId", session);
					tmpJson.put("Authority", auth);
					return tmpJson.toString();
				}catch(NumberFormatException nfe){
					nfe.printStackTrace();
					return new JSONObject().put("Login", "Please only use numbers.").toString();
				}
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
			return "error";
		}
	}
	public String[] UserLogin(int Iaccount, String pwd, Connection conn) throws SQLException{
		String loginSql = "exec 登入 " + Iaccount + ", '" + pwd + "'";
		Statement stmt = null;
		stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(loginSql);
		String[] result = new String[2];
	    if(rs.next()) {
	    	result[0] = "Success";
	    	result[1] = rs.getString(1);
	    }
	    else {
	    	result[0] = "Fail";
	    	result[1] = "";
	    }
		return result;
	}
		
	//顯示座位表
	@POST
    	@Path("/seat")
    	@Produces(MediaType.APPLICATION_JSON)
    	public String getSeatTable(String jsonString) {
		try {
			JSONObject obj = new JSONObject(jsonString);
			String floor = obj.getString("Floor");
			
			JSONObject parentObj = new JSONObject();

			JSONArray parentAry = new JSONArray(getQueryJSONString(
	        		"exec 取得樓層長寬 " + floor
	        		));
			JSONObject tmpObj = parentAry.getJSONObject(0);
			parentObj.put("Column", tmpObj.getString("Column"));
			parentObj.put("Row", tmpObj.getString("Row"));

			//取得該樓層座位表
			JSONArray childAry = new JSONArray(getQueryJSONString(
	        		"exec 取得座位表 " + floor
	        		));			
			parentObj.put("Seat", childAry);
			
			return parentObj.toString();
		}catch (JSONException e) {
			e.printStackTrace();			
		}
		return "[]";
    	}
		
    	//顯示個人檔案
    	@POST
    	@Path("/profile")
    	@Produces(MediaType.APPLICATION_JSON)
    	public String getProfile(String jsonString) {
    		try {
			JSONObject obj = new JSONObject(jsonString);
			String employeeId = obj.getString("EmployeeId");
			return getQueryJSONString(
	        		"exec 顯示個人檔案 " + employeeId
	        		);
		}catch (JSONException e) {
			e.printStackTrace();			
		}	
		return "[]";
    	}
    
    	//搜尋行編
	@POST
	@Path("/search_by_id")
	@Produces(MediaType.APPLICATION_JSON)
	public String searchById(String jsonString) {
		try {			
			JSONObject obj = new JSONObject(jsonString);
			String employeeId = obj.getString("EmployeeId");
			return getQueryJSONString(
	        		"exec 搜尋行編 " + employeeId
	        		);
		}catch (JSONException e) {
			e.printStackTrace();
		}
		return "[]";
	}
	
	//搜尋姓名
	@POST
	@Path("/search_by_name")
	@Produces(MediaType.APPLICATION_JSON)
	public String searchByName(String jsonString) {
		try {			
			JSONObject obj = new JSONObject(jsonString);
			String name = obj.getString("Name");
			return getQueryJSONString(
	        		"exec 搜尋姓名 '" + name + "'"
	        		);
		}catch (JSONException e) {
			e.printStackTrace();
		}
		return "[]";
	}
	
	//搜尋科別
	@POST
	@Path("/search_by_dep")
	@Produces(MediaType.APPLICATION_JSON)
	public String searchByDep(String jsonString) {
		try {			
			JSONObject obj = new JSONObject(jsonString);
			String dep = obj.getString("Department");
			return getQueryJSONString(
	        		"exec 搜尋科別 " + dep
	        		);
		}catch (JSONException e) {
			e.printStackTrace();
		}	
		return "[]";
	}
	
	//搜尋分機
	@POST
	@Path("/search_by_ext")
	@Produces(MediaType.APPLICATION_JSON)
	public String searchByExt(String jsonString) {
		try {			
			JSONObject obj = new JSONObject(jsonString);
			String ext = obj.getString("Extension");
			return getQueryJSONString(
	        		"exec 搜尋分機 " + ext
	        		);
		}catch (JSONException e) {
			e.printStackTrace();
		}
		return "[]";
	}
	
	//搜尋無座位員工
	@POST
	@Path("/search_without_seat")
	@Produces(MediaType.APPLICATION_JSON)
	public String searchWithoutSeat() {
		return getQueryJSONString(
        		"exec 搜尋無座位員工"
        		);
	}
	
	//多重條件查詢
	@POST
	@Path("/search_by_multiple")
	@Produces(MediaType.APPLICATION_JSON)
	public String searchByMultiple(String jsonString) {
		try {
			JSONObject obj = new JSONObject(jsonString);
			String id = obj.getString("EmployeeId");
			String name = obj.getString("Name");
			String dep = obj.getString("Department");
			String ext = obj.getString("Extension");
			
			String mandate = "select Id, Name, Extension, DepName from EMPLOYEE";
			mandate += " inner join SEAT on EMPLOYEE.Department = DEPARTMENT.DepId where";
			mandate += id.equals("") ? "" : " Id = " + id + " and";
			mandate += name.equals("") ? "" : " Name like '%" + name + "%' and";
			mandate += dep.equals("") ? "" : " Department = " + dep + " and";
			mandate += ext.equals("") ? "" : " Extension = " + ext + " and";
			mandate = mandate.substring(0, mandate.length() - 4);
			System.out.println(mandate);
			return getQueryJSONString(mandate);
		}catch (JSONException e) {
			e.printStackTrace();
		}
		return"[]";
	}
	
	//列示科別
	@POST
	@Path("/list_dep")
	@Produces(MediaType.APPLICATION_JSON)
	public String searchDep() throws ClassNotFoundException, SQLException, JSONException {
		Connection conn = ConnectDB();
		ResultSet rs = SearchDepartmentSql(conn);
		JSONObject j = new JSONObject();
		String [] tmp = new String[12];
		int i = 0;
		while(rs.next()) {
			tmp[i++] = rs.getString(1);
		}
		return j.put("DepartmentInfo", tmp).toString();
	}
	public ResultSet SearchDepartmentSql(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();
		String SearchSql = "select DepName from DEPARTMENT";
		return stmt.executeQuery(SearchSql);
	}
	
	private String getQueryJSONString (String statement) {
		JSONArray jsonAry = null;
		JSONObject jsonObj = null;
		
		try {
	        	Connection con = ConnectDB();
	        	Statement s = con.createStatement();
	        	ResultSet r = s.executeQuery(statement);
	        	        
	        	ResultSetMetaData metaData = r.getMetaData();
	        	int columnCount = metaData.getColumnCount();
	        	jsonAry = new JSONArray();
	        
	        	while (r.next()) {
				jsonObj = new JSONObject();

				for (int i=1; i<=columnCount; i++){
					String columnName = metaData.getColumnLabel(i);
					String value = r.getString(columnName);
					jsonObj.put(columnName, value);
		        	}
		        	jsonAry.put(jsonObj);		        
			}
			s.close();
			con.close();
		    }catch (Exception e) {
			e.printStackTrace();
		    }		
		    return jsonAry.toString();    	
    	}
}
