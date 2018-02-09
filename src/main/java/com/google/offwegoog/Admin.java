package com.google.offwegoog;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import java.io.IOException;
import java.sql.*;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@WebServlet(name = "Admin", value = "/admin")
public class Admin extends HttpServlet {
  Connection conn; // Cloud SQL connection

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {
    req.getRequestDispatcher("admin.jsp").forward(req, resp);
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {
    // Create a map of the httpParameters that we want and run it through jSoup
    Map<String, String> adminOptions =
      req.getParameterMap()
        .entrySet()
        .stream()
        .filter(a -> a.getKey().startsWith("admin_"))
        .collect(
          Collectors.toMap(
            p -> p.getKey(), p -> Jsoup.clean(p.getValue()[0], Whitelist.basic())));

    if (adminOptions.get("admin_deleteTableName") != null) {
      // Build the SQL command to insert the blog post into the database
      try (PreparedStatement deleteTableStatement = conn.prepareStatement("DROP TABLE IF EXISTS " + adminOptions.get("admin_deleteTableName") + ";")) {
        deleteTableStatement.execute();

        // Send the user to the confirmation page with personalised confirmation text
        String confirmation = adminOptions.get("admin_deleteTableName")  + " was deleted.";

        req.setAttribute("confirmation", confirmation);
        req.setAttribute("destination_url", "/admin");
        req.getRequestDispatcher("/confirm.jsp").forward(req, resp);

      } catch (SQLException e) {
        throw new ServletException("SQL error when creating idea", e);
      }
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
        } catch (SQLException e) {
        throw new ServletException("Unable to connect to SQL server", e);
      }

    } finally {
      // Nothing really to do here.
    }
  }
}