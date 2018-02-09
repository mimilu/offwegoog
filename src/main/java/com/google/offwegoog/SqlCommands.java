package com.google.offwegoog;

public class SqlCommands {
  // Cloud SQL table creation commands
  static final String CREATE_IDEA_TABLE =
    "CREATE TABLE IF NOT EXISTS ideas ( idea_id INT NOT NULL "
      + "AUTO_INCREMENT, timestamp DATETIME NOT NULL, "
      + "title VARCHAR(256) NOT NULL, "
      + "description VARCHAR(1337) NOT NULL, PRIMARY KEY (idea_id) )";

  static final String CREATE_POLL_TABLE =
    "CREATE TABLE IF NOT EXISTS polls ( poll_id INT NOT NULL "
      + "AUTO_INCREMENT, timestamp DATETIME NOT NULL, "
      + "owner_id VARCHAR(64) NOT NULL, "
      + "title VARCHAR(256) NOT NULL, "
      + "description VARCHAR(1337) NOT NULL, "
      + "is_open BOOL DEFAULT 1 NOT NULL, "
      + "PRIMARY KEY (poll_id) )";

  static final String CREATE_POLLED_IDEAS_TABLE =
    "CREATE TABLE IF NOT EXISTS polled_ideas ( poll_id INT DEFAULT 0 NOT NULL "
      + ", idea_id INT DEFAULT 0 NOT NULL, "
      + "PRIMARY KEY (poll_id, idea_id) )";

  static final String CREATE_POLL_IDEA_VOTES_TABLE =
    "CREATE TABLE IF NOT EXISTS poll_idea_votes ( poll_id INT DEFAULT 0 NOT NULL "
      + ", idea_id INT DEFAULT 0 NOT NULL "
      + ", user_id VARCHAR(64) NOT NULL "
      + ", PRIMARY KEY (poll_id, user_id, idea_id) )";

  // Idea creation query
  static final String CREATE_IDEA =
    "INSERT INTO ideas (idea_id, timestamp, title, description) VALUES (?, ?, ?, ?)";

  // Idea creation query
  static final String CREATE_POLL =
    "INSERT INTO polls (poll_id, timestamp, owner_id, title, description) VALUES (?, ?, ?, ?, ?)";

  static final String DELETE_IDEA = "DELETE FROM ideas WHERE idea_id = ?";

  static final String DELETE_POLL = "DELETE FROM polls WHERE poll_id = ?";

  static final String DELETE_POLL_VOTES_FOR_USER = "DELETE FROM poll_idea_votes WHERE poll_id = ? AND user_id = ?";

  // Idea creation query
  static final String INSERT_POLLED_IDEAS =
    "INSERT INTO polled_ideas (poll_id, idea_id) VALUES (?, ?)";

  // Insert a vote on an idea within a poll. Ignore duplicate votes.
  static final String INSERT_POLL_IDEA_VOTE =
    "INSERT IGNORE INTO poll_idea_votes (poll_id, user_id, idea_id) VALUES (?, ?, ?)";

  static final String SELECT_ALL_IDEAS =
    "SELECT idea_id, timestamp, title, description FROM ideas";

  static final String SELECT_IDEA =
    "SELECT idea_id, timestamp, title, description FROM ideas WHERE idea_id = ?";

  static final String PARTIAL_SELECT_IDEAS_IN =
    "SELECT idea_id, timestamp, title, description FROM ideas WHERE idea_id IN (";

  static final String SELECT_OWNED_POLLS =
    "SELECT poll_id, timestamp, title, is_open, description FROM polls WHERE owner_id = ?";

  static final String SELECT_POLL =
    "SELECT poll_id, owner_id, timestamp, title, description, is_open FROM polls WHERE poll_id = ?";

  static final String SELECT_POLLS_CONTAINING_IDEA =
    "SELECT poll_id FROM polled_ideas WHERE idea_id = ?";

  static final String SELECT_POLL_IDEA_VOTES =
    "SELECT user_id FROM poll_idea_votes WHERE (poll_id = ? AND idea_id = ?)";

  static final String SELECT_POLLED_IDEAS =
    "SELECT poll_id, idea_id FROM polled_ideas WHERE poll_id = ?";

  static final String SELECT_POLL_IDEA_VOTE_FOR_USER =
    "SELECT user_id FROM poll_idea_votes WHERE (poll_id = ? AND idea_id = ? AND user_id = ?)";

  static final String UPDATE_IDEA = "UPDATE ideas SET title = ?, description = ? WHERE idea_id = ?";

  static final String UPDATE_POLL_STATUS = "UPDATE polls SET is_open = ? WHERE poll_id = ?";
}
