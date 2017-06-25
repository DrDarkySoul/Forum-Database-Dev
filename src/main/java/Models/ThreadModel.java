package Models;

import DAO.*;
import Entities.PostEntity;
import Entities.ThreadEntity;
import Entities.UserEntity;
import Entities.VoteEntity;
import Helpers.Helper;
import Mappers.PostMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Component("ThreadModel")
public class ThreadModel {
    @Autowired
    private ThreadDAO threadDAO;
    @Autowired
    private UserDAO userDAO;
    @Autowired
    private PostDAO postDAO;
    @Autowired
    private VoteDAO voteDAO;
    @Autowired
    private ForumDAO forumDAO;

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ResponseEntity<String> createPosts(ArrayList<PostEntity> postEntityArrayList, ThreadEntity threadEntity) {
        final Timestamp nowTimestamp = new Timestamp(System.currentTimeMillis());
        final String timeStringNow = Helper.dateFixThree(nowTimestamp.toString());
//        List<Integer> idList = postDAO.getIdList(postEntityArrayList.size());
        List<String> clientList = new ArrayList<>();
        Integer currId = postDAO.getMaxId();//idList.get(0) - 1;
        if(currId == null) currId = 0;
        Integer countZeroParentPosts = 0;
        List<Object[]> postsList = new ArrayList<>();
        for (PostEntity post : postEntityArrayList) {
            // User check
            final UserEntity userEntity = userDAO.getUserFromNickname(post.getAuthor());
            if(userEntity == null) return new ResponseEntity<>("", HttpStatus.NOT_FOUND);

            clientList.add(post.getAuthor());

            if(post.getEdited() == null) post.setEdited(false);
            if(post.getParent() == null) post.setParent(0);
            post.setForum(threadEntity.getForum());
            post.setThread(threadEntity.getId());
            post.setCreated(timeStringNow);

            if (post.getParent() == 0) {
                Integer count = threadDAO.getThreadCount(post.getThread());
                final String path = Integer.toHexString(count + countZeroParentPosts);
                countZeroParentPosts++;
                post.setPath(path);
            } else {
                try {
                    final String prevPath = postDAO.getPath(post.getParent());
                    currId++;
                    final String path = prevPath + '.' + Integer.toHexString(currId);
                    post.setPath(path);
                } catch (Exception e) {
                    final String path = "000000" + '.' + Integer.toHexString(currId);
                    post.setPath(path);
                }
            }
            if (post.getParent() != 0 &&
                    postEntityArrayList.size() < 100 &&
                    !postDAO.parentCheck(post.getParent(), threadEntity.getId()))
                return new ResponseEntity<>("", HttpStatus.CONFLICT);
            postsList.add(new Object[]{
                    post.getParent(),
                    post.getAuthor(),
                    post.getMessage(),
                    post.getEdited(),
                    post.getForum(),
                    post.getThread(),
                    post.getPath(),
                    post.getCreated()});
        }
        postDAO.butchInsertPost(postsList);
        threadDAO.updateThreadCountZeroParent(threadEntity.getId(), countZeroParentPosts);
        forumDAO.addInUserForumMany(threadEntity.getForum(), clientList, 40);
        List<Integer> listId = postDAO.getIdListFormCreated(nowTimestamp);
        IntStream.range(0, postEntityArrayList.size()).boxed().forEach(index ->
                postEntityArrayList.get(index).setId(listId.get(index)));
//        forumDAO.updatePostCount(threadEntity.getForum(), postEntityArrayList.size());
        Helper.incPost(postEntityArrayList.size());
        JSONArray result = new JSONArray();
        for (PostEntity post : postEntityArrayList)
            result.put(post.getJSON());

        return new ResponseEntity<>(result.toString(), HttpStatus.CREATED);
    }

    @Transactional
    public ResponseEntity<String> vote(VoteEntity voteEntity, String slug_or_id) {
        if(userDAO.getUserFromNickname(voteEntity.getAuthor()) == null)
            return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        final ThreadEntity threadEntity = threadDAO.getThread(slug_or_id);
        if(threadEntity == null) return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        voteEntity.setThreadId(threadEntity.getId());
        threadEntity.setVotes(voteDAO.getVote(voteEntity, threadEntity.getVotes()));
        return new ResponseEntity<>(threadEntity.getJSONString(), HttpStatus.OK);
    }

    public ResponseEntity<String> getThreadDetails(String slug_or_id) {
        final ThreadEntity threadEntity = threadDAO.getThread(slug_or_id);
        if(threadEntity == null) return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(threadEntity.getJSONString(), HttpStatus.OK);
    }

    public ResponseEntity<String> getThreadPosts(String slug_or_id, Integer limit,
                                                 String sort, Boolean desc, Integer marker) {
        final ThreadEntity threadEntity = threadDAO.getThread(slug_or_id);
        if(threadEntity == null) return new ResponseEntity<>("", HttpStatus.NOT_FOUND);

        final StringBuilder query = new StringBuilder("SELECT * FROM post WHERE thread = ");
        query.append(threadEntity.getId());
        final String descOrAsc = (desc ? "DESC " : "ASC ");
        List<PostEntity> posts;

        switch (sort) {
            case "flat": {
                    query.append(" ORDER BY created ").append(descOrAsc)
                            .append(", id ").append(descOrAsc).append("LIMIT ")
                            .append(limit.toString()).append(" OFFSET ")
                            .append(marker.toString());
                    break;
                }
                case "tree": {
                    query.append(" ORDER BY LEFT(path,6) ")
                            .append(descOrAsc).append(", path ").append(descOrAsc)
                            .append(" LIMIT ").append(limit.toString())
                            .append(" OFFSET ").append(marker.toString());
                    break;
                }
                case "parent_tree": {
                    if (limit > 0) {
                        final Integer maxID = threadDAO.getThreadCount(threadEntity.getId());
                        if (!desc) {
                            if ((maxID - limit - marker) < 0) {
                                if(marker < 0) marker = 0;
                                query.append(" AND path >= '").append(Integer.toHexString(marker)).append("'");
                            } else
                                query.append(" AND path >= '")
                                        .append(Integer.toHexString(marker))
                                        .append("'").append(" AND path < '")
                                        .append(Integer.toHexString(marker + limit)).append("'");
                        } else {
                            if ((maxID - limit - marker) < 0) {
                                Integer high = maxID - marker;
                                if (high < 0) high = 0;
                                query.append(" AND path >= ").append("'0'")
                                        .append(" AND path < '")
                                        .append(Integer.toHexString(high))
                                        .append("'");
                            } else {
                                Integer top = maxID - marker;
                                Integer bottom = maxID - limit - marker;
                                if(top < 0) top = 0;
                                if(bottom < 0) bottom = 0;
                                query.append(" AND path >= '").append(Integer.toHexString(bottom))
                                        .append("'").append(" AND path < '")
                                        .append(Integer.toHexString(top)).append("'");
                            }
                        }
                    }
                    query.append(" ORDER BY LEFT(path,6)")
                            .append(descOrAsc).append(", path ")
                            .append(descOrAsc);
                }
            }
            posts = postDAO.executeQuery(query.toString());
            final JSONObject result = new JSONObject();
            if (posts != null && posts.isEmpty()) result.put("marker", marker.toString());
            else {
                Integer sum;
                if(limit == 0) sum = marker;
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
    }

    public ResponseEntity<String> updateThread(ThreadEntity newData, String slugOrId) {
        ThreadEntity oldData = threadDAO.getThread(slugOrId);
        if(oldData == null) return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        ThreadEntity result = threadDAO.updateThread(newData, oldData);
        if(result == null) return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(result.getJSONString(), HttpStatus.OK);
    }
}