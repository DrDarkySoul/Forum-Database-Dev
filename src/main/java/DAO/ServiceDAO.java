package DAO;

import Entities.ServiceEntity;
import Helpers.Helper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by Rishat_Valitov on 16.06.17.
 */
@Component("ServiceDAO")
public class ServiceDAO {
    @Autowired
    private JdbcTemplate jdbcTemplate;

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
        jdbcTemplate.execute("TRUNCATE TABLE client, forum, thread, vote, post, forum_user, thread_parent_zero CASCADE;");
        return new ServiceEntity();
    }
}
