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
	
	private int unreadCount;
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
					contentBean.setLocation("liked");
					contentBean.setFeedName("Liked articles");
				break;

				case -4:
					contentBean.setLocation("all");
					contentBean.setFeedName("All articles");
				break;
				
				default:
					conn = ds.getConnection();
					ps = conn.prepareStatement("SELECT name FROM Feeds WHERE id = ?");
					ps.setInt(1, contentBean.getFeedId());
					
					ResultSet rs = ps.executeQuery();
					
					if(rs.first() == true)
					{
						contentBean.setLocation("feed");
						contentBean.setFeedName(rs.getString("name"));
					}
					
					ps.close();
					conn.close();
				break;
			}
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
		Connection conn = null;
		PreparedStatement ps = null;
		
		try
		{
			conn = ds.getConnection();
			ps = conn.prepareStatement("DELETE FROM Subscriptions WHERE user_id = ? AND feed_id = ?");
			ps.setInt(1, contentBean.getUserBean().getUser().getId());
			ps.setInt(2, contentBean.getFeedId());
			
			ps.executeUpdate();
			
			contentBean.setLocation("home");
			contentBean.setFeedId(-1);
			contentBean.setFeedName("Home");
			
			ps.close();
			conn.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return "home.xhtml?faces-redirect=true";
	}
	
	public int getUnreadCount()
	{
		Connection conn = null;
		PreparedStatement ps = null;
		
		try
		{
			conn = ds.getConnection();
			ps = conn.prepareStatement("SELECT COUNT(article_id) AS unread FROM Unread WHERE user_id = ?");
			ps.setInt(1, contentBean.getUserBean().getUser().getId());
			
			ResultSet rs = ps.executeQuery();
			
			if(rs.first() == true)
			{
				unreadCount = rs.getInt("unread");
			}
			
			ps.close();
			conn.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return unreadCount;
	}
	
	public void setUnreadCount(int unreadCount)
	{
		this.unreadCount = unreadCount;
	}
	
	public boolean isActive(int id)
	{
		return (id == contentBean.getFeedId());
	}
}
