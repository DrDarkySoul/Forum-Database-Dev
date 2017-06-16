package Controllers;

import Models.UserModel;
import Entities.UserEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/")
public class UserController {

    private final UserModel userService;

    UserController(JdbcTemplate jdbcTemplate) {
        this.userService = new UserModel(jdbcTemplate);
    }

    @RequestMapping(path = "/{nickname}/create",
            method = RequestMethod.POST)
    public ResponseEntity<String> createUser(@RequestBody UserEntity user,
                                             @PathVariable(name = "nickname") String nickname) {
        user.setNickname(nickname);
        return (userService.createUser(user));
    }

    @RequestMapping(path = "/{nickname}/profile",
            method = RequestMethod.GET)
    public ResponseEntity<String> getUser(@PathVariable(name = "nickname") String nickname) {
        return (userService.getUserProfile(nickname));
    }

    @RequestMapping(path = "/{nickname}/profile",
            method = RequestMethod.POST)
    public ResponseEntity<String> updateUser(@RequestBody UserEntity body,
                                             @PathVariable(name = "nickname") String nickname) {
        return (userService.updateProfile(body, nickname));
    }
}
