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
 * Servlet implementation class LoginServlet
 */
@WebServlet("/Login")
public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public LoginServlet() {
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
		
		String account = request.getParameter("account");
		String pw = request.getParameter("pw");
		System.out.println("account:"+account+" pw:"+pw);
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
					System.out.println("account1:"+rs.getString("account")+" pw1:"+rs.getString("pw"));
					if(rs.getString("account").equals(account)&&rs.getString("pw").equals(pw)){
						if(rs.getInt("canLogin")==1){
							mysql.close();
							MySQL mysql1 = new MySQL("localhost", "KKNews", "kknews", "kknews123");
							ResultSet rs1 = mysql1.query("select nickname from user where account='"+account+"';");
							rs1.next();
							out.print("#"+rs1.getString("nickname"));//登陆成功,返回昵称
							rs1.close();
						}else{
							out.print("ban");//封号
						}
						break;
					}
				}
				//到了表结尾都没发现账号密码匹配
				if(rs.isAfterLast()){
					out.print("fail");//登陆失败
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			mysql.close();
			out.close();
		}
	}

}
