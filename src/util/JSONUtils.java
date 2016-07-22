package util;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Joms on 7/11/2016.
 */
public class JSONUtils {
    //TODO: Better Exception Handling
    public static JSONObject getJSONObjectFromFile(String fileName) {
        JSONParser fileParser = new JSONParser();
        JSONObject obj = null;

        try {
            obj = (JSONObject) fileParser.parse(new FileReader(fileName));
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (ParseException e) {
            e.printStackTrace();
        }

        return obj;
    }

    public static <T> List<T> toList(JSONArray arr, Class<T> type) {
        List<T> list = new ArrayList<>();
        for (int i=0; i<arr.size(); i++) {
            try {
                list.add(type.cast(arr.get(i)));
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    public static void main(String[] args) {
        JSONParser parser = new JSONParser();
        String s = "{\"1\": [\"1\",\"2\",\"3\",\"4\"]}";
        JSONObject obj = null;
        try {
            obj = (JSONObject) parser.parse(s);
        }
        catch(ParseException e) {
            e.printStackTrace();
        }
        JSONArray a = (JSONArray) obj.get("1");
        System.out.println(toList(a, String.class));
        List<String> b = toList(a, String.class);

    }
}
