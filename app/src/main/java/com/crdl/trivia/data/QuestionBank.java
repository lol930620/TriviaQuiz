package com.crdl.trivia.data;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.crdl.trivia.controller.AppController;
import com.crdl.trivia.model.Question;

import org.json.JSONArray;
import org.json.JSONException;
import java.util.ArrayList;
import java.util.List;

import static com.crdl.trivia.controller.AppController.TAG;

public class QuestionBank {

    ArrayList<Question> questionArrayList = new ArrayList<>();

    //API for questions
    //JSON Array of question-answer array
    private String url = "https://raw.githubusercontent.com/curiousily/simple-quiz/master/script/statements-data.json";

    public List<Question> getQuestions(final AnswerListAsyncResponse callBack){

        //fetch JSON question data
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                (JSONArray) null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        //receive info/data stored in the calling API/url
                        //response = array of question array in the API url
                        //Log.d(TAG,"OnResponse: "+ response);

                        for(int i = 0; i < response.length(); i++){
                            try {
                                Question question = new Question();
                                question.setAnswer(response.getJSONArray(i).get(0).toString());
                                question.setAnswerTrue(response.getJSONArray(i).getBoolean(1));

                                //add question object to the list
                                questionArrayList.add(question);

                                //0th index of the JSON array, the question
                                //Log.d("JSON", "onResponse: "+response.getJSONArray(i).get(0));
                                //1st index of the JSON array, the true or false answer
                                //Log.d("JSON", "onResponse: "+response.getJSONArray(i).getBoolean(1));

                                //Log.d("Hello", "onResponse: "+question);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        if(null != callBack){
                            callBack.processFinished(questionArrayList);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }
        ){

        };

        //add the request to queue in AppController
        AppController.getInstance().addToRequestQueue(jsonArrayRequest);

        //return the stored questions and answers
        return questionArrayList;
    }
}
