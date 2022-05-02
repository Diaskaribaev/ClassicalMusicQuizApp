package com.example.classicalmusicquiz;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;

public class QuizActivity extends AppCompatActivity {

    private static final String TAG = "xz";

    private static final int CORRECT_ANSWER_DELAY_MILLIS = 3000;
    private static final String REMAINING_SONGS_KEY = "remaining_songs";
    private int[] mButtonIDs = {R.id.buttonA, R.id.buttonB, R.id.buttonC, R.id.buttonD};
    private ArrayList<Integer> mRemainingSampleIDs;
    private ArrayList<Integer> mAnswerSampleIDs;
    private int mAnswerSampleID;
    private int mCurrentScore;
    private int mHighScore;
    private Button[] mButtons;
    private MyOnClickListener myOnCLickListener;

    private SimpleExoPlayer mPlayer;
    private PlayerView mPlayerView;
    private PlaybackStateListener playbackStateListener;
    private Sample answerSample;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);
        mPlayerView = findViewById(R.id.playerView);
        playbackStateListener = new PlaybackStateListener();
        myOnCLickListener = new MyOnClickListener();
        initializeQuiz();
        initializePlayer(Uri.parse(answerSample.getUri()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }

    private void initializeQuiz() {
        boolean isNewGame = !getIntent().hasExtra(REMAINING_SONGS_KEY);
        if (isNewGame) {
            QuizUtils.setCurrentScore(this, 0);
            mRemainingSampleIDs = Sample.getAllSampleIDs(this);
        } else {
            mRemainingSampleIDs = getIntent().getIntegerArrayListExtra(REMAINING_SONGS_KEY);
        }
        mCurrentScore = QuizUtils.getCurrentScore(this);
        mHighScore = QuizUtils.getHighScore(this);
        mAnswerSampleIDs = QuizUtils.generateQuestion(mRemainingSampleIDs);
        mAnswerSampleID = QuizUtils.getCorrectAnswerID(mAnswerSampleIDs);
        mPlayerView.setDefaultArtwork(ContextCompat.getDrawable(this, R.drawable.question_mark));
        if (mAnswerSampleIDs.size() < 2) {
            QuizUtils.endGame(this);
            finish();
        }
        mButtons = initializeButtons(mAnswerSampleIDs);
        answerSample = Sample.getSampleByID(this, mAnswerSampleID);

        if (answerSample == null) {
            Toast.makeText(this, getString(R.string.sample_not_found_error),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private Button[] initializeButtons(ArrayList<Integer> answerSampleIDs) {
        Button[] buttons = new Button[mButtonIDs.length];
        for (int i = 0; i < answerSampleIDs.size(); i++) {
            Button currentButton = findViewById(mButtonIDs[i]);
            Sample currentSample = Sample.getSampleByID(this, answerSampleIDs.get(i));
            buttons[i] = currentButton;
            currentButton.setOnClickListener(myOnCLickListener);
            if (currentSample != null) {
                currentButton.setText(currentSample.getComposer());
            }
        }
        return buttons;
    }

    private void initializePlayer(Uri mediaUri) {
      if (mPlayer == null) {
        MediaSource mediaSource = buildMediaSource(mediaUri);
        mPlayer = new SimpleExoPlayer.Builder(this).build();
        mPlayerView.setPlayer(mPlayer);
        mPlayer.addListener(playbackStateListener);
        mPlayer.prepare(mediaSource);
        mPlayer.setPlayWhenReady(true);
      }
    }

    private MediaSource buildMediaSource(Uri uri) {
       DataSource.Factory dataSourceFactory =
               new DefaultDataSourceFactory(this, Util.getUserAgent(this, "ClassicalMusicQuiz"));
       ProgressiveMediaSource.Factory progressiveMediaSourceFactory =
               new ProgressiveMediaSource.Factory(dataSourceFactory);
       MediaSource mediaSource = progressiveMediaSourceFactory.createMediaSource(uri);

       return mediaSource;
    }

    private void releasePlayer() {
        if (mPlayer != null) {
            mPlayer.removeListener(playbackStateListener);
            mPlayer.release();
            mPlayer = null;
        }
    }

    class PlaybackStateListener implements Player.EventListener {
        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            String stateString;
            switch (playbackState) {
                case ExoPlayer.STATE_IDLE:
                    stateString = "ExoPlayer.STATE_IDLE      -";
                    break;
                case ExoPlayer.STATE_BUFFERING:
                    stateString = "ExoPlayer.STATE_BUFFERING -";
                    break;
                case ExoPlayer.STATE_READY:
                    stateString = "ExoPlayer.STATE_READY     -";
                    break;
                case ExoPlayer.STATE_ENDED:
                    stateString = "ExoPlayer.STATE_ENDED     -";
                    break;
                default:
                    stateString = "UNKNOWN_STATE             -";
                    break;
            }
            Log.d(TAG, "changed state to " + stateString + " playWhenReady: " + playWhenReady);
        }
    }

    class MyOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            showCorrectAnswer();
            Button pressedButton = (Button) v;
            int userAnswerIndex = -1;
            for (int i = 0; i < mButtons.length; i++) {
                if (pressedButton.getId() == mButtonIDs[i]) {
                    userAnswerIndex = i;
                }
            }
            int userAnswerSampleID = mAnswerSampleIDs.get(userAnswerIndex);
            if (QuizUtils.userCorrect(mAnswerSampleID, userAnswerSampleID)) {
                mCurrentScore++;
                QuizUtils.setCurrentScore(QuizActivity.this, mCurrentScore);
                if (mCurrentScore > mHighScore) {
                    mHighScore = mCurrentScore;
                    QuizUtils.setHighScore(QuizActivity.this, mHighScore);
                }
            }
            mRemainingSampleIDs.remove(Integer.valueOf(mAnswerSampleID));
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mPlayer.stop();
                    Intent nextQuestionIntent = new Intent(QuizActivity.this, QuizActivity.class);
                    nextQuestionIntent.putExtra(REMAINING_SONGS_KEY, mRemainingSampleIDs);
                    finish();
                    startActivity(nextQuestionIntent);
                }
            }, CORRECT_ANSWER_DELAY_MILLIS);
        }

        private void showCorrectAnswer() {
            mPlayerView.setDefaultArtwork(Sample.getComposerArtBySampleID(QuizActivity.this, mAnswerSampleID));
            for (int i = 0; i < mAnswerSampleIDs.size(); i++) {
                int buttonSampleID = mAnswerSampleIDs.get(i);
                mButtons[i].setEnabled(false);
                if (buttonSampleID == mAnswerSampleID) {
                    mButtons[i].getBackground().setColorFilter(ContextCompat.getColor(QuizActivity.this, android.R.color.holo_green_light),
                        PorterDuff.Mode.MULTIPLY);
                } else {
                    mButtons[i].getBackground().setColorFilter(ContextCompat.getColor(QuizActivity.this, android.R.color.holo_red_light),
                        PorterDuff.Mode.MULTIPLY);
                }
                mButtons[i].setTextColor(Color.WHITE);
            }
        }
    }
}
