package io.kream.rss;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.annotation.Resource;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
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
	private String HTML;
	
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
		HTML = "";
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
	
	public String getHTML()
	{
		Connection conn = null;
		PreparedStatement ps = null;
		
		try
		{
			String sql = "SELECT s.folder, f.id, f.name, f.icon, u.unread FROM Subscriptions s JOIN Feeds f ON s.feed_id = f.id LEFT JOIN (SELECT a.feed_id, COUNT(a.feed_id) AS unread FROM Unread JOIN Articles a ON article_id = a.id WHERE user_id = ? GROUP BY feed_id) AS u ON f.id = u.feed_id WHERE s.user_id = ? ORDER BY s.folder, f.name";
			conn = ds.getConnection();
			ps = conn.prepareStatement(sql);
			ps.setInt(1, 1);
			ps.setInt(2, 1);
			
			ResultSet rs = ps.executeQuery();
			
			HTML = "<div id='subscriptions'><ul class='connected sortable'>";
			String prevFolder = null;
			
			while(rs.next() == true)
			{
				String name = rs.getString("name");
				String folder = rs.getString("folder");
				int unread = rs.getInt("unread");
				
				if(folder == null)
				{
					HTML += String.format("<li><a class='%s'>%s</a> ", name.equals("Phoronix") ? "active" : "", name);
					if(unread > 0)
					{
						HTML += String.format("<span class='badge'>%d</span>", unread);
					}
					HTML += "</li>";
				}
				else
				{
//					if(prevFolder == null)
//					{
//						HTML += "<ul>";
//					}
//					if(folder.equals(prevFolder) == false)
//					{
//						prevFolder = folder;
//					}
				}
			}
			HTML += "</ul></div>";
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		return HTML;
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
