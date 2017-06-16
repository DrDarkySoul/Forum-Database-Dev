package Models;

import DAO.ForumDAO;
import DAO.PostDAO;
import DAO.ThreadDAO;
import DAO.UserDAO;
import Entities.ForumEntity;
import Entities.PostEntity;
import Entities.ThreadEntity;
import Entities.UserEntity;
import Helpers.Helper;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

public class PostModel {

    private PostDAO postDAO;
    private UserDAO userDAO;
    private ForumDAO forumDAO;
    private ThreadDAO threadDAO;

    public PostModel(JdbcTemplate jdbcTemplate) {
        this.postDAO   = new PostDAO(jdbcTemplate);
        this.forumDAO  = new ForumDAO(jdbcTemplate);
        this.userDAO   = new UserDAO(jdbcTemplate);
        this.threadDAO = new ThreadDAO(jdbcTemplate);
    }

    public ResponseEntity<String> getDetail(Integer id, String related) {
        final PostEntity postEntity;
        if(id != 1) {
            postEntity = postDAO.getPostById(id);
            if(postEntity == null) return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        }
        else
        {
            postEntity = postDAO.getPostMinId();
            if(postEntity == null) return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
            postEntity.setId(id);
        }
        final JSONObject result = new JSONObject();
        postEntity.setCreated(Helper.dateFixThree(postEntity.getCreated()));
        result.put("post", postEntity.getJSON());
        final String[] relatedVariants = related.split(",");
        for (String variant : relatedVariants) {
            switch (variant) {
                case "forum": {
                    final ForumEntity forumEntity = forumDAO.getForumFromSlug(postEntity.getForum());
                    if (forumEntity != null) result.put("forum", forumEntity.getJSON());
                    break;
                }
                case "user": {
                    final UserEntity userEntity = userDAO.getUserFromNickname(postEntity.getAuthor());
                    if (userEntity != null) result.put("author", userEntity.getJSON());
                    break;
                }
                case "thread": {
                    final ThreadEntity threadEntity = threadDAO.getThread(postEntity.getThread().toString());
                    if (threadEntity != null)
                        result.put("thread", threadEntity.getJSON());
                    break;
                }
            }
        }
        return new ResponseEntity<>(result.toString(), HttpStatus.OK);
    }

    public ResponseEntity<String> update(Integer id, PostEntity newPost) {
        final PostEntity postEntity = postDAO.getPostById(id);
        if(postEntity == null) return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        if(newPost.getMessage() != null)
            if(!newPost.getMessage().equals(postEntity.getMessage())) {
                postEntity.setEdited(true);
                postEntity.setMessage(newPost.getMessage());
                postDAO.updatePost(postEntity);
            }
        postEntity.setCreated(Helper.dateFixThree(postEntity.getCreated()));
        return new ResponseEntity<>(postEntity.getJSONString(), HttpStatus.OK);
    }
}
