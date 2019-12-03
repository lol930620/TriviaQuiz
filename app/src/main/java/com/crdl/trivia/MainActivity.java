package com.crdl.trivia;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.crdl.trivia.data.AnswerListAsyncResponse;
import com.crdl.trivia.data.QuestionBank;
import com.crdl.trivia.model.Question;
import com.crdl.trivia.model.Score;
import com.crdl.trivia.util.Prefs;

import java.util.ArrayList;
import java.util.List;

import static android.provider.Telephony.BaseMmsColumns.MESSAGE_ID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView questionTextview;
    private TextView questionCounterTextview;
    private Button trueButton;
    private Button falseButton;
    private ImageButton previousButton;
    private ImageButton nextButton;
    private int currentQuestionIndex = 0;
    private int currentHighScore = 0;
    private int scoreCounter = 0;
    private Score score;
    private TextView scoreView;
    private TextView highestScoreView;
    private List<Question> questionList;

    private Prefs prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = new Prefs(MainActivity.this);

        nextButton = findViewById(R.id.next_button);
        previousButton = findViewById(R.id.prev_button);
        trueButton = findViewById(R.id.true_button);
        falseButton = findViewById(R.id.false_button);
        questionTextview = findViewById(R.id.question_textview);
        questionCounterTextview = findViewById(R.id.counter_text);

        score = new Score();    //score object
        scoreView = findViewById(R.id.high_score);
        highestScoreView = findViewById(R.id.highest_score);


        nextButton.setOnClickListener(this);
        previousButton.setOnClickListener(this);
        trueButton.setOnClickListener(this);
        falseButton.setOnClickListener(this);


        //get previous state
        currentQuestionIndex = prefs.getState();
        Log.d("State", "onCreate: "+prefs.getState());

        questionList = new QuestionBank().getQuestions(new AnswerListAsyncResponse() {
            @Override
            public void processFinished(ArrayList<Question> questionArrayList) {
                questionTextview.setText(questionArrayList.get(currentQuestionIndex).getAnswer());
                questionCounterTextview.setText(currentQuestionIndex+" / "+questionArrayList.size());
                Log.d("Inside", "processFinished: "+questionArrayList);
            }
        });

        scoreView.setText("Current Score: 0");

        //get data back from shared preferences
        Prefs getSharedData = new Prefs(this);
        //default value 0 in case there is no value in "message" key
        int value = getSharedData.getHighScore();
        highestScoreView.setText("Highest Score: "+value);
    }

    //From implemented View.OnClickListener interface
    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.prev_button:
                if(currentQuestionIndex>0) {
                    currentQuestionIndex = (currentQuestionIndex - 1) & questionList.size();
                    updateQuestion();
                }
                break;
            case R.id.next_button:
                currentQuestionIndex = (currentQuestionIndex+1)%questionList.size();
                updateQuestion();
                break;
            case R.id.true_button:
                checkAnswer(true);
                break;
            case R.id.false_button:
                checkAnswer(false);
                break;
        }
    }

    private void checkAnswer(boolean correct) {
        boolean answerIsTrue = questionList.get(currentQuestionIndex).isAnswerTrue();
        int toastMessageId = 0;

        if(answerIsTrue == correct){
            toastMessageId = R.string.correct_answer;
            fadeView();
            addPoints();
            //updateScore(1);
        }else{
            toastMessageId = R.string.wrong_answer;
            //shake when the user's answer is wrong
            shakeAnimation();
            deductPoints();
            //updateScore(0);
        }

        Toast.makeText(MainActivity.this, toastMessageId, Toast.LENGTH_SHORT).show();

    }

    private void updateQuestion() {
        String question = questionList.get(currentQuestionIndex).getAnswer();
        questionTextview.setText(question);
        questionCounterTextview.setText(currentQuestionIndex+" / "+questionList.size());
    }

    //fading animation method when user answered correct
    private void fadeView(){
        final CardView cardView = findViewById(R.id.cardView);
        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);

        alphaAnimation.setDuration(350);
        alphaAnimation.setRepeatCount(1);
        alphaAnimation.setRepeatMode(Animation.REVERSE);

        cardView.setAnimation(alphaAnimation);

        alphaAnimation.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationStart(Animation animation){
                cardView.setCardBackgroundColor(Color.GREEN);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                cardView.setCardBackgroundColor(Color.WHITE);
                currentQuestionIndex = (currentQuestionIndex+1)%questionList.size();
                updateQuestion();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    //shake animation method when user answered wrong
    private void shakeAnimation(){
        Animation shake = AnimationUtils.loadAnimation(MainActivity.this, R.anim.shake_animation);
        final CardView cardView = findViewById(R.id.cardView);
        cardView.setAnimation(shake);

        shake.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                cardView.setCardBackgroundColor(Color.RED);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                cardView.setCardBackgroundColor(Color.WHITE);
                currentQuestionIndex = (currentQuestionIndex+1)%questionList.size();
                updateQuestion();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                
            }
        });
    }

    private void addPoints(){
        scoreCounter += 100;

        //get data back from shared preferences
        SharedPreferences getSharedData = getSharedPreferences(MESSAGE_ID, MODE_PRIVATE);
        //default value 0 in case there is no value in "message" key
        int value = getSharedData.getInt("high_score", 0);

        score.setScore(scoreCounter);
        scoreView.setText("Current Score: "+Integer.toString(score.getScore()));

        if(scoreCounter > value){
            //save highest score and set the highest score view to 'Highest Score: new highest score'
            Prefs save = new Prefs(this);
            save.saveHighestScore(scoreCounter);
            highestScoreView.setText("Highest Score: "+save.getHighScore());
        }

        Log.d("Score: ", "Current Score: "+score.getScore());
    }

    private void deductPoints(){
        if(scoreCounter > 0) {
            scoreCounter -= 100;
            score.setScore(scoreCounter);
            scoreView.setText("Current Score: "+Integer.toString(score.getScore()));
        }

        Log.d("Score: ", "Current Score: "+score.getScore());
    }

    @Override
    protected void onPause() {
        //save the current state of question when user exits out of the app
        prefs.setState(currentQuestionIndex);

        super.onPause();
    }
}
