$(document).ready(function()
{
	$("#overlay").click(hideOverlay);

	$("#overlay").hide();
	$("#add-subscription").hide();
	$(".popup").hide();
	$("#email-registered").hide();

	$("article a").attr("target", "_blank");
	setSortable();

	loadSidebar();
	//loadFeed();
	loadFeatured();

	$("input").on("input", function()
	{
		if($(this).val() != "")
		{
			$(this).removeClass("input-empty");
		}
	});

	$("#new-email").blur(function()
	{
		if(validateEmail($(this).val()) == true)
		{
			var input = $(this);

			$.ajax
			({
				url: "/util.php?function=check-email",
				type: "POST",
				data: { "email": $(this).val() },
				success: function(data)
				{
					if(data == "OK")
					{
						$("#email-registered").fadeOut();
					}
					else
					{
						$("#email-registered").fadeIn();
					}
				}
			});
		}
	});

	$(".action-bar").append('<span class="remove-article">Remove</span>');

	$(".action-bar").on("click", ".remove-article", function()
	{
		$(this).parent().parent().slideUp(function()
		{
			$(this).remove();
		});
	});

	$("button[type='submit']").click(function()
	{
		$(this).prevAll("input").filter(function()
		{
			return !this.value;
		}).addClass("input-empty");

		$(this).prevAll("input").filter(function()
		{
			return this.value;
		}).removeClass("input-empty");

		if($(this).prevAll().hasClass("input-error") || $(this).prevAll().hasClass("input-empty") || $("#email-registered").is(":visible"))
		{
			event.preventDefault();
		}
	});

	$("#new-subscription").click(function()
	{
		$("#add-subscription").fadeIn("fast");
	});

	$("#add-subscription button").click(function()
	{
		$("#add-subscription").fadeOut("fast");

		$.ajax
		({
			url: "/util.php?function=add-subscription",
			type: "POST",
			data: { "url": $("#add-subscription input").val() },
			success: function(data)
			{
				$("#add-subscription input").val("");
				setSortable();
				loadSidebar();
				loadFeed();
			}
		});
	});

	$(".action-bar span:contains('ike')").click(function()
	{
		like($(this));
	});

	$(".action-bar span:contains('read')").click(function()
	{
		markAsRead($(this));
	});

	$(".open-popup").click(function()
	{
		$("#overlay").fadeIn("fast");
		$($(this).attr("target-popup")).fadeIn("fast");
		$("#form-question").text($("#form-question").text().replace("FEED", $("#feed-name").text()));
	});

	$("input[type='email']").blur(function()
	{
		if(validateEmail($(this).val()) == false)
		{
			$(this).addClass("input-error");
		}
	});

	$("input[type='email']").on("input", function()
	{
		if(validateEmail($(this).val()) == true)
		{
			$(this).removeClass("input-error");
		}
	});

	$("span:contains('Refresh')").click(function()
	{
		loadFeed();
	});

	$(".confirm-password, .confirm-email").blur(function()
	{
		if($(this).prev().prev().val() != $(this).val())
		{
			$(this).prev().prev().addClass("input-error");
			$(this).addClass("input-error");
		}
		else
		{
			$(this).prev().prev().removeClass("input-error");
			$(this).removeClass("input-error");
		}
	});

	$(".confirm-password, .confirm-email").on("input", function()
	{
		if($(this).prev().prev().val() == $(this).val())
		{
			$(this).prev().prev().removeClass("input-error");
			$(this).removeClass("input-error");
		}
	});

	$("#fullscreen").click(function()
	{
		$("#landing").toggleClass("fullscreen-absolute");
		($(this).text() == "v") ? $(this).text("u") : $(this).text("v");
	});
});

function like(element)
{
	// TODO: Check status in database.
	$.ajax
	({
		url: "/util.php?function=like",
		type: "POST",
		data: { "article_id": element.parent().parent().attr("id"), "liked": element.parent().parent().hasClass("liked") ? "true" : "false" },
		success: function(data)
		{
			element.parent().parent().toggleClass("liked");
			(element.text() == "Like") ? element.text("Unlike") : element.text("Like");
			element.parent().parent().removeClass("unread");
			element.next().text("Mark as unread");
			loadSidebar();
		}
	});
}

function markAsRead(element)
{
	$.ajax
	({
		url: "/util.php?function=mark-as-read",
		type: "POST",
		data: { "article_id": element.parent().parent().attr("id"), "unread": element.parent().parent().hasClass("unread") ? "true" : "false" },
		success: function(data)
		{
			element.parent().parent().toggleClass("unread");
			(element.text() == "Mark as read") ? element.text("Mark as unread") : element.text("Mark as read");
			loadSidebar();
		}
	});
}

function unsubscribe()
{
	$.ajax
	({
		url: "/util.php?function=unsubscribe",
		type: "POST",
		success: function()
		{
			hideOverlay();
			location.reload();
		}
	});
}

function loadSidebar()
{
	$.ajax
	({
		url: "util.php?function=count-unread",
		type: "GET",
		success: function(data)
		{
			$("#sidebar-content").empty();
			$("#sidebar-content").append
			(
				$("<div>").attr("id", "menu").append
				(
					$("<ul>").append
					(
						$("<li>").append
						(
							$("<a>").attr({ "id": "home", "feed": "home", "href": "javascript:;" }).text("Home")
						),
						$("<li>").append
						(
							$("<a>").attr({ "id": "unread", "feed": "unread", "href": "javascript:;" }).text("Unread"),
							$("<span>").addClass("badge").text(data != "0" ? " " + data : "")
						),
						$("<li>").append
						(
							$("<a>").attr({ "id": "liked", "feed": "liked", "href": "javascript:;" }).text("Liked")
						),
						$("<li>").append
						(
							$("<a>").attr({ "id": "all", "feed": "all", "href": "javascript:;" }).text("All articles")
						)
					)
				),
				$("<div>").attr("id", "subscriptions")
			);

			$.ajax
			({
				url: "util.php?function=load-sidebar",
				type: "GET",
				success: function(data)
				{
					$("#subscriptions").html(data);
					setSortable();

					$(".sortable").sortable().bind("sortupdate", function(e, ui)
					{
						var ui = ui;
						$.ajax
						({
							url: "/util.php?function=set-folder",
							type: "POST",
							data: { "feed_id": ui.item.children().first().attr("feed"), "folder": ui.endparent.prev().text() },
							success: function()
							{
								loadSidebar();
							}
						});
					});

					$("#subscriptions li a, #menu li a").click(function()
					{
						var link = $(this);

						$.ajax
						({
							url: "/util.php?function=open-feed",
							type: "GET",
							data: { "feed":  link.attr("feed") },
							success: function(data)
							{
								loadSidebar();
								loadFeed();
							}
						});
					});
				}
			});
		}
	});
}

function changeEmail()
{
	$.ajax
	({
		url: "/util.php?function=change-email",
		type: "POST",
		data: { "email": $("#change-email input[name='email']").val() }
	});
}

function loadFeatured()
{
	$.ajax
	({
		url: "/util.php?function=load-featured",
		type: "GET",
		success: function(data)
		{
			$("#featured").empty();

			$.parseJSON(data).forEach(function(article)
			{
				$("#featured").append
				(
					$("<article>").attr("id", article.id).append
					(
						$("<div>").addClass("date").text(article.date),
						$("<h2>").append
						(
							$("<a>").attr("href", article.url).text(article.title)
						),
						$("<div>").addClass("content").html("<p>" + article.content + "</p>"),
						$("<div>").addClass("action-bar").append
						(
							$("<span>").text("Like").click(function()
							{
								like($(this));
							})
						)
					)
				);

				$("#reader article:last-child h2").after(function()
				{
					if(article.author != null)
					{
						return $("<div>").addClass("author").html("by <b>" + article.author + "</b>");
					}
				});
			});

			$(".action-bar").append('<span class="remove-article">Remove</span>');
			$("article a").attr("target", "_blank");
		}
	});
}

function loadFeed()
{
	$.ajax
	({
		url: "util.php?function=feed-name",
		type: "GET",
		success: function(data)
		{
			var feedName = data;

			$.ajax
			({
				url: "/util.php?function=load-feed",
				type: "GET",
				success: function(data)
				{
					$(".open-popup").click(function()
					{
						$("#overlay").fadeIn("fast");
						$($(this).attr("target-popup")).fadeIn("fast");
						$("#form-question").text($("#form-question").text().replace("FEED", $("#feed-name").text()));
					});

					$("#reader").empty();
					$("#feed-name").text(feedName);

					if(data != "[]")
					{
						$.parseJSON(data).forEach(function(article)
						{
							$("#reader").append
							(
								$("<article>").attr("id", article.id).addClass(article.status[0]).addClass(article.status[1]).append
								(
									$("<div>").addClass("date").text(article.date),
									$("<h2>").append
									(
										$("<a>").attr("href", article.url).text(article.title)
									),
									$("<div>").addClass("content").html("<p>" + article.content + "</p>"),
									$("<div>").addClass("action-bar").append
									(
										$("<span>").text(article.status[0] == "liked" ? "Unlike" : "Like").click(function()
										{
											like($(this));
										}),
										$("<span>").text(article.status[1] == "unread" ? "Mark as read" : "Mark as unread").click(function()
										{
											markAsRead($(this));
										})
									)
								)
							);

							$("#reader article:last-child h2").after(function()
							{
								if(article.author != null)
								{
									return $("<div>").addClass("author").html("by <b>" + article.author + "</b>");
								}
							});
						});

						$("article a").attr("target", "_blank");
					}
					else
					{
						$("#reader").append
						(
							$("<span>").css({ "display": "block", "padding-top": "15px", "text-align": "center", "font-size": "18px" }).text("There are no unread articles.")
						);
					}
				}
			});
		}
	});

}

function sign_in()
{
	$.ajax
	({
		url: "/util.php?function=sign-in",
		type: "POST",
		data: { "email": $("#sign-in input[name='email']").val(), "password": $("#sign-in input[name='password']").val(), "remember_me": $("#remember-me").is(":checked") == true ? "true" : "false" },
		success: function(data)
		{
			if(data == "OK")
			{
				location.reload();
			}
			else
			{
				// TODO: Incorrect password.
			}
		}
	});
}

function register()
{
	$.ajax
	({
		url: "/util.php?function=register",
		type: "POST",
		data: { "real_name": $("#register input[name='real-name']").val(), "email": $("#register input[name='email']").val(), "password": $("#register input[name='password']").val() },
		success: function(data)
		{
			if(data == "OK")
			{
				open("home.php", "_self");
			}
		}
	});
}

function validateEmail(email)
{
	var regex = /^[_a-z0-9-]+(\.[_a-z0-9-]+)*@[a-z0-9-]+(\.[a-z0-9-]+)*(\.[a-z]{2,4})$/;

	if(regex.test(email) == true)
	{
		return true;
	}
	else
	{
		return false;
	}
}

function hideOverlay()
{
	$("#overlay").fadeOut("fast");
	$(".popup").fadeOut("fast");
}

function setSortable()
{
	$(".sortable").sortable(
	{
		connectWith: ".connected"
	});
}
