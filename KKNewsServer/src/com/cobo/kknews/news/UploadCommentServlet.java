package com.cobo.kknews.news;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.cobo.kknews.utils.MySQL;


/**
 * Servlet implementation class UploadCommentServlet
 */
@WebServlet("/UploadComment")
public class UploadCommentServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UploadCommentServlet() {
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
		String account = request.getParameter("account");
		String time = request.getParameter("time");
		String content  = request.getParameter("content");
		
		MySQL mysql = new MySQL("localhost", "KKNews", "kknews", "kknews123");
		try {
			//插入评论数据
			mysql.insert("insert into comment(nid,account,time,content)values("+nid+",'"+account+"','"+time+"','"+content+"');");
			//更新该新闻的评论数(+1)
			mysql.update("update news set commentAmount=commentAmount+1 where nid="+nid+";");
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			out.print("success");
			mysql.close();
			out.close();
		}
	}

}
