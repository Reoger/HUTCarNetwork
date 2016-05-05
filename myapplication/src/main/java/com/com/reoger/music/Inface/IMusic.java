package com.com.reoger.music.Inface;

import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by 24540 on 2016/4/20.
 */
public interface IMusic {
    /**暂停播放*/
    public void pauseMusic();
    /**继续播放*/
    public void resumeMusic();
    /**停止播放*/
    public void stopMusic();
    /**开始播放*/
    public void startMusic();
    /**更新进度条*/
    public void init(SeekBar seekBar, TextView timeStare, TextView timeTop, IsMusicOver over);
    /**重新开始*/
    public void restMusic();
    /**删除,释放资源*/
    public void removeMusic();
}
