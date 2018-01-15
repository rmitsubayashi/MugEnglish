package pelicann.linnca.com.corefunctionality.connectors;

import org.json.JSONArray;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class FacebookAPIConnector implements EndpointConnectorReturnsJSON {
    public static final String SELF_USER_ID = "me";

    public FacebookAPIConnector(){}

    @Override
    public void fetchJSONArrayFromGetRequest(final OnFetchJSONListener listener, List<String> params) {
        int parameterCt = params.size();
        ArrayBlockingQueue<Runnable> taskQueue = new ArrayBlockingQueue<>(parameterCt);
        int coreCt = Runtime.getRuntime().availableProcessors();
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(coreCt, coreCt,
                1, TimeUnit.SECONDS, taskQueue,
                new ThreadPoolExecutor.DiscardOldestPolicy());
        for (final String parameter : params) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        HttpURLConnection conn = formatHttpConnection(parameter);
                        InputStream is = conn.getInputStream();
                        //convert input stream to string
                        Scanner s = new Scanner(is).useDelimiter("\\A");
                        String str = s.hasNext() ? s.next() : "";

                        JSONArray jsonArray = new JSONArray(str);
                        listener.onFetchJSONArray(jsonArray);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            executor.execute(runnable);
        }
    }

    private HttpURLConnection formatHttpConnection(String... parameterValue) throws Exception{
        String urlString = formatURL(parameterValue);
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-agent", "Mozzila/5.0");

        return conn;
    }

    private String formatURL(String... params){
        String accessToken = params[0];
        String pageID = params[1];
        String fields = params[2];

        return "https://graph.facebook.com/"+pageID+"?fields="+fields+"&access_token="+accessToken;
    }
}
