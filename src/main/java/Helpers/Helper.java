package Helpers;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Rishat_Valitov on 16.06.17.
 */

public class Helper {
    private Integer id = 0;
    private String slug = "";
    private String flag = "";

    public static AtomicInteger getForum() {
        return forum;
    }

    public static void setForum(AtomicInteger forum) {
        Helper.forum = forum;
    }

    public static AtomicInteger getPost() {
        return post;
    }

    public static void setPost(AtomicInteger post) {
        Helper.post = post;
    }

    public static AtomicInteger getThread() {
        return thread;
    }

    public static void setThread(AtomicInteger thread) {
        Helper.thread = thread;
    }

    public static AtomicInteger getUser() {
        return user;
    }

    public static void setUser(AtomicInteger user) {
        Helper.user = user;
    }

    static private AtomicInteger forum = new AtomicInteger(0);
    static private AtomicInteger post = new AtomicInteger(0);
    static private AtomicInteger thread = new AtomicInteger(0);
    static private AtomicInteger user = new AtomicInteger(0);

    public Helper(String rawIdentifier) {
        try {
            id = Integer.parseInt(rawIdentifier);
            flag = "id";
        } catch (Exception e) {
            slug = rawIdentifier;
            flag = "slug";
        }
    }

    public String getFlag(){
        return flag;
    }

    public String getSlug(){
        return slug;
    }

    public Integer getId() {
        return id;
    }

    public static String dateFixZero(String date) {
        final StringBuilder time = new StringBuilder(date);
        time.replace(10, 11, "T");
        time.append(":00");
        return time.toString();
    }

    public static String dateFixThree(String date) {
        final StringBuilder time = new StringBuilder(date);

        if (time.substring(time.length() - 3).equals("+00")) {
            time.replace(time.length() - 3, time.length(), "");
        } else if (time.substring(time.length() - 3).equals("+03")) {
            time.replace(time.length() - 3, time.length(), "");
        }
        time.replace(10, 11, "T");
        time.append("+03:00");
        return time.toString();
    }

    public static String dataFixSpace(String date) {
        final StringBuilder time = new StringBuilder(date);
        time.replace(10, 11, " ");
        return time.toString();
    }

    public static void incForum() {
        forum.incrementAndGet();
    }

    public static void incPost(Integer num) {
        post.addAndGet(num);
    }

    public static void incUser() {
        user.incrementAndGet();
    }

    public static void incThread() {
        thread.incrementAndGet();
    }

    public static void toZero() {
        forum.set(0);
        user.set(0);
        post.set(0);
        thread.set(0);
    }
}
