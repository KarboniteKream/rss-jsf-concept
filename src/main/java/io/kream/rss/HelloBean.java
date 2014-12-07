package io.kream.rss;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.annotation.Resource;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

@ManagedBean(name = "helloBean")
@SessionScoped
public class HelloBean {
	private String email;
	private String password;
	
//	@Resource(name = "jdbc/database")
	DataSource ds;
	
	public HelloBean()
	{
		try
		{
			Context ctx = new InitialContext();
			ds = (DataSource)ctx.lookup("java:comp/env/jdbc/database");
		}
		catch(NamingException e)
		{
			e.printStackTrace();
		}
	}
	
	public String validate()
	{
		Connection conn = null;
		PreparedStatement ps = null;
		
		try
		{
			String sql = "SELECT id, email FROM Users where email = ?";
			conn = ds.getConnection();
			ps = conn.prepareStatement(sql);
			ps.setString(1, email);
			
			ResultSet rs = ps.executeQuery();
			rs.first();
			
			return rs.getString("email");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		return "FAIL";
	}
	
	public String getEmail()
	{
		return email;
	}
	
	public String getPassword()
	{
		return password;
	}
	
	public void setEmail(final String email)
	{
		this.email = email;
	}
	
	public void setPassword(final String password)
	{
		this.password = password;
	}
}
