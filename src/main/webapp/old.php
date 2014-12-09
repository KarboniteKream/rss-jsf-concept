<?php
	session_start();

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

	function feedName()
	{
		if($_SESSION["location"] == "home")
		{
			echo "Home";
		}
		else if($_SESSION["location"] == "unread")
		{
			echo "Unread";
		}
		else if($_SESSION["location"] == "liked")
		{
			echo "Liked";
		}
		else if($_SESSION["location"] == "all")
		{
			echo "All articles";
		}
		else
		{
			require("database.php");
			$statement = $conn->prepare("SELECT name FROM Feeds WHERE id = :id");
			$statement->bindParam(":id", $_SESSION["location"]);
			$statement->execute();

			$feed = $statement->fetch(PDO::FETCH_OBJ);
			echo $feed->name;

			$statement = null;
			$conn = null;
		}
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

	function unsubscribe()
	{
		require("database.php");

		$statement = $conn->prepare("DELETE FROM Subscriptions WHERE user_id = :user_id AND feed_id = :feed_id");

		$statement->bindParam(":user_id", $_SESSION["user_id"]);
		$statement->bindParam(":feed_id", $_SESSION["location"]);
		$statement->execute();

		$_SESSION["location"] = "home";

		$statement = null;
		$conn = null;
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

	function countUnread()
	{
		require("database.php");

		$statement = $conn->prepare("SELECT COUNT(article_id) AS unread FROM Unread WHERE user_id = :user_id");
		$statement->bindParam(":user_id", $_SESSION["user_id"]);
		$statement->execute();
		$unread = $statement->fetch(PDO::FETCH_OBJ)->unread;

		echo $unread;

		$statement = null;
		$conn = null;
	}

	function loadFeed()
	{
		require("database.php");

		$status = array("", "");

		if($_SESSION["location"] == "unread")
		{
			$statement = $conn->prepare("SELECT a.id, a.title, a.url, a.author, a.date, a.content FROM Unread l JOIN Articles a ON l.article_id = a.id WHERE user_id = :user_id");
			$statement->bindParam(":user_id", $_SESSION["user_id"]);
			$statement->execute();
			$status[1] = "unread";
		}
		else if($_SESSION["location"] == "liked")
		{
			$statement = $conn->prepare("SELECT a.id, a.title, a.url, a.author, a.date, a.content FROM Liked l JOIN Articles a ON l.article_id = a.id WHERE user_id = :user_id");
			$statement->bindParam(":user_id", $_SESSION["user_id"]);
			$statement->execute();
			$status[0] = "liked";
		}
		else if($_SESSION["location"] == "all")
		{
			$statement = $conn->prepare("SELECT l.user_id AS liked, u.user_id AS unread, a.id, a.title, a.url, a.author, a.date, a.content FROM Subscriptions s JOIN Feeds f ON s.feed_id = f.id JOIN Articles a ON f.id = a.feed_id LEFT JOIN Liked l ON a.id = l.article_id LEFT JOIN Unread u ON a.id = u.article_id WHERE s.user_id = :user_id ORDER BY a.date DESC LIMIT 25");
			$statement->bindParam(":user_id", $_SESSION["user_id"]);
			$statement->execute();
		}
		else
		{
			$statement = $conn->prepare("SELECT a.id, a.title, a.url, a.author, a.date, a.content FROM Users u JOIN Unread ur ON u.id = ur.user_id JOIN Articles a ON ur.article_id = a.id JOIN Feeds f ON a.feed_id = f.id WHERE u.id = :user_id AND f.id = :id ORDER BY a.date DESC");
			$statement->bindParam(":user_id", $_SESSION["user_id"]);
			$statement->bindParam(":id", $_SESSION["location"]);
			$statement->execute();
			$status[1] = "unread";
		}

		$articles = $statement->fetchAll(PDO::FETCH_OBJ);
		$data = array();

		foreach($articles as $article)
		{
			if($_SESSION["location"] == "all")
			{
				$status[0] = $article->liked != NULL ? "liked" : "";
				$status[1] = $article->unread != NULL ? "unread" : "";
			}

			$data[] = array("status" => $status, "id" => $article->id, "title" => $article->title, "url" => $article->url, "author" => $article->author, "date" => date("Y-m-d", strtotime($article->date)), "content" => $article->content);
		}

		echo json_encode($data, JSON_UNESCAPED_UNICODE);

		$statement = null;
		$conn = null;
	}

	function loadSidebar()
	{
		require("database.php");
		// FIXME: user_id = 1?
		$statement = $conn->prepare("SELECT s.folder, f.id, f.name, f.icon, u.unread FROM Subscriptions s JOIN Feeds f ON s.feed_id = f.id LEFT JOIN (SELECT a.feed_id, COUNT(a.feed_id) AS unread FROM Unread JOIN Articles a ON article_id = a.id WHERE user_id = 1 GROUP BY feed_id) AS u ON f.id = u.feed_id WHERE s.user_id = :user_id ORDER BY s.folder, f.name");
		$statement->bindParam(":user_id", $_SESSION["user_id"]);
		$statement->execute();
		$feeds = $statement->fetchAll(PDO::FETCH_OBJ);

		if(count($feeds) > 0)
		{
			$idx = 0;

			echo "<ul class='connected sortable'>\n";
			while($feeds[$idx]->folder == NULL && $idx < count($feeds))
			{
				printf("<li><a feed='%s' href='javascript:;' class='%s'", $feeds[$idx]->id, $feeds[$idx]->id == $_SESSION['location'] ? 'active' : '');
				if($feeds[$idx]->icon != NULL)
				{
					$icon = base64_encode($feeds[$idx]->icon);
					printf("style='background-image: url(data:image/png;base64,%s)'", $icon);
				}
				printf(">%s</a> ", $feeds[$idx]->name);
				if($feeds[$idx]->unread != NULL)
				{
					printf("<span class='badge'>%s</span>", $feeds[$idx]->unread);
				}
				printf("</li>");
				$idx++;
			}
			echo "</ul>\n<ul>";

			$folder = NULL;
			for(; $idx < count($feeds); $idx++)
			{
				if($folder != $feeds[$idx]->folder)
				{
					if($folder != NULL)
					{
						echo "<li class='empty-li' />\n</ul>\n</li>\n";
					}

					$folder = $feeds[$idx]->folder;
					printf("<li class='folder'>\n<input type='checkbox' id='folder-toggle' />\n<label for='folder-toggle'>%s</label>\n<ul class='connected sortable'>\n", $feeds[$idx]->folder);
				}

				printf("<li><a feed='%s' href='javascript:;' class='%s'", $feeds[$idx]->id, $feeds[$idx]->id == $_SESSION['location'] ? 'active' : '');
				if($feeds[$idx]->icon != NULL)
				{
					$icon = base64_encode($feeds[$idx]->icon);
					printf("style='background-image: url(data:image/png;base64,%s)'", $icon);
				}
				printf(">%s</a> ", $feeds[$idx]->name);
				if($feeds[$idx]->unread != NULL)
				{
					printf("<span class='badge'>%s</span>", $feeds[$idx]->unread);
				}
				printf("</li>");
			}
			echo "<li class='empty-li' />\n</ul>\n</li>\n</ul>";
		}

		$statement = null;
		$conn = null;
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
