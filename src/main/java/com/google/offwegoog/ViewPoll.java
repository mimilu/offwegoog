package com.google.offwegoog;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Map;
import java.util.HashMap;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@WebServlet(name = "ViewPoll", value = "/viewPoll")
public class ViewPoll extends HttpServlet {
  Connection conn; // Cloud SQL connection

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws IOException, ServletException {
    // Retrieve blog posts from Cloud SQL database and display them

    // If the voteId parameter is set, display a single poll.
    String encodedId = req.getParameter("id");
    if (encodedId != null && !encodedId.isEmpty()) {
      String decodedString = new String(Base64.getUrlDecoder().decode(encodedId)); // Decode the websafe ID.
      Integer decodedId = Integer.parseInt(decodedString);

      showPoll(req, resp, decodedId);
      return;
    }

    showOwnedPolls(req, resp);
  }

  void showPoll(HttpServletRequest req, HttpServletResponse resp, Integer pollId) throws IOException, ServletException {
    try (PreparedStatement selectPoll = conn.prepareStatement(SqlCommands.SELECT_POLL)) {
      selectPoll.setInt(1, pollId);
      ResultSet rs = selectPoll.executeQuery();
      Map<Integer, Map<String, String>> polls = new HashMap<>();

      rs.next();

      // Add the details as request attributes.
      req.setAttribute("poll_id", req.getParameter("poll_id"));
      req.setAttribute("title", rs.getString("polls.title"));
      req.setAttribute("owner_id", rs.getString("polls.owner_id"));
      req.setAttribute("timestamp", rs.getString("polls.timestamp"));
      req.setAttribute("description", rs.getString("polls.description"));
      req.setAttribute("is_open", rs.getBoolean("polls.is_open") ? "yes" : "");

      PreparedStatement selectPolledIdeas = conn.prepareStatement(SqlCommands.SELECT_POLLED_IDEAS);
      selectPolledIdeas.setInt(1, pollId);
      ResultSet polledIdeaIds = selectPolledIdeas.executeQuery();

      ArrayList<Integer> ideaIds = new ArrayList<Integer>();
      while(polledIdeaIds.next()) {
        ideaIds.add(polledIdeaIds.getInt("idea_id"));
      }

      StringBuilder builder = new StringBuilder();
      builder.append(SqlCommands.PARTIAL_SELECT_IDEAS_IN);

      for( int i = 0 ; i < ideaIds.size(); i++ ) {
        builder.append("?,");
      }
      builder.deleteCharAt( builder.length() -1 ); // Remove the last comma.
      builder.append(")");

      PreparedStatement statementIdeas = conn.prepareStatement(builder.toString());
      int index = 1;
      for(Integer id : ideaIds) {
        statementIdeas.setInt(index++, id);
      }
      ResultSet ideaResults = statementIdeas.executeQuery();

      Map<Integer, Map<String, String>> ideas = new HashMap<>();

      while(ideaResults.next()) {
        Map<String, String> ideaContents = new HashMap<>();

        PreparedStatement selectPollIdeaVotes = conn.prepareStatement(SqlCommands.SELECT_POLL_IDEA_VOTES);
        selectPollIdeaVotes.setInt(1, pollId);
        selectPollIdeaVotes.setInt(2, ideaResults.getInt("ideas.idea_id"));
        ResultSet pollIdeaVotes = selectPollIdeaVotes.executeQuery();
        int voteCount = 0;
        while(pollIdeaVotes.next()) {
          voteCount++;
        }

        PreparedStatement selectUserPollIdeaVotes = conn.prepareStatement(SqlCommands.SELECT_POLL_IDEA_VOTE_FOR_USER);
        selectUserPollIdeaVotes.setInt(1, pollId);
        selectUserPollIdeaVotes.setInt(2, ideaResults.getInt("ideas.idea_id"));
        UserService userService = UserServiceFactory.getUserService();
        String userId = userService.getCurrentUser().getUserId();
        selectUserPollIdeaVotes.setString(3, userId);
        ResultSet userPollIdeaVotes = selectUserPollIdeaVotes.executeQuery();
        boolean userDidVote = false;
        while(userPollIdeaVotes.next()) {
          userDidVote = true;
        }

        // Store the particulars for a blog in a map
        ideaContents.put("title", ideaResults.getString("ideas.title"));
        ideaContents.put("description", ideaResults.getString("ideas.description"));
        ideaContents.put("timestamp", ideaResults.getString("ideas.timestamp"));
        ideaContents.put("vote_count", String.valueOf(voteCount));
        ideaContents.put("user_voted", userDidVote ? "yes" : "no");

        // Store the post in a map with key of the ideaId
        ideas.put(ideaResults.getInt("ideas.idea_id"), ideaContents);
      }

      req.setAttribute("ideas", ideas);
      req.getRequestDispatcher("viewPoll.jsp").forward(req, resp);
    } catch (SQLException e) {
      throw new ServletException("SQL error", e);
    }
  }

  void showOwnedPolls(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
    try (PreparedStatement selectOwnedPolls = conn.prepareStatement(SqlCommands.SELECT_OWNED_POLLS)) {
      UserService userService = UserServiceFactory.getUserService();
      String userId = userService.getCurrentUser().getUserId();
      selectOwnedPolls.setString(1, userId);
      ResultSet rs = selectOwnedPolls.executeQuery();
      Map<Integer, Map<String, String>> polls = new HashMap<>();

      while (rs.next()) {
        Map<String, String> pollContents = new HashMap<>();

        // Store the particulars for a blog in a map
        pollContents.put("title", rs.getString("polls.title"));
        pollContents.put("description", rs.getString("polls.description"));
        pollContents.put("timestamp", rs.getString("polls.timestamp"));
        pollContents.put("is_open", rs.getBoolean("polls.is_open") ? "yes" : "");

        // Store the post in a map with key of the ideaId
        polls.put(rs.getInt("polls.poll_id"), pollContents);
      }

      req.setAttribute("polls", polls);
      req.getRequestDispatcher("viewOwnedPolls.jsp").forward(req, resp);
    } catch (SQLException e) {
      throw new ServletException("SQL error", e);
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

        conn.createStatement().executeUpdate(SqlCommands.CREATE_IDEA_TABLE); // Create idea table.
        conn.createStatement().executeUpdate(SqlCommands.CREATE_POLL_TABLE); // Create poll table.
        conn.createStatement().executeUpdate(SqlCommands.CREATE_POLLED_IDEAS_TABLE); // Create polled ideas table.
        conn.createStatement().executeUpdate(SqlCommands.CREATE_POLL_IDEA_VOTES_TABLE); // Create polled ideas table.
      } catch (SQLException e) {
        throw new ServletException("Unable to connect to SQL server", e);
      }

    } finally {
      // Nothing really to do here.
    }
  }
}