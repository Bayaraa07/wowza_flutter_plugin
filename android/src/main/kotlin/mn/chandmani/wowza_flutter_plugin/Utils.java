package mn.chandmani.wowza_flutter_plugin;

import androidx.annotation.Nullable;

import org.json.JSONObject;

import java.util.Map;

public class Utils {

    @Nullable
    public static <T> T argument(Object arguments, String key) {
        if (arguments == null) {
            return null;
        } else if (arguments instanceof Map) {
            return (T) ((Map<?, ?>) arguments).get(key);
        } else if (arguments instanceof JSONObject) {
            return (T) ((JSONObject) arguments).opt(key);
        } else {
            throw new ClassCastException();
        }
    }

    public static boolean hasArgument(Object arguments, String key) {
        if (arguments == null) {
            return false;
        } else if (arguments instanceof Map) {
            return ((Map<?, ?>) arguments).containsKey(key);
        } else if (arguments instanceof JSONObject) {
            return ((JSONObject) arguments).has(key);
        } else {
            throw new ClassCastException();
        }
    }
}
