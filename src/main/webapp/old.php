<?php
	function checkEmail()
	{
		require("database.php");
		$statement = $conn->prepare("SELECT email FROM Users WHERE email = :email");
		$statement->bindParam(":email", $_POST["email"]);
		$statement->execute();

		if($statement->rowCount() != 0)
		{
			echo "ERR";
		}
		else
		{
			echo "OK";
		}

		$statement = null;
		$conn = null;
	}

	function signOut()
	{
		if(session_start() == true)
		{
			unset($_COOKIE["remember_me"]);
			setcookie("remember_me", NULL, -1);
			session_unset();
			$root = $_SERVER["HTTP_HOST"];
			header("Location: http://$root");
		}
	}

	function changeEmail()
	{
		require("database.php");

		$statement = $conn->prepare("UPDATE Users SET email = :email WHERE id = :id");
		$statement->bindParam(":email", $_POST["email"]);
		$statement->bindParam(":id", $_SESSION["user_id"]);
		$statement->execute();
	}

	function setFolder()
	{
		require("database.php");
		$folder = $_POST["folder"] == "" ? null : $_POST["folder"];

		$statement = $conn->prepare("UPDATE Subscriptions SET folder = :folder WHERE user_id = :user_id AND feed_id = :feed_id");
		$statement->bindParam(":folder", $folder);
		$statement->bindParam(":user_id", $_SESSION["user_id"]);
		$statement->bindParam(":feed_id", $_POST["feed_id"]);
		$statement->execute();

		$statement = null;
		$conn = null;
	}

	function like()
	{
		require("database.php");

		if($_POST["liked"] == "true")
		{
			$statement = $conn->prepare("DELETE FROM Liked WHERE user_id = :user_id AND article_id = :article_id");
		}
		else
		{
			$statement = $conn->prepare("INSERT INTO Liked (user_id, article_id) VALUES (:user_id, :article_id)");
		}

		$statement->bindParam(":user_id", $_SESSION["user_id"]);
		$statement->bindParam(":article_id", $_POST["article_id"]);
		$statement->execute();

		if($_POST["liked"] == "false")
		{
			$statement = $conn->prepare("DELETE FROM Unread WHERE user_id = :user_id AND article_id = :article_id");
			$statement->bindParam(":user_id", $_SESSION["user_id"]);
			$statement->bindParam(":article_id", $_POST["article_id"]);
			$statement->execute();
		}

		$statement = null;
		$conn = null;
	}

	function markAsRead()
	{
		require("database.php");

		if($_POST["unread"] == "true")
		{
			$statement = $conn->prepare("DELETE FROM Unread WHERE user_id = :user_id AND article_id = :article_id");
		}
		else
		{
			$statement = $conn->prepare("INSERT INTO Unread (user_id, article_id) VALUES (:user_id, :article_id)");
		}

		$statement->bindParam(":user_id", $_SESSION["user_id"]);
		$statement->bindParam(":article_id", $_POST["article_id"]);
		$statement->execute();

		$statement = null;
		$conn = null;
	}

	function deleteAccount()
	{
		require("database.php");

		$statement = $conn->prepare("DELETE FROM Liked WHERE user_id = :id");
		$statement->bindParam(":id", $_SESSION["user_id"]);
		$statement->execute();

		$statement = $conn->prepare("DELETE FROM Unread WHERE user_id = :id");
		$statement->bindParam(":id", $_SESSION["user_id"]);
		$statement->execute();

		$statement = $conn->prepare("DELETE FROM Subscriptions WHERE user_id = :id");
		$statement->bindParam(":id", $_SESSION["user_id"]);
		$statement->execute();

		$statement = $conn->prepare("DELETE FROM Users WHERE id = :id");
		$statement->bindParam(":id", $_SESSION["user_id"]);

		if($statement->execute() == true)
		{
			unset($_COOKIE["remember_me"]);
			setcookie("remember_me", NULL, -1);
			session_unset();
			$root = $_SERVER["HTTP_HOST"];
			header("Location: http://$root");
			$statement = null;
			$conn = null;
			exit();
		}
	}

	function addSubscription()
	{
		$url = $_POST["url"];
		$xml = simplexml_load_file($url);

		$url = parse_url($_POST["url"]);
		$url = $url["scheme"] . "://" . $url["host"];
		$icon = file_get_contents("http://www.google.com/s2/favicons?domain=" . $url);

		$name = $xml->channel->title;
		$articles = isset($xml->item) ? $xml->item : $xml->channel->item;

		require("database.php");
		$statement = $conn->prepare("INSERT INTO Feeds (name, icon) VALUES (:name, :icon)");
		$statement->bindParam(":name", $name);
		$statement->bindParam(":icon", $icon);
		$statement->execute();

		$feed_id = $conn->lastInsertId();
		$_SESSION["location"] = $feed_id;

		$statement_a = $conn->prepare("INSERT INTO Articles (feed_id, title, url, author, date, content) VALUES (:feed_id, :title, :url, :author, :date, :content)");

		$statement_u = $conn->prepare("INSERT INTO Unread (user_id, article_id) VALUES (:user_id, :article_id)");
		$statement_u->bindParam(":user_id", $_SESSION["user_id"]);

		foreach($articles as $article)
		{
			$dc = $article->children("http://purl.org/dc/elements/1.1/");

			if(isset($article->author) == true)
			{
				$author = $article->author;
			}
			else if(isset($dc->creator) == true)
			{
				$author = $dc->creator;
			}
			else
			{
				$author = NULL;
			}

			if(isset($article->pubDate) == true)
			{
				$date = $article->pubDate;
			}
			else if(isset($dc->date) == true)
			{
				$date = $dc->date;
			}
			else
			{
				$date = NULL;
			}

			$date = date("Y-m-d H:i:s", strtotime($date));

			$statement_a->bindParam(":feed_id", $feed_id);
			$statement_a->bindParam(":title", $article->title);
			$statement_a->bindParam(":url", $article->link);
			$statement_a->bindParam(":author", $author);
			$statement_a->bindParam(":date", $date);
			$statement_a->bindParam(":content", $article->description);
			$statement_a->execute();

			$article_id = $conn->lastInsertId();
			$statement_u->bindParam(":article_id", $article_id);
			$statement_u->execute();
		}

		$statement = $conn->prepare("INSERT INTO Subscriptions (user_id, feed_id) VALUES (:user_id, :feed_id)");
		$statement->bindParam(":user_id", $_SESSION["user_id"]);
		$statement->bindParam(":feed_id", $feed_id);
		$statement->execute();

		$statement = null;
		$conn = null;
	}
?>
