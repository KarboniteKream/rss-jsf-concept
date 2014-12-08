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

@ManagedBean(name = "userBean")
@SessionScoped
public class UserBean
{
	private int id;
	private String email;
	private String password;
	private boolean rememberMe;
	
	private String errorMessage;
	
	private String location;
	
//	@Resource(name = "jdbc/database")
	DataSource ds;
	
	public UserBean()
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
		
		location = "arch";
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
	
	public String sign_in()
	{
		Connection conn = null;
		PreparedStatement ps = null;
		
		try
		{
			String sql = "SELECT id FROM Users where email = ?";
			conn = ds.getConnection();
			ps = conn.prepareStatement(sql);
			ps.setString(1, email);
			
			ResultSet rs = ps.executeQuery();
			rs.first();
			
			id = rs.getInt("id");
			
			return "home";
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		return "index";
	}
	
	public String register()
	{
		return "home.xhtml?faces-redirect=true";
	}
	
	public void setEmail(final String email)
	{
		this.email = email;
	}
	
	public String getEmail()
	{
		return email;
	}
	
	public void setPassword(final String password)
	{
		this.password = password;
	}
	
	public String getPassword()
	{
		return password;
	}
	
	public void setRememberMe(final boolean rememberMe)
	{
		this.rememberMe = rememberMe;
	}
	
	public boolean getRememberMe()
	{
		return rememberMe;
	}
	
	public void setErrorMessage(String errorMessage)
	{
		this.errorMessage = errorMessage;
	}
	
	public String getErrorMessage()
	{
		return errorMessage;
	}
	
	public void setLocation(String location)
	{
		this.location = location;
	}

	public String getLocation()
	{
		return location;
	}
}
