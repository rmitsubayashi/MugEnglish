package pelicann.linnca.com.corefunctionality.connectors;

import org.w3c.dom.Document;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public abstract class WikiBaseEndpointConnector implements EndpointConnectorReturnsXML {

	/*
	 * WikiDataのデータはjsonとxml形式、どっちかを選べる。
	 * ビルゲーツの出身学校をクエリーしたところ、
	 * クエリーの時間はxmlとjsonでほとんど変わらなかった。
	 * httpURLConnectionで送る→返信→コンソールにアウトプットする時間
	 * どっちも660ms ~ 700msの間の数値。
	 * 「どちらが速いから」は採用理由として不適切？？
	 * 
	 * 見やすいのはどっち？
	 * そんなに変わらないと思う。
	 * https://www.w3.org/TR/sparql11-results-json/
	 * と
	 * https://www.w3.org/TR/rdf-sparql-XMLres/
	 * 両方とも見にくい..
	 *
	 * 追記
	 * FB APIはjsonだから統一するべき？
	 */
	
	//クエリーの言語設定
	//@language@を指定言語に置き換える
	private final String language;
	//main languages
	static public final String ENGLISH = "en";
	static public final String JAPANESE = "ja";
	//if we want to extend to other languages
	static public final String LANGUAGE_PLACEHOLDER = "@language@";
	
	WikiBaseEndpointConnector(){
		this.language = ENGLISH;
	}
	
	WikiBaseEndpointConnector(String language){
		this.language = language;
	}

	//we are creating multiple threads.
	//for each thread, we make a connection to the WikiBase server and get a response.
	@Override
	public void fetchDOMFromGetRequest(final OnFetchDOMListener listener, List<String> parameterValues){
		int parameterCt = parameterValues.size();
		ArrayBlockingQueue<Runnable> taskQueue = new ArrayBlockingQueue<>(parameterCt);
		int coreCt = Runtime.getRuntime().availableProcessors();
		final ThreadPoolExecutor executor = new ThreadPoolExecutor(coreCt, coreCt,
				1, TimeUnit.SECONDS, taskQueue,
				new ThreadPoolExecutor.DiscardOldestPolicy());
		final AtomicBoolean alreadyCalledListenerOnStop = new AtomicBoolean(false);
		for (final String parameter : parameterValues){
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					if (listener.shouldStop()){
						return;
					}
					try {
						HttpURLConnection conn = formatHttpConnection(parameter);
						InputStream resultInputStream = fetchHttpConnectionResponse(conn);
						if (resultInputStream == null)
							return;
						DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
						DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
						Document document = documentBuilder.parse(resultInputStream);
						document.getDocumentElement().normalize();
						if (!listener.shouldStop()) {
							listener.onFetchDOM(document);
						} else {
							//doesn't stop threads already running?
							executor.shutdownNow();
							//just in case a non-killed thread tries
							// to call onStop
							// (we only want one call to the listener's onStop)
							if (!alreadyCalledListenerOnStop.getAndSet(true)) {
								listener.onStop();
							}
						}
					} catch (Exception e){
						listener.onError();
					}
				}
			};
			executor.execute(runnable);
		}
	}
	
	protected abstract String formatURL(String parameterValue);
	
	String formatRequestLanguage(String str){
		return str.replace(LANGUAGE_PLACEHOLDER, language);
	}
	
	private HttpURLConnection formatHttpConnection(String parameterValue) throws Exception{
		String urlString = formatURL(parameterValue);
		URL url = new URL(urlString);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		
		conn.setRequestMethod("GET");
		conn.setRequestProperty("User-agent", "Mozzila/5.0");
		
		return conn;
	}
	
	
	
	private InputStream fetchHttpConnectionResponse(HttpURLConnection conn){
		try {
			return conn.getInputStream();
		} catch (IOException e){
			e.printStackTrace();
			return null;
		}
	}
}