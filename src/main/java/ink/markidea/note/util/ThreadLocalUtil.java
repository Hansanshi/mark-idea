package ink.markidea.note.util;

/**
 * @author hansanshi
 */
public class ThreadLocalUtil {

    private static ThreadLocal<String> threadLocalUsername = new ThreadLocal<>();

    public static void setUsername(String username) {
        threadLocalUsername.set(username);
    }

    public static String getUsername(){
        return threadLocalUsername.get();
    }

    public static void clearUsername(){
        threadLocalUsername.remove();
    }
}
