package com.cobo.kknews.utils;

import java.sql.*;

/**
 * 这个类封装了对mysql数据库的常用操作(增删改查)
 * @author cobo
 *
 */
public class MySQL{

	private Connection connection;
	private Statement statement;
	private ResultSet resultSet;

	public MySQL(){}

	public MySQL(String address, String dbName, String user, String password){
		try{
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection((new StringBuilder("jdbc:mysql://")).append(address).append(":3306/").append(dbName).toString(), user, password);
			statement = connection.createStatement();
			if (!connection.isClosed()){
				System.out.println("Database connected seccess !");
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public ResultSet showTables(){
		try{
			resultSet = statement.executeQuery("show tables;");
		}catch (SQLException e){
			e.printStackTrace();
		}
		return resultSet;
	}

	public void createTable(String sqlCreateTable){
		try{
			statement.execute(sqlCreateTable);
			System.out.println("Create table seccess !");
		}catch (SQLException e){
			e.printStackTrace();
		}
	}

	public void insert(String sqlInsert) throws SQLException{
		statement.execute(sqlInsert);
		System.out.println("Insert data to table seccess !");
	}

	public void delete(String sqlDelete){
		try{
			statement.execute(sqlDelete);
			System.out.println("Delete data to table seccess !");
		}catch (SQLException e){
			e.printStackTrace();
		}
	}

	public void update(String sqlUpdate){
		try{
			statement.execute(sqlUpdate);
			System.out.println("sqlUpdate data to table seccess !");
		}catch (SQLException e){
			e.printStackTrace();
		}
	}

	public ResultSet query(String sqlQuery){
		try{
			resultSet = statement.executeQuery(sqlQuery);
			System.out.println("Query table seccess !");
		}catch (SQLException e){
			e.printStackTrace();
		}
		return resultSet;
	}

	public void close(){
		try{
			if (resultSet != null){
				resultSet.close();
			}
		}catch (SQLException e){
			e.printStackTrace();
		}
		try{
			if (statement != null){
				statement.close();
			}
		}catch (SQLException e){
			e.printStackTrace();
		}
		try{
			if (connection != null){
				connection.close();
			}
		}catch (SQLException e){
			e.printStackTrace();
		}
	}
}