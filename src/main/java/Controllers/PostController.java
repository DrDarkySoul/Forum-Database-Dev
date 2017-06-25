package Controllers;

import Models.PostModel;
import Entities.PostEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/post/")
public class PostController {

    @Autowired
    private PostModel postModel;

    public PostController(JdbcTemplate jdbcTemplate) {
//        this.postModel = new PostModel(jdbcTemplate);
    }

    @RequestMapping(path = "/{id}/details", method = RequestMethod.GET)
    public ResponseEntity<String> getPostDetail(@PathVariable(name = "id") Integer id,
                                                @RequestParam(value = "related",
                                                                defaultValue = "") String related) {
        return (postModel.getDetail(id, related));
    }

    @RequestMapping(path = "/{id}/details", method = RequestMethod.POST)
    public ResponseEntity<String> changePostDetail(@PathVariable(name = "id") Integer id,
                                                   @RequestBody PostEntity body) {
        return (postModel.update(id, body));
    }
}
