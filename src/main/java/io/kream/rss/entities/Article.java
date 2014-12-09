package io.kream.rss.entities;

public class Article
{
	private int id;
	private String title;
	private String url;
	private String author;
	private String date;
	private String content;
	
	private boolean liked;
	private boolean unread;

	public Article(int id, String title, String url, String author, String date, String content, boolean liked, boolean unread)
	{
		this.setId(id);
		this.setTitle(title);
		this.setUrl(url);
		this.setAuthor(author);
		this.setDate(date);
		this.setContent(content);
		
		this.liked = liked;
		this.unread = unread;
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public String getAuthor()
	{
		return author;
	}

	public void setAuthor(String author)
	{
		this.author = author;
	}

	public String getDate()
	{
		return date;
	}

	public void setDate(String date)
	{
		this.date = date;
	}

	public String getContent()
	{
		return content;
	}

	public void setContent(String content)
	{
		this.content = content;
	}

	public boolean isLiked()
	{
		return liked;
	}

	public void setLiked(boolean liked)
	{
		this.liked = liked;
	}

	public boolean isUnread()
	{
		return unread;
	}

	public void setUnread(boolean unread)
	{
		this.unread = unread;
	}
}
