package com.example.classicalmusicquiz;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

class QuizUtils {

    private static final String CURRENT_SCORE_KEY = "current_score";
    private static final String HIGH_SCORE_KEY = "high_score";
    private static final String GAME_FINISHED = "game_finished";
    private static final int NUM_ANSWERS = 4;


    static ArrayList<Integer> generateQuestion(ArrayList<Integer> remainingSampleIDs){

        Collections.shuffle(remainingSampleIDs);

        ArrayList<Integer> answers = new ArrayList<>();

        for(int i = 0; i < NUM_ANSWERS; i++){
            if(i < remainingSampleIDs.size()) {
                answers.add(remainingSampleIDs.get(i));
            }
        }

        return answers;
    }


    static int getHighScore(Context context){
        SharedPreferences mPreferences = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return mPreferences.getInt(HIGH_SCORE_KEY, 0);
    }


    static void setHighScore(Context context, int highScore){
        SharedPreferences mPreferences = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(HIGH_SCORE_KEY, highScore);
        editor.apply();
    }


    static int getCurrentScore(Context context){
        SharedPreferences mPreferences = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return mPreferences.getInt(CURRENT_SCORE_KEY, 0);
    }


    static void setCurrentScore(Context context, int currentScore){
        SharedPreferences mPreferences = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(CURRENT_SCORE_KEY, currentScore);
        editor.apply();
    }


    static int getCorrectAnswerID(ArrayList<Integer> answers){
        Random r = new Random();
        int answerIndex = r.nextInt(answers.size());
        return answers.get(answerIndex);
    }


    static boolean userCorrect(int correctAnswer, int userAnswer){
        return userAnswer == correctAnswer;
    }



    static void endGame(Context context){
        Intent endGame = new Intent(context, MainActivity.class);
        endGame.putExtra(GAME_FINISHED, true);
        context.startActivity(endGame);
    }
}

