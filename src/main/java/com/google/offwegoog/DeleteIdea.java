package com.google.offwegoog;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.*;
import java.util.Base64;

@SuppressWarnings("serial")
@WebServlet(name = "DeleteIdea", value="/deleteIdea")
public class DeleteIdea extends HttpServlet {
  Connection conn; // Cloud SQL connection

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {

    String ideaId = req.getParameter("id");
    String decodedString = new String(Base64.getUrlDecoder().decode(ideaId)); // Decode the websafe ID.
    Integer decodedId = Integer.parseInt(decodedString);

    try (PreparedStatement selectPollsContainingIdea = conn.prepareStatement(SqlCommands.SELECT_POLLS_CONTAINING_IDEA)) {
      selectPollsContainingIdea.setInt(1, decodedId);
      ResultSet pollsContainingIdea = selectPollsContainingIdea.executeQuery();

      while(pollsContainingIdea.next()) {
        req.setAttribute("confirmation", "Unable to delete idea. It is part of at least one poll.");
        req.getRequestDispatcher("/confirm.jsp").forward(req, resp);
        return;
      }

      PreparedStatement statementDeletePost = conn.prepareStatement(SqlCommands.DELETE_IDEA);
      statementDeletePost.setInt(1, decodedId);
      statementDeletePost.executeUpdate();

      final String confirmation = "Idea deleted.";

      req.setAttribute("confirmation", confirmation);
      req.getRequestDispatcher("/confirm.jsp").forward(req, resp);

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
        conn.createStatement().executeUpdate(SqlCommands.CREATE_POLL_TABLE); // create poll table
        conn.createStatement().executeUpdate(SqlCommands.CREATE_POLLED_IDEAS_TABLE); // create poll table
      } catch (SQLException e) {
        throw new ServletException("Unable to connect to SQL server", e);
      }

    } finally {
      // Nothing really to do here.
    }
  }
}