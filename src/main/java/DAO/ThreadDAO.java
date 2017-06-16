package DAO;

import Entities.ThreadEntity;
import Helpers.Helper;
import Mappers.ThreadMapper;
import org.json.JSONArray;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.util.List;

/**
 * Created by Rishat_Valitov on 16.06.17.
 */
public class ThreadDAO {
    private final JdbcTemplate jdbcTemplate;

    public ThreadDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ThreadEntity getThread(String slug_or_id) {
        final Helper threadIdentifier = new Helper(slug_or_id);
        try {
            final ThreadEntity threadEntity;
            if (threadIdentifier.getFlag().equals("id"))
                threadEntity = jdbcTemplate.queryForObject("SELECT * FROM thread WHERE id = ?",
                        new Object[]{threadIdentifier.getId()}, new ThreadMapper());
            else
                threadEntity = jdbcTemplate.queryForObject("SELECT * FROM thread WHERE LOWER(slug) = LOWER(?)",
                        new Object[]{threadIdentifier.getSlug()}, new ThreadMapper());
            threadEntity.setCreated(Helper.dateFixZero(threadEntity.getCreated()));
            return threadEntity;
        } catch (Exception e) {
            return null;
        }
    }

    public ThreadEntity insertThread(ThreadEntity threadEntity, String forumSlug) {
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            final PreparedStatement preparedStatement = connection.prepareStatement(
                    "INSERT INTO thread (title,author,forum,message,slug," +
                            "votes,created) VALUES (?,?,?,?,?,?,?::TIMESTAMPTZ)",
                    new String[]{"id"});
            preparedStatement.setString(1, threadEntity.getTitle());
            preparedStatement.setString(2, threadEntity.getAuthor());
            preparedStatement.setString(3, threadEntity.getForum());
            preparedStatement.setString(4, threadEntity.getMessage());
            preparedStatement.setString(5, threadEntity.getSlug());
            preparedStatement.setInt(6, threadEntity.getVotes());
            preparedStatement.setString(7, threadEntity.getCreated());
            return preparedStatement;
        }, keyHolder);
        threadEntity.setId((int) keyHolder.getKey());
        jdbcTemplate.update("UPDATE forum SET threads = threads + 1 WHERE LOWER(slug) = LOWER(?)",
                forumSlug);
        return threadEntity;
    }

    public String getThreadList(String forumSlug, Integer limit, String since, String query) {
        final List<ThreadEntity> threadEntityList;
        if (limit != null)
            if (since != null)
                threadEntityList = jdbcTemplate.query(query, new Object[]{forumSlug, since, limit}, new ThreadMapper());
            else
                threadEntityList = jdbcTemplate.query(query, new Object[]{forumSlug, limit}, new ThreadMapper());
        else if (since != null)
            threadEntityList = jdbcTemplate.query(query, new Object[]{forumSlug, since}, new ThreadMapper());
        else
            threadEntityList = jdbcTemplate.query(query, new Object[]{forumSlug}, new ThreadMapper());
        final JSONArray result = new JSONArray();
        threadEntityList.forEach(threadEntity -> {
            threadEntity.setCreated(Helper.dateFixZero(threadEntity.getCreated()));
            result.put(threadEntity.getJSON());
        });
        return result.toString();
    }

    public ThreadEntity updateThread(ThreadEntity threadEntityNew, ThreadEntity threadEntityOld) {
        if (threadEntityNew.getMessage() != null && threadEntityNew.getTitle() != null)
            try {
                jdbcTemplate.update("UPDATE thread SET title = ?, message = ? WHERE id = ?",
                        threadEntityNew.getTitle(), threadEntityNew.getMessage(), threadEntityOld.getId());
                threadEntityOld.setTitle(threadEntityNew.getTitle());
                threadEntityOld.setMessage(threadEntityNew.getMessage());
            } catch (Exception e) {
                return null;
            }
        else if (threadEntityNew.getMessage() != null && threadEntityNew.getTitle() == null)
            try {
                jdbcTemplate.update("UPDATE thread SET message = ? WHERE id = ?",
                        threadEntityNew.getMessage(), threadEntityOld.getId());
                threadEntityOld.setMessage(threadEntityNew.getMessage());
            } catch (Exception e) {
                return null;
            }
        else if (threadEntityNew.getMessage() == null && threadEntityNew.getTitle() != null)
            try {
                jdbcTemplate.update("UPDATE thread SET title = ? WHERE id = ?",
                        threadEntityNew.getTitle(), threadEntityOld.getId());
                threadEntityOld.setTitle(threadEntityNew.getTitle());
            } catch (Exception e) {
                return null;
            }
        return threadEntityOld;
    }
}
