package DAO;

import Entities.PostEntity;
import Entities.ThreadEntity;
import Helpers.Helper;
import Mappers.PostMapper;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rishat_Valitov on 16.06.17.
 */
@Component("PostDAO")
public class PostDAO {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public PostDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public PostEntity getPostById(Integer id) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM post WHERE id = ?",
                    new Object[]{id}, new PostMapper());
        } catch (Exception e) {
            return null;
        }
    }

    public PostEntity getPostMinId() {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM post WHERE id IN (SELECT MIN(id) FROM post)",
                    new Object[]{}, new PostMapper());

        } catch (Exception e) {
            return null;
        }
    }

    public Integer getMaxId() {
        try {
            return jdbcTemplate.queryForObject("SELECT MAX(id) FROM post",
                    new Object[]{}, Integer.class);

        } catch (Exception e) {
            return null;
        }
    }

    public void updatePost(PostEntity postEntity) {
        try {
            jdbcTemplate.update("UPDATE post SET message = ?, isedited = true WHERE id = ?",
                    postEntity.getMessage(), postEntity.getId());
        } catch (Exception ignored) {}
    }

    public void butchInsertPost(List<Object[]> list) {
        jdbcTemplate.batchUpdate("INSERT INTO post (parent, author, message, isEdited, forum, thread, path, created) " +
                "VALUES (?,?,?,?,?,?,?,?::timestamptz)", list);
    }

    public Boolean parentCheck(Integer parent, Integer threadId) {
        try {
            final List<PostEntity> posts = jdbcTemplate.query(
                    "SELECT * FROM post WHERE id = ? AND thread = ?",
                    new Object[]{parent, threadId}, new PostMapper());
            if (posts.isEmpty())
                return false;
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public String getPath(Integer postId) {
        return jdbcTemplate.queryForObject("SELECT path FROM post WHERE id = ?;",
                new Object[]{postId}, String.class);
    }

    public List<Integer> getIdList(Integer size) {
        return jdbcTemplate.queryForList("SELECT nextval('post_id_seq') from generate_series(1, ?);", new Object[]{size}, Integer.class);
    }

    public List<Integer> getIdListFormCreated(Timestamp created) {
        return jdbcTemplate.queryForList("SELECT id FROM post WHERE created = ? ORDER BY id", new Object[]{created}, Integer.class);
    }

    public List<PostEntity> executeQuery(String query) {
        return jdbcTemplate.query(query, new PostMapper());
    }

    public List<Integer> getIds(Integer id) {
        return jdbcTemplate.queryForList("SELECT id FROM post WHERE id > ? ORDER BY id", new Object[]{id}, Integer.class);
    }

    public Integer getCountOfMainPosts(Integer threadId) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM post WHERE parent=0 AND thread=?",
                new Object[]{threadId}, Integer.class
        );
    }
}
