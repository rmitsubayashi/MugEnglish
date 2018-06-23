package pelicann.linnca.com.corefunctionality.connectors;

import org.json.JSONArray;

import java.util.List;

public interface EndpointConnectorReturnsJSON {
    interface OnFetchJSONListener {
        void onFetchJSONArray(JSONArray result);
        //so far we don't have any APIs returning JSONObjects
    }
    void fetchJSONArrayFromGetRequest(OnFetchJSONListener onFetchJSONListener, List<String> query);
}
