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
 * Servlet implementation class ShowCommentServlet
 */
@WebServlet("/ShowComment")
public class ShowCommentServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ShowCommentServlet() {
        super();
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
		
		int nid = Integer.parseInt(request.getParameter("nid"));
		String operation = request.getParameter("operation");
		System.out.println(operation);
		
		if(operation.equals("show")){
			MySQL mysql = new MySQL("localhost", "KKNews", "kknews", "kknews123");
			ResultSet rs = mysql.query("select cid,content,comment.time,nickname,iconUrl from"
					+ " comment,user where user.account=comment.account and nid="+nid+" order by cid desc limit 10;");
			show(out,mysql,rs);
		}else if(operation.equals("showLatest")){
			MySQL mysql = new MySQL("localhost", "KKNews", "kknews", "kknews123");
			int maxCid = Integer.parseInt(request.getParameter("maxCid"));
			int max = 0;
			ResultSet rs = mysql.query("select max(cid) from comment where nid="+maxCid+";");
			try {
				rs.next();
				max = rs.getInt("max(cid)");
			} catch (SQLException e) {
				e.printStackTrace();
			}
			mysql.close();
			if(maxCid == max){
				out.print("no data");
			}else{
				MySQL mysql1 = new MySQL("localhost", "KKNews", "kknews", "kknews123");
				ResultSet rs1 = mysql1.query("select cid,content,comment.time,nickname,iconUrl from"
						+ " comment,user where user.account=comment.account and nid="+nid+" and cid>"+maxCid+" limit 5;");
				show(out,mysql1,rs1);
			}
		}else{
			MySQL mysql = new MySQL("localhost", "KKNews", "kknews", "kknews123");
			int minCid = Integer.parseInt(request.getParameter("minCid"));
			System.out.println("minCid:"+minCid);
			int min = 0;
			ResultSet rs = mysql.query("select min(cid) from comment where nid="+nid+";");
			try {
				rs.next();
				min = rs.getInt("min(cid)");
				System.out.println("min:"+min);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			mysql.close();
			if(minCid == min){
				out.print("no data");
			}else{
				MySQL mysql1 = new MySQL("localhost", "KKNews", "kknews", "kknews123");
				ResultSet rs1 = mysql1.query("select cid,content,comment.time,nickname,iconUrl from"
						+ " comment,user where user.account=comment.account and nid="+nid+" and cid<"+minCid+" order by cid desc limit 5;");
				show(out,mysql1,rs1);
			}
		}
	}
	
	private void show(PrintWriter out, MySQL mysql, ResultSet rs) {
		try {
			if(rs.next()){
				JSONArray ja = new JSONArray();
				JSONObject jo = null;
				rs.previous();
				while(rs.next()){
					jo = new JSONObject();
					jo.put("cid", rs.getInt("cid"));
					jo.put("content",rs.getString("content"));
					jo.put("nickname", rs.getString("nickname"));
					jo.put("iconUrl",rs.getString("iconUrl"));
					jo.put("time",rs.getString("comment.time").substring(5, 16));
					ja.put(jo);
				}
				out.print(ja.toString());
				System.out.println(ja.toString());
			}else{
				System.out.println("no data");
				out.print("no data");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			mysql.close();
			out.close();
		}
	}

}
