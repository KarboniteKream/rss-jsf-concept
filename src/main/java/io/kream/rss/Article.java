package io.kream.rss;

public class Article
{
	private int id;
	private String title;
	private String url;
	private String author;
	private String date;
	private String content;
	
	public Article(int id, String title, String url, String author, String date, String content)
	{
		this.setId(id);
		this.setTitle(title);
		this.setUrl(url);
		this.setAuthor(author);
		this.setDate(date);
		this.setContent(content);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}
