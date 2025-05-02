package project.kristiyan.database;

import org.sqlite.JDBC;
import project.kristiyan.models.User;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

public class Database {
    private String url = "jdbc:sqlite:users.sqlite";
    private String createTableSQL = "CREATE TABLE IF NOT EXISTS users (" +
            " id BIGINT NOT NULL PRIMARY KEY," +
            " username VARCHAR(255) NOT NULL," +
            " time VARCHAR(100) NOT NULL," +
            " last_time_message_sent VARCHAR(100) NOT NULL);";

    private String selectUsers = "SELECT * FROM users;";

    private String insertUser = "INSERT INTO users (id, username, time, last_time_message_sent) VALUES (?, ?, ?, ' ');";

    private String deleteUser = "DELETE FROM users WHERE id = ?;";

    public Database() throws Exception {
        DriverManager.registerDriver(new JDBC());
        Connection connection = getConnection();
        connection.createStatement().execute(createTableSQL);
        connection.close();
    }

    private Connection getConnection() throws Exception {
        return DriverManager.getConnection(url);
    }

    public List<User> getUsers() {
        List<User> users = new ArrayList<>();
        try {
            Connection connection = getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(selectUsers);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                users.add(new User(
                        resultSet.getBigDecimal("id").longValue(),
                        resultSet.getString("username"),
                        resultSet.getString("time"),
                        resultSet.getString("last_time_message_sent")
                ));
            }
            resultSet.close();
            connection.close();
            return users;

        } catch (Exception e) {
            return users;
        }
    }

    public void insertUser(User user) throws Exception {
        Connection connection = getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(insertUser);

        preparedStatement.setBigDecimal(1, BigDecimal.valueOf(user.id));
        preparedStatement.setString(2, user.username);
        preparedStatement.setString(3, user.time);

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
}
