package io.kream.rss.beans;

import io.kream.rss.entities.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

@ManagedBean(name = "userBean")
@SessionScoped
public class UserBean
{
	private DataSource ds;

	private User user;
	private String confirmPassword;
	private boolean rememberMe;
	
	private String loginMessage;
	private String registerMessage;

	public UserBean()
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

		user = new User();
		rememberMe = false;
		
		loginMessage = "";
		registerMessage = "";
	}

	public User getUser()
	{
		return user;
	}

	public String getRealName()
	{
		return user.getRealName();
	}

	public void setRealName(String realName)
	{
		user.setRealName(realName);
	}

	public String getEmail()
	{
		return user.getEmail();
	}

	public void setEmail(String email)
	{
		user.setEmail(email);
	}

	public String getPassword()
	{
		return user.getPassword();
	}

	public void setPassword(String password)
	{
		user.setPassword(password);
	}

	public boolean isRememberMe()
	{
		return rememberMe;
	}

	public void setRememberMe(boolean rememberMe)
	{
		this.rememberMe = rememberMe;
	}

	public String signIn()
	{
		Connection conn = null;
		PreparedStatement ps = null;

		String response = null;

		try
		{
			conn = ds.getConnection();

			ps = conn.prepareStatement("SELECT id FROM Users WHERE email = ? AND password = SHA2(?, 512)");
			ps.setString(1, user.getEmail());
			ps.setString(2, user.getPassword());

			ResultSet rs = ps.executeQuery();

			if(rs.first() == true)
			{
//				if(rememberMe == true)
				{

				}

				user.setId(rs.getInt("id"));

				response = "home.xhtml?faces-redirect=true";
			}
			else
			{
				loginMessage = "Incorrect email/password.";
				response = "failure";
			}

			ps.close();
			conn.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		return response;
	}

	public String register()
	{
		Connection conn = null;
		PreparedStatement ps = null;
		
		if(user.getRealName().equals("") == true || user.getEmail().equals("") == true || user.getPassword().equals("") == true || user.getPassword().equals(confirmPassword) == false)
		{
			return "failure";
		}

		try
		{
			conn = ds.getConnection();

			ps = conn.prepareStatement("INSERT INTO Users (real_name, email, password) VALUES (?, ?, SHA2(?, 512))");
			ps.setString(1, user.getRealName());
			ps.setString(2, user.getEmail());
			ps.setString(3, user.getPassword());

			ps.executeUpdate();

			ps.close();
			conn.close();
		}
		catch(MySQLIntegrityConstraintViolationException e)
		{
			registerMessage = "This email is already registered.";
			return "failure";
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		return "home.xhtml?faces-redirect=true";
	}

	public String getRegisterMessage()
	{
		return registerMessage;
	}

	public void setRegisterMessage(String registerMessage)
	{
		this.registerMessage = registerMessage;
	}

	public String getConfirmPassword()
	{
		return confirmPassword;
	}

	public void setConfirmPassword(String confirmPassword)
	{
		this.confirmPassword = confirmPassword;
	}

	public String getLoginMessage()
	{
		return loginMessage;
	}

	public void setLoginMessage(String loginMessage)
	{
		this.loginMessage = loginMessage;
	}
}
