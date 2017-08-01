package org.sample.test;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@Path("/Search") 
public class Search {
	
	//connect DB
	public Connection ConnectDB() throws ClassNotFoundException, SQLException {
		Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		Connection conn = DriverManager.getConnection("jdbc:sqlserver://10.10.6.111;"
		            + "databaseName=Test;user=sa;password=Sc2014sb06;");
		return conn;
	}
	
	//search employee without seat
	public ResultSet SearchEmployeeWithoutSeatSql(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();
		String searchSql = "Select Id, Name from EMPLOYEE where Id not in (select EmployeeId from SEAT)";
		return stmt.executeQuery(searchSql);
	}
	
	//search employee with multiple condition
	public ResultSet SearchEmployeeSql(Connection conn, String id, String name, String extention, String department) throws SQLException {
		Statement stmt = conn.createStatement();
		String searchSql, tmpId, tmpName, tmpExt, tmpDep, and = " and ";
		if(id.equals("")) {tmpId = "1 = 1";} else {tmpId = " Id LIKE '%" + id + "%'";}
		if(name.equals("")) {tmpName = "1 = 1";} else {tmpName = " Name LIKE '%" + name + "%'";}
		if(extention.equals("")) {tmpExt = "1 = 1";} else {tmpExt = " Extention LIKE '%" + extention + "%'";}
		if(department.equals("")) {tmpDep = "1 = 1";} else {tmpDep = " DEPARTMENT.DepName LIKE '%" + department + "%'";}
		searchSql = "select Id, Name, Extention, DEPARTMENT.DepName " + "from EMPLOYEE, DEPARTMENT" + " where DEPARTMENT.DepId = EMPLOYEE.Department and " + tmpId + and + tmpName + and + tmpExt + and + tmpDep;
		return stmt.executeQuery(searchSql);
	}
	
	//search department
	public ResultSet SearchDepartmentSql(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();
		String SearchSql = "select DepName from DEPARTMENT";
		return stmt.executeQuery(SearchSql);
	}
		
	@POST
	@Path("/EmployeeWithoutSeat")
	@Produces(MediaType.APPLICATION_JSON)
	public String SearchEmployeeWithoutSeat() throws SQLException, JSONException, ClassNotFoundException {
		Connection conn;
		conn = ConnectDB();
		ResultSet rs = SearchEmployeeWithoutSeatSql(conn);
		JSONArray result = new JSONArray();
		JSONObject mainOb = new JSONObject();
		while(rs.next()) {
			JSONObject tmp = new JSONObject();
			tmp.put("Id", rs.getString("Id"));
			tmp.put("Name", rs.getString("Name"));
			result.put(tmp);
		}
		mainOb.put("Info", result);
		return mainOb.toString();
	}
	
	@POST
	@Path("/SearchEmployee")
	@Produces(MediaType.APPLICATION_JSON)
	public String SearchEmployee(String message) throws SQLException, JSONException, ClassNotFoundException {
		JSONObject j = new JSONObject(message);
		String id, name, extention, department;
		id = j.getJSONObject("SearchInfo").get("Id").toString();
		name = j.getJSONObject("SearchInfo").get("Name").toString();
		extention = j.getJSONObject("SearchInfo").get("Extention").toString();
		department = j.getJSONObject("SearchInfo").get("Department").toString();
		Connection conn = ConnectDB();
		ResultSet rs = SearchEmployeeSql(conn, id, name, extention, department);
		ResultSetMetaData rsmd = rs.getMetaData();
		JSONArray result = new JSONArray();
		JSONObject mainOb = new JSONObject();
		int c = rsmd.getColumnCount();
		while(rs.next()) {
			JSONObject tmp = new JSONObject();
			tmp.put("Id", rs.getString("Id"));
			tmp.put("Name", rs.getString("Name"));
			tmp.put("Extention", rs.getString("Extention"));
			tmp.put("Department", rs.getString(4));
			result.put(tmp);
		}
		return mainOb.put("Info", result).toString();
	}
	
	@POST
	@Path("/SearchDepartment")
	@Produces(MediaType.APPLICATION_JSON)
	public String SearchDepartment() throws ClassNotFoundException, SQLException, JSONException {
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
}
