package io.kream.rss.beans;

import io.kream.rss.entities.Article;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

@ManagedBean(name = "utilBean")
@SessionScoped
public class UtilBean
{
	private DataSource ds;
	
	@ManagedProperty(value = "#{contentBean}")
	private ContentBean contentBean;
	
	private int articleId;
	
	public UtilBean()
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
	
	public void setContentBean(ContentBean contentBean)
	{
		this.contentBean = contentBean;
	}
	
	public String changeFeed(String id)
	{
		Connection conn = null;
		PreparedStatement ps = null;
		
		try
		{
			conn = ds.getConnection();
			
			ps = conn.prepareStatement("SELECT name FROM Feeds WHERE id = ?");
			ps.setInt(1, Integer.parseInt(id));
			
			ResultSet rs = ps.executeQuery();
			
			if(rs.first() == true)
			{
				contentBean.setLocation("feed");
				contentBean.setFeedId(Integer.parseInt(id));
				contentBean.setFeedName(rs.getString("name"));
				
				return "home.xhtml?faces-redirect=true";
			}
			
			ps.close();
			conn.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return "failure";
	}

	public String markAsRead()
	{
		return "success";
	}

	public String like()
	{
		return "success";
	}
	
	public String unsubscribe()
	{
		return "success";
	}
}
