package com.example.ryomi.myenglish.connectors;

import java.sql.Connection;
import java.sql.DriverManager;

public class dbConnector {
	public static Connection CONN = null;
	
	public static void init(){
		try {
			CONN = DriverManager.getConnection("jdbc:mariadb://localhost:3306/wikidata-based-english-application", "root", "password");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static boolean isInitiated(){
		return CONN != null;
	}
}
