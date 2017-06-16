package DAO;

import Entities.UserEntity;
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
