package DAO;

import Entities.PostEntity;
import Mappers.PostMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

/**
 * Created by Rishat_Valitov on 16.06.17.
 */
public class PostDAO {
    private final JdbcTemplate jdbcTemplate;

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

    // TODO: Fix query
    public PostEntity getPostMinId() {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM post WHERE id IN (SELECT MIN(id) FROM post)",
                    new Object[]{}, new PostMapper());

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

    public Integer getMaxId() {
        try {
            return jdbcTemplate.queryForObject("SELECT MAX(id) FROM post", Integer.class);
        } catch (Exception e) {
            return null;
        }
    }

    public void butchInsertPost(List<Object[]> list) {
        jdbcTemplate.batchUpdate("INSERT INTO post (parent,author,message,isEdited,forum,thread,path,created) " +
                "VALUES (?,?,?,?,?,?,?,?::timestamptz)", list);
    }
}
