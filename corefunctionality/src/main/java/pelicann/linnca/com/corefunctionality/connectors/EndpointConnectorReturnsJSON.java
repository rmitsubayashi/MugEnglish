package pelicann.linnca.com.corefunctionality.connectors;

import org.json.JSONArray;

import java.util.List;

public interface EndpointConnectorReturnsJSON {
    interface OnFetchJSONListener {
        void onFetchJSONArray(JSONArray result);
    }
    void fetchJSONArrayFromGetRequest(OnFetchJSONListener onFetchJSONListener, List<String> query);
}
