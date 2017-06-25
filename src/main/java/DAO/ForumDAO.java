package DAO;

import com.google.common.collect.Lists;
import Entities.ForumEntity;
import Entities.UserEntity;
import Helpers.Helper;
import Mappers.ForumMapper;
import Mappers.UserMapper;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Rishat_Valitov on 16.06.17.
 */

@Component("ForumDAO")
public class ForumDAO {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public ForumDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ForumEntity getForumFromSlug(String slug) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM forum WHERE LOWER(slug) = ?",
                    new Object[]{slug.toLowerCase()}, new ForumMapper());
        } catch (Exception e) {
            return null;
        }
    }

    public void insertForum(ForumEntity forumEntity) {
        try {
            jdbcTemplate.update("INSERT INTO forum (slug, title, author, posts, threads) VALUES(?,?,?,?,?)",
                    forumEntity.getSlug(), forumEntity.getTitle(), forumEntity.getUser(),
                    forumEntity.getPosts(), forumEntity.getThreads());
        } catch (Exception ignored) {}
        Helper.incForum();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void updatePostCount(String forumSlug, Integer size) {
        jdbcTemplate.update("UPDATE forum SET posts = posts + " + size +
                " WHERE LOWER(slug) = LOWER(?)", forumSlug);
    }

    public String getForumUsers(String query, ArrayList<Object> parameters) {
        final List<UserEntity> usersList = jdbcTemplate.query(query, parameters.toArray(), new UserMapper());
        final JSONArray result = new JSONArray();
        for (UserEntity user : usersList) {
            result.put(user.getJSON());
        }
        return result.toString();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void addInUserForum(String forumSlug, String userNickname){
        try {
            jdbcTemplate.update("INSERT INTO forum_user (author, forum) VALUES (?,?) ON CONFLICT DO NOTHING",
                    userNickname, forumSlug);
        }
        catch (Exception ignore) {}
    }

    public void addInUserForumMany(String forumSlug, List<String> userList, Integer size) {
        final List<Object[]> result = userList.stream().distinct()
                .map(id -> new Object[]{forumSlug, id}).collect(Collectors.toList());
        final List<List<Object[]>> listList = Lists.partition(result, size);
        listList.forEach(list -> {
            Boolean finished = false;
            while (!finished) {
                try {
                    jdbcTemplate.batchUpdate(
                            "INSERT INTO forum_user (forum, author) VALUES (?,?) ON CONFLICT DO NOTHING",
                            list);
                    finished = true;
                } catch (DeadlockLoserDataAccessException ignored) {}
            }
        });
    }
}
