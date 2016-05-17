package com.com.reoger.music.Utils;

import com.com.reoger.music.mode.Music;

import java.util.Comparator;

/**
 * Created by 24540 on 2016/5/13.
 */
public class PinyinComparator implements Comparator<Music> {
    public int compare(Music o1, Music o2) {
        if (o1.getSortLetters().equals("@")
                || o2.getSortLetters().equals("#")) {
            return -1;
        } else if (o1.getSortLetters().equals("#")
                || o2.getSortLetters().equals("@")) {
            return 1;
        } else {
            return o1.getSortLetters().compareTo(o2.getSortLetters());
        }
    }
}
