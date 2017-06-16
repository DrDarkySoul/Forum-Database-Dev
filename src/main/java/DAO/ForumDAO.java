package DAO;

import Entities.ForumEntity;
import Mappers.ForumMapper;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Created by Rishat_Valitov on 16.06.17.
 */
public class ForumDAO {
    private final JdbcTemplate jdbcTemplate;

    public ForumDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ForumEntity getForumFromSlug(String slug) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM forum WHERE LOWER(slug) = LOWER(?)",
                    new Object[]{slug}, new ForumMapper());
        } catch (Exception e) {
            return null;
        }
    }

    public void insertForum(ForumEntity forumEntity) {
        try {
            jdbcTemplate.update("INSERT INTO forum (title, \"user\", slug, posts, threads) VALUES(?,?,?,?,?)",
                    forumEntity.getTitle(), forumEntity.getUser() , forumEntity.getSlug() ,
                    forumEntity.getPosts(), forumEntity.getThreads());
        } catch (Exception ignored) {}
    }

    public void updatePostCount(String forumSlug, Integer size) {
        jdbcTemplate.update("UPDATE forum SET posts = posts + " + size +
                " WHERE LOWER(slug) = LOWER(?)", forumSlug);
    }
}
