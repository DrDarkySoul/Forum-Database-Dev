package Models;

import DAO.ForumDAO;
import DAO.ThreadDAO;
import DAO.UserDAO;
import Entities.ForumEntity;
import Entities.ThreadEntity;
import Entities.UserEntity;
import Helpers.DataBaseHelper;
import Helpers.DateFix;
import Mappers.ThreadMapper;
import Mappers.UserMapper;
import org.json.JSONArray;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;

public class ForumModel {
    private final ForumDAO forumDAO;
    private final UserDAO userDAO;
    private final ThreadDAO threadDAO;

    public ForumModel(JdbcTemplate jdbcTemplate) {
        this.userDAO = new UserDAO(jdbcTemplate);
        this.forumDAO = new ForumDAO(jdbcTemplate);
        this.threadDAO = new ThreadDAO(jdbcTemplate);
    }

    public ResponseEntity<String> createForum(ForumEntity forumEntity) {
        // Check user
        UserEntity userEntity = userDAO.getUserFromNickname(forumEntity.getUser());
        if(userEntity == null)
            return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        forumEntity.setUser(userEntity.getNickname());
        // Check slug
        final ForumEntity forumEntityNew = forumDAO.getForumFromSlug(forumEntity.getSlug());
        if (forumEntityNew != null)
            return new ResponseEntity<>(forumEntityNew.getJSONString(), HttpStatus.CONFLICT);
        // Insert note
        forumDAO.insertForum(forumEntity);
        return new ResponseEntity<>(forumEntity.getJSONString(), HttpStatus.CREATED);
    }

    public ResponseEntity<String> getForumDetails(String forumSlug) {
        final ForumEntity forum = forumDAO.getForumFromSlug(forumSlug);
        if(forum == null) return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(forum.getJSONString(), HttpStatus.OK);
    }

    public ResponseEntity<String> createThread(ThreadEntity threadEntity, String forumSlug) {
        // Thread check
        final ThreadEntity threadEntityNew = threadDAO.getThread(threadEntity.getSlug());
        if (threadEntityNew != null)
            return new ResponseEntity<>(threadEntityNew.getJSONString(), HttpStatus.CONFLICT);
        // User check
        final UserEntity userEntity = userDAO.getUserFromNickname(threadEntity.getAuthor());
        if(userEntity == null) return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        // Forum check
        final ForumEntity forumEntity = forumDAO.getForumFromSlug(forumSlug);
        if(forumEntity == null) return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        threadEntity.setForum(forumEntity.getSlug());
        threadEntity = threadDAO.insertThread(threadEntity, forumSlug);
        return new ResponseEntity<>(threadEntity.getJSONString(), HttpStatus.CREATED);
    }

    private String queryGetThreadBuilder(Integer limit, String since, Boolean desc)
    {
        final StringBuilder query = new StringBuilder("SELECT * FROM thread WHERE LOWER(forum) = LOWER(?)");
        if (since != null)
            if (desc) query.append(" AND created <=?::timestamptz ");
            else query.append(" AND created >=?::timestamptz ");

        query.append(" ORDER BY created ");
        if (desc) query.append(" DESC ");

        if (limit != null)
            query.append(" LIMIT ?");
        return query.toString();
    }

    public ResponseEntity<String> getThreads(String forumSlug, Integer limit, String since, Boolean desc) {
        // Forum check
        final ForumEntity forum = forumDAO.getForumFromSlug(forumSlug);
        if(forum == null) return new ResponseEntity<>("", HttpStatus.NOT_FOUND);

        String query = this.queryGetThreadBuilder(limit, since, desc);
        if (since != null) since = DataBaseHelper.dataFixReplaceSpace(since);
        String result = threadDAO.getThreadList(forumSlug, limit, since, query);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    public ResponseEntity<String> getForumUsers(String forumSlug, Integer limit, String since, Boolean desc) {
        // Forum check
        final ForumEntity forum = forumDAO.getForumFromSlug(forumSlug);
        if(forum == null) return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        final StringBuilder query = new StringBuilder(
                "SELECT *, OCTET_LENGTH(LOWER(nickname)) FROM users WHERE nickname IN")
                .append("(SELECT users.nickname FROM users FULL OUTER JOIN post ")
                .append("ON LOWER(users.nickname) = LOWER(post.author) FULL OUTER JOIN thread ")
                .append("ON LOWER(users.nickname) = LOWER(thread.author) WHERE LOWER(post.forum) = LOWER(?) ")
                .append("OR LOWER(thread.forum) = LOWER(?) GROUP BY users.nickname)");

        if (since != null)
            if (desc) query.append(" AND nickname<'").append(since).append("'");
            else query.append(" AND nickname>'").append(since).append("'");

        query.append("  ORDER BY nickname");
        if (desc) query.append(" DESC");
        if (limit != null) query.append(" LIMIT ").append(limit);
        String result = userDAO.getUserList(query.toString(), forumSlug);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
