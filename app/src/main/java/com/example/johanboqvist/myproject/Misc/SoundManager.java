package com.example.johanboqvist.myproject.Misc;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import com.example.johanboqvist.myproject.R;

/**
 * Created by johanboqvist on 25/05/16.
 */
public class SoundManager {

    private SoundPool       soundPool;
    private int             sounds[] = new int[2];
    public static boolean   SOUND = true;

    public SoundManager(Context context){

        soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);

        sounds[0] = soundPool.load(context.getApplicationContext(), R.raw.coin, 1);
        sounds[1] = soundPool.load(context.getApplicationContext(), R.raw.hit, 1);
    }

    public void playSound(int index, float volume){
        if(SOUND){
            soundPool.play(sounds[index], volume, volume, 1, 0, 1.0f);
        }
    }
}
  