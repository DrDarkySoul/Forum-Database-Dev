package Helpers;

/**
 * Created by Rishat_Valitov on 16.06.17.
 */

public class Helper {
    private Integer id = 0;
    private String slug = "";
    private String flag = "";

    public static Integer getForum() {
        return forum;
    }

    public static void setForum(Integer forum) {
        Helper.forum = forum;
    }

    public static Integer getPost() {
        return post;
    }

    public static void setPost(Integer post) {
        Helper.post = post;
    }

    public static Integer getThread() {
        return thread;
    }

    public static void setThread(Integer thread) {
        Helper.thread = thread;
    }

    public static Integer getUser() {
        return user;
    }

    public static void setUser(Integer user) {
        Helper.user = user;
    }

    static private Integer forum = 0;
    static private Integer post = 0;
    static private Integer thread = 0;
    static private Integer user = 0;

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
        forum += 1;
    }

    public static void incPost(Integer num) {
        post += num;
    }

    public static void incUser() {
        user += 1;
    }

    public static void incThread() {
        thread += 1;
    }

    public static void toZero() {
        forum = 0;
        user = 0;
        post = 0;
        thread = 0;
    }
}
