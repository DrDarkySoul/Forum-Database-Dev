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
        final Integer forum = jdbcTemplate.queryForObject ("SELECT COUNT(*) FROM forum", new Object[]{}, Integer.class);
        final Integer user = jdbcTemplate.queryForObject  ("SELECT COUNT(*) FROM client", new Object[]{}, Integer.class);
        //final Integer threadLikePost = jdbcTemplate.queryForObject("SELECT count(*) FROM thread WHERE id NOT IN (SELECT DISTINCT thread FROM post)", new Object[]{}, Integer.class);
        final Integer thread = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM post", new Object[]{}, Integer.class);// - threadLikePost;
        final Integer post = jdbcTemplate.queryForObject  ("SELECT COUNT(*) FROM thread", new Object[]{}, Integer.class);// + threadLikePost;
        return new ServiceEntity(forum, post, thread, user);
//        return new ServiceEntity(Helper.getForum().get(), Helper.getPost().get(), Helper.getThread().get(), Helper.getUser().get());
    }

    @Transactional
    public ServiceEntity clear() {
        jdbcTemplate.update("DELETE FROM thread_parent_zero;" +
                "DELETE FROM forum_user;"+
                "DELETE FROM post; "     +
                "DELETE FROM thread; "   +
                "DELETE FROM forum; "    +
                "DELETE FROM client; "   +
                "DELETE FROM vote;");
        return new ServiceEntity();
    }
}
