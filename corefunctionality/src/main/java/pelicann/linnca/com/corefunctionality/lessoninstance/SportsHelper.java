package pelicann.linnca.com.corefunctionality.lessoninstance;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

import pelicann.linnca.com.corefunctionality.connectors.EndpointConnectorReturnsXML;
import pelicann.linnca.com.corefunctionality.connectors.SPARQLDocumentParserHelper;
import pelicann.linnca.com.corefunctionality.connectors.WikiBaseEndpointConnector;
import pelicann.linnca.com.corefunctionality.connectors.WikiDataSPARQLConnector;
import pelicann.linnca.com.corefunctionality.connectors.WikipediaConnector;
import pelicann.linnca.com.corefunctionality.db.Database;
import pelicann.linnca.com.corefunctionality.userinterests.WikiDataEntity;

/*
 * Wikipedia のページを探索して、
 * スポーツ名につける動詞を判断する
 *
 */

//一回やって、結果をどこかに保存する用
//スポーツの例文が出てきたときにすぐに対応できるように
//I play soccer
//I do karate
//I (go) ski(ing)
//の三種類からどれが最適か判断する


/*
 * 基本的にplayは球技またはチームスポーツ。
 * ingがつくのは個人でやる動作。
 * その他はdo。
 * playはwikipediaでplayが使われているかで行けそう。
 * 数えたところn>2で行けそう
 * 例外はwomen's tennis
 * その他、curlingとhurlingはチームスポーツだからplay。
 * finswimmingとかマイナースポーツはわからん笑
 * いろいろ見落としてるかも汗
 */

/*
 * 英語ではこれって話で、ほかの言語ではこの区別はないかもしれない。
 * 実際日本語はすべて「する」。
 * だから、外国語の訳はここに入れるとややこしくなる？
 * ともかく、今はこれで十分
 */

/*
 * 今のところinflectionはNLGのAPIは使わず自分でやる。
 * あまりスポーツのような特例はないから。。
 */

public class SportsHelper {
	private static String TAG = "sportsHelper";
	private WikiDataSPARQLConnector sparqlConn;
	private WikipediaConnector wikipediaConn;
	private Database db;
	
	public static final String PAST = "past";
	public static final String PRESENT3RD = "present3rd";
	public static final String PASTPARTICIPLE = "pastParticiple";
	public static final String PRESENTPARTICIPLE = "presentParticiple";
	
	public SportsHelper(Database db){
		sparqlConn = new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE);
		wikipediaConn = new WikipediaConnector();
		this.db = db;
	}

	//returns 'V O' or 'V' depending on the sport
	//we want to just pass in the wikidata ID and search the database,
	//but that's hard to do because of the asynchronous nature of Firebase.
	//so we are just inputting the verb and object we fetch from the theme class
	public static String getVerbObject(String verb, String object, String tense){
		String inflectedVerb = inflectVerb(verb, tense);
		String objectPart = "";
		if (!object.equals("")){
			objectPart += " " + object;
		}
		return inflectedVerb + objectPart;
	}

	//to generate all pairs
	public void run(){
		EndpointConnectorReturnsXML.OnFetchDOMListener onFetchDOMListener = new EndpointConnectorReturnsXML.OnFetchDOMListener() {
			@Override
			public boolean shouldStop() {
				return false;
			}

			@Override
			public void onStop() {

			}

			@Override
			public void onFetchDOM(Document result) {
				//the result is a list of all sports and a link to their respective
				// Wikipedia pages
				NodeList list = result.getElementsByTagName(WikiDataSPARQLConnector.RESULT_TAG);
				for (int i=0; i<list.getLength(); i++){
					Node n =list.item(i);
					final String finalName = TermAdjuster.adjustSportsEN(
							SPARQLDocumentParserHelper.findValueByNodeName(n, "sportLabel")
					);
					String url = SPARQLDocumentParserHelper.findValueByNodeName(n, "sitelink");
					int lastIndexURL = url.lastIndexOf('/');
					url = url.substring(lastIndexURL+1);
					final String id = WikiDataEntity.getWikiDataIDFromReturnedResult(
							SPARQLDocumentParserHelper.findValueByNodeName(n, "sport")
					);
					//decide verb
					//get # of the word 'played' in the Wikipedia page
					//to see if the sport should use 'play' as the verb
					EndpointConnectorReturnsXML.OnFetchDOMListener onFetchDOMListener2 = new EndpointConnectorReturnsXML.OnFetchDOMListener() {
						@Override
						public boolean shouldStop() {
							return false;
						}

						@Override
						public void onStop() {

						}

						@Override
						public void onFetchDOM(Document result) {
							String text = SPARQLDocumentParserHelper.DOMToString(result);
							//6 = played.length()
							int playedCt = ( text.length() - text.replace("played", "").length() ) / 6;
							String name = finalName;
							String verb;
							List<String> ingExceptions = new ArrayList<>();
							ingExceptions.add("Q213711");//hurling
							ingExceptions.add("Q20898537");//women's curling
							ingExceptions.add("Q1148620");//finswimming
							ingExceptions.add("Q83462");//weightlifting
							ingExceptions.add("Q124100");//bodybuilding
							ingExceptions.add("Q32112");//boxing
							ingExceptions.add("Q838781");//eventing
							ingExceptions.add("Q136851");//curling
							ingExceptions.add("Q1741178");//Kiiking?
							ingExceptions.add("Q1637219");//speedcubing
							ingExceptions.add("Q895138");//powerbocking
							if	(name.length() > 2 &&
									(name.substring(name.length()-3)).equals("ing") &&
									!ingExceptions.contains(id)){
								//for '~ing' we should trim the end
								//ie skiing -> ski
								//but we shouldn't if it's a multiple worded sport
								//ie Greco-Roman wrestling
								//we did Greco-Roman wresling
								//not we Greco-Roman wrestled
								if (name.indexOf(' ') != -1){
									verb = "do";
								} else {
									//we should remove the 'ing'
									verb = name.substring(0, name.length()-3);
									//handle exceptions manually
									if ((verb.substring(verb.length()-2)).equals("mm") ||
											(verb.substring(verb.length()-2)).equals("nn") )
										verb = verb.substring(0,verb.length()-1);

									if (verb.equals("wrestl") || verb.equals("fenc"))
										verb += "e";


									//we don't need a sport name
									//we ski
									//not we ski ski
									name = "";
								}
								//if played was used in the wikipedia page, it most likely uses 'play', not 'do'
							} else if (playedCt > 2 || name.equals("tennis") || name.equals("Goalball")){
								verb = "play";
							} else {
								//everything else will be a do sport
								verb = "do";
							}

							db.addSport(id, verb, name);
						}

						@Override
						public void onError(){}
					};
					List<String> urlList = new ArrayList<>(1);
					urlList.add(url);
					wikipediaConn.fetchDOMFromGetRequest(onFetchDOMListener2, urlList);
				}
			}

			@Override
			public void onError(){

			}
		};
		String sportsQuery = getSportsQuery();
		List<String> queryList = new ArrayList<>(1);
		queryList.add(sportsQuery);
		sparqlConn.fetchDOMFromGetRequest(onFetchDOMListener, queryList);

	}
	
	public static String inflectVerb(String verb, String tense){
		switch (tense){
		case PAST :
			if (verb.equals("do"))
				verb = "did";
			else if (verb.equals("run"))
				verb = "ran";
			else if (verb.equals("swim"))
				verb = "swam";
			else if (verb.charAt(verb.length()-1) == 'e')
				verb += "d";
			else
				verb += "ed";
			break;
		case PRESENTPARTICIPLE :
			if (verb.equals("run"))
				verb += "ning";
			else if (verb.equals("swim"))
				verb += "ming";
			else if (!verb.equals("canoe") && verb.charAt(verb.length()-1) == 'e')
				verb = verb.substring(0, verb.length()-1) + "ing";
			else
				verb += "ing";
			break;
		case PRESENT3RD :
			if(verb.equals("do"))
				verb += "es";
			else
				verb += "s";
			break;
		case PASTPARTICIPLE :
			if (verb.equals("do"))
				verb = "done";
			else if (verb.equals("swim"))
				verb = "swum";
			else if (verb.equals("run"))
				;
			else if (verb.charAt(verb.length()-1) == 'e')
				verb += "d";
			else
				verb += "ed";
			break;
		
		}
		
		return verb;
	}
	
	private String getSportsQuery() {
		return
				"SELECT DISTINCT ?sport ?sportLabel ?sitelink "+
				"WHERE " + 
				"{ " +
				"    {?sport wdt:P279 ?sportTypes} UNION " +
				"	 {?sport wdt:P31 wd:Q31629} UNION " +
				"    {?sport wdt:P31 wd:Q349} . " +
				"    Values ?sportTypes {wd:Q7128792 wd:Q201965 wd:Q212434 wd:Q216048 wd:Q1188693 wd:Q2755547 } . " +
				"    ?sitelink schema:about ?sport . " +
				"    ?sitelink schema:inLanguage 'en' . " +
				"    ?sitelink schema:isPartOf <https://en.wikipedia.org/> . " +
				"    FILTER NOT EXISTS {?sport wdt:P31 wd:Q4167836} . " +
			    "    FILTER NOT EXISTS {?sport wdt:P31 wd:Q7082029} . " +
				"	 SERVICE wikibase:label { bd:serviceParam wikibase:language '" +
						WikiDataSPARQLConnector.ENGLISH + "' }" +
				"}";
	}
	
	
	
}
