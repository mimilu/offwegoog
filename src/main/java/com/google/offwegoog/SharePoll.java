package com.google.offwegoog;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.stream.Collectors;
import javax.servlet.annotation.WebServlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

@SuppressWarnings("serial")
@WebServlet(name = "SharePoll", value = "/sharePoll")
public class SharePoll extends HttpServlet {

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
    throws IOException, ServletException {

    String encodedId = req.getParameter("id");
    if (encodedId == null || encodedId.isEmpty()) {
      PrintWriter out = resp.getWriter();
      out.println("EMPTY ID!");
      return;
    }

    Properties props = new Properties();
    Session session = Session.getDefaultInstance(props, null);

    // Create a map of the httpParameters that we want and run it through jSoup
    Map<String, String> sharedPollForm =
      req.getParameterMap()
        .entrySet()
        .stream()
        .filter(a -> a.getKey().startsWith("offsitePoll_"))
        .collect(
          Collectors.toMap(
            p -> p.getKey(), p -> Jsoup.clean(p.getValue()[0], Whitelist.basic())));

    String recipientsList = sharedPollForm.get("offsitePoll_recipients");
    recipientsList.replaceAll("\\s+","");
    String[] recipients = recipientsList.split(";");

      for(String recipient : recipients) {
        try {
          Message msg = new MimeMessage(session);
          msg.setFrom(new InternetAddress("no-reply@offwegoog.appspotmail.com", "Off We Goog - No-reply"));
          msg.addRecipient(Message.RecipientType.TO,
            new InternetAddress(recipient, "Poll Participant"));
          msg.setSubject("Please vote in " + sharedPollForm.get("offsitePoll_title"));
          msg.setText("You've been asked to vote in a poll to select an offsite.\n" +
            "https://offwegoog.appspot.com/viewPoll?id=" + encodedId + "\n\n" +
            "Off We Goog is a database and poll system for teams organizing offsites :)");
          Transport.send(msg);
        } catch (AddressException e) {
          // ...
        } catch (MessagingException e) {
          // ...
        } catch (UnsupportedEncodingException e) {
          // ...
        }
      }

    req.setAttribute("confirmation", "Mail sent!");
    req.setAttribute("destination_url", "/viewPoll?id=" + encodedId);
    req.getRequestDispatcher("/confirm.jsp").forward(req, resp);
  }
}