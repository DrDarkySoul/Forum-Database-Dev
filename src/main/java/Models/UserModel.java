package Models;

import DAO.UserDAO;
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

    private final UserDAO userDAO;

    public UserModel(JdbcTemplate jdbcTemplate) {
        this.userDAO = new UserDAO(jdbcTemplate);
    }

    public ResponseEntity<String> createUser(UserEntity userEntity) {
        String result = userDAO.insertUser(userEntity);
        if(result == null) {
            String conflictResult = userDAO.getConflictUsers(userEntity);
            return new ResponseEntity<>(conflictResult, HttpStatus.CONFLICT);
        } else
            return new ResponseEntity<>(userEntity.getJSONString(), HttpStatus.CREATED);
    }

    public ResponseEntity<String> getUserProfile(String nickname) {
        UserEntity userEntity = userDAO.getUserFromNickname(nickname);
        if (userEntity == null) return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(userEntity.getJSONString(), HttpStatus.OK);
    }

    public ResponseEntity<String> updateProfile(UserEntity newUserEntity, String nickname) {
        if (newUserEntity.getJSONString().equals("{}"))
        {
            newUserEntity = userDAO.getUserFromNickname(nickname);
            if(newUserEntity == null) return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
            return new ResponseEntity<>(newUserEntity.getJSONString(), HttpStatus.OK);
        }
        UserEntity oldUserEntity = userDAO.getUserFromNickname(nickname);
        if(oldUserEntity == null) return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
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
