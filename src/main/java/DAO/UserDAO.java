package DAO;

import Entities.UserEntity;
import Helpers.Helper;
import Mappers.UserMapper;
import org.json.JSONArray;
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
            jdbcTemplate.update("INSERT INTO client (nickname, fullname, about, email) VALUES (?,?,?,?)",
                    userEntity.getNickname(), userEntity.getFullname(), userEntity.getAbout(), userEntity.getEmail());
            Helper.incUser();
            return userEntity.getJSONString();
        } catch (Exception e) {
            return null;
        }
    }

    public String getConflictUsers(UserEntity userEntity) {
        final List<UserEntity> answer;
        try {
            answer = jdbcTemplate.query(
                    "SELECT * FROM client WHERE LOWER(email) = LOWER(?) OR LOWER(nickname) = LOWER(?)",
                    new Object[]{userEntity.getEmail(), userEntity.getNickname()}, new UserMapper());

        } catch (Exception e) {
            return null;
        }
        if(answer.isEmpty()) return null;
        final JSONArray result = new JSONArray();
        answer.forEach(row -> result.put(row.getJSON()));
        return result.toString();
    }

    public String updateUser(UserEntity userEntity) {
        try {
            jdbcTemplate.update("UPDATE client SET (fullname, about, email) = (?, ?, ?) WHERE LOWER(nickname) = ?",
                    userEntity.getFullname(), userEntity.getAbout(), userEntity.getEmail(), userEntity.getNickname().toLowerCase());
            return userEntity.getJSONString();
        } catch (Exception e) {
            return null;
        }
    }

    public UserEntity getUserFromNickname(String nickname) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM client WHERE LOWER(nickname) = ?",
                    new Object[]{nickname.toLowerCase()}, new UserMapper());
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
