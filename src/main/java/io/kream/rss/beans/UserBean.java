package io.kream.rss.beans;

import io.kream.rss.entities.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

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
	private DataSource ds;
	
	private User user;
	private boolean rememberMe;
	
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
		
		user = new User();
	}

	public User getUser()
	{
		return user;
	}
	
	public String getRealName()
	{
		return user.getRealName();
	}
	
	public void setRealName(String realName)
	{
		user.setRealName(realName);
	}
	
	public String getEmail()
	{
		return user.getEmail();
	}
	
	public void setEmail(String email)
	{
		user.setEmail(email);
	}
	
	public String getPassword()
	{
		return user.getPassword();
	}
	
	public void setPassword(String password)
	{
		user.setPassword(password);
	}

	public boolean isRememberMe()
	{
		return rememberMe;
	}

	public void setRememberMe(boolean rememberMe)
	{
		this.rememberMe = rememberMe;
	}
	
	public String signIn()
	{
		Connection conn = null;
		PreparedStatement ps = null;
		
		String response = null;
		
		try
		{
			conn = ds.getConnection();
			
			ps = conn.prepareStatement("SELECT id FROM Users WHERE email = ? AND password = SHA2(?, 512)");
			ps.setString(1, user.getEmail());
			ps.setString(2, user.getPassword());
			
			ResultSet rs = ps.executeQuery();
			
			if(rs.first() == true)
			{
//				if(rememberMe == true)
				{
					
				}

				user.setId(rs.getInt("id"));
				
				response = "home.xhtml?faces-redirect=true";
			}
			else
			{
				response = "failure";
			}
			
			ps.close();
			conn.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return response;
	}

	public String register()
	{
		Connection conn = null;
		PreparedStatement ps = null;
		
		try
		{
			conn = ds.getConnection();
			
			ps = conn.prepareStatement("INSERT INTO Users (real_name, email, password) VALUES (?, ?, SHA2(?, 512))");
			ps.setString(1, user.getRealName());
			ps.setString(2, user.getEmail());
			ps.setString(3, user.getPassword());
			
			ps.executeUpdate();
			
			ps.close();
			conn.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return "home.xhtml?faces-redirect=true";
	}
}
