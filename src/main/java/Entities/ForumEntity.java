package Entities;

import org.json.JSONObject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ForumEntity {

    private String slug, title, user;
    private Integer posts, threads;

    public ForumEntity() {
        this.slug    = "";
        this.title   = "";
        this.user    = "";
        this.posts   = 0;
        this.threads = 0;
    }

    @JsonCreator
    public ForumEntity(
            @JsonProperty("slug")    String slug,
            @JsonProperty("title")   String title,
            @JsonProperty("user")    String user,
            @JsonProperty("posts")   Integer posts,
            @JsonProperty("threads") Integer threads) {
        this.slug    = slug;
        this.title   = title;
        this.user    = user;
        this.posts   = posts;
        this.threads = threads;
    }

    public String getSlug()     { return slug;    }
    public String getTitle()    { return title;   }
    public String getUser()     { return user;    }
    public Integer getPosts()   { return posts;   }
    public Integer getThreads() { return threads; }

    public void setSlug(String slug)        { this.slug = slug;       }
    public void setTitle(String title)      { this.title = title;     }
    public void setUser(String user)        { this.user = user;       }
    public void setPosts(Integer posts)     { this.posts = posts;     }
    public void setThreads(Integer threads) { this.threads = threads; }

    public JSONObject getJSON() {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("slug", slug);
        jsonObject.put("title",title);
        jsonObject.put("user", user);
        jsonObject.put("posts", posts);
        jsonObject.put("threads", threads);
        return jsonObject;
    }

    public String getJSONString() { return this.getJSON().toString(); }
}
