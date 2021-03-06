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
    <h1>
        Edit Idea
    </h1>

    <form method="POST" action="/editIdea">
        <div>
            <label for="title"><b>Idea Title</b></label>
            <input type="text" name="offsiteIdea_title" id="title" size="40" value="<c:out value="${title}" />" class="form-control" />
            <p />
        </div>

        <div>
            <label for="description"><b>Describe this idea in more detail</b></label>
            <br />
            <textarea name="offsiteIdea_description" id="description" rows="10" cols="50" class="form-control"><c:out value="${description}" /></textarea>
        </div>

        <input type="hidden" name="offsiteIdea_id" value="${id}">
        <br />
        <button type="submit">Submit Edits</button>
    </form>
</div>
<div id="footer">Created by mimichen@ - 2018</div>
</body>
</html>