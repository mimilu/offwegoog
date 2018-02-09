<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
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
<h1>Admin View</h1>
<form method="POST" action="/admin">
    <div>
        <label for="title">Delete the table with the matching name</label>
        <input type="text" name="admin_deleteTableName" id="title" size="40" class="form-control" />
        <p />
    </div>
    <INPUT type="submit" name="submit" Value="Administrate!">
</form>
</div>
<div id="footer">Created by mimichen@ - 2018</div>
</html>
