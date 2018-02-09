package com.google.offwegoog;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Base64;

@SuppressWarnings("serial")
@WebServlet(name = "DeletePoll", value="/deletePoll")
public class DeletePoll extends HttpServlet {
  Connection conn; // Cloud SQL connection

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {

    String pollId = req.getParameter("id");
    String decodedString = new String(Base64.getUrlDecoder().decode(pollId)); // Decode the websafe ID.
    Integer decodedId = Integer.parseInt(decodedString);

    try (PreparedStatement statementDeletePost = conn.prepareStatement(SqlCommands.DELETE_POLL)) {
      statementDeletePost.setInt(1, decodedId);
      statementDeletePost.executeUpdate();

      final String confirmation = "Poll deleted.";

      req.setAttribute("confirmation", confirmation);
      req.setAttribute("destination_url", "/viewPoll");
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

        conn.createStatement().executeUpdate(SqlCommands.CREATE_POLL_TABLE); // create poll table
      } catch (SQLException e) {
        throw new ServletException("Unable to connect to SQL server", e);
      }

    } finally {
      // Nothing really to do here.
    }
  }
}