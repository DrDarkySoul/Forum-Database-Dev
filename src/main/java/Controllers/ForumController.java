package Controllers;

import Models.ForumModel;
import Entities.ForumEntity;
import Entities.ThreadEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/forum/")
public class ForumController {
    private final ForumModel forumModel;

    public ForumController(JdbcTemplate jdbcTemplate) {
        this.forumModel = new ForumModel(jdbcTemplate);
    }

    @RequestMapping(path = "/create", method = RequestMethod.POST)
    public ResponseEntity<String> createForum(@RequestBody ForumEntity body) {
        return (forumModel.createForum(body));
    }

    @RequestMapping(path = "/{slug}/create", method = RequestMethod.POST)
    public ResponseEntity<String> createThread(@RequestBody ThreadEntity body,
                                               @PathVariable(name = "slug") String slug) {
        return (forumModel.createThread(body, slug));
    }

    @RequestMapping(path = "/{slug}/details", method = RequestMethod.GET)
    public ResponseEntity<String> getForumDetails(@PathVariable(name = "slug") String slug) {
        return (forumModel.getForumDetails(slug));
    }

    @RequestMapping(path = "/{slug}/threads", method = RequestMethod.GET)
    public ResponseEntity<String> getForumThreads(@PathVariable String slug,
                                                  @RequestParam(value = "limit", required = false) Integer limit,
                                                  @RequestParam(value = "since", required = false) String since,
                                                  @RequestParam(value = "desc", required = false) Boolean desc) {
        if(desc == null) desc = false;
        return (forumModel.getThreads(slug, limit, since, desc));
    }

    @RequestMapping(path = "/{slug}/users", method = RequestMethod.GET)
    public ResponseEntity<String> getForumUsers(@PathVariable(name = "slug") String slug,
                                                @RequestParam(value = "limit", required = false) Integer limit,
                                                @RequestParam(value = "since", required = false) String since,
                                                @RequestParam(value = "desc", required = false) Boolean desc){
        if(desc == null) desc = false;
        return (forumModel.getForumUsers(slug, limit, since, desc));
    }
}
