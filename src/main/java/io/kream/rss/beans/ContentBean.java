package io.kream.rss.beans;

import io.kream.rss.entities.Article;
import io.kream.rss.entities.Feed;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

@ManagedBean(name = "contentBean")
// request scoped?
@SessionScoped
public class ContentBean
{
	private DataSource ds;
	
	@ManagedProperty(value = "#{userBean}")
	private UserBean userBean;
	
	private String location;
	
	private int feedId;
	private String feedName;
	
	private String message;

	private List<String> folders;
	private ArrayList<ArrayList<Feed>> feeds;
	private int idx;
	
	private List<Article> articles;
	private List<Article> featured;
	
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

		location = "home";
		feedId = -1;
		setFeedName("Home");

		setFolders(new ArrayList<String>());
		feeds = new ArrayList<ArrayList<Feed>>();
		idx = 0;
		
		setArticles(new ArrayList<Article>());
		setFeatured(new ArrayList<Article>());
	}
	
	public void setUserBean(UserBean userBean)
	{
		this.userBean = userBean;
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
	
	public List<Feed> getFeeds()
	{
		Connection conn = null;
		PreparedStatement ps = null;
		
		try
		{
			conn = ds.getConnection();
			
			feeds.clear();
			idx = 0;
			
//			ps = conn.prepareStatement("SELECT COUNT(article_id) AS unread FROM Unread WHERE user_id = ?");
//			ps.setInt(1, userBean.getUser().getId());
//			sidebar += "<li><a id='unread' feed='unread' href='javascript:;'>Unread</a> ";
//			if(unread > 0)
//			{
//				sidebar += String.format("<span class='badge'>%d</span></li>", unread);
//			}
//			
//			ps.close();
			
			ps = conn.prepareStatement("SELECT s.folder, f.id, f.name, f.icon, u.unread FROM Subscriptions s JOIN Feeds f ON s.feed_id = f.id LEFT JOIN (SELECT a.feed_id, COUNT(a.feed_id) AS unread FROM Unread JOIN Articles a ON article_id = a.id WHERE user_id = ? GROUP BY feed_id) AS u ON f.id = u.feed_id WHERE s.user_id = ? ORDER BY s.folder, f.name");
			ps.setInt(1, userBean.getUser().getId());
			ps.setInt(2, userBean.getUser().getId());
			
			ResultSet rs = ps.executeQuery();

			ArrayList<Feed> l = new ArrayList<Feed>();
			String prevFolder = null;
			
			while(rs.next() == true)
			{
				String folder = rs.getString("folder");
				
				if(prevFolder != null && folder.equals(prevFolder) == false)
				{
					prevFolder = folder;
					feeds.add(l);
					l = new ArrayList<Feed>();
				}
				else if(prevFolder == null && folder != prevFolder)
				{
					prevFolder = folder;
					feeds.add(l);
					l = new ArrayList<Feed>();
				}
				
				l.add(new Feed(rs.getInt("id"), rs.getString("name"), rs.getBlob("icon"), rs.getInt("unread"), folder));
			}
			
			feeds.add(l);

			ps.close();
			conn.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		return feeds.get(0);
	}
	
	public void setFeeds(ArrayList<ArrayList<Feed>> feeds)
	{
		this.feeds = feeds;
	}
	
	public List<String> getFolders()
	{
		Connection conn = null;
		PreparedStatement ps = null;
		
		try
		{
			conn = ds.getConnection();
			folders.clear();
			
			ps = conn.prepareStatement("SELECT DISTINCT folder FROM Subscriptions WHERE user_id = ? ORDER BY folder");
			ps.setInt(1, userBean.getUser().getId());
			
			ResultSet rs = ps.executeQuery();
			
			while(rs.next() == true)
			{
				folders.add(rs.getString("folder"));
			}
			
			ps.close();
			conn.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		return folders;
	}

	public void setFolders(List<String> folders)
	{
		this.folders = folders;
	}
	
	public List<Feed> getFolderFeeds()
	{
		int i = (idx == feeds.size() - 1) ? idx : idx + 1;
		
		List<Feed> folderFeeds = feeds.get(i);
		idx++;
		
		return folderFeeds;
	}

	public List<Article> getArticles()
	{
		Connection conn = null;
		PreparedStatement ps = null;
		
		try
		{
			conn = ds.getConnection();
			articles.clear();
			ps = conn.prepareStatement("SELECT a.id, a.title, a.url, a.author, a.date, a.content FROM Users u JOIN Unread ur ON u.id = ur.user_id JOIN Articles a ON ur.article_id = a.id JOIN Feeds f ON a.feed_id = f.id WHERE u.id = ? AND f.id = ? ORDER BY a.date DESC");
			
			if(location.equals("feed") && userBean.getUser().getId() != -1)
			{
				ps.setInt(1, userBean.getUser().getId());
				ps.setInt(2, feedId);
				
				ResultSet rs = ps.executeQuery();
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
				
				while(rs.next() == true)
				{
					articles.add(new Article(rs.getInt("id"), rs.getString("title"), rs.getString("url"), rs.getString("author"), df.format(rs.getDate("date")), rs.getString("content")));
				}
				
				if(articles.size() > 0)
				{
					//articles = "<span style='display: block; padding-top: 15px; text-align: center; font-size: 18px;'>There are no unread articles.</span>";
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

	public void setArticles(List<Article> articles)
	{
		this.articles = articles;
	}

	public List<Article> getFeatured() {
		Connection conn = null;
		PreparedStatement ps = null;
		
		featured.clear();
		
		try
		{
			conn = ds.getConnection();
			
			ps = conn.prepareStatement("SELECT a.id, a.title, a.url, a.author, a.date, a.content FROM Liked l JOIN Articles a ON l.article_id = a.id GROUP BY l.article_id ORDER BY COUNT(l.article_id) DESC");
			
			ResultSet rs = ps.executeQuery();
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			
			while(rs.next() == true)
			{
				// number of likes
				featured.add(new Article(rs.getInt("id"), rs.getString("title"), rs.getString("url"), rs.getString("author"), df.format(rs.getDate("date")), rs.getString("content")));
			}
			
			ps.close();
			conn.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return featured;
	}

	public void setFeatured(List<Article> featured)
	{
		this.featured = featured;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public String getFeedName() {
		return feedName;
	}

	public void setFeedName(String feedName) {
		this.feedName = feedName;
	}
}
