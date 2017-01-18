package com.example.ryomi.myenglish.questionmanager;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import com.example.ryomi.myenglish.questiongenerator.Question;
import com.example.ryomi.myenglish.questiongenerator.Theme;

//manages the execution of questions
public class QuestionManager {
	//for console application only
	private Scanner scanner;
	private List<ThemeInstance> instanceList = new ArrayList<ThemeInstance>();
	
	public QuestionManager(){

	}
	
	//for console application only
	public void setScanner(Scanner scanner){
		this.scanner = scanner;
	}
	
	public List<String> getListOfInstancesForTheme(Theme theme) throws Exception{
		List<String> result = new ArrayList<String>();
		createInstanceList(theme);
		
		for (ThemeInstance ti : instanceList){
			result.add(ti.createdDate);
		}
		
		return result;
	}
	
	public int getInstanceID(int index){
		return instanceList.get(index).instanceID;
	}
	
	//assumes questions are generated
	public void executeQuestions(Theme theme) throws Exception{
		String themeName = theme.getName();
		System.out.println(themeName);
		
		Timestamp ts = this.getCurrentDate();
		
		List<Question> questionList = theme.getQuestions();
		for (Question q : questionList){
			String questionString = q.createQuestionGUI();
			System.out.println(questionString);
			while (true){
				String answer = scanner.nextLine();
				if (q.checkAnswer(answer)){
					System.out.println("正解!");
					break;
				}
			}
		}
	}
	
	private Timestamp getCurrentDate(){
		Date date = new Date();
		Timestamp ts = new Timestamp(date.getTime());
		return ts;
	}
	
	//to display a list of instances the user can choose from
	private class ThemeInstance {
		private int instanceID;
		private String createdDate;
		
		private ThemeInstance(int id, String date){
			this.instanceID = id;
			this.createdDate = date;
		}
	}
	
	private void createInstanceList(Theme theme) throws Exception{
		/*PreparedStatement ps = dbConnector.CONN.prepareStatement("SELECT instanceID, createdDate FROM `questioninstances` "
				+ "WHERE userID = ? AND themeID = ? ORDER BY createdDate ASC");
		ps.setInt(1, user.getUserID());
		ps.setInt(2, theme.getThemeID());
		ResultSet rs = ps.executeQuery();
		while (rs.next()){
			int instanceID = rs.getInt("instanceID");
			Timestamp createdDate = rs.getTimestamp("createdDate");
			//format to m/d/y
			Date date = new Date(createdDate.getTime());
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
			String formattedDate = sdf.format(date);
			instanceList.add(new ThemeInstance(instanceID, formattedDate));
		}*/
		
	}
	
	private class Attempt {
		private int instanceID;
		private int timeTaken;
		private int date;
		boolean correct;
	}
	
	private void saveAttempt(int themeID, int instanceID){
		
	}
}
