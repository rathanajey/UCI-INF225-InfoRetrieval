<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>ICS Search Engine :: Results</title>
<link
	href="https://netdna.bootstrapcdn.com/bootstrap/3.0.0/css/bootstrap.min.css"
	rel="stylesheet">

<!-- /container -->
<script
	src="https://ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js"
	type="text/javascript">
	
</script>
<script
	src="https://netdna.bootstrapcdn.com/bootstrap/3.0.0/js/bootstrap.min.js"
	type="text/javascript">
	
</script>

<style type="text/css">
#searchResultsRow {
	min-height: 80%;
}

#searchResults {
	margin: 0 auto;
}

#tfheader {
	
}

#tfnewsearch {
	float: left;
	width: 1400px;
}

.li {
	font-size: 16px;
	padding: 5px 5px;
}

.tftextinput {
	margin: 0;
	padding: 5px 15px;
	font-family: Arial, Helvetica, sans-serif;
	font-size: 14px;
	border: 1px solid #0076a3;
	border-right: 0px;
	border-top-left-radius: 5px 5px;
	border-bottom-left-radius: 5px 5px;
}

.tfbutton {
	margin: 0;
	padding: 5px 15px;
	font-family: Arial, Helvetica, sans-serif;
	font-size: 14px;
	outline: none;
	cursor: pointer;
	text-align: center;
	text-decoration: none;
	color: #ffffff;
	border: solid 1px #0076a3;
	border-right: 0px;
	background: #0095cd;
	background: -webkit-gradient(linear, left top, left bottom, from(#00adee),
		to(#0078a5));
	background: -moz-linear-gradient(top, #00adee, #0078a5);
	border-top-right-radius: 5px 5px;
	border-bottom-right-radius: 5px 5px;
}

.tfbutton:hover {
	text-decoration: none;
	background: #007ead;
	background: -webkit-gradient(linear, left top, left bottom, from(#0095cc),
		to(#00678e));
	background: -moz-linear-gradient(top, #0095cc, #00678e);
}
/* Fixes submit button height problem in Firefox */
.tfbutton::-moz-focus-inner {
	border: 0;
}

.tfclear {
	clear: both;
}
</style>
</head>
<body>
	<div class="container">
		<div class="container-fluid">
			<div class="row">


				<!-- HTML for SEARCH BAR -->
				<div id="tfheader">
					<form id="tfnewsearch" class="form-inline" method="get"
						action="search">

						<h1 style="text-align: left">
							<a href="index.html">ICS Search Engine </a>
						</h1>
						<input type="text" class="tftextinput" name="q" size="65"
							maxlength="120" value="${query} " /><input type="submit"
							value="search" class="tfbutton">
					</form>
					<div class="tfclear"></div>
				</div>
				<hr>
			</div>

			<!-- This is where all of our search results will be placed -->
			<div class="row" id="searchResultsRow">
				<ol type="1" start="${listStart}" id="searchResults">
					<c:forEach var="url" items="${listData}">
						<li><a href="${url}">${url}</a></li>
					</c:forEach>
				</ol>
				<br>
				<hr>
			</div>

			<!-- The rest of this is just pagination on the bottom -->
			<div class="row">

				<%-- Display next link except for first page --%>
				<c:if test="${currentPage != 1}">
					<td><a href="search?page=${currentPage - 1}">Previous</a></td>
				</c:if>

				<%--Display page numbers, no link for current page.--%>
				<table border="1" cellpadding="5" cellspacing="5">
					<tr>
						<c:forEach begin="1" end="${numOfPages}" var="i">
							<c:if test="${(i mod 30) eq 1 and i > 1}">
					</tr>
					<tr>
						</c:if>
						<c:choose>
							<c:when test="${currentPage eq i}">
								<td>${i}</td>
							</c:when>
							<c:otherwise>
								<td><a href="search?page=${i}">${i}</a></td>
							</c:otherwise>
						</c:choose>
						</c:forEach>
					</tr>
				</table>

				<%--For displaying Next link --%>
				<c:if test="${currentPage lt numOfPages}">
					<td><a href="search?page=${currentPage + 1}">Next</a></td>
				</c:if>
				<br>
				<hr>


			</div>
		</div>
	</div>

</body>
</html>