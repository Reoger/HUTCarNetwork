package com.com.reoger.music.Utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;


import com.com.reoger.music.mode.Music;
import com.cwp.android.baidutest.R;

import java.util.ArrayList;


/**
 * Created by 24540 on 2016/4/19.
 * 歌曲列表的适配器
 */
public class MyAdapter extends BaseAdapter implements SectionIndexer {
    private ArrayList<Music> data = null;
    private LayoutInflater layoutInflater;
    private Context context;


    public MyAdapter(Context context, ArrayList<Music> data) {
        this.context = context;
        this.data = data;
        this.layoutInflater = LayoutInflater.from(context);
    }

    /**
     * 当ListView数据发生变化时,调用此方法来更新ListView
     *
     * @param list
     */
    public void updateListView(ArrayList<Music> list) {
        this.data = list;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int postition, View converView, ViewGroup parents) {
        handlerp p;
        final Music Model = data.get(postition);
        if (converView == null) {
            //获取组布局
            p = new handlerp();
            converView = layoutInflater.inflate(R.layout.layout_list_item, null);
            p.mSongName = (TextView) converView.findViewById(R.id.id_song_name);
            p.mCurrpaly = (ImageView) converView.findViewById(R.id.id_playing);
        //    p.mMenuRight = (ImageButton) converView.findViewById(R.id.id_menu_item);
            p.mSingerName = (TextView) converView.findViewById(R.id.id_singer_name);
            p.mSongTime = (TextView) converView.findViewById(R.id.id_item_time);
            p.tvLetter = (TextView) converView.findViewById(R.id.catalog);//字母

            converView.setTag(p);
        } else {
            p = (handlerp) converView.getTag();
        }
        //根据position获取分类的首字母的Char ascii值
        int section = getSectionForPosition(postition);

        //如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
        if (postition == getPositionForSection(section)) {
            p.tvLetter.setVisibility(View.VISIBLE);
            p.tvLetter.setText(Model.getSortLetters());
        } else {
            p.tvLetter.setVisibility(View.GONE);
        }

        p.mSingerName.setText(data.get(postition).getmMusicArtist());
        p.mSongName.setText(data.get(postition).getmMusicName());
        p.mSongTime.setText(data.get(postition).getmMusicTime());
     //   p.mMenuRight.setOnClickListener(v -> Toast.makeText(context, "你点击了子菜单", Toast.LENGTH_SHORT).show());


        return converView;
    }


    @Override
    public Object[] getSections() {
        return new Object[0];
    }

    /**
     * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
     */
    @Override
    public int getPositionForSection(int sectionIndex) {
        for (int i = 0; i < getCount(); i++) {
            String sortStr = data.get(i).getSortLetters();
            char firstChar = sortStr.toUpperCase().charAt(0);
            if (firstChar == sectionIndex) {
                return i;
            }
        }

        return -1;
    }

    /**
     * 根据ListView的当前位置获取分类的首字母的Char ascii值
     */
    @Override
    public int getSectionForPosition(int position) {
        return data.get(position).getSortLetters().charAt(0);
    }

    /**
     * 提取英文的首字母，非英文字母用#代替。
     *
     * @param str
     * @return
     */
    private String getAlpha(String str) {
        String sortStr = str.trim().substring(0, 1).toUpperCase();
        // 正则表达式，判断首字母是否是英文字母
        if (sortStr.matches("[A-Z]")) {
            return sortStr;
        } else {
            return "#";
        }
    }

    public class handlerp {
        public TextView mSongName;
        public TextView mSingerName;
        public TextView mSongTime;
        public ImageButton mMenuRight;
        public ImageView mCurrpaly;
        public TextView tvLetter;

    }

}
