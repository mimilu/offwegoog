<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<html>
<head>
  <link href='/layout.css' rel='stylesheet' type='text/css'>
  <title>Off We Goog - Plan Your Next Offsite</title>
</head>
<body>
<div id="navbar"><a href="/">Ideas</a> - <a href="/viewPoll">Polls</a></div>
<div id="header">
  <h1>Off We Goog - Plan Your Next Offsite</h1>
    <h2>An Offsite Idea Database and Polling Service</h2>
</div>
<div id="centerColHeader">
  <h2>
    ${confirmation}. You will be redirected shortly.
    <% String destinationURL = "/";
      if (request.getAttribute("destination_url") != null) {
    destinationURL = request.getAttribute("destination_url").toString();
    } %>
    <meta http-equiv="Refresh" content="3;url=<%= destinationURL %>">
  </h2>
</div>
<div id="footer">Created by mimichen@ - 2018</div>
</body>
</html>