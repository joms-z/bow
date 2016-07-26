package util;

import java.util.Collection;
import java.util.List;

/**
 * Created by Joms on 6/30/2016.
 */
public class StringUtils {
    public static boolean isField(String field) {
        return field!=null && !field.trim().equals("");
    }

    public static String joinList(Collection<String> lst, char delimiter) {
        if (lst == null || lst.isEmpty())
            return "";
        StringBuilder res = new StringBuilder();
        for (String s : lst) {
            if (isField(s)) {
                res.append(s);
                res.append(delimiter);
            }
        }
        if (res.charAt(res.length()-1) == delimiter)
            res.deleteCharAt(res.length()-1);
        return res.toString();
    }

    public static String stringOr(String string1, String string2) {
        if (isField(string1))
            return string1;

        return string2;
    }
}
