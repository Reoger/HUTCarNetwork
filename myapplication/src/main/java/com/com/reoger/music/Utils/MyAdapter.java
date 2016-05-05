package com.com.reoger.music.Utils;

import android.content.Context;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.com.reoger.music.mode.Music;
import com.cwp.android.baidutest.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by 24540 on 2016/4/19.
 * 歌曲列表的适配器
 */
public class  MyAdapter extends BaseAdapter {
    private ArrayList<HashMap<String,Object>>data;
    private LayoutInflater layoutInflater;
    private Context context;
    private Music m;


    public MyAdapter(Context context,ArrayList<HashMap<String ,Object>> data){
        this.context = context;
        this.data = data;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount(){
        return data.size();
    }

    @Override
    public  Object getItem(int position){
        return data.get(position);
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public View getView(final int postition,View converView,ViewGroup parents){
        handlerp p=null;
        if(converView ==null){
            //获取组布局
           p= new handlerp();
            converView = layoutInflater.inflate(R.layout.layout_list_item,null);
            p.mSongName = (TextView)converView.findViewById(R.id.id_song_name);
            p.mCurrpaly = (ImageView)converView.findViewById(R.id.id_playing);
            p.mMenuRight =(ImageButton)converView.findViewById(R.id.id_menu_item);
            p.mSingerName = (TextView)converView.findViewById(R.id.id_singer_name);
            p.mSongTime = (TextView) converView.findViewById(R.id.id_item_time);
          //  p.mLinearLayout = (LinearLayout)converView.findViewById(R.id.line1);
            converView.setTag(p);
        }else{
            p = (handlerp) converView.getTag();
        }

        p.mSingerName.setText(data.get(postition).get("musicArtist")+"");
        p.mSongName.setText(data.get(postition).get("musicName")+"");
        p.mSongTime.setText(data.get(postition).get("musicTime")+"");
        p.mMenuRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "你点击了子菜单", Toast.LENGTH_SHORT).show();
            }
        });
 //       final handlerp finalP = p;
//        p.mLinearLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//            //    Toast.makeText(context, "你点击"+postition, Toast.LENGTH_SHORT).show();
//                finalP.mCurrpaly.setVisibility(View.VISIBLE);
//                m = new Music();
//                m.setmMusicName(finalP.mSongName + "");
//                m.setmMusicPath(data.get(postition).get("musicPath") + "");
//                MainActivity mainA = new MainActivity();
//            //    mainA.playMusicOnAcivity(postition);//播放选中的音乐
//                LogUtils.i("AD", "穿过来的index所对应的值" + postition);
    //            mainA.test();


  //          }
   //     });

        return converView;
    }



    public class handlerp{
        public TextView mSongName;
        public TextView mSingerName;
        public TextView mSongTime;
        public ImageButton mMenuRight;
        public ImageView mCurrpaly;
        public LinearLayout mLinearLayout;

    }

    /**
     * 播放音乐的逻辑代码 test
     */
    public void playMusic()  {
               MediaPlayer mediaPlayer = new MediaPlayer();
        try{
            mediaPlayer.setDataSource(m.getmMusicPath());
            mediaPlayer.prepare();
        }catch (Exception e){
            e.printStackTrace();
        }

        mediaPlayer.start();
    }


}
