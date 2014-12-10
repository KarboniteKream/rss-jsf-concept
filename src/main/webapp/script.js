$(document).ready(function()
{
	$("#overlay").click(hideOverlay);

	$("#overlay").hide();
	$("#add-subscription").hide();
	$(".popup").hide();
	//$("#email-registered").hide();

	// OK
	$("article a").attr("target", "_blank");
	setSortable();
	// OK

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

	// remove 'remove' from index.html
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
		$(this).parent().parent().toggleClass("liked");
		($(this).text() == "Like") ? $(this).text("Unlike") : $(this).text("Like");
		$(this).parent().parent().removeClass("unread");
		$(this).next().text("Mark as unread");
	});

	$(".action-bar span:contains('read')").click(function()
	{
		alert("OK");
		markAsRead($(this));
	});

	$(".open-popup").click(function()
	{
		$("#overlay").fadeIn("fast");
		$($(this).attr("target-popup")).fadeIn("fast");
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

	$("span:contains('Refresh')").click(function(){});

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

function changeEmail()
{
	$.ajax
	({
		url: "/util.php?function=change-email",
		type: "POST",
		data: { "email": $("#change-email input[name='email']").val() }
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
	$(".sortable").sortable
	({
		connectWith: ".connected"
	});
}
