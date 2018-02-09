package com.google.offwegoog;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@WebServlet(name = "CreatePoll", value = "/createPoll")
public class CreatePoll extends HttpServlet {
  Connection conn; // Cloud SQL connection

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {
    // Retrieve blog posts from Cloud SQL database and display them

    try (ResultSet rs = conn.prepareStatement(SqlCommands.SELECT_ALL_IDEAS).executeQuery()) {
      Map<Integer, Map<String, String>> storedIdeas = new HashMap<>();

      while (rs.next()) {
        Map<String, String> ideaContents = new HashMap<>();

        // Store the particulars for a blog in a map
        ideaContents.put("title", rs.getString("ideas.title"));
        ideaContents.put("description", rs.getString("ideas.description"));
        ideaContents.put("timestamp", rs.getString("ideas.timestamp"));

        // Store the post in a map with key of the ideaId
        storedIdeas.put(rs.getInt("ideas.idea_id"), ideaContents);
      }

      req.setAttribute("ideas", storedIdeas);
      req.getRequestDispatcher("createPoll.jsp").forward(req, resp);
    } catch (SQLException e) {
      throw new ServletException("SQL error", e);
    }
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {
    // Create a map of the httpParameters that we want and run it through jSoup
    Map<String, String> offsitePoll =
      req.getParameterMap()
        .entrySet()
        .stream()
        .filter(a -> a.getKey().startsWith("offsitePoll_"))
        .collect(
          Collectors.toMap(
            p -> p.getKey(), p -> Jsoup.clean(p.getValue()[0], Whitelist.basic())));

    // Build the SQL command to insert the poll into the database
    try (PreparedStatement statementCreatePoll = conn.prepareStatement(SqlCommands.CREATE_POLL)) {
      statementCreatePoll.setTimestamp(1, null); // Null id generates the next id automatically.
      statementCreatePoll.setTimestamp(2, new Timestamp(new Date().getTime()));
      UserService userService = UserServiceFactory.getUserService();
      String userId = userService.getCurrentUser().getUserId();
      statementCreatePoll.setString(3, userId);
      statementCreatePoll.setString(4, offsitePoll.get("offsitePoll_title"));
      statementCreatePoll.setString(5, offsitePoll.get("offsitePoll_description"));
      statementCreatePoll.executeUpdate();

      PreparedStatement nextPollId = conn.prepareStatement("SELECT LAST_INSERT_ID()");
      ResultSet idSet = nextPollId.executeQuery();
      idSet.next();
      Integer pollId = idSet.getInt(1);

      PreparedStatement statementInsertPolledIdeas = conn.prepareStatement(SqlCommands.INSERT_POLLED_IDEAS);
      String[] ideaIds = req.getParameterValues("offsitePoll_ideaIds");
      for(int i = 0; i < ideaIds.length; i++) {
        statementInsertPolledIdeas.setInt(1, pollId); // Null id generates the next id automatically.
        String decodedString = new String(Base64.getUrlDecoder().decode(ideaIds[i])); // Decode the websafe ID.
        Integer decodedId = Integer.parseInt(decodedString);
        statementInsertPolledIdeas.setInt(2, decodedId);
        statementInsertPolledIdeas.executeUpdate();
      }

      // Send the user to the confirmation page with personalised confirmation text
      String confirmation = "Poll " + offsitePoll.get("offsitePoll_title") + " created.";

      req.setAttribute("confirmation", confirmation);
      String encodedId = Base64.getUrlEncoder().encodeToString(String.valueOf(pollId).getBytes());
      req.setAttribute("destination_url", "/viewPoll?id=" + encodedId);
      req.getRequestDispatcher("/confirm.jsp").forward(req, resp);

    } catch (SQLException e) {
      throw new ServletException("SQL error when creating idea", e);
    }
  }

  @Override
  public void init() throws ServletException {
    try {
      String url = System.getProperty("cloudsql");

      try {
        conn = DriverManager.getConnection(url);

        // Create the tables so that the SELECT query doesn't throw an exception
        // if the user visits the page before any posts have been added

        conn.createStatement().executeUpdate(SqlCommands.CREATE_IDEA_TABLE); // create idea table
        conn.createStatement().executeUpdate(SqlCommands.CREATE_POLL_TABLE); // create poll table
        // Create poll idea relationship table.
        conn.createStatement().executeUpdate(SqlCommands.CREATE_POLLED_IDEAS_TABLE);
      } catch (SQLException e) {
        throw new ServletException("Unable to connect to SQL server", e);
      }

    } finally {
      // Nothing really to do here.
    }
  }
}