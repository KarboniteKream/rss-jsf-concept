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

		message = "";
	}

	public UserBean getUserBean()
	{
		return userBean;
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

	public List<String> getFolders()
	{
		Connection conn = null;
		PreparedStatement ps = null;

		List<String> folders = new ArrayList<String>();

		try
		{
			conn = ds.getConnection();

			ps = conn.prepareStatement("SELECT DISTINCT folder FROM Subscriptions WHERE user_id = ? AND folder IS NOT NULL ORDER BY folder");
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

	public List<Feed> getFeeds(String folder)
	{
		Connection conn = null;
		PreparedStatement ps = null;

		List<Feed> feeds = new ArrayList<Feed>();

		try
		{
			conn = ds.getConnection();

			if(folder.equals("") == true)
			{
				ps = conn.prepareStatement("SELECT f.id, f.name, f.icon, u.unread FROM Subscriptions s JOIN Feeds f ON s.feed_id = f.id LEFT JOIN (SELECT a.feed_id, COUNT(a.feed_id) AS unread FROM Unread JOIN Articles a ON article_id = a.id WHERE user_id = ? GROUP BY feed_id) AS u ON f.id = u.feed_id WHERE s.user_id = ? AND s.folder IS NULL ORDER BY f.name");
				ps.setInt(1, userBean.getUser().getId());
				ps.setInt(2, userBean.getUser().getId());
			}
			else
			{
				ps = conn.prepareStatement("SELECT f.id, f.name, f.icon, u.unread FROM Subscriptions s JOIN Feeds f ON s.feed_id = f.id LEFT JOIN (SELECT a.feed_id, COUNT(a.feed_id) AS unread FROM Unread JOIN Articles a ON article_id = a.id WHERE user_id = ? GROUP BY feed_id) AS u ON f.id = u.feed_id WHERE s.user_id = ? AND s.folder = ? ORDER BY f.name");
				ps.setInt(1, userBean.getUser().getId());
				ps.setInt(2, userBean.getUser().getId());
				ps.setString(3, folder);
			}

			ResultSet rs = ps.executeQuery();

			while(rs.next() == true)
			{
				feeds.add(new Feed(rs.getInt("id"), rs.getString("name"), rs.getBlob("icon"), rs.getInt("unread")));
			}

			ps.close();
			conn.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		return feeds;
	}

	public List<Article> getArticles()
	{
		Connection conn = null;
		PreparedStatement ps = null;

		List<Article> articles = new ArrayList<Article>();

		try
		{
			conn = ds.getConnection();

			if(location.equals("unread") == true)
			{
				ps = conn.prepareStatement("SELECT a.id, a.title, a.url, a.author, a.date, a.content FROM Unread l JOIN Articles a ON l.article_id = a.id WHERE user_id = ? ORDER BY a.date DESC");
				ps.setInt(1, userBean.getUser().getId());
			}
			else if(location.equals("liked") == true)
			{
				ps = conn.prepareStatement("SELECT a.id, a.title, a.url, a.author, a.date, a.content FROM Liked l JOIN Articles a ON l.article_id = a.id WHERE user_id = ?");
				ps.setInt(1, userBean.getUser().getId());
			}
			else if(location.equals("all") == true)
			{
				ps = conn.prepareStatement("SELECT l.user_id AS liked, u.user_id AS unread, a.id, a.title, a.url, a.author, a.date, a.content FROM Subscriptions s JOIN Feeds f ON s.feed_id = f.id JOIN Articles a ON f.id = a.feed_id LEFT JOIN Liked l ON a.id = l.article_id LEFT JOIN Unread u ON a.id = u.article_id WHERE s.user_id = ? ORDER BY a.date DESC LIMIT 25");
				ps.setInt(1, userBean.getUser().getId());
			}
			else if(location.equals("feed") == true && userBean.getUser().getId() != -1)
			{
				ps = conn.prepareStatement("SELECT a.id, a.title, a.url, a.author, a.date, a.content FROM Users u JOIN Unread ur ON u.id = ur.user_id JOIN Articles a ON ur.article_id = a.id JOIN Feeds f ON a.feed_id = f.id WHERE u.id = ? AND f.id = ? ORDER BY a.date DESC");
				ps.setInt(1, userBean.getUser().getId());
				ps.setInt(2, feedId);
			}

			if(ps != null)
			{
				ResultSet rs = ps.executeQuery();
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

				while(rs.next() == true)
				{
					if(location.equals("feed") == true || location.equals("unread"))
					{
						articles.add(new Article(rs.getInt("id"), rs.getString("title"), rs.getString("url"), rs.getString("author"), df.format(rs.getDate("date")), rs.getString("content"), false, true));
					}
					else if(location.equals("liked") == true)
					{
						articles.add(new Article(rs.getInt("id"), rs.getString("title"), rs.getString("url"), rs.getString("author"), df.format(rs.getDate("date")), rs.getString("content"), true, false));
					}
					else if(location.equals("all") == true)
					{
						articles.add(new Article(rs.getInt("id"), rs.getString("title"), rs.getString("url"), rs.getString("author"), df.format(rs.getDate("date")), rs.getString("content"), rs.getInt("liked") > 0, rs.getInt("unread") > 0));
					}
				}

				if(articles.size() > 0)
				{
					//articles = "<span style='display: block; padding-top: 15px; text-align: center; font-size: 18px;'>There are no unread articles.</span>";
				}

				ps.close();
			}

			conn.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		return articles;
	}

	public List<Article> getFeatured() {
		Connection conn = null;
		PreparedStatement ps = null;

		List<Article> featured = new ArrayList<Article>();

		try
		{
			conn = ds.getConnection();

			ps = conn.prepareStatement("SELECT a.id, a.title, a.url, a.author, a.date, a.content FROM Liked l JOIN Articles a ON l.article_id = a.id GROUP BY l.article_id ORDER BY COUNT(l.article_id) DESC");

			ResultSet rs = ps.executeQuery();
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

			while(rs.next() == true)
			{
				// number of likes
				featured.add(new Article(rs.getInt("id"), rs.getString("title"), rs.getString("url"), rs.getString("author"), df.format(rs.getDate("date")), rs.getString("content"), false, false));
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

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public String getFeedName()
	{
		return feedName;
	}

	public void setFeedName(String feedName)
	{
		this.feedName = feedName;
	}
}
