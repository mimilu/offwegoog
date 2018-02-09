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
    <h1 align="center">Create a Poll</h1>
</div>
<div id="centerBody">
<form method="POST" action="/createPoll">
<div>
    <label for="title"><b>Poll Title:</b></label>
    <input type="text" name="offsitePoll_title" id="title" size="40" value="Choosing Our Next Offsite" class="form-control" />
    <p />
</div>

<div>
    <label for="description"><b>Add more detailed instructions or a description:</b></label>
    <br />
    <textarea name="offsitePoll_description" id="description" rows="10" cols="50" class="form-control">Vote for what you want to do - multiple votes are allowed.</textarea>
</div>
    <p><b>Select your favorite offsite ideas from the database:</b><p>
<table>
    <tr>
        <th>Include in Poll</th>
        <th>Offsite Idea</th>
    </tr>
    <c:forEach var="idea" items="${ideas}">
        <tr>
            <td>
                <%
                    // Grab the key and convert it into a string in preparation for encoding
                    Integer keyInt = ((Map.Entry<Integer, Map<String, String>>)pageContext.getAttribute("idea")).getKey();

                    // Encode the entity's key with Base64
                    String encodedID = Base64.getUrlEncoder().encodeToString(String.valueOf(keyInt).getBytes());
                %>
                <input type="checkbox" name="offsitePoll_ideaIds" VALUE=<%= encodedID %>>
            </td>
            <td>
                <h3><c:out value="${idea.value.get('title')}" /></h3>
                <c:out value="${idea.value.get('description')}" />
                </br />
            </td>
        </tr>
    </c:forEach>
</table>
    <br />
    <INPUT type="submit" name="submit" Value="Create Poll">
</form>
</div>
<div id="footer">Created by mimichen@ - 2018</div>
</body>
</html>
