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
 * Servlet implementation class ModifyUserInfoServlet
 */
@WebServlet("/ModifyUserInfo")
public class ModifyUserInfoServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ModifyUserInfoServlet() {
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
		
		String tag =request.getParameter("tag");
		if("nickname".equals(tag)){
			String account = request.getParameter("account");
			String newNickname= request.getParameter("newNickname");
			System.out.println(account+","+newNickname);
			MySQL mysql = new MySQL("localhost", "KKNews", "kknews", "kknews123");
			mysql.update("update user set nickname='"+newNickname+"' where account='"+account+"';");
			mysql.close();
		}else{
			PrintWriter out = response.getWriter();
			String account = request.getParameter("account");
			String oldPW = request.getParameter("oldPW");
			String newPW = request.getParameter("newPW");
			System.out.println(account+","+oldPW+","+newPW);
			MySQL mysql = new MySQL("localhost", "KKNews", "kknews", "kknews123");
			ResultSet rs = mysql.query("select account,pw,canLogin from user;");
			try {
				if(!rs.next()){
					//空表
					out.print("fail");//登陆失败
				}else{
					//非空表
					rs.previous();
					while(rs.next()){
						if(rs.getString("account").equals(account)&&rs.getString("pw").equals(oldPW)){
							mysql.close();
							MySQL mysql1 = new MySQL("localhost", "KKNews", "kknews", "kknews123");
							mysql1.update("update user set pw='"+newPW+"' where account='"+account+"';");
							out.print("success");
							mysql1.close();
							break;
						}
					}
					//到了表结尾都没发现账号密码匹配
					if(rs.isAfterLast()){
						out.print("fail");//验证失败
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			} finally{
				out.close();
			}
		}
	}

}
