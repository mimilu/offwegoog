<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.Base64" %>
<%@ page import="java.util.Map" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
    <h1>Offsite Ideas Database</h1>
    Check out these ideas submitted by others. Feel free to contribute to existing ideas or <a href='/createIdea'>add your own</a>.
    <p />
    Need help deciding? <a href='/createPoll'>Create a poll</a>.<br />
    Check your <a href='/viewPoll'>created polls here</a>.<br />
  </div>
<div id="centerBody">
  <table>
    <c:forEach var="idea" items="${ideas}">
      <tr>
        <td>
          <h3><c:out value="${idea.value.get('title')}" /></h3>
          <c:out value="${idea.value.get('description')}" />
          </br />
          <%
            // Grab the key and convert it into a string in preparation for encoding
            Integer keyInt = ((Map.Entry<Integer, Map<String, String>>)pageContext.getAttribute("idea")).getKey();

            // Encode the entity's key with Base64
            String encodedID = Base64.getUrlEncoder().encodeToString(String.valueOf(keyInt).getBytes());
            %>
          <a href="/deleteIdea?id=<%= encodedID %>">Delete Idea</a> - <a href="/editIdea?id=<%= encodedID %>">Edit Idea</a>
        </td>
      </tr>
    </c:forEach>
  </table>
  <h3>Want to do something that isn't on this list? <a href='/createIdea'>Add an idea!</a></h3>
</div>
<div id="footer">Created by mimichen@ - 2018</div>
</body>
</html>
