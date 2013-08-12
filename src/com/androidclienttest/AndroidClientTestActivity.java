package com.androidclienttest;

import android.app.Activity;
import android.content.Context;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import java.net.*;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import android.widget.*;
import android.view.*;
import android.provider.Settings.Secure;

//Main class for the Android Client Application
public class AndroidClientTestActivity extends Activity implements View.OnClickListener{
	
	//All the variables and attributes
	private BufferedReader communityReader;
	private PrintWriter communityWriter;
	private String sessionID;
	private String userName;
	private Spinner testSplashScreenSpinner;
	private String userSelectedTest;
	private ArrayList<Question> listOfQuestions = new ArrayList<Question>();
	private ArrayList<String> listOfAnswers = new ArrayList<String>();
	private ArrayList<String> scores = new ArrayList<String>();
	private int questionIndex = 0;
	private int numCorrect;
	private int numberOfTimesTaken; 
	private double averageScore;
	private int totalScore;
	private int lowScore;
	private int highScore;
	
    
	/** Called when the activity is first created. */
	 //Setting view
    public void onClick(View v){}
    
	@Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        //Set the screen to the main view
        setContentView(R.layout.main);   
    }
    
	//Function that will change the screen to the main screen
    public void goToMainScreen(View v){
    	setContentView(R.layout.main);
    }
    
    //Function that will change the screen to the register/login screen
    public void goToRegisterLogin(View v){
    	communityWriter.println("CLEAN_UP");
    	setContentView(R.layout.register_login);
    }
   
    //On button click, go to the Register Screen
    public void goToRegisterScreen(View v){
    	setContentView(R.layout.register);
    }
    
    //Function to go to the login screen
    public void goToLoginScreen(View v){
    	setContentView(R.layout.login_screen);
    }
    
    //Function to go the the test splash screen
    public void goToTestSplashScreen(){
    	setContentView(R.layout.test_splash_screen);
    	CommandProcessor commandProcessor = new CommandProcessor(communityReader, communityWriter);
    	//Create the welcome message, test spinner header , and test spinner
    	final TextView welcomeMessage = (TextView) findViewById(R.id.welcome);
    	final TextView spinnerHeader = (TextView) findViewById(R.id.selectTestText);
    	final Spinner testMenu = (Spinner) findViewById(R.id.testPicker);
    	//Set welcome message
    	welcomeMessage.setText("Welcome, " + userName);
    	
    	//Create an ArrayAdapter that will populate the spinner
    	ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item);
    	spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	
    	//Clear the list of tests and get the new list
    	commandProcessor.getListOfTests().clear();
    	commandProcessor.processCommand("POPULATE_TEST_MENU");
    	
    	//Add the names of the test as elements to the spinner object
    	for(int x = 0; x < commandProcessor.getNumTests(); x++){
    		spinnerArrayAdapter.add(commandProcessor.getListOfTests().get(x));
    	}
    	
    	//Set spinner header
    	spinnerHeader.setText("Please select any of the " + commandProcessor.getNumTests() + " tests to take.");
    	//Set the options in the spinner
    	testMenu.setAdapter(spinnerArrayAdapter);
    	//Update the screen
    	updateSplashScreenSpinner(testMenu);
    }
    
    //Function to load the test picked by the user
    public void getSelectedTest(View v){
    	//Create new command processor
    	CommandProcessor commandProcessor = new CommandProcessor(communityReader, communityWriter);
    	//Get the selected test
    	userSelectedTest = testSplashScreenSpinner.getSelectedItem().toString();
    	//Send command to the server
    	communityWriter.println("LOAD_TEST");
    	communityWriter.flush();
    	communityWriter.println(userSelectedTest);
    	communityWriter.flush();
    	try{
    		//Read in the result
    		String result = communityReader.readLine();
    		//If an error occured, go to error screen
    		if(result.equals("ERROR")){
    			setContentView(R.layout.error);
    		}else{
    			//If it isn't an error, process the command
	    		commandProcessor.processCommand(result);
	    		//Load the test after the command is processed
	    		for(int x = 0; x < commandProcessor.getTest().size(); x++){
	    			listOfQuestions.add(commandProcessor.getTest().get(x));
	    			listOfAnswers.add("");
	    		}
	    		//Set global question index to 0
	    		questionIndex = 0;
	    		//Go to the test interface
	    		goToTestInterface();
    		}
    	}catch(Exception e){
    		//Handle the error
    		setContentView(R.layout.error);
    	}
    }
    
    //Function to get the stats of a selected test
    public void getTestStats(View v){
    	//Get the name of the test picked by the user
    	userSelectedTest = testSplashScreenSpinner.getSelectedItem().toString();
    	//Clear all the old test scores
    	scores.clear();
    	//Send command to the server to load the test scores
    	communityWriter.println("LOAD_TEST_STATISTICS");
    	communityWriter.flush();
    	communityWriter.println(userSelectedTest);
    	communityWriter.flush();
    	try{
    		//Read in how many scores are going to come in
    		numberOfTimesTaken = Integer.parseInt(communityReader.readLine());
    		//Set variables for average, low, and high score
    		averageScore = 0;
    		totalScore = 0;
    		lowScore = 0;
    		highScore = 0;
    		int temp;
    		//Read in all the scores
    		for(int x = 0; x < numberOfTimesTaken; x++){
    			scores.add(communityReader.readLine());
    			//Parse and format the scores
    			totalScore += Integer.parseInt(scores.get(x));
    			temp = Integer.parseInt(scores.get(x));
    			//Condition statements to determine high and low scores
    			if(x == 0){
    				highScore = temp;
    				lowScore = temp;
    			}
    			if(temp < lowScore){
    				lowScore = temp;
    			}
    			if(temp > highScore){
    				highScore = temp;
    			}
    		}
    		//Calculate average score
    		averageScore = ((totalScore/numberOfTimesTaken));
    		//Go to the test stats screen with username and test name
    		goToTestStats(userName, userSelectedTest);
    	}catch(Exception e){
    		//Handle the error
    	}
    }
    
    //Function that shows all the score information in the test statistics screen
    private void goToTestStats(String nameOfUser, String testName){
    	//Set the screen view
    	setContentView(R.layout.test_statistics);
    	//Create all the labels, buttons, and alert dialog
    	final TextView nameOfTheUser = (TextView) findViewById(R.id.testStats);
    	final TextView nameOfTheTest = (TextView) findViewById(R.id.testName);
    	final TextView totalTest = (TextView) findViewById(R.id.numberOfTimes);
    	final TextView lowestScore = (TextView) findViewById(R.id.lowestScore);
    	final TextView highestScore = (TextView) findViewById(R.id.highestScore);
    	final TextView averageScoreView = (TextView) findViewById(R.id.averageScore);
    	final Button backButton = (Button) findViewById(R.id.backButton);
    	final Button viewAll = (Button) findViewById(R.id.viewAllScores);
    	final AlertDialog scoreListBox = new AlertDialog.Builder(this).create();
    	final DecimalFormat f = new DecimalFormat("###.##");
    	
    	//Setting the fields based on the provided information
    	nameOfTheUser.setText(nameOfUser + " Statistics");
    	nameOfTheTest.setText(testName);
    	totalTest.setText("Number of Times Taken: " + numberOfTimesTaken);
    	lowestScore.setText("Lowest Score: " + f.format(lowScore));
    	highestScore.setText("Highest Score: " + f.format(highScore));
    	averageScoreView.setText("Average Score: " + f.format(averageScore));
    	scoreListBox.setTitle("All Scores for " + testName + ":");
    	scoreListBox.setMessage("");
    	scoreListBox.setButton("Close", new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
				//scoreListBox.hide();
			}
		});
    	
    	//Implement on click listener for go back to menu button
    	backButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v){
				scores.clear();
				goToTestSplashScreen();
			}
		});
    	
    	//Implement on click listener for view all test scores button
    	viewAll.setOnClickListener(new View.OnClickListener(){
    		@Override
    		public void onClick(View v){
    			String scoreList = "";
    			for(int x = 0; x < scores.size(); x++){
    				scoreList += "Attempt " + (x+1) + ": " + f.format(Integer.parseInt(scores.get(x))) + "\n";
    			}
    			scoreListBox.setMessage(scoreList);
    			scoreListBox.show();    			
    		}
    	});
    }
    
    //Function to update the spinner on the test splash screen
    public void updateSplashScreenSpinner(Spinner s){
    	testSplashScreenSpinner = s;
    }
    
    //Function to go to the test interface
    public void goToTestInterface(){
    	//Set the screen view
    	setContentView(R.layout.test_interface);
    	//Create all the attributes of text labels, buttons, and radio buttons
    	final TextView testName = (TextView) findViewById(R.id.nameOfTest);
    	final TextView questionNumber = (TextView) findViewById(R.id.questionNumber);
    	final TextView question = (TextView) findViewById(R.id.testQuestion);
    	final Button nextButton = (Button) findViewById(R.id.buttonNext);
    	final Button backButton = (Button) findViewById(R.id.buttonBack);
    	final RadioGroup allAnswers = (RadioGroup) findViewById(R.id.radioGroup1);
    	final RadioButton answerA = (RadioButton) findViewById(R.id.radio0);
    	final RadioButton answerB = (RadioButton) findViewById(R.id.radio1);
    	final RadioButton answerC = (RadioButton) findViewById(R.id.radio2);
    	final RadioButton answerD = (RadioButton) findViewById(R.id.radio3);
    	
    	//Set the test name
    	testName.setText(userSelectedTest);
    	//Alter the next button depending on what question the user is on
    	if(questionIndex == (listOfQuestions.size() - 1)){
    		nextButton.setText("Submit");
    	}else{
    		nextButton.setText("Next");
    	}
    	//Set the question text
    	questionNumber.setText("Question #" + (questionIndex+1) + ":");
    	//Clear the answers and then if the user has already answered the question, 
    	//update the question to reflect the user's answer
    	allAnswers.clearCheck();
    	if(listOfAnswers.get(questionIndex).equals("A")){
    		answerA.setChecked(true);
    	}
    	if(listOfAnswers.get(questionIndex).equals("B")){
			answerB.setChecked(true);
	    }
    	if(listOfAnswers.get(questionIndex).equals("C")){
			answerC.setChecked(true);
    	}
    	if(listOfAnswers.get(questionIndex).equals("D")){
			answerD.setChecked(true);
    	}
    	if(listOfAnswers.get(questionIndex).equals("")){
			allAnswers.check(-1);
    	}
    	
    	//Se the text for the question and the possible answers
    	question.setText(listOfQuestions.get(questionIndex).getQuestion());
    	answerA.setText(listOfQuestions.get(questionIndex).getAnswerA());
    	answerB.setText(listOfQuestions.get(questionIndex).getAnswerB());
    	answerC.setText(listOfQuestions.get(questionIndex).getAnswerC());
    	answerD.setText(listOfQuestions.get(questionIndex).getAnswerD());
    	
    	//On-click listener for the next button
    	nextButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//Save the user's answers
				setAnswer(answerA, answerB, answerC, answerD);
				goToNextQuestion();	
			}
		});
    	
    	//On-click listener for the back button
    	backButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//Save the user's answers
				setAnswer(answerA, answerB, answerC, answerD);
				goToPreviousQuestion();			
			}
		});
    }
    
    //Function to go to the next question in the test
    public void goToNextQuestion(){
    	//Check to see if we are at the end of the test
    	if(questionIndex == (listOfQuestions.size() - 1)){
    		//Display short message saying we are at the end of the test
    		Context context = getApplicationContext();
    		CharSequence text = "End of test...";
    		int duration = Toast.LENGTH_SHORT;
    		Toast.makeText(context,  text, duration).show();
    		//Prompt user if they are ready to submit their test
    		final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
    		alertDialog.setTitle("Submit Test");
    		alertDialog.setMessage("You are about to sumbit your test.\nContinue?");
    		//Prompt user that they have some blank answers
    		final AlertDialog alertEmptyAnswers = new AlertDialog.Builder(this).create();
    		alertEmptyAnswers.setTitle("Blank Answers");
    		//Continue with blank answers
    		alertEmptyAnswers.setButton("Continue", new DialogInterface.OnClickListener(){
    			public void onClick(DialogInterface dialog, int which){
    				goToGradingScreen();
    			}
    		});
    		//Go back and answer blank questions
    		alertEmptyAnswers.setButton2("Go Back", new DialogInterface.OnClickListener(){
    			public void onClick(DialogInterface dialog, int which){
    				alertDialog.hide();
    			}
    		});
    		//Cancel submission
    		alertDialog.setButton("Cancel", new DialogInterface.OnClickListener(){
    			public void onClick(DialogInterface dialog, int which){
    				//Do nothing
    			}
    		});
    		//Submit the test to the server
    		alertDialog.setButton2("Submit", new DialogInterface.OnClickListener(){
    			public void onClick(DialogInterface dialog, int which){
    				int unAnswered = 0;
    				//Check for un-answered questions
    				for(int x = 0; x < listOfAnswers.size(); x++){
    					if(listOfAnswers.get(x).equals("")){
    						unAnswered++;
    					}
    				}
    				if(unAnswered != 0){
    					//Pup-up alert for blank answers
    					alertEmptyAnswers.setMessage("You left " + unAnswered + " answer(s) blank.\nGo back and answer them?");
    					alertEmptyAnswers.show();
    				}else{
    					goToGradingScreen();
    				}
    			}
    		});
    		//Show alert for submission
    		alertDialog.show();
    	}else{
    		//Increase index to go to next question
    		questionIndex++;
    		//Go back to the test interface/update
    		goToTestInterface();
    	}
    }
    
    //Function to go back to the previous question
    public void goToPreviousQuestion(){
    	//Check to see if we are at the beginning of the test
    	if(questionIndex == 0){
    		//Display message if we are at the beginning
    		Context context = getApplicationContext();
    		CharSequence text = "This is the first question...";
    		int duration = Toast.LENGTH_SHORT;
    		Toast.makeText(context,  text, duration).show();
    	}else{
    		//Decrease question index
    		questionIndex--;
    		//Update and go to the test interface
    		goToTestInterface();
    	}
    }
    
    //Function to go to the grading screen
    void goToGradingScreen(){
    	//Set the grading screen
    	setContentView(R.layout.grading);
    	//Set the button and progress bar
    	Button viewResults = (Button) findViewById(R.id.viewResults);
    	ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBarGrading);
    	//Set visibility of progress bar
    	viewResults.setVisibility(4);
    	//Create new command processor
    	CommandProcessor commandProcessor = new CommandProcessor(communityReader, communityWriter);
    	//Set the list of answers
    	commandProcessor.setListOfAnswers(listOfAnswers);
    	//Set the session ID
    	commandProcessor.setSessionID(sessionID);
    	//Process the command
    	commandProcessor.processCommand("GRADE_TEST");
    	//get the number correct
    	numCorrect = commandProcessor.getNumCorrect();
    	//Set progress bar to not visible, and view the results
    	progressBar.setVisibility(4);
    	viewResults.setVisibility(0);
    	//On-click listener for viewing the results
    	viewResults.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				viewTestResults();
			}
		});
    }
    
    //Function to go to the results screen
    void viewTestResults(){
    	//Set the screen to the results screen
    	setContentView(R.layout.test_results);
    	//Create all the attributes for text views and button
    	TextView results = (TextView) findViewById(R.id.testResults);
    	TextView scoreDisplay = (TextView) findViewById(R.id.testScore);
    	Button menuButton = (Button) findViewById(R.id.backToMenu);
    	DecimalFormat f = new DecimalFormat("###.##");
    	//Set the text for how many questions are correct
    	results.setText("You got " + numCorrect + " questions correct!");
    	//Calculate the score and format, then display
    	double score = ((double)numCorrect/(double)listOfQuestions.size())*100;
    	scoreDisplay.setText("Your score is " + f.format(score) + "%"); 
    	//On-click listener to go back to the test splash screen
    	menuButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//Clean up function
				cleanUp();
				goToTestSplashScreen();
			}
		});
    }
    
    //Function to clean up some variables to start again
    public void cleanUp(){
    	//Send the clean-up command to the server
    	communityWriter.println("CLEAN_UP");
    	//Clear lists and reset variables
    	listOfQuestions.clear();
    	listOfAnswers.clear();
    	questionIndex = 0;
    	numCorrect = -1;
    }
    
    //Function to set the answer to a question based on a radio group
    public void setAnswer(RadioButton a, RadioButton b, RadioButton c, RadioButton d){
    	//Set blank if nothing
    	listOfAnswers.set(questionIndex, "");
    	//Check for A, B, C, or D answers and set accordingly
    	if(a.isChecked()){
    		listOfAnswers.set(questionIndex, "A");
    	}
    	if(b.isChecked()){
    		listOfAnswers.set(questionIndex, "B");
    	}
    	if(c.isChecked()){
    		listOfAnswers.set(questionIndex, "C");
    	}
    	if(d.isChecked()){
    		listOfAnswers.set(questionIndex, "D");
    	}
    	//Tell the server what the user picked
    	communityWriter.println("User picked " + listOfAnswers.get(questionIndex));
    }
    
    //Function to login into the server
    public void loginToServer(View v){
    	//Create the edit text fields to get user input
    	final EditText loginInput1 = (EditText) findViewById(R.id.loginInput1);
    	final EditText loginInput2 = (EditText) findViewById(R.id.loginInput2);
    	final EditText loginInput3 = (EditText) findViewById(R.id.loginInput3);
    	
    	//Get the values entered by user
    	String name = loginInput1.getText().toString();
    	String ESUUserName = loginInput2.getText().toString();
    	String password = loginInput3.getText().toString();
    	String deviceUID = Secure.getString(getBaseContext().getContentResolver(), Secure.ANDROID_ID);
    	
    	//Display message if the user leaves information out
    	if(name.isEmpty() || ESUUserName.isEmpty() || password.isEmpty() || deviceUID.isEmpty()){
    		Context context = getApplicationContext();
    		CharSequence text = "You are missing some data...";
    		int duration = Toast.LENGTH_SHORT;
    		Toast.makeText(context,  text, duration).show();
    	}else{
    		//Create a new command processor, set the login credentials, and process login command
    		CommandProcessor commandProcessor = new CommandProcessor(communityReader, communityWriter);
    		commandProcessor.setCredentials(name, ESUUserName, password, deviceUID);
    		commandProcessor.processCommand("LOGIN_TO_SERVER");
    		
    		//Set synchronize until we get login status
    		commandProcessor.setSynchronize(true);
    		//process command
    		communityWriter.println("GET_LOGIN_STATUS");
    		while(commandProcessor.getSynchronize()){
    			try{
    				commandProcessor.processCommand(communityReader.readLine());
    			}catch(Exception e){
    				//Handle error
    			}
    		}
    		communityWriter.flush();
    		//If we log in successfully, go to test splash screen
    		if(commandProcessor.getLoginStatus()){
    			Context context = getApplicationContext();
        		CharSequence text = "Log-In Successful!";
        		int duration = Toast.LENGTH_LONG;
        		Toast.makeText(context,  text, duration).show();
        		//Set the session ID for this session
        		commandProcessor.processCommand("SET_SESSION_ID");
        		sessionID = commandProcessor.getSessionID();
          		userName = name;
        		//Go to test splash screen
          		goToTestSplashScreen();
    		}else{
    			//Failed login, go to the error screen
    			setContentView(R.layout.login_fail);
    		}
    	}	
    }
    
    //Function to register with the server
    public void registerWithServer(View v){
    	//Create the user input fields
    	final EditText editText1 = (EditText) findViewById(R.id.registerInput1);
    	final EditText editText2 = (EditText) findViewById(R.id.registerInput2);
    	final EditText editText3 = (EditText) findViewById(R.id.registerInput3);
    	
    	//Get the user input from the input fields
    	String name = editText1.getText().toString();
    	String ESUUserName = editText2.getText().toString();
    	String password = editText3.getText().toString();
    	String deviceUID = Secure.getString(getBaseContext().getContentResolver(), Secure.ANDROID_ID);
    	
    	//Check to see if the user left any information blank and alert them
    	if(name.isEmpty() || ESUUserName.isEmpty() || password.isEmpty() || deviceUID.isEmpty()){
    		Context context = getApplicationContext();
    		CharSequence text = "You are missing some data...";
    		int duration = Toast.LENGTH_SHORT;
    		Toast.makeText(context,  text, duration).show();
    	}else{
    		//Create new command processor, set the credentials, and then process the command
    		CommandProcessor commandProcessor = new CommandProcessor(communityReader, communityWriter);
    		commandProcessor.setCredentials(name, ESUUserName, password, deviceUID);
    		commandProcessor.processCommand("REGISTER_WITH_SERVER");
    		
    		//Set synchronize until we get registration status
    		commandProcessor.setSynchronize(true);
    		communityWriter.println("GET_REGISTRATION_STATUS");
    		while(commandProcessor.getSynchronize()){
    			try{
    				commandProcessor.processCommand(communityReader.readLine());
    			}catch(Exception e){
    				//Handle error
    			}
    		}
    		communityWriter.flush();
    		//Check the registration status for successful registration
    		if(commandProcessor.getRegistrationStatus()){
    			//Display success message
    			Context context = getApplicationContext();
        		CharSequence text = "Registration Successful!";
        		int duration = Toast.LENGTH_LONG;
        		Toast.makeText(context,  text, duration).show();
        		//Go to the register/login screen again
        		setContentView(R.layout.register_login);
    		}else{
    			//Go to the register fail screen
    			setContentView(R.layout.registration_fail);
    		}
    	}
    }
    
    //Initial function when application first loads
    public void initial(View v){
    	//Create input field for IP address	
    	final EditText input = (EditText) findViewById(R.id.ipAddress);
        //Get user input
    	String IPAddress = input.getText().toString();
        //Default client port number
    	int port = 9041;
         	
	    //Try to create the network socket    
	    try{
	    	//create network socket with default port number and user entered IP Address
	      	SocketAddress socketAddress = new InetSocketAddress(IPAddress, port);
	       	Socket socket = new Socket();
	       	socket.connect(socketAddress);	
	       	try{
	       		//Create new BufferedReader and PrintWriter for socket communication
	       		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	       		PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
	       		//Set the global reader and writers
	       		communityReader = in;
	       		communityWriter = out;
	       		//Tell server we are an Android device
	       		out.println("ANDROID_DEVICE");	
	       		//Go to the register/login screen
	       		setContentView(R.layout.register_login);
	         	}catch (Exception e){
	          		//Change to go to an error screen
	         		setContentView(R.layout.error);    		
	        	}
	         }catch (Exception e){
	        	//Change to go to an error screen
	        	setContentView(R.layout.error);
	        	
	        }
	    }

    //Function to go back to the main screen
	public void backToMainScreen(View v){
		setContentView(R.layout.main);
	}
}
