package com.cobo.kknews.news;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.cobo.kknews.utils.MySQL;


/**
 * Servlet implementation class ShowNewsServlet
 */
@WebServlet("/ShowNews")
public class ShowNewsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * Default constructor. 
     */
    public ShowNewsServlet() {
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html;charaset=utf-8");
		response.setCharacterEncoding("utf-8");
		request.setCharacterEncoding("utf-8");
		PrintWriter out = response.getWriter();
		
		String operation = request.getParameter("operation");
		int kind = Integer.parseInt(request.getParameter("kind"));
		System.out.println(operation);
		if(operation.equals("showNews")){
			MySQL mysql = new MySQL("localhost", "KKNews", "kknews", "kknews123");
			ResultSet rs = mysql.query("select nid,title,source,imageUrl,time,commentAmount from news where kind="
					+kind+" order by nid desc limit 10;");
			showNews(mysql,rs,out);
		}else if(operation.equals("showLatestNews")){
			int maxID = Integer.parseInt(request.getParameter("maxID"));
			System.out.println("maxID:"+maxID);
			showLatestNews(kind,maxID,out);
		}else{
			int minID = Integer.parseInt(request.getParameter("minID"));
			System.out.println("minID:"+minID);
			showAgoNews(kind,minID,out);
		}
	}
		
	private void showLatestNews(int kind, int maxID, PrintWriter out) {
		MySQL mysql = new MySQL("localhost", "KKNews", "kknews", "kknews123");
		ResultSet rs = mysql.query("select max(nid) from news where kind="+kind+";");
		int max = 0;
		try {
			rs.next();
			max = rs.getInt("max(nid)");
			System.out.println("max:"+max);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if(maxID==max){
			System.out.println("no data");
			out.print("no data");
		}else{
			rs = mysql.query("select nid,title,source,imageUrl,time,commentAmount from news where kind="
				+kind+" and nid>"+maxID+";");
			showNews(mysql,rs,out);
		}
		
	}
	
	private void showAgoNews(int kind, int minID, PrintWriter out) {
		MySQL mysql = new MySQL("localhost", "KKNews", "kknews", "kknews123");
		ResultSet rs = mysql.query("select min(nid) from news where kind="+kind+";");
		int min = 0;
		try {
			rs.next();
			min = rs.getInt("min(nid)");
			System.out.println("min:"+min);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if(minID==min){
			System.out.println("no data");
			out.print("no data");
		}else{
			rs = mysql.query("select nid,title,source,imageUrl,time,commentAmount from news where kind="
				+kind+" and nid<"+minID+" order by nid desc limit 5;");
			showNews(mysql,rs,out);
		}
		
	}

	private void showNews(MySQL mysql,ResultSet rs,PrintWriter out){
		try {
			if(rs.next()){
				JSONArray ja = new JSONArray();
				JSONObject jo = null;
				rs.previous();
				while(rs.next()){
					jo = new JSONObject();
					jo.put("nid", rs.getInt("nid"));
					jo.put("title",rs.getString("title"));
					jo.put("source", rs.getString("source"));
					jo.put("imageUrl",rs.getString("imageUrl"));
					jo.put("time",rs.getString("time").substring(0, 16));
					jo.put("commentAmount",rs.getInt("commentAmount"));
					ja.put(jo);
				}
				out.print(ja.toString());
				System.out.println(ja.toString());
			}else{
				out.print("no data");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			out.close();
			mysql.close();
		}
		
	}
	

}
