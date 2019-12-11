package com.zuxia.util;

import java.lang.reflect.Field;
import java.security.interfaces.RSAKey;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.omg.CORBA.PUBLIC_MEMBER;

public class DBUtil {   
	private static final String url = "jdbc:mysql://localhost:3306/userDB?characterEncoding=utf8";//连接对象
	private static final String DRIVER = "com.mysql.jdbc.Driver";//加载驱动
	private static final String username = "root"; 
	private static final String password = "123456";
    //第一次加载这个工具类就执行static中的代码
    static{ //静态语句块
    	try {
			Class.forName(DRIVER);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    /**连接对象*/
    public static Connection GetConnect(){
    	try {
    		
			return DriverManager.getConnection(url,username,password);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return null;
    }
    /**关闭连接对象*/
    public static void getClose(ResultSet rs, Connection conn ,PreparedStatement ps){
			try {
				if(rs!=null)rs.close();
				if(ps!=null)ps.close();
				if(conn!=null)conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    }
    /**增删改方法封装 */
    public static int IUDstatement(String sql,Object...params){
    	Connection conn = GetConnect(); //连接对象
    	System.out.println(conn);
    	int line = 0;
    	PreparedStatement ps = null; 
    	try {
			ps = conn.prepareStatement(sql);
			if(params!=null && params.length>0){
	    		for(int i = 0 ; i<params.length; i++){
	        	    ps.setObject(i+1,params[i]);	
	        	}
	    	}
			line = ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			e.printStackTrace();
		}finally{
			
			DBUtil.getClose(null, conn, ps);
		}
    	return line;
    }
    /**查询的方法*/
    public static List SelStatement(Class cla,String sql,Object... parmas){
		Connection conn=DBUtil.GetConnect();
		PreparedStatement ps=null;
		ResultSet rs=null;
		List list=new ArrayList();
		try {
			//预编译
			ps=conn.prepareStatement(sql);
			//赋值
			if(parmas!=null && parmas.length>0){
				for(int i=0;i<parmas.length;i++){
					ps.setObject(i+1, parmas[i]);
				}
			}
			//执行
			rs = ps.executeQuery();
			//遍历
			while(rs.next()){
				Object obj = reflect(cla, rs);
				//添加集合
				list.add(obj);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}finally{
			DBUtil.getClose(rs, conn, ps);
		}
		return list;
	}
	
	public static Object reflect(Class cla,ResultSet rs) throws InstantiationException, IllegalAccessException, IllegalArgumentException, SQLException, ClassNotFoundException{
		//创建对象
		Object obj = cla.newInstance();
		//封装数据
		Field[] fls = cla.getDeclaredFields();
		for(Field fl : fls){
			fl.setAccessible(true);
			//判断当前属性是否为一个对象需要再一次反射封装
			String typeName = fl.getType().getName();
			if(typeName.contains("entity")){
				Object object = reflect(Class.forName(typeName), rs);
				fl.set(obj, object);
			}else{
				fl.set(obj, rs.getObject(fl.getName()));
			}
		}
		return obj;
	}
}
