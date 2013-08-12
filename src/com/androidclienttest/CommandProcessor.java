package com.androidclienttest;

import java.io.*;
import java.util.ArrayList;

//Object to process all the commands and handle server communication
public class CommandProcessor {
	//All the attribute and variables needed
	private BufferedReader in;
	private PrintWriter out;
	private String name;
	private String ESUUserName;
	private String password;
	private String deviceUID;
	private boolean synchronize;
	private boolean registrationStatus = false;
	private boolean loginStatus = false;
	private String sessionID;
	private ArrayList<String> listOfTests = new ArrayList<String>();
	private ArrayList<Question> loadedTest = new ArrayList<Question>();
	private ArrayList<String> listOfAnswers = new ArrayList<String>();
	private int numTests = -1;
	private int numQuestions;
	private int numCorrect = -1;
	
	//Constructor that accepts a BufferedReader and PrintWriter
	CommandProcessor(BufferedReader input, PrintWriter output){
		in = input;
		out = output;
	}
	
	//Main processing function, accepts the command to process
	public void processCommand(String command){
		//Command to register with the server
		if(command.equals("REGISTER_WITH_SERVER")){
			sendRegister();
		}
		//Command to send registration credentials to server
		if(command.equals("SEND_REGISTRATION_CREDENTIALS")){
			sendCredentials();
		}
		//Command to process a successful registration
		if(command.equals("REGISTRATION_SUCCESSFUL")){
			registrationStatus = true;
			synchronize = false;
		}
		//Command to process a failed registration
		if(command.equals("REGISTRATION_FAILED")){
			registrationStatus = false;
			synchronize = false;
		}
		//Command to login into the server
		if(command.equals("LOGIN_TO_SERVER")){
			loginToServer();
		}
		//Command to send log-in credentials to the server
		if(command.equals("SEND_LOGIN_CREDENTIALS")){
			sendCredentials();
		}
		//Command to process successful login
		if(command.equals("LOGIN_SUCCESSFUL")){
			loginStatus = true;
			synchronize = false;
		}
		//Command to process failed login to server
		if(command.equals("LOGIN_FAILED")){
			loginStatus = false;
			synchronize = false;
		}
		//Command to set the current session ID
		if(command.equals("SET_SESSION_ID")){
			setSessionID();
		}
		//Command to populate the test menu
		if(command.equals("POPULATE_TEST_MENU")){
			populateTestMenu();
		}
		//Command to process loaded test
		if(command.equals("TEST_LOADED")){
			readInTest();
		}
		//Command to grade the test
		if(command.equals("GRADE_TEST")){
			gradeTest();
		}
		//Command to load the test statistics
		if(command.equals("LOAD_TEST_STATISTICS")){
		}
	}
	
	//Function to send the register command to server
	private void sendRegister(){
		//Send command to server and wait till server gets it
		synchronize = true;
		out.println("REGISTER");
		out.flush();
		while(synchronize){
			try{
				processCommand(in.readLine());
			}catch(Exception e){
				//Handle the error
			}	
		}	
	}
	
	//Function to login to the server
	private void loginToServer(){
		synchronize = true;
		//Send command to server, wait till server gets command
		out.println("LOGIN");
		while(synchronize){
			try{
				processCommand(in.readLine());
			}catch(Exception e){
				//Handle the error
			}
		}
		out.flush();
	}
	
	//Function to send credentials to the server
	private void sendCredentials(){
		//Send all the data to the server
		try{
			out.println(name);
			out.flush();
			out.println(ESUUserName);
			out.flush();
			//Hash the password client-side
			out.println(SimpleMD5.MD5(password));
			out.flush();
			out.println(deviceUID);
			out.flush();
			synchronize = false;	
		}catch(Exception e){
			//Handle the error
		}
	}
	
	//Function to set the session ID
	private void setSessionID(){
		synchronize = true;
		//Send command to server and wait till server gets it
		out.println("GET_SESSION_ID");
		out.flush();
		try{
			//Set the session ID
			sessionID = in.readLine();
		}catch(Exception e){
			//Handle the error
		}
	}
	
	//Command to populate the test menu
	private void populateTestMenu(){
		synchronize = true;
		String input;
		//Send the command to the server
		out.println("GET_TEST_MENU");
		out.flush();
		while(synchronize){
			try{
				input = in.readLine();
				if(!input.isEmpty()){
					out.println("END_SYNCHRONIZE");
					out.flush();
					//Get the number of tests
					numTests = Integer.parseInt(input);
					synchronize = false;	
				}else{
					out.println("GET_TEST_MENU");
					out.flush();
				}
			}catch(Exception e){
				//Handle the error
			}
		}
		//When we have the number of tests, read in the test names
		for(int x = 0; x < numTests; x++){
			try{
				//Add the test name to the list
				listOfTests.add(in.readLine());
			}catch(Exception e){
				//Handle the error
			}
		}
	}
	
	//Function to read in the test from the server
	private void readInTest(){
		//Attributes needed to save the questions
		String question = "", answerA = "", answerB = "", answerC = "", answerD = "";
		//Send command to the server
		out.println("GET_TEST");
		out.flush();
		//Try to get the number of questions
		try{
			numQuestions = Integer.parseInt(in.readLine());
		}catch(Exception e){
			//Handle the error
		}
		//Read in all the questions
		for(int x = 0; x < numQuestions; x++){
			try{
				question = in.readLine();
				answerA = in.readLine();
				answerB = in.readLine();
				answerC = in.readLine();
				answerD = in.readLine();
				//Create a new question object with the read in data
				Question testQuestion = new Question(question, answerA, answerB, answerC, answerD);
				//Add the question to the test
				loadedTest.add(testQuestion);
			}catch(Exception e){
				//Handle the error
			}
		}
	}
	
	//Function to grade the test
	private void gradeTest(){
		//-1 Number correct means an error occured
		numCorrect = -1;
		//Send command to the server
		out.println("GRADE_TEST");
		out.flush();
		//For however many questions we have, send our list of answers
		for(int x = 0; x < listOfAnswers.size(); x++){
			out.println(listOfAnswers.get(x));
			out.flush();
		}
		//After sending all the answers, send session ID for security
		out.println(sessionID);
		out.flush();
		//Try to get the number of questions correct back from the server
		try{
			numCorrect = Integer.parseInt(in.readLine());
		}catch(Exception e){
			//Handle the error
		}
	}
	
	//Function to return the session ID
	public String getSessionID(){
		return sessionID;
	}
	
	//Function to set the session ID
	public void setSessionID(String ID){
		sessionID = ID;
	}
	
	//Function to set the credentials of the user
	public void setCredentials(String n, String esuN, String p, String dUID){
		name = n;
		ESUUserName = esuN;
		password = p;
		deviceUID = dUID;
	}
	
	//Function to return registration status
	public boolean getRegistrationStatus(){
		return registrationStatus;
	}
	
	//Function to set the registration status
	public void setRegistrationStatus(boolean t){
		registrationStatus = t;
	}
	
	//Function to get the login status
	public boolean getLoginStatus(){
		return loginStatus;
	}
	
	//Function to set synchronize ability
	public void setSynchronize(boolean b){
		synchronize = b;
	}

	//Function to get synchronize value
	public boolean getSynchronize(){
		return synchronize;
	}
	
	//Function to return the number of tests available
	public int getNumTests(){
		return numTests;
	}
	
	//Function to set the number of questions for a test
	public void setNumQuestions(int x){
		numQuestions = x;
	}
	
	//Function to return the list of tests
	public ArrayList<String> getListOfTests(){
	        return listOfTests;
	}
	
	//Function to return the test
	public ArrayList<Question> getTest(){
		 return loadedTest;
	}
	
	//Function to return the list of answers
	public void setListOfAnswers(ArrayList<String> answers){
		listOfAnswers = answers;
	}
	
	//Function to return the number of correct answers
	public int getNumCorrect(){
		return numCorrect;
	}
}
