package com.google.offwegoog;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.annotation.WebServlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

@SuppressWarnings("serial")
@WebServlet(name = "EditIdea", value = "/editIdea")
public class EditIdea extends HttpServlet {
  Connection conn; // Cloud SQL connection

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {
    String ideaId = req.getParameter("id");
    if (ideaId.isEmpty()) {
      PrintWriter out = resp.getWriter();
      out.println("EMPTY ID!");
      return;
    }

    // Build up the PreparedStatement
    try (PreparedStatement selectStatement = conn.prepareStatement(SqlCommands.SELECT_IDEA)) {
      String decodedString = new String(Base64.getUrlDecoder().decode(ideaId)); // Decode the websafe ID.
      Integer decodedId = Integer.parseInt(decodedString);
      selectStatement.setInt(1, decodedId);

      ResultSet results = selectStatement.executeQuery();

      results.next();
      req.setAttribute("title", results.getString("title"));
      req.setAttribute("description", results.getString("description"));
      req.setAttribute("id", ideaId);
      req.getRequestDispatcher("/editIdea.jsp").forward(req, resp);

    } catch (SQLException e) {
      throw new ServletException("SQL error", e);
    }
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {

    // Create a map of the httpParameters that we want and run it through jSoup
    Map<String, String> offsiteIdea =
      req.getParameterMap()
        .entrySet()
        .stream()
        .filter(a -> a.getKey().startsWith("offsiteIdea_"))
        .collect(
          Collectors.toMap(
            p -> p.getKey(), p -> Jsoup.clean(p.getValue()[0], Whitelist.basic())));

    // Build up the PreparedStatement
    try (PreparedStatement statementUpdateIdea = conn.prepareStatement(SqlCommands.UPDATE_IDEA)) {
      statementUpdateIdea.setString(1, offsiteIdea.get("offsiteIdea_title"));
      statementUpdateIdea.setString(2, offsiteIdea.get("offsiteIdea_description"));
      // Decode the websafe ID.
      String encodedId = offsiteIdea.get("offsiteIdea_id");
      String decodedString = new String(Base64.getUrlDecoder().decode(encodedId)); // Decode the websafe ID.
      Integer decodedId = Integer.parseInt(decodedString);
      statementUpdateIdea.setInt(3, decodedId);
      statementUpdateIdea.executeUpdate(); // Execute update query

      // Send the user to the confirmation page with personalised confirmation text
      String confirmation = "Offsite Idea " + offsiteIdea.get("offsiteIdea_title") + " updated.";

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
        conn.createStatement().executeUpdate(SqlCommands.CREATE_POLL_TABLE); // create idea table
      } catch (SQLException e) {
        throw new ServletException("Unable to connect to SQL server", e);
      }

    } finally {
      // Nothing really to do here.
    }
  }
}