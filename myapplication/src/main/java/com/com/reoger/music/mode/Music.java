package com.com.reoger.music.mode;

/**
 * Created by 24540 on 2016/4/19.
 */
public class Music {
    private String mMusicPath;
    private String mMusicName;
    private String mMusicTime;
    private String mMusicArtist; //歌手
    private String sortLetters;  //字母
    private int currentIndex ;
    private int mMusicRating;

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
    }


    public void setSortLetters(String sortLetters) {
        this.sortLetters = sortLetters;
    }


    public String getmMusicTime() {
        return mMusicTime;
    }


    public void setmMusicTime(String mMusicTime) {
        this.mMusicTime = mMusicTime;
    }

    public String getmMusicArtist() {
        return mMusicArtist;
    }

    public void setmMusicArtist(String mMusicArtist) {
        this.mMusicArtist = mMusicArtist;
    }





    public void setmMusicRating(int mMusicRating) {
        this.mMusicRating = mMusicRating;
    }


    public String getSortLetters() {
        return sortLetters;
    }


    public String getmMusicPath() {
        return mMusicPath;
    }

    public void setmMusicPath(String mMusicPath) {
        this.mMusicPath = mMusicPath;
    }

    public String getmMusicName() {
        return mMusicName;
    }

    public void setmMusicName(String mMusicName) {
        this.mMusicName = mMusicName;
    }


}
