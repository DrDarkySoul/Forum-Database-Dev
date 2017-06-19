package Models;

import DAO.UserDAO;
import Entities.UserEntity;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

public class UserModel {

    private final UserDAO userDAO;

    public UserModel(JdbcTemplate jdbcTemplate) {
        this.userDAO = new UserDAO(jdbcTemplate);
    }

    public ResponseEntity<String> createUser(UserEntity userEntity) {

        String conflictResult = userDAO.getConflictUsers(userEntity);
        if(conflictResult != null) return new ResponseEntity<>(conflictResult, HttpStatus.CONFLICT);
        if(userDAO.insertUser(userEntity) == null) return new ResponseEntity<>("{}", HttpStatus.CONFLICT);
        return new ResponseEntity<>(userEntity.getJSONString(), HttpStatus.CREATED);
    }

    public ResponseEntity<String> getUserProfile(String nickname) {
        UserEntity userEntity = userDAO.getUserFromNickname(nickname);
        if (userEntity == null) return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(userEntity.getJSONString(), HttpStatus.OK);
    }

    public ResponseEntity<String> updateProfile(UserEntity newUserEntity, String nickname) {
        UserEntity oldUserEntity = userDAO.getUserFromNickname(nickname);
        if(oldUserEntity == null) return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        if (newUserEntity.getJSONString().equals("{}"))
            return new ResponseEntity<>(oldUserEntity.getJSONString(), HttpStatus.OK);
        final JSONObject userOld = oldUserEntity.getJSON();
        final JSONObject userNew = newUserEntity.getJSON();
        if (!userNew.has("about"))    newUserEntity.setAbout(userOld.get("about").toString());
        if (!userNew.has("email"))    newUserEntity.setEmail(userOld.get("email").toString());
        if (!userNew.has("fullname")) newUserEntity.setFullname(userOld.get("fullname").toString());
        newUserEntity.setNickname(nickname);
        if(userDAO.updateUser(newUserEntity) != null)
            return new ResponseEntity<>(newUserEntity.getJSONString(), HttpStatus.OK);
        else
            return new ResponseEntity<>("", HttpStatus.CONFLICT);
    }
}
