package ru.itmo.wp.model.repository.impl;

import ru.itmo.wp.model.database.DatabaseUtils;
import ru.itmo.wp.model.domain.Talk;
import ru.itmo.wp.model.exception.RepositoryException;
import ru.itmo.wp.model.repository.TalkRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("SqlNoDataSourceInspection")
public class TalkRepositoryImpl implements TalkRepository {
    private final DataSource DATA_SOURCE = DatabaseUtils.getDataSource();

    @Override
    public void save(Talk talk) {
        try (Connection connection = DATA_SOURCE.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO `Talk` (`sourceUserId`, `targetUserId`, `text`, `creationTime`) VALUES (?, ?, ?, NOW())")) {
                statement.setLong(1, talk.getSourceUserId());
                statement.setLong(2, talk.getTargetUserId());
                statement.setString(3, talk.getText());
                if (statement.executeUpdate() != 1) {
                    throw new RepositoryException("Can't save Talk.");
                }
            }
        } catch (SQLException e) {
            throw new RepositoryException("Can't save Talk.", e);
        }
    }

    @Override
    public List<Talk> findAllByUserId(long userId) {
        List<Talk> talks = new ArrayList<>();
        try (Connection connection = DATA_SOURCE.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT t.`id`, t.`sourceUserId`, t.`targetUserId`, t.`text`, t.`creationTime`, " +
                            "       s.`login` AS `sourceUserLogin`, tg.`login` AS `targetUserLogin` " +
                            "FROM `Talk` t " +
                            "JOIN `User` s  ON s.`id`  = t.`sourceUserId` " +
                            "JOIN `User` tg ON tg.`id` = t.`targetUserId` " +
                            "WHERE t.`sourceUserId`=? OR t.`targetUserId`=? " +
                            "ORDER BY t.`creationTime` ASC, t.`id` ASC")) {
                statement.setLong(1, userId);
                statement.setLong(2, userId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        talks.add(toTalk(resultSet));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RepositoryException("Can't find Talk.", e);
        }
        return talks;
    }

    private Talk toTalk(ResultSet resultSet) throws SQLException {
        Talk talk = new Talk();
        talk.setId(resultSet.getLong("id"));
        talk.setSourceUserId(resultSet.getLong("sourceUserId"));
        talk.setTargetUserId(resultSet.getLong("targetUserId"));
        talk.setText(resultSet.getString("text"));
        talk.setCreationTime(resultSet.getTimestamp("creationTime"));
        talk.setSourceUserLogin(resultSet.getString("sourceUserLogin"));
        talk.setTargetUserLogin(resultSet.getString("targetUserLogin"));
        return talk;
    }
}
