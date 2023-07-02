package in.magicapi.gateway;

import java.util.HashMap;
import java.util.Map;

public interface MagicApiResultListener {
    void onMagicApiResult(Map<String, String> map);
}
