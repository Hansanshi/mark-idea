package ink.markidea.note.util;


import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * @author hansanshi
 */
public class MD5Util {

    private static final String PASSWORD_SALT = "dhijruyht#$%utjitu";

    private static   String byteArrayToHexString(byte[] b) {
        StringBuilder resultSb = new StringBuilder();
        for (byte value : b) {
            resultSb.append(byteToHexString(value));
        }

        return resultSb.toString();
    }

    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0){
            n += 256;
        }
        int d1 = n / 16;
        int d2 = n % 16;
        return HEX_DIGITS[d1] + HEX_DIGITS[d2];
    }

    /**
     *@author hansanshi
     */
    private static String MD5Encode(String origin) {

        String resultString = null;
        try {
            resultString = origin;
            MessageDigest md = MessageDigest.getInstance("MD5");
            resultString = byteArrayToHexString(md.digest(resultString.getBytes(StandardCharsets.UTF_8)));

        } catch (Exception e) {
        }
        return resultString.toUpperCase();
    }

    public static   String MD5EncodeUtf8(String origin) {
        origin = origin + PASSWORD_SALT;
        return MD5Encode(origin);
    }


    private static final String[] HEX_DIGITS = {"0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};
}
