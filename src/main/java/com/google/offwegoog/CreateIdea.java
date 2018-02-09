package com.google.offwegoog;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.SQLException;
import java.util.Date;
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
@WebServlet(name = "CreateIdea", value="/createIdea")
public class CreateIdea extends HttpServlet {
    Connection conn; // Cloud SQL connection

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/ideaEntry.jsp").forward(req, resp);
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

        // Build the SQL command to insert the blog post into the database
        try (PreparedStatement statementCreateIdea = conn.prepareStatement(SqlCommands.CREATE_IDEA)) {
            statementCreateIdea.setTimestamp(1,  null); // Null id generates the next id automatically.
            statementCreateIdea.setTimestamp(2, new Timestamp(new Date().getTime()));
            statementCreateIdea.setString(3, offsiteIdea.get("offsiteIdea_title"));
            statementCreateIdea.setString(4, offsiteIdea.get("offsiteIdea_description"));
            statementCreateIdea.executeUpdate();

            // Send the user to the confirmation page with personalised confirmation text
            String confirmation = "Your idea, " + offsiteIdea.get("offsiteIdea_title") + ", was added to the database.";

            req.setAttribute("confirmation", confirmation);
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
            } catch (SQLException e) {
                throw new ServletException("Unable to connect to SQL server", e);
            }

        } finally {
            // Nothing really to do here.
        }
    }
}