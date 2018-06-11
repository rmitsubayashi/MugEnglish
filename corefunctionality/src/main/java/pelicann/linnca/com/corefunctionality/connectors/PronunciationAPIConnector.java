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

//fetches pronunciation from mecapi
//note: we can install this program on our own server
//if we end up using a server
public class PronunciationAPIConnector implements EndpointConnectorReturnsJSON {
    public PronunciationAPIConnector(){}

    @Override
    public void fetchJSONArrayFromGetRequest(final OnFetchJSONListener listener, List<String> parameters){
        int parameterCt = parameters.size();
        ArrayBlockingQueue<Runnable> taskQueue = new ArrayBlockingQueue<>(parameterCt);
        int coreCt = Runtime.getRuntime().availableProcessors();
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(coreCt, coreCt,
                1, TimeUnit.SECONDS, taskQueue,
                new ThreadPoolExecutor.DiscardOldestPolicy());
        for (final String parameter : parameters) {
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

    private HttpURLConnection formatHttpConnection(String parameterValue) throws Exception{
        String urlString = formatURL(parameterValue);
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-agent", "Mozzila/5.0");

        return conn;
    }

    private String formatURL(String parameter){
        return "http://yapi.ta2o.net/apis/mecapi.cgi?sentence="+parameter+
                "&response=pronounciation&format=json";
    }
}
