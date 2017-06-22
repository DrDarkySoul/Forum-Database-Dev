package Entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.json.JSONObject;

public class ThreadEntity {

    private Integer id, votes;
    private String title, forum, message, created, slug, author;

    public ThreadEntity() {}

    @JsonCreator
    public ThreadEntity(
            @JsonProperty("id") Integer id,
            @JsonProperty("title") String title,
            @JsonProperty("author") String author,
            @JsonProperty("slug") String slug,
            @JsonProperty("message") String message,
            @JsonProperty("forum") String forum,
            @JsonProperty("votes") Integer votes,
            @JsonProperty("created") String created) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.forum = forum;
        this.message = message;
        this.slug = slug;
        this.created = created;
    }

    public void setTitle(String title)     { this.title = title;     }
    public void setSlug(String slug)       { this.slug = slug;       }
    public void setId(Integer id)          { this.id = id;           }
    public void setAuthor(String author)   { this.author = author;   }
    public void setCreated(String created) { this.created = created; }
    public void setForum(String forum)     { this.forum = forum;     }
    public void setMessage(String message) { this.message = message; }
    public void setVotes(Integer votes)    { this.votes = votes;     }

    public String getTitle()      { return title;   }
    public String getSlug()       { return slug;    }
    public Integer getId()        { return id;      }
    public Integer getVotes()     { return votes;   }
    public String getAuthor()     { return author;  }
    public String getCreated()    { return created; }
    public String getForum()      { return forum;   }
    public String getMessage()    { return message; }

    public String getJSONString() { return this.getJSON().toString(); }

    public JSONObject getJSON() {
        final JSONObject jsonObject = new JSONObject();

        jsonObject.put("author", author);
        jsonObject.put("created", created);
        jsonObject.put("forum", forum);
        jsonObject.put("id", id);
        jsonObject.put("message", message);
        jsonObject.put("slug", slug);
        jsonObject.put("title", title);
        jsonObject.put("votes", votes);

        return jsonObject;
    }
}
