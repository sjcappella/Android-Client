package com.androidclienttest;

//Question object
public class Question {
	//Question attributes
	private String question;
	private String answerA;
	private String answerB;
	private String answerC;
	private String answerD;
	
	//Constructor that takes all the attributes
	Question(String q, String a, String b, String c, String d){
		//Set the attributes from parameter value
		question = q;
		answerA = a;
		answerB = b;
		answerC = c;
		answerD = d;
	}
	
	//Function to return the question
	public String getQuestion(){
		return question;
	}
	
	//Function to return AnswerA
	public String getAnswerA(){
		return answerA;
	}
	
	//Function to return AnswerB
	public String getAnswerB(){
		return answerB;
	}
	
	//Function to return AnswerC
	public String getAnswerC(){
		return answerC;
	}
	
	//Function to return AnswerD
	public String getAnswerD(){
		return answerD;
	}
	
}
