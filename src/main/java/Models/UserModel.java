package Models;

import Entities.UserEntity;
import Mappers.UserMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

public class UserModel {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserModel(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ResponseEntity<String> createUser(UserEntity userEntity) {
        try {
            jdbcTemplate.update("INSERT INTO users (nickname, fullname, about, email) VALUES (?,?,?,?)",
                    userEntity.getNickname(), userEntity.getFullname(), userEntity.getAbout(), userEntity.getEmail());
            return new ResponseEntity<>(userEntity.getJSONString(), HttpStatus.CREATED);
        } catch (Exception e) {
            final List<UserEntity> answer = jdbcTemplate.query("SELECT * FROM users WHERE LOWER(email) = LOWER(?) OR LOWER(nickname) = LOWER(?)",
                    new Object[]{userEntity.getEmail(), userEntity.getNickname()}, new UserMapper());
            final JSONArray result = new JSONArray();
            answer.forEach(row -> result.put(row.getJSON()));
            return new ResponseEntity<>(result.toString(), HttpStatus.CONFLICT);
        }
    }

    UserEntity getUserFromNickname(String nickname) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM users WHERE LOWER(nickname) = LOWER(?)",
                    new Object[]{nickname}, new UserMapper());
        } catch (Exception e) {
            return null;
        }
    }

    public ResponseEntity<String> get(String nickname) {
        try {
            final UserEntity user = jdbcTemplate.queryForObject(
                    "SELECT * FROM users WHERE LOWER(nickname) = LOWER(?)",
                    new Object[]{nickname}, new UserMapper());
            return new ResponseEntity<>(user.getJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<String> update(UserEntity userEntity, String nickname) {
        if (userEntity.getJSONString().equals("{}"))
        {
            try {
                final UserEntity user = jdbcTemplate.queryForObject(
                        "SELECT * FROM users WHERE LOWER(nickname) = LOWER(?)",
                        new Object[]{nickname}, new UserMapper());
                return new ResponseEntity<>(user.getJSONString(), HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
            }
        }

        final ResponseEntity<String> resultGet;
        try {
            final UserEntity user = jdbcTemplate.queryForObject("SELECT * FROM users WHERE LOWER(nickname) = LOWER(?)",
                    new Object[]{nickname}, new UserMapper());
            resultGet = new ResponseEntity<>(user.getJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        }

        if (resultGet.getStatusCode() == HttpStatus.NOT_FOUND)
            return resultGet;

        final JSONObject userBefore = new JSONObject(resultGet.getBody());
        final JSONObject userNow    = userEntity.getJSON();

        if (!userNow.has("about"))    userEntity.setAbout(userBefore.get("about").toString());
        if (!userNow.has("email"))    userEntity.setEmail(userBefore.get("email").toString());
        if (!userNow.has("fullname")) userEntity.setFullname(userBefore.get("fullname").toString());

        userEntity.setNickname(nickname);

        try {
            jdbcTemplate.update("UPDATE users SET (fullname,about,email)=(?,?,?) WHERE LOWER(nickname)= LOWER(?)",
                    userEntity.getFullname(), userEntity.getAbout(), userEntity.getEmail(), nickname);
            return new ResponseEntity<>(userEntity.getJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("", HttpStatus.CONFLICT);
        }
    }
}
