package com.example.ryomi.myenglish.tools;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.example.ryomi.myenglish.connectors.WikiDataSPARQLConnector;
import com.example.ryomi.myenglish.connectors.WikipediaConnector;
import com.example.ryomi.myenglish.connectors.SPARQLDocumentParserHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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

public class IdentifyWhetherSportsUsePlayDoGo {
	private WikiDataSPARQLConnector sparqlConn;
	private WikipediaConnector wikipediaConn;
	
	public static final String PAST = "past";
	public static final String PRESENT3RD = "present3rd";
	public static final String PASTPARTICIPLE = "pastParticiple";
	public static final String PRESENTPARTICIPLE = "presentParticiple";
	
	public IdentifyWhetherSportsUsePlayDoGo(){
		sparqlConn = new WikiDataSPARQLConnector();
		wikipediaConn = new WikipediaConnector();
	}
	
	public boolean sportExists(String wikiDataID) throws Exception{
		/*
		PreparedStatement ps = dbConnector.CONN.prepareStatement(
				"Select `wikiDataID` from `sportverbmapping` WHERE `wikiDataID` = ? LIMIT 1");
		ps.setString(1, wikiDataID);
		ResultSet rs = ps.executeQuery();
		return rs.next();*/
		return true;
	}
	
	public String findVerbObject(String wikiDataID, String tense) throws Exception{
		/*PreparedStatement ps = dbConnector.CONN.prepareStatement(
				"SELECT name, verb from `sportverbmapping` WHERE `wikiDataID` = ? LIMIT 1");
		ps.setString(1, wikiDataID);
		ResultSet rs = ps.executeQuery();
		if (!rs.next())
		{
			System.out.println("Couldn't find sport : Class IdentifyWhetherSportsUsePlayDoGo");
			return this.caseOfSportsVerb("play", tense) + " a sport";
		}
		String obj = rs.getString("name");
		String verb = rs.getString("verb");
		//do this manually
		String inflectedVerb = this.caseOfSportsVerb(verb, tense);
		
		String returnString = inflectedVerb;
		if (!obj.equals(""))
			returnString += " " + obj;
		return returnString;*/
		return "";
	}
	
	public void run() throws Exception{
		Document doc = fetchSports();
		NodeList list = doc.getElementsByTagName("result");
		for (int i=0; i<list.getLength(); i++){
			Node n =list.item(i);
			String name = SPARQLDocumentParserHelper.findValueByNodeName(n, "sportLabel");
			String url = SPARQLDocumentParserHelper.findValueByNodeName(n, "sitelink");
			int lastIndexURL = url.lastIndexOf('/');
			url = url.substring(lastIndexURL+1);
			String id = SPARQLDocumentParserHelper.findValueByNodeName(n, "sport");
			int lastIndexID = id.lastIndexOf('/');
			id = id.substring(lastIndexID+1);
			//remove the "women's" part (feminist?)
			name = name.replace("women\'s ", "");
			//~sport ie water sport
			//should always be pluralized??
			//not dancesport
			if(name.length() > 5 && (name.substring(name.length()-6)).equals(" sport") )
				name += "s";
			
			//decide verb
			//get # of the word 'played' in the Wikipedia page
			//to see if the sport should use 'play' as the verb
			String text = wikipediaConn.getWikiDataAsString(url);
			//6 = played.length()
			int playedCt = ( text.length() - text.replace("played", "").length() ) / 6;
			
			String verb = "";
			List<String> ingExceptions = new ArrayList<String>();
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
			if	(name.length() > 2 && 
			(name.substring(name.length()-3)).equals("ing") &&
			!ingExceptions.contains(id)){
				//for '~ing' we should trim the end
				//ie skiing -> skii
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
					
					
					//we don't need a name
					//we ski
					//not we ski ski
					name = "";
				}
			} else if (playedCt > 2 || name.equals("tennis") || name.equals("Goalball")){
				verb = "play";
			} else {
				//everything else will be a do sport
				verb = "do";
			}
			
			//no duplicates
			//update data if exists
			FirebaseAuth auth = FirebaseAuth.getInstance();
			if (auth.getCurrentUser() != null) {
				FirebaseDatabase db = FirebaseDatabase.getInstance();
				String userID = auth.getCurrentUser().getUid();
				DatabaseReference ref = db.getReference("utils/sportsVerbMapping");
				if (ref != null){
					ref.child(id).child("name").setValue(name);
					ref.child(id).child("verb").setValue(verb);
				}
			}
		}
	}
	
	private String caseOfSportsVerb(String verb, String tense){
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
	
	private Document fetchSports() throws Exception{
		String query = 
				"SELECT DISTINCT ?sport ?sportLabel ?sitelink "+
				"WHERE " + 
				"{ " +
				"    {?sport wdt:P279 ?sportTypes} UNION " +
				"	 {?sport wdt:P31 wd:Q31629} . " +
				"    Values ?sportTypes {wd:Q7128792 wd:Q201965 wd:Q212434 wd:Q216048 wd:Q1188693 wd:Q2755547 } . " +
				"    ?sitelink schema:about ?sport . " +
				"    ?sitelink schema:inLanguage 'en' . " +
				"    ?sitelink schema:isPartOf <https://en.wikipedia.org/> . " +
				"    FILTER NOT EXISTS {?sport wdt:P31 wd:Q4167836} . " +
			    "    FILTER NOT EXISTS {?sport wdt:P31 wd:Q7082029} . " +
				"	SERVICE wikibase:label { bd:serviceParam wikibase:language 'en' }" +
				"}";
		
		return sparqlConn.fetchDOMFromGetRequest(query);
	}
	
	
	
}
