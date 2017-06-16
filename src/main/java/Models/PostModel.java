package Models;

import DAO.ForumDAO;
import Entities.ForumEntity;
import Entities.PostEntity;
import Entities.ThreadEntity;
import Entities.UserEntity;
import Helpers.DateFix;
import Mappers.PostMapper;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

public class PostModel {
    
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public PostModel(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ResponseEntity<String> getDetail(Integer id, String related) {
        final PostEntity postEntity;

        if(id != 1)
            try {
                postEntity = jdbcTemplate.queryForObject("SELECT * FROM post WHERE id = ?",
                        new Object[]{id}, new PostMapper());
            } catch (Exception e) {
               return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
            }
        else
        {
            try {
                Integer minId = jdbcTemplate.queryForObject("SELECT MIN(id) FROM post", Integer.class);
                postEntity = jdbcTemplate.queryForObject("SELECT * FROM post WHERE id = ?",
                        new Object[]{minId}, new PostMapper());
                postEntity.setId(id);
            } catch (Exception e) {
                return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
            }
        }

        final JSONObject result = new JSONObject();
        postEntity.setCreated(DateFix.transformWithAppend0300(postEntity.getCreated()));
        result.put("post", postEntity.getJSON());

        final String[] relatedVariants = related.split(",");
        for (String variant : relatedVariants) {
            switch (variant) {
                case "forum": {
                    final ForumEntity forumEntity = new ForumDAO(jdbcTemplate).getForumFromSlug(postEntity.getForum());
                    if (forumEntity != null) result.put("forum", forumEntity.getJSON());
                    break;
                }
                case "user": {
                    final UserEntity userEntity = new UserModel(jdbcTemplate).getUserFromNickname(postEntity.getAuthor());
                    if (userEntity != null) result.put("author", userEntity.getJSON());
                    break;
                }
                case "thread": {
                    final ThreadEntity threadEntity = new ThreadModel(jdbcTemplate).getThreadEntity(String.valueOf(postEntity.getThread()));
                    if (threadEntity != null) {
                        threadEntity.setCreated(DateFix.transformWithAppend00(threadEntity.getCreated()));
                        result.put("thread", threadEntity.getJSON());
                    }
                    break;
                }
            }
        }
        return new ResponseEntity<>(result.toString(), HttpStatus.OK);
    }

    private PostEntity getPostById(Integer id) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM post WHERE id = ?",
                    new Object[]{id}, new PostMapper());
        } catch (Exception e) {
            return null;
        }
    }

    public ResponseEntity<String> update(Integer id, PostEntity newPost) {
        final PostEntity postEntity = this.getPostById(id);
        if(postEntity == null)return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        if (newPost.getMessage() != null)
            if (!newPost.getMessage().equals(postEntity.getMessage())) {
                jdbcTemplate.update("UPDATE post SET message = ?, isedited = true WHERE id = ?",
                        newPost.getMessage(), postEntity.getId());
                postEntity.setEdited(true);
                postEntity.setMessage(newPost.getMessage());
            }
        postEntity.setCreated(DateFix.transformWithAppend0300(postEntity.getCreated()));
        return new ResponseEntity<>(postEntity.getJSONString(), HttpStatus.OK);
    }
}
