package DAO;

import Entities.UserEntity;
import Mappers.UserMapper;
import org.json.JSONArray;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

/**
 * Created by Rishat_Valitov on 16.06.17.
 */
public class UserDAO {
    private final JdbcTemplate jdbcTemplate;

    public UserDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String insertUser(UserEntity userEntity) {
        try {
            jdbcTemplate.update("INSERT INTO users (nickname, fullname, about, email) VALUES (?,?,?,?)",
                    userEntity.getNickname(), userEntity.getFullname(), userEntity.getAbout(), userEntity.getEmail());
            return userEntity.getJSONString();
        } catch (Exception e) {
            return null;
        }
    }

    public String getConflictUsers(UserEntity userEntity) {
        final List<UserEntity> answer = jdbcTemplate.query(
                "SELECT * FROM users WHERE LOWER(email) = LOWER(?) OR LOWER(nickname) = LOWER(?)",
                new Object[]{userEntity.getEmail(), userEntity.getNickname()}, new UserMapper());
        final JSONArray result = new JSONArray();
        answer.forEach(row -> result.put(row.getJSON()));
        return result.toString();
    }

    public String updateUser(UserEntity userEntity) {
        try {
            jdbcTemplate.update("UPDATE users SET (fullname,about,email)=(?,?,?) WHERE LOWER(nickname)= LOWER(?)",
                    userEntity.getFullname(), userEntity.getAbout(), userEntity.getEmail(), userEntity.getNickname());
            return userEntity.getJSONString();
        } catch (Exception e) {
            return null;
        }
    }

    public UserEntity getUserFromNickname(String nickname) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM users WHERE LOWER(nickname) = LOWER(?)",
                    new Object[]{nickname}, new UserMapper());
        } catch (Exception e) {
            return null;
        }
    }

    public String getUserList(String query, String forumSlug) {
        final List<UserEntity> userEntityList = jdbcTemplate.query(query, new Object[]{forumSlug, forumSlug}, new UserMapper());
        final JSONArray result = new JSONArray();
        userEntityList.forEach(userEntity -> result.put(userEntity.getJSON()));
        return result.toString();
    }
}
