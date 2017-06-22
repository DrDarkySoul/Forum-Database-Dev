package Mappers;

import Entities.VoteEntity;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class VoteMapper implements RowMapper<VoteEntity> {

    @Override
    public VoteEntity mapRow(ResultSet resultSet, int i) throws SQLException {
        final VoteEntity voteEntity = new VoteEntity();

        voteEntity.setId((resultSet.getInt("id")));
        voteEntity.setThreadId(resultSet.getInt("thread_id"));
        voteEntity.setAuthor(resultSet.getString("author"));
        voteEntity.setVoice(resultSet.getInt("voice"));

        return voteEntity;
    }
}
