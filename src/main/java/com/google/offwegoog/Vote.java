package com.google.offwegoog;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Base64;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@WebServlet(name = "Vote", value = "/vote")
public class Vote extends HttpServlet {
  Connection conn; // Cloud SQL connection

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {
    String encodedId = req.getParameter("id");
    if (encodedId.isEmpty()) {
      PrintWriter out = resp.getWriter();
      out.println("EMPTY ID!");
      return;
    }
    String decodedPollIdString = new String(Base64.getUrlDecoder().decode(encodedId)); // Decode the websafe ID.
    Integer pollId = Integer.parseInt(decodedPollIdString);

    // Build the SQL command to insert the poll into the database
    try (PreparedStatement deletePreviousVotes = conn.prepareStatement(SqlCommands.DELETE_POLL_VOTES_FOR_USER)) {
      conn.setAutoCommit(false);
      // Delete any old votes the user has cast for this.
      deletePreviousVotes.setInt(1, pollId);
      UserService userService = UserServiceFactory.getUserService();
      String userId = userService.getCurrentUser().getUserId();
      deletePreviousVotes.setString(2, userId);
      deletePreviousVotes.executeUpdate();

      PreparedStatement statementInsertPollIdeaVote = conn.prepareStatement(SqlCommands.INSERT_POLL_IDEA_VOTE);
      String[] ideaIds = req.getParameterValues("offsitePoll_ideaIds");
      for(int i = 0; i < ideaIds.length; i++) {
        statementInsertPollIdeaVote.setInt(1, pollId); // Null id generates the next id automatically.
        statementInsertPollIdeaVote.setString(2, userId);
        String decodedIdeaIdString = new String(Base64.getUrlDecoder().decode(ideaIds[i])); // Decode the websafe ID.
        Integer decodedId = Integer.parseInt(decodedIdeaIdString);
        statementInsertPollIdeaVote.setInt(3, decodedId);
        statementInsertPollIdeaVote.executeUpdate();
      }
      conn.setAutoCommit(true);

      // Send the user to the confirmation page with personalised confirmation text
      String confirmation = "Vote cast for " + ideaIds.length + " ideas!";

      req.setAttribute("confirmation", confirmation);
      req.setAttribute("destination_url", "/viewPoll?id=" + encodedId);
      req.getRequestDispatcher("/confirm.jsp").forward(req, resp);

    } catch (SQLException e) {
      throw new ServletException("SQL error when voting", e);
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
        conn.createStatement().executeUpdate(SqlCommands.CREATE_POLL_IDEA_VOTES_TABLE); // Create polled ideas table.
      } catch (SQLException e) {
        throw new ServletException("Unable to connect to SQL server", e);
      }

    } finally {
      // Nothing really to do here.
    }
  }
}