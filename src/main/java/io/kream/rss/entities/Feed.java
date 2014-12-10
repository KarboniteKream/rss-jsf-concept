package io.kream.rss.entities;

import java.sql.Blob;
import java.sql.SQLException;
import java.util.Base64;

public class Feed
{
	private int id;
	private String name;
	private String icon;
	private int unread;

	public Feed(int id, String name, Blob icon, int unread)
	{
		this.id = id;
		this.name = name;

		try
		{
			this.icon = Base64.getEncoder().encodeToString(icon.getBytes(1, (int)icon.length()));

			if(this.icon.equals("") == false)
			{
				this.icon = "background-image: url(data:image/png;base64," + this.icon + ");";
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}

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

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getIcon()
	{
		return icon;
	}

	public void setIcon(String icon)
	{
		this.icon = icon;
	}

	public int getUnread()
	{
		return unread;
	}

	public void setUnread(int unread)
	{
		this.unread = unread;
	}
}
