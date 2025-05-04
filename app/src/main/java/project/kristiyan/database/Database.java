package project.kristiyan.database;

import org.sqlite.JDBC;
import project.kristiyan.models.DUser;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

public class Database {
    private String url = "jdbc:sqlite:users.sqlite";
    private String createTableSQL = "CREATE TABLE IF NOT EXISTS users (" +
            " id BIGINT NOT NULL PRIMARY KEY," +
            " username VARCHAR(255) NOT NULL," +
            " time VARCHAR(100) NOT NULL," +
            " has_sent BOOLEAN NOT NULL DEFAULT 0);";

    private String selectUsers = "SELECT * FROM users;";

    private String insertUser = "INSERT INTO users (id, username, time, has_sent) VALUES (?, ?, ?, 0);";

    private String deleteUser = "DELETE FROM users WHERE id = ?;";

    private String updateUser = "UPDATE users SET has_sent = ? WHERE id = ?;";

    public Database() throws Exception {
        DriverManager.registerDriver(new JDBC());
        Connection connection = getConnection();
        connection.createStatement().execute(createTableSQL);
        connection.close();
    }

    private Connection getConnection() throws Exception {
        return DriverManager.getConnection(url);
    }

    public List<DUser> getUsers() {
        List<DUser> DUsers = new ArrayList<>();
        try {
            Connection connection = getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(selectUsers);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                DUsers.add(new DUser(
                        resultSet.getBigDecimal("id").longValue(),
                        resultSet.getString("username"),
                        resultSet.getString("time"),
                        resultSet.getBoolean("has_sent")
                ));
            }
            resultSet.close();
            connection.close();
            return DUsers;

        } catch (Exception e) {
            return DUsers;
        }
    }

    public void insertUser(DUser DUser) throws Exception {
        Connection connection = getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(insertUser);

        preparedStatement.setBigDecimal(1, BigDecimal.valueOf(DUser.id));
        preparedStatement.setString(2, DUser.username);
        preparedStatement.setString(3, DUser.time);

        preparedStatement.executeUpdate();

        connection.close();
    }

    public void deleteUser(long id) throws Exception {
        Connection connection = getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(deleteUser);
        preparedStatement.setBigDecimal(1, BigDecimal.valueOf(id));
        preparedStatement.executeUpdate();
        connection.close();
    }

    public void updateUser(long id, boolean has_sent) throws Exception {
        Connection connection = getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(updateUser);
        preparedStatement.setBoolean(1, has_sent);
        preparedStatement.setBigDecimal(2, BigDecimal.valueOf(id));
        preparedStatement.executeUpdate();
        connection.close();
    }
}
