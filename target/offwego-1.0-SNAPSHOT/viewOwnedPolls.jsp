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
<h1>Owned Polls</h1>
These are the polls you've created.<p/>
<table>
    <c:forEach var="poll" items="${polls}">
        <tr>
            <td>
                <%
                    boolean isClosed = ((Map.Entry<Integer, Map<String, String>>) pageContext.getAttribute("poll"))
                            .getValue().get("is_open").isEmpty();
                    String pollStatus = isClosed ? "Closed for voting" : "Open for voting";

                    // Grab the key and convert it into a string in preparation for encoding
                    Integer keyInt = ((Map.Entry<Integer, Map<String, String>>) pageContext.getAttribute("poll")).getKey();

                    // Encode the entity's key with Base64
                    String encodedID = Base64.getUrlEncoder().encodeToString(String.valueOf(keyInt).getBytes());
                %><h3><a href="/viewPoll?id=<%= encodedID %>"><c:out value="${poll.value.get('title')}"/></a> - <c:out value="<%= pollStatus %>"/></h3>
                <c:out value="${poll.value.get('description')}"/>
                <br/>
                <a href="/closePoll?id=<%= encodedID %>">Close Poll</a> - <a href="/deletePoll?id=<%= encodedID %>">Delete Poll</a>
            </td>
        </tr>
    </c:forEach>
</table>
</div>
<div id="footer">Created by mimichen@ - 2018</div>
</body>
</html>
