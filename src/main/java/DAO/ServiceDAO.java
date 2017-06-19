package DAO;

import Entities.ServiceEntity;
import Helpers.Helper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by Rishat_Valitov on 16.06.17.
 */

public class ServiceDAO {
    private final JdbcTemplate jdbcTemplate;

    public ServiceDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public ServiceEntity getInfo() {
//        final Integer forum = jdbcTemplate.queryForObject ("SELECT COUNT(*) FROM forum", new Object[]{}, Integer.class);
//        final Integer user = jdbcTemplate.queryForObject  ("SELECT COUNT(*) FROM users", new Object[]{}, Integer.class);
        // TODO: Optimize query for threads like posts
        final Integer threadLikePost = jdbcTemplate.queryForObject("SELECT count(*) FROM thread WHERE id NOT IN (SELECT DISTINCT thread FROM post)", new Object[]{}, Integer.class);
        final Integer thread = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM post", new Object[]{}, Integer.class) - threadLikePost;
        final Integer post = jdbcTemplate.queryForObject  ("SELECT COUNT(*) FROM thread", new Object[]{}, Integer.class) + threadLikePost;
        return new ServiceEntity(Helper.getForum(), post - threadLikePost, thread + threadLikePost, Helper.getUser());
    }

    @Transactional
    public ServiceEntity clear() {
        jdbcTemplate.update("DELETE FROM count_parent_zero;" + "DELETE FROM link_user_forum;"+ "DELETE FROM users; " + "DELETE FROM post; " + "DELETE FROM thread; " +
                "DELETE FROM forum; " + "DELETE FROM vote;");
        Helper.toZero();
        return new ServiceEntity();
    }
}
