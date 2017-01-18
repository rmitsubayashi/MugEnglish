package com.example.ryomi.myenglish.questiongenerator.questions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.example.ryomi.myenglish.db.database2classmappings.QuestionTypeMappings;
import com.example.ryomi.myenglish.questiongenerator.GrammarRules;
import com.example.ryomi.myenglish.questiongenerator.Question;

//句読点も含む
public class SentencePuzzleQuestion extends Question{
	private String translation;
	//答えは複数可能
	private List<String> answers;
	//ユーザーはこれから文を組む
	private List<String> puzzlePieces;
	
	public SentencePuzzleQuestion(
			String translation, List<String> answers, List<String> puzzlePieces){
		super.questionType = QuestionTypeMappings.SENTENCE_PUZZLE;
		this.translation = translation;
		this.answers = new ArrayList<String>(answers);
		this.puzzlePieces = new ArrayList<String>(puzzlePieces);
	}
	
	public String createQuestionGUI(){
		Collections.shuffle(puzzlePieces);
		String gui = translation + "\n";
		String piecesString = "";
		for (int i=0; i<puzzlePieces.size();i++){
			piecesString += puzzlePieces.get(i) + " | ";
		}
		//remove last " | "
		piecesString = piecesString.substring(0, piecesString.length() - 3);
		gui += piecesString + "\n";
		gui += "訳を選択肢から組み立てて、タイプしてください";
		
		return gui;
		
	}
	
	public boolean checkAnswer(String answer){
		//まず大文字にする
		answer = GrammarRules.uppercaseFirstLetterOfSentence(answer);
		
		if (this.answers.contains(answer))
			return true;
		else
			return false;
	}

}
