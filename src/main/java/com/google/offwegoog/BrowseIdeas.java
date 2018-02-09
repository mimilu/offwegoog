package com.google.offwegoog;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.HashMap;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@WebServlet(name = "BrowseIdeas", value = "/browseIdeas")
public class BrowseIdeas extends HttpServlet {
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
      req.getRequestDispatcher("browseIdeas.jsp").forward(req, resp);
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

        conn.createStatement().executeUpdate(SqlCommands.CREATE_IDEA_TABLE); // create idea table
      } catch (SQLException e) {
        throw new ServletException("Unable to connect to SQL server", e);
      }

    } finally {
      // Nothing really to do here.
    }
  }
}