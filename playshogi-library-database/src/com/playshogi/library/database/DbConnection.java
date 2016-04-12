package com.playshogi.library.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DbConnection {

	private static final Logger LOGGER = Logger.getLogger("DbConnection");

	private final String database = "playshogi";
	private final String host = "localhost";
	private final int port = 3306;
	private final String url = "jdbc:mysql://" + host + ":" + port + "/" + database;
	private final String user = "playshogi";
	private final String password = "playshogiDB1";

	private Connection connection;

	public DbConnection() {
		LOGGER.log(Level.INFO, "Initiating DbConnection");

		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Could not load JDBC driver", e);
		}
	}

	public void start() {
		LOGGER.log(Level.INFO, "Connecting to database");

		try {
			connection = DriverManager.getConnection(url, user, password);
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Could not establish JDBC connection", e);
		}

	}

	public Connection getConnection() {
		if (connection == null) {
			start();
		}
		return connection;
	}

	public static void main(final String[] args) {
		new DbConnection().start();
	}
}