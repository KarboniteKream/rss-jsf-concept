package io.kream.rss.beans;

import io.kream.rss.entities.Article;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

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
	
	public String changeFeed()
	{
		Connection conn = null;
		PreparedStatement ps = null;
		
		try
		{
			conn = ds.getConnection();
			
			switch(contentBean.getFeedId())
			{
				case -1:
					contentBean.setLocation("home");
					contentBean.setFeedName("Home");
				break;

				case -2:
					contentBean.setLocation("unread");
					contentBean.setFeedName("Unread articles");
				break;

				case -3:
					contentBean.setLocation("home");
					contentBean.setFeedName("Liked articles");
				break;

				case -4:
					contentBean.setLocation("all");
					contentBean.setFeedName("All articles");
				break;
				
				default:
					ps = conn.prepareStatement("SELECT name FROM Feeds WHERE id = ?");
					ps.setInt(1, contentBean.getFeedId());
					
					ResultSet rs = ps.executeQuery();
					
					if(rs.first() == true)
					{
						contentBean.setLocation("feed");
						contentBean.setFeedName(rs.getString("name"));
					}
					
					ps.close();
				break;
			}

			conn.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return "home.xhtml?faces-redirect=true";
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
