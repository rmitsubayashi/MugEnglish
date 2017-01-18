package com.example.ryomi.myenglish.questiongenerator;


//DOM
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.example.ryomi.myenglish.connectors.SPARQLDocumentParserHelper;
import com.example.ryomi.myenglish.connectors.EndpointConnector;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;


public abstract class Theme {
	public static int NEW_INSTANCE = -1;
	protected String themeID;
	protected String name;
	protected String description;
	//the user can create multiple instances of the same theme
	protected int instanceID;
	//so we can grab the wikidata IDs
	protected String wikiDataIDPH;
	protected List<Question> questions = new ArrayList<Question>();;
	//これが出題される問題のトピック
	protected Document documentOfTopics = null;
	/*
	 * トピック数が足りないときに付け足す用のバックアップ
	 *IDを入れるかxmlファイルを入れるか悩む
	 *xmlのほうが速いけどID入れるほうが最近のデータをクエリーできる。
	 *今は適当に2，3個ハードコードする。
	 *データベースが整ったら、トップ100ぐらい
	 *( ?entity wikibase:sitelinks ?num で判断)
	 * で事前に検索してデータベースに入れる。
	 * クエリーによっては7~10秒かかる場合もあるから
	 */
	protected Set<String> backupIDsOfTopics = new HashSet<String>();;
	protected int themeTopicCount;
	protected EndpointConnector connector = null;
	
	
	public Theme(EndpointConnector connector){
		this.connector = connector;
	};
	
	public String getThemeID(){
		return this.themeID;
	}
	
	public String getName(){
		return this.name;
	}
	
	public String getDescription(){
		return this.description;
	}
	
	public List<Question> getQuestions(){
		return questions;
	}
	
	public void createQuestions(int instanceID) throws Exception {
		this.instanceID = instanceID;
		
		if (this.instanceID == NEW_INSTANCE){
			createNewQuestions();
		} else {
			recoverOldQuestions(instanceID);
		}
	}
	
	//検索するのは特定のentityひとつに対するクエリー
	//UNIONしてまとめて検索してもいいけど時間が異常にかかる
	protected abstract String getSPARQLQuery();
	//一つ一つのクエリーを送って、まとめる
	protected abstract void populateResults(Set<String> wikiDataIDs) throws Exception;
	//ドキュメントのデータを、わかりやすいクラスに入れる
	protected abstract void processResultsIntoClassWrappers();
	//問題を作ってリストに保存する
	protected abstract void createQuestionsFromResults();
		
	
	protected int countResults(Document doc){
		return doc.getElementsByTagName("result").getLength();
		
	}
	
	protected void addResultsToMainDocument(Document newDocument){
		if (this.documentOfTopics == null){
			this.documentOfTopics = newDocument;
			return;
		}
		
		//<results>タグは一つしかない前提
		Node documentOfTopicsHead = documentOfTopics.getElementsByTagName("results").item(0);
		int dotResultsCount = this.countResults(documentOfTopics);
		NodeList newDocumentResults = newDocument.getElementsByTagName("result");
		int newDocumentResultsCount = newDocumentResults.getLength();
		
		for (int i=0; i<newDocumentResultsCount; i++){
			if (dotResultsCount >= themeTopicCount) return;
			
			Node nextNode = newDocumentResults.item(i);
			//we need to import from new document to main document
			//importNode(Node, deep) where deep = copy children as well
			Node importedNextNode = documentOfTopics.importNode(nextNode, true);
			documentOfTopicsHead.appendChild(importedNextNode);
			dotResultsCount ++;
		}
		
	}
	
	//ひとつのクエリーで複数のエンティティを入れる必要があるかも？？
	protected String addEntityToQuery(String entity){
		String query = this.getSPARQLQuery();
		return String.format(query, entity);
	}
	
	protected String addEntittToQuery(List<String> entityList){
		String query = this.getSPARQLQuery();
		return String.format(query, entityList);
	}
	
	private void populateTopicsWithUserInterests() throws Exception{
		Set<String> userInterests = null;
		this.populateResults(userInterests);
	}
	
	private void populateRemainingTopicsWithBackupTopics() throws Exception{
		if (documentOfTopics == null || this.countResults(documentOfTopics) < themeTopicCount){
			this.populateResults(backupIDsOfTopics);
		}
	}
	
	private void createNewQuestions() throws Exception{
		//add instance into database
		/*PreparedStatement ps = dbConnector.CONN.prepareStatement("SELECT MAX(instanceNumber) AS maxInstanceNumber FROM `questionInstances` WHERE " +
				"userID = ? AND themeID = ?");
		ps.setInt(1, user.getUserID());
		ps.setInt(2, this.themeID);
		ResultSet rs = ps.executeQuery();
		int maxInstanceNumber;
		if (rs.next()){
			maxInstanceNumber = rs.getInt("maxInstanceNumber");
		} else {
			maxInstanceNumber = 0;
		}
		
		int instanceNumber = maxInstanceNumber + 1;
		//create new question instance
		createNewInstance(instanceNumber, user);
		
		
		Statement st3 = dbConnector.CONN.createStatement();
		//grabbing last insert in createNewInstance()
		ResultSet rs3 = st3.executeQuery("SELECT LAST_INSERT_ID() AS id");
		int instanceID;
		if (rs3.next()){
			instanceID = rs3.getInt("id");
		} else {
			System.out.println("Could not create/retrieve question instance");
			return;
		}
		
		this.populateTopicsWithUserInterests(user);
		//ユーザーの興味だけでは質問を作れなかった場合
		this.populateRemainingTopicsWithBackupTopics();
		
		this.saveWikiDataIDsOfInstance(instanceID);
		
		this.processResultsIntoClassWrappers();
		
		this.createQuestionsFromResults();
		*/
	}
	
	private void createNewInstance(int instanceNumber) throws Exception{
		/*Statement st = dbConnector.CONN.createStatement();
		ResultSet rs = st.executeQuery("SELECT currentVersionNumber FROM `themes` WHERE themeID = " + this.themeID + " LIMIT 1");
		String themeVersionNumber = "";
		if (rs.next()){
			themeVersionNumber = rs.getString("currentVersionNumber");
		}
		
		PreparedStatement ps2 = dbConnector.CONN.prepareStatement("INSERT INTO `questioninstances` (themeID, themeVersionNumber, instanceNumber, userID)"
				+ " VALUES (?,?,?,?)");
		ps2.setInt(1, this.themeID);
		ps2.setString(2, themeVersionNumber);
		ps2.setInt(3, instanceNumber);
		ps2.setInt(4, user.getUserID());
		
		ps2.executeQuery();*/
	}
	
	private void recoverOldQuestions(int instanceID) throws Exception{
		/*PreparedStatement ps = dbConnector.CONN.prepareStatement("SELECT wikiDataID FROM `questioninstancewikidataids` WHERE instanceID = ? " );
		ps.setInt(1, instanceID);
		Set<String> instanceWikiDataIDs = new HashSet<String>();
		ResultSet rs = ps.executeQuery();
		while(rs.next()){
			String id = rs.getString("wikiDataID");
			instanceWikiDataIDs.add(id);
		}
		
		this.populateResults(instanceWikiDataIDs);
		//should not have to call this
		//but we might if data on the WikiData database is changed
		this.populateRemainingTopicsWithBackupTopics();
		
		this.processResultsIntoClassWrappers();
		
		this.createQuestionsFromResults();*/
	}
	
	private void saveWikiDataIDsOfInstance(int instanceID) throws Exception{
		//1. loop through document and get all unique wikiData IDs
		//2. save them in a database along with the instance ID
		Set<String> wikiDataIDs = new HashSet<String>();
		NodeList allResults = documentOfTopics.getElementsByTagName("result");
		int resultLength = allResults.getLength();
		for (int i=0; i<resultLength; i++){
			Node head = allResults.item(i);
			String wikiDataID = SPARQLDocumentParserHelper.findValueByNodeName(head, wikiDataIDPH);
			int lastIndexID = wikiDataID.lastIndexOf('/');
			wikiDataID = wikiDataID.substring(lastIndexID+1);
			wikiDataIDs.add(wikiDataID);
			
		}
		
		for (String id : wikiDataIDs){
			/*PreparedStatement ps = dbConnector.CONN.prepareStatement("INSERT INTO `questioninstancewikidataids` (instanceID, wikiDataID) VALUES (?,?)");
			ps.setInt(1, instanceID);
			ps.setString(2, id);
			ps.executeQuery();*/
		}
	}
	
	
}
