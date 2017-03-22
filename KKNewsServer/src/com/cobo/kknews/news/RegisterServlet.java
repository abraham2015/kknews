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

import com.cobo.kknews.utils.MySQL;

/**
 * Servlet implementation class RegisterServlet
 */
@WebServlet("/Register")
public class RegisterServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RegisterServlet() {
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
		
		if(operation.equals("checkAccount")){
			//检查账号是否可用
			String account = request.getParameter("account");
			MySQL mysql = new MySQL("localhost", "KKNews", "kknews", "kknews123");
			ResultSet rs = mysql.query("select account from user;");
			try {
				if(!rs.next()){
					//空表
					out.print("canUse");
				}else{
					//非空表
					rs.previous();
					while(rs.next()){
						if(rs.getString("account").equals(account)){
							out.print("cannotUse");
							break;
						}
					}
					//到了表结尾都没发现账号重复
					if(rs.isAfterLast()){
						out.print("canUse");
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			} finally{
				mysql.close();
				out.close();
			}
		}else{
			//注册
			String account = request.getParameter("account");
			String pw = request.getParameter("pw");
			String nickname = request.getParameter("nickname");
			String time = request.getParameter("time");
			MySQL mysql = new MySQL("localhost", "KKNews", "kknews", "kknews123");
			try {
				mysql.insert("insert into user(account,pw,nickname,iconUrl,time) values('"+account+"','"+pw+"','"+nickname+"','','"+time+"');");
				out.print("success");
			} catch (SQLException e) {
				e.printStackTrace();
			}finally {
				mysql.close();
				out.close();
			}
			
		}
	}

}
