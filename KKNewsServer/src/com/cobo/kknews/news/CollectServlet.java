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
 * Servlet implementation class CollectServlet
 */
@WebServlet("/Collect")
public class CollectServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CollectServlet() {
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
		
		String operation = request.getParameter("operation");
		
		if(operation.equals("upload")){
			//上传收藏信息
			int nid = Integer.parseInt(request.getParameter("nid"));
			String account = request.getParameter("account");
			String time= request.getParameter("time");
			MySQL mysql = new MySQL("localhost", "KKNews", "kknews", "kknews123");
			try {
				mysql.insert("insert into collect values("+nid+",'"+account+"','"+time+"');");
				out.print("success");
			} catch (SQLException e) {
				e.printStackTrace();
			}finally{
				mysql.close();
				out.close();
			}
		}else if(operation.equals("remove")){
			//移除某条收藏信息
			int nid = Integer.parseInt(request.getParameter("nid"));
			String account = request.getParameter("account");
			MySQL mysql = new MySQL("localhost", "KKNews", "kknews", "kknews123");
			mysql.delete("delete from collect where nid="+nid+" and account='"+account+"';");
			out.print("success");
			mysql.close();
			out.close();
		}else if(operation.equals("show")){
			//展示收藏信息
			String account = request.getParameter("account");
			MySQL mysql = new MySQL("localhost", "KKNews", "kknews", "kknews123");
			ResultSet rs = mysql.query("select news.nid,title,source,imageUrl,news.time,"
					+ "commentAmount,collect.time from news,collect where news.nid=collect.nid"
					+ " and account='"+account+"' order by collect.time desc;");
			try {
				if(rs.next()){
					rs.previous();
					JSONArray ja = new JSONArray();
					JSONObject jo = null;
					rs.previous();
					while(rs.next()){
						jo = new JSONObject();
						jo.put("nid", rs.getInt("news.nid"));
						jo.put("title",rs.getString("title"));
						jo.put("source", rs.getString("source"));
						jo.put("imageUrl",rs.getString("imageUrl"));
						jo.put("newsTime",rs.getString("news.time").substring(0, 16));
						jo.put("commentAmount",rs.getInt("commentAmount"));
						jo.put("collectTime",rs.getString("Collect.time").substring(0, 16));
						ja.put(jo);
					}
					out.print(ja.toString());
					System.out.println(ja.toString());
				}else{
					out.print("no data");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}finally {
				mysql.close();
				out.close();
			}
		}else{
			String account = request.getParameter("account");
			MySQL mysql = new MySQL("localhost", "KKNews", "kknews", "kknews123");
			ResultSet rs = mysql.query("select nid from collect where account='"+account+"';");
			try {
				if(rs.next()){
					rs.previous();
					StringBuffer sb = new StringBuffer();
					while(rs.next()){
						sb.append(rs.getString("nid")).append(",");
					}
					out.print(sb.toString());
					System.out.println(sb.toString());
				}else{
					out.print("");
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}finally{
				mysql.close();
				out.close();
			}
		}
	}

}
