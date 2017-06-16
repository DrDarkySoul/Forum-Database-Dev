package DAO;

import Entities.PostEntity;
import Mappers.PostMapper;
import org.springframework.jdbc.core.JdbcTemplate;

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
}
