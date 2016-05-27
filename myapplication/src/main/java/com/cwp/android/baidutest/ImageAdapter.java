package com.cwp.android.baidutest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

/**
 * Created by Adminis on 2016/5/27.
 */
public class ImageAdapter extends BaseAdapter {

    private int[] mId;
    private LayoutInflater mInflater;

    public ImageAdapter(Context context , int[] data){
        this.mId = data;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return Integer.MAX_VALUE;
    }

    @Override
    public Object getItem(int position) {

        return mId[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

      ViewHolder hodler ;
        if(convertView == null){
            hodler = new ViewHolder();
            convertView = mInflater.inflate(R.layout.base_item,null);

            hodler.img = (ImageView) convertView.findViewById(R.id.base_item_iamge);

            convertView.setTag(hodler);
        }else {
            hodler = (ViewHolder) convertView.getTag();
        }

//        hodler.img.setBackgroundResource(R.mipmap.ic_launcher);
        hodler.img.setImageResource(mId[position%9]);

        return convertView;

    }


    public final class ViewHolder{

        public ImageView img;

    }
}
