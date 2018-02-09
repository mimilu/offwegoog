<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.Base64" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
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
<h1><c:out value="${title}"/></h1>
<c:out value="${description}"/>
<p/>
<form method="POST" action="/vote?id=<%= request.getParameter("id") %>">
    <%
        boolean isOpen = request.getAttribute("is_open").toString().contentEquals("yes");
        UserService userService = UserServiceFactory.getUserService();
        String userId = userService.getCurrentUser().getUserId();
        boolean isOwner = request.getAttribute("owner_id").toString().contentEquals(userId);
    %>
    <table>
        <tr>
            <% if (isOpen) { %><th>Your Vote</th><% } else { %><th>Vote Count</th><% } %>
            <% if (isOwner && isOpen) { %><th>Vote Count</th><% } %>
            <th>Offsite Idea</th>
        </tr>
        <c:forEach var="idea" items="${ideas}">
            <%
                // Grab the key and convert it into a string in preparation for encoding
                Map.Entry<Integer, Map<String, String>> idea = (Map.Entry<Integer, Map<String, String>>) pageContext.getAttribute("idea");
                Integer keyInt = idea.getKey();

                // Encode the entity's key with Base64
                String encodedID = Base64.getUrlEncoder().encodeToString(String.valueOf(keyInt).getBytes());
                boolean userVoted = idea.getValue().get("user_voted").contentEquals("yes");

            %>
            <tr>
                <td>
                    <%
                        if (isOpen) {
                    %>
                    <input type="checkbox" name="offsitePoll_ideaIds" <%
    if (userVoted) {
        out.print("checked=\"checked\"");
    } %> VALUE=<%= encodedID %>>
                    <% } else { %>
                    <c:out value="${idea.value.get('vote_count')}"/>
                    <% } %>
                </td>
                <% if (isOwner && isOpen) { %>
                <td>
                    <c:out value="${idea.value.get('vote_count')}"/>
                </td>
                <% } %>
                <td>
                    <h3><c:out value="${idea.value.get('title')}"/></h3>
                    <c:out value="${idea.value.get('description')}"/>
                    </br />
                </td>
            </tr>
        </c:forEach>
    </table>
    <p/>
    <% if (isOpen) { %>
    <INPUT type="submit" name="submit" Value="Submit Vote">
</form>

<form method="POST" action="/sharePoll?id=<%= request.getParameter("id") %>">

    Email this poll to others by entering email addresses separated by ';' characters:<br/>
    <textarea name="offsitePoll_recipients" id="description" rows="5" cols="50" class="form-control"></textarea>
    <br/>
    <input type="hidden" name="offsitePoll_title" value="${title}">
    <input type="hidden" name="offsitePoll_title" value="${description}">
    <INPUT type="submit" name="submit" Value="Share Poll">
    <% } %>
</form>

<% if (isOwner) {
    if (isOpen) { %>
<h3><a href="/closePoll?id=<%= request.getParameter("id") %>">Close this poll</a> to disallow further votes.</h3>
<% } else { %>
<h3><a href="/openPoll?id=<%= request.getParameter("id") %>">Reopen this poll</a> to get more votes.</h3>
<% }
} %>
</div>
<div id="footer">Created by mimichen@ - 2018</div>
</body>
</html>
