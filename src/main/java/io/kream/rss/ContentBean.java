package io.kream.rss;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Base64.Encoder;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

@ManagedBean(name = "contentBean")
@SessionScoped
public class ContentBean
{
	private int userId;
	private String location;
	private int feedId;
	
	private String sidebar;
	private String articles;
	
//	@Resource(name = "jdbc/database")
	DataSource ds;
	
	public ContentBean()
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
		
		userId = 1;
		location = "feed";
		feedId = 26;
	}
	
	public int getUserId()
	{
		return userId;
	}
	
	public void setUserId(int userId)
	{
		this.userId = userId;
	}
	
	public String getLocation()
	{
		return location;
	}
	
	public void setLocation(String location)
	{
		this.location = location;
	}
	
	public int getFeedId()
	{
		return feedId;
	}
	
	public void setFeedId(int feedId)
	{
		this.feedId = feedId;
	}
	
	public String getSidebar()
	{
		Connection conn = null;
		PreparedStatement ps = null;
		
		sidebar = "";
		
		try
		{
			conn = ds.getConnection();
			
			ps = conn.prepareStatement("SELECT COUNT(article_id) AS unread FROM Unread WHERE user_id = ?");
			ps.setInt(1, userId);
			ResultSet rs = ps.executeQuery();
			rs.first();
			int unread = rs.getInt("unread");
			
			sidebar += "<div id='menu'><ul>";
			sidebar += "<li><a id='home' feed='home' href='javascript:;'>Home</a></li>";
			sidebar += "<li><a id='unread' feed='unread' href='javascript:;'>Unread</a> ";
			if(unread > 0)
			{
				sidebar += String.format("<span class='badge'>%d</span></li>", unread);
			}
			sidebar += "<li><a id='liked' feed='liked' href='javascript:;'>Liked</a></li>";
			sidebar += "<li><a id='all' feed='all' href='javascript:;'>All articles</a></li>";
			sidebar += "</ul></div>";
			
			ps.close();
			
			ps = conn.prepareStatement("SELECT s.folder, f.id, f.name, f.icon, u.unread FROM Subscriptions s JOIN Feeds f ON s.feed_id = f.id LEFT JOIN (SELECT a.feed_id, COUNT(a.feed_id) AS unread FROM Unread JOIN Articles a ON article_id = a.id WHERE user_id = ? GROUP BY feed_id) AS u ON f.id = u.feed_id WHERE s.user_id = ? ORDER BY s.folder, f.name");
			ps.setInt(1, userId);
			ps.setInt(2, userId);
			
			rs = ps.executeQuery();
			
			sidebar += "<div id='subscriptions'><ul class='connected sortable'>";
			
			while(rs.next() == true)
			{
				int id = rs.getInt("id");
				String folder = rs.getString("folder");
				unread = rs.getInt("unread");
				
				if(folder != null)
				{
					rs.previous();
					break;
				}
				
				Blob blob = rs.getBlob("icon");
				String iconStr = Base64.getEncoder().encodeToString(blob.getBytes(1, (int)blob.length()));
				
				sidebar += String.format("<li><a feed='%d' href='javascript:;' class='%s'",
						id, feedId == id ? "active" : "");
				
				if(iconStr.equals("") == false)
				{
					sidebar += String.format("style='background-image: url(data:image/png;base64,%s);'", iconStr);
				}
				
				sidebar += String.format(">%s</a> ", rs.getString("name"));
				
				if(unread > 0)
				{
					sidebar += String.format("<span class='badge'>%d</span>", unread);
				}
				
				sidebar += "</li>";
			}
			
			sidebar += "</ul><ul>";

			String prevFolder = null;
			
			while(rs.next() == true)
			{
				int id = rs.getInt("id");
				String folder = rs.getString("folder");
				unread = rs.getInt("unread");
				
				if(folder.equals(prevFolder) == false)
				{
					if(prevFolder != null)
					{
						sidebar += "<li class='empty-li' /></ul></li>";
					}
					
					prevFolder = folder;
					sidebar += String.format("<li class='folder'><input type='checkbox' id='folder-toggle' /><label for='folder-toggle'>%s</label><ul class='connected sortable'>", folder);
				}
				
				//icon badge
				sidebar += String.format("<li><a feed='%d' href='javascript:;' class='%s'>%s</a> ", id, feedId == id ? "active" : "", rs.getString("name"));

				if(unread > 0)
				{
					sidebar += String.format("<span class='badge'>%d</span>", unread);
				}
				
				sidebar += "</li>";
			}

			sidebar += "<li class='empty-li' /></ul></li>";
			sidebar += "</ul></div>";
			
			ps.close();
			conn.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		return sidebar;
	}
	
	public String getArticles()
	{
		Connection conn = null;
		PreparedStatement ps = null;
		
		articles = "";
		
		try
		{
			conn = ds.getConnection();
			
			if(location.equals("feed"))
			{
				ps = conn.prepareStatement("SELECT a.id, a.title, a.url, a.author, a.date, a.content FROM Users u JOIN Unread ur ON u.id = ur.user_id JOIN Articles a ON ur.article_id = a.id JOIN Feeds f ON a.feed_id = f.id WHERE u.id = ? AND f.id = ? ORDER BY a.date DESC");
				ps.setInt(1, userId);
				ps.setInt(2, feedId);
				
				ResultSet rs = ps.executeQuery();
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
				
				boolean empty = true;
				
				while(rs.next() == true)
				{
					empty = false;
					
					articles += String.format("<article id='%d' class='unread'><div class='date'>%s</div><h2><a href='%s'>%s</a></h2>",
							rs.getInt("id"), df.format(rs.getDate("date")), rs.getString("url"), rs.getString("title"));
					
					String author = rs.getString("author");
					if(author != null)
					{
						articles += String.format("<div class='author'>by <b>%s</b></div>", author);
					}
					
					articles += String.format("<div class='content'><p>%s</p></div><div class='action-bar'><span>Like</span><span>Mark as read</span></div></article>",
							rs.getString("content"));
				}
				
				if(empty == true)
				{
					articles = "<span style='display: block; padding-top: 15px; text-align: center; font-size: 18px;'>There are no unread articles.</span>";
				}
			}
			
			ps.close();
			conn.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return articles;
	}
}
