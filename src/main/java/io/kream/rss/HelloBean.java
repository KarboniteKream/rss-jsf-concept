package io.kream.rss;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

@ManagedBean(name = "helloBean")
@SessionScoped
public class HelloBean {
	private String email;
	private String password;
	
	public String getEmail()
	{
		return email;
	}
	
	public String getPassword()
	{
		return password;
	}
	
	public void setEmail(final String email)
	{
		this.email = email;
	}
	
	public void setPassword(final String password)
	{
		this.password = password;
	}
}
