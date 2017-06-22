package DAO;

import Entities.VoteEntity;
import Mappers.VoteMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

/**
 * Created by Rishat_Valitov on 16.06.17.
 */
public class VoteDAO {
    private final JdbcTemplate jdbcTemplate;

    public VoteDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Integer getVote(VoteEntity voteEntity, Integer votes) {
        final List<VoteEntity> voteEntityList = jdbcTemplate.query(
                "SELECT * FROM vote WHERE (thread_id, LOWER(author))=(?,?)",
                new Object[]{voteEntity.getThreadId(), voteEntity.getAuthor().toLowerCase()}, new VoteMapper());
        if (voteEntityList.isEmpty()) {
            votes += voteEntity.getVoice();
            jdbcTemplate.update("UPDATE thread SET votes = ? WHERE id = ?", votes, voteEntity.getThreadId());
            jdbcTemplate.update("INSERT INTO vote (thread_id, author, voice) VALUES(?, ?, ?)",
                    voteEntity.getThreadId(), voteEntity.getAuthor(), voteEntity.getVoice());
        } else {
            jdbcTemplate.update("UPDATE vote SET voice = ? WHERE id = ? ",
                    voteEntity.getVoice(), voteEntityList.get(0).getId());
            if (((voteEntity.getVoice() == -1) && (voteEntityList.get(0).getVoice() == 1)) ||
                    ((voteEntity.getVoice() == 1) && (voteEntityList.get(0).getVoice() == -1))) {
                votes += voteEntity.getVoice() * 2;
                jdbcTemplate.update("UPDATE thread SET votes = ? WHERE id = ?", votes, voteEntity.getThreadId());
            }
        }
        return votes;
    }
}
