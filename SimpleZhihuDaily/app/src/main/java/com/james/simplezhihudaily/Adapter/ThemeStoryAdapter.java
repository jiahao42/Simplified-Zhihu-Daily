package com.james.simplezhihudaily.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import com.james.simplezhihudaily.Model.ThemeStory;
import com.james.simplezhihudaily.R;

public class ThemeStoryAdapter extends ArrayAdapter<ThemeStory> {
    private int resourceID;
    private View view;
    private ViewHolder viewHolder;
    private int length;
    public ThemeStoryAdapter(Context context, int textViewResourceID, List<ThemeStory> objects){
        super(context,textViewResourceID,objects);
        length = objects.size();
        resourceID = textViewResourceID;
    }
    public int getLength(){
        return length;
    }

    /**
     *
     * @param position
     * @param convertView   将之前加载好的布局进行缓存，以便之后可以重用
     * @param parent
     * @return
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        ThemeStory story = getItem(position);
        if (convertView == null){//如果不为空则直接对convertView进行重用
            view = LayoutInflater.from(getContext()).inflate(resourceID,null);
            viewHolder = new ViewHolder();
            viewHolder.title = (TextView)view.findViewById(R.id.title);
            viewHolder.imageView = (ImageView) view.findViewById(R.id.title_image);
            view.setTag(viewHolder);
        }else {
            view = convertView;
            viewHolder = (ViewHolder)view.getTag();
        }
        viewHolder.title.setText(story.getTitle());
        viewHolder.imageView.setImageBitmap(story.getBitmap());
        return view;
    }
    private class ViewHolder
    {
        TextView title;
        ImageView imageView;
    }
}
