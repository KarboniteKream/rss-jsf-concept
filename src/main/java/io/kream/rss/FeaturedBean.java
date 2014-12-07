package io.kream.rss;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

@ManagedBean(name = "featuredBean")
@SessionScoped
public class FeaturedBean
{
	private String HTML;
	
	// @Resource(name = "jdbc/database")
	DataSource ds;
	
	public FeaturedBean()
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
		
		HTML = "";
	}
	
	public String getHTML()
	{
		Connection conn = null;
		PreparedStatement ps = null;
		
		try
		{
			conn = ds.getConnection();
			
			String sql = "SELECT a.id, a.title, a.url, a.author, a.date, a.content FROM Liked l JOIN Articles a ON l.article_id = a.id GROUP BY l.article_id ORDER BY COUNT(l.article_id) DESC";
			ps = conn.prepareStatement(sql);
			
			ResultSet rs = ps.executeQuery();
			
			while(rs.next() == true)
			{
				// author
				HTML += String.format("<article id='%d'><div class='date'>%s</div><h2><a href='%s'>%s</a></h2>",
						rs.getInt("id"), rs.getString("date"), rs.getString("url"), rs.getString("title")); 
				HTML += String.format("<div class='content'><p>%s</p></div><div class='action-bar'><span>Like</span</div></article>",
						rs.getString("content"));
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return HTML;
	}
}
