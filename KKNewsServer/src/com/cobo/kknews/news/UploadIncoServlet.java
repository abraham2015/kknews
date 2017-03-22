package com.cobo.kknews.news;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.cobo.kknews.utils.MySQL;


/**
 * Servlet implementation class UploadIncoServlet
 */
@WebServlet("/UploadInco")
public class UploadIncoServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String path;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UploadIncoServlet() {
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
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html;charset=utf-8");
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		PrintWriter out = response.getWriter();
		// 创建文件项目工厂对象
		DiskFileItemFactory factory = new DiskFileItemFactory();
		// 设置缓冲区大小为 5M
		factory.setSizeThreshold(1024 * 1024 * 5);
     	ServletFileUpload servletFileUpload = new ServletFileUpload(factory);
     	String account = null;
		// 解析结果放在List中
		try {
			List<FileItem> list = servletFileUpload.parseRequest(request);
			System.out.println("list:"+(list.size()));
			for (FileItem item : list) {
				String name = item.getFieldName();
				InputStream is = item.getInputStream();
				account = item.getName().substring(0, item.getName().indexOf("."));
				if (name.contains("content")) {
					System.out.println(inputStream2String(is));
				} else if (name.contains("img")) {
					try {
						path = "/usr/local/tomcat/webapps/KKNews_data/user_inco/"+item.getName();
						inputStream2File(is, path);
						break;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		} catch (FileUploadException e) {
			e.printStackTrace();
			System.out.println("failure");
			out.write("failure");
		}
		MySQL mysql = new MySQL("localhost", "KKNews", "kknews", "kknews123");
		mysql.update("update user set iconUrl='"+account+".jpg'"+" where account='"+account+"';");
		mysql.close();
		out.flush();
		out.close();
	}
	// 流转化成字符串
	public static String inputStream2String(InputStream is) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int i = -1;
		while ((i = is.read()) != -1) {
			baos.write(i);
		}
		return baos.toString();
	}

	// 流转化成文件
	public static void inputStream2File(InputStream is, String savePath) throws Exception {
		File file = new File(savePath);
		InputStream inputSteam = is;
		BufferedInputStream bis = new BufferedInputStream(inputSteam);
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
		byte[] bys = new byte[1024];
		int len=0;
		while ((len = bis.read(bys)) != -1) {
			bos.write(bys,0,len);
		}
		bos.flush();
		bos.close();
		bis.close();
	}
}
