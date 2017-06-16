package Models;

import DAO.PostDAO;
import DAO.ThreadDAO;
import DAO.UserDAO;
import DAO.VoteDAO;
import Entities.PostEntity;
import Entities.ThreadEntity;
import Entities.UserEntity;
import Entities.VoteEntity;
import Helpers.Helper;
import Mappers.PostMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ThreadModel {
    private final ThreadDAO threadDAO;
    private final UserDAO userDAO;
    private final PostDAO postDAO;
    private final VoteDAO voteDAO;

    public ThreadModel(JdbcTemplate jdbcTemplate) {
        this.threadDAO = new ThreadDAO(jdbcTemplate);
        this.userDAO = new UserDAO(jdbcTemplate);
        this.postDAO = new PostDAO(jdbcTemplate);
        this.voteDAO = new VoteDAO(jdbcTemplate);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ResponseEntity<String> createPosts(ArrayList<PostEntity> postEntityArrayList, String slug_or_id) {
        // Thread check
        final ThreadEntity threadEntity = threadDAO.getThread(slug_or_id);
        if(threadEntity == null) return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        List<Object[]> postsList = new ArrayList<>();
        final Timestamp nowTimestamp = new Timestamp(System.currentTimeMillis());
        final JSONArray result = new JSONArray();
        Integer maxId = postDAO.getMaxId();
        if(maxId == null) maxId = 0;
        Integer currId = maxId;
        Integer id = currId;
        Integer countS = 0;
        for (PostEntity post : postEntityArrayList) {
            id++;
            if (post.getParent() != 0 && postEntityArrayList.size() < 100)
                try {
                    final List<PostEntity> posts = jdbcTemplate.query(
                            "SELECT * FROM post WHERE id = ? AND thread = ?",
                            new Object[]{post.getParent(), threadEntity.getId()}, new PostMapper());
                    if (posts.isEmpty())
                        return new ResponseEntity<>("", HttpStatus.CONFLICT);
                } catch (Exception e) {
                    return new ResponseEntity<>("", HttpStatus.CONFLICT);
                }
            // User check
            final UserEntity userEntity = userDAO.getUserFromNickname(post.getAuthor());
            if(userEntity == null) return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
            post.setForum(threadEntity.getForum());
            post.setThread(threadEntity.getId());
            String createdTimeWithTS = Helper.dateFixThree(nowTimestamp.toString());
            post.setCreated(createdTimeWithTS);
            if (post.getParent() == 0) {
                Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM post WHERE parent = 0 AND thread = ?",
                        new Object[]{post.getThread()}, Integer.class);
                final String path = Integer.toHexString(count + countS);
                countS++;
                post.setPath(path);
            } else {
                try {
                    final String prevPath = jdbcTemplate.queryForObject("SELECT path FROM post WHERE id = ?;",
                            new Object[]{post.getParent()}, String.class);
                    currId++;
                    final String path = prevPath+'.' + Integer.toHexString(currId);
                    post.setPath(path);
                } catch (Exception e) {
                    final String path = "000000" + '.' + Integer.toHexString(currId);
                    post.setPath(path);
                }
            }
            postsList.add(new Object[]{
                    post.getParent(),
                    post.getAuthor(),
                    post.getMessage(),
                    post.getEdited(),
                    post.getForum(),
                    post.getThread(),
                    post.getPath(),
                    post.getCreated()});
            post.setId(id);
            result.put(post.getJSON());
        }
        jdbcTemplate.batchUpdate("INSERT INTO post (parent,author,message,isEdited,forum,thread,path,created) " +
                "VALUES (?,?,?,?,?,?,?,?::timestamptz)", postsList);
        jdbcTemplate.update("UPDATE forum SET posts = posts + " + postEntityArrayList.size() +
                        " WHERE LOWER(slug) = LOWER(?)", threadEntity.getForum());
        return new ResponseEntity<>(result.toString(), HttpStatus.CREATED);
    }

    @Transactional
    public ResponseEntity<String> vote(VoteEntity voteEntity, String slug_or_id) {
        if(userDAO.getUserFromNickname(voteEntity.getNickname()) == null)
            return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        final ThreadEntity threadEntity = threadDAO.getThread(slug_or_id);
        if(threadEntity == null) return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        voteEntity.setThreadId(threadEntity.getId());
        threadEntity.setVotes(voteDAO.getVote(voteEntity, threadEntity.getVotes()));
        threadEntity.setCreated(Helper.dateFixZero(threadEntity.getCreated()));
        return new ResponseEntity<>(threadEntity.getJSONString(), HttpStatus.OK);
    }

    public ResponseEntity<String> getThreadDetails(String slug_or_id) {
        final ThreadEntity threadEntity = threadDAO.getThread(slug_or_id);
        if(threadEntity == null) return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        try {
            threadEntity.setCreated(Helper.dateFixZero(threadEntity.getCreated()));
            return new ResponseEntity<>(threadEntity.getJSONString(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<String> getThreadPosts(String slug_or_id, Integer limit,
                                                 String sort, Boolean desc, Integer marker) {
        final StringBuilder query = new StringBuilder("SELECT * FROM post WHERE thread = ");
        final ThreadEntity threadEntity = threadDAO.getThread(slug_or_id);
        if (threadEntity != null) {
            query.append(threadEntity.getId());
            List<PostEntity> posts = null;
            if (sort == null) sort = "flat";
            switch (sort) {
                case "flat": {
                    if (desc != null && desc) query.append(" ORDER BY created DESC, id DESC LIMIT ").append(limit.toString()).append(" OFFSET ").append(marker.toString());
                    else query.append(" ORDER BY created, id LIMIT ").append(limit.toString()).append(" OFFSET ").append(marker.toString());
                    break;
                }
                case "tree": {
                    query.append(" ORDER BY LEFT(path,6)");
                    if (desc != null && desc)
                        query.append(" DESC").append(", path DESC");
                    if (desc != null && !desc)
                        query.append(", path ASC");
                    query.append(" LIMIT ").append(limit.toString()).append(" OFFSET ").append(marker.toString());
                    break;
                }
                case "parent_tree": {
                    if (limit != null) {
                        final Integer maxID = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM post WHERE parent = 0 AND thread = ? ",
                                new Object[]{threadEntity.getId()}, Integer.class);
                        if (desc != null && !desc) {
                            if ((maxID - limit - marker) < 0) {
                                if(marker < 0) marker = 0;
                                query.append(" AND path >= '").append(Integer.toHexString(marker)).append("'");
                            } else
                                query.append(" AND path >= '").append(Integer.toHexString(marker)).append("'")
                                        .append(" AND path < '").append(Integer.toHexString(marker + limit)).append("'");
                        } else {
                            if ((maxID - limit - marker) < 0) {
                                Integer high = maxID - marker;
                                if(high < 0) high = 0;
                                query.append(" AND path >= ").append("'0'").append(" AND path < '").append(Integer.toHexString(high)).append("'");
                            } else {
                                Integer top = maxID - marker;
                                Integer bottom = maxID - limit - marker;
                                if(top < 0) top = 0;
                                if(bottom < 0) bottom = 0;
                                query.append(" AND path >= '").append(Integer.toHexString(bottom)).append("'")
                                        .append(" AND path < '").append(Integer.toHexString(top)).append("'");
                            }
                        }
                    }
                    query.append(" ORDER BY LEFT(path,6)");
                    if (desc != null && desc) {
                        query.append(" DESC");
                        query.append(", path DESC");
                    }
                    if (desc != null && !desc) query.append(", path ASC");
                    break;
                }
            }
            try {
                posts = jdbcTemplate.query(query.toString(), new PostMapper());
            } catch (Exception ignored) {}

            final JSONObject result = new JSONObject();
            if (posts != null && posts.isEmpty()) result.put("marker", marker.toString());
            else {
                Integer sum;
                if(limit == null) sum = marker;
                else sum = limit + marker;
                result.put("marker", sum.toString());
            }

            final JSONArray resultArray = new JSONArray();
            if (posts != null)
                for (PostEntity postEntity : posts) {
                    postEntity.setCreated(Helper.dateFixThree(postEntity.getCreated()));
                    resultArray.put(postEntity.getJSON());
                }
            result.put("posts", resultArray);
            return new ResponseEntity<>(result.toString(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<String> updateThread(ThreadEntity newData, String slug_or_id) {
        final EntryIdentifier threadIdentifier = new EntryIdentifier(slug_or_id);
        if (this.getThreadDetails(slug_or_id).getStatusCode().equals(HttpStatus.OK)) {
            if (newData.getMessage() != null && newData.getTitle() != null)
                try {
                    if (threadIdentifier.getFlag().equals("id"))
                        jdbcTemplate.update("UPDATE thread SET title = ?, message = ? WHERE id = ?",
                                newData.getTitle(), newData.getMessage(), threadIdentifier.getId());
                    else
                        jdbcTemplate.update("UPDATE thread SET message = ?, title = ? WHERE LOWER(slug) = LOWER(?)",
                                newData.getMessage(), newData.getTitle(), threadIdentifier.getSlug());
                } catch (Exception e) {
                    return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
                }
            else if (newData.getMessage() != null && newData.getTitle() == null)
                try {
                    if (threadIdentifier.getFlag().equals("id"))
                        jdbcTemplate.update("UPDATE thread SET message=? WHERE id=?",
                                newData.getMessage(), threadIdentifier.getId());
                    else
                        jdbcTemplate.update("UPDATE thread SET message=? WHERE LOWER(slug)=LOWER(?)",
                                newData.getMessage(), threadIdentifier.getSlug());
                } catch (Exception e) {
                    return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
                }
            else if (newData.getMessage() == null && newData.getTitle() != null) {
                try {
                    if (threadIdentifier.getFlag().equals("id"))
                        jdbcTemplate.update("UPDATE thread SET  title=? WHERE id=?",
                                newData.getTitle(), threadIdentifier.getId());
                    else
                        jdbcTemplate.update("UPDATE thread SET title=? WHERE LOWER(slug)=LOWER(?)",
                                newData.getTitle(), threadIdentifier.getSlug());
                } catch (Exception e) {
                    return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
                }
            }
            final ThreadEntity threadEntity = threadDAO.getThread(slug_or_id);
            if (threadEntity != null) {
                newData = threadEntity;
                newData.setCreated(Helper.dateFixZero(newData.getCreated()));
            }
            return new ResponseEntity<>(newData.getJSONString(), HttpStatus.OK);
        } else
            return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
    }
}
