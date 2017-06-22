package Entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.json.JSONObject;

public class VoteEntity {

    private Integer id, threadId, voice;
    private String author;

    public VoteEntity() {}

    @JsonCreator
    public VoteEntity(
            @JsonProperty("id") Integer id,
            @JsonProperty("thread_id") Integer threadId,
            @JsonProperty("nickname") String author,
            @JsonProperty("voice") Integer voice) {
        this.id = id;
        this.threadId = threadId;
        this.author = author;
        this.voice = voice;
    }

    public Integer getId()       { return id;       }
    public Integer getThreadId() { return threadId; }
    public Integer getVoice()    { return voice;    }
    public String getAuthor()    { return author;   }

    public void setId(Integer id)             { this.id = id;             }
    public void setThreadId(Integer threadId) { this.threadId = threadId; }
    public void setAuthor(String author)      { this.author = author;     }
    public void setVoice(Integer voice)       { this.voice = voice;       }

    public String getJSONString() { return this.getJSON().toString(); }

    public JSONObject getJSON() {
        final JSONObject jsonObject = new JSONObject();

        jsonObject.put("threadId", threadId);
        jsonObject.put("author", author);
        jsonObject.put("voice", voice);

        return jsonObject;
    }
}
