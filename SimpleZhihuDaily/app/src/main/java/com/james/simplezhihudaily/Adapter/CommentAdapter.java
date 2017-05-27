package com.james.simplezhihudaily.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import com.james.simplezhihudaily.Model.Comment;
import com.james.simplezhihudaily.R;

public class CommentAdapter extends ArrayAdapter<Comment> {
    private int resourceID;
    private View view;
    private ViewHolder viewHolder;
    private int length;
    public CommentAdapter(Context context, int textViewResourceID, List<Comment> objects){
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
        Comment comment = getItem(position);
        if (convertView == null){//如果不为空则直接对convertView进行重用
            view = LayoutInflater.from(getContext()).inflate(resourceID,null);
            viewHolder = new ViewHolder();
            viewHolder.content = (TextView)view.findViewById(R.id.content);
            viewHolder.avatar = (ImageView) view.findViewById(R.id.avatar);
            viewHolder.name = (TextView)view.findViewById(R.id.name);
            viewHolder.time = (TextView)view.findViewById(R.id.time);
            viewHolder.likes = (TextView)view.findViewById(R.id.likes);
            view.setTag(viewHolder);
        }else {
            view = convertView;
            viewHolder = (ViewHolder)view.getTag();
        }
        viewHolder.content.setText(comment.getContent());
        viewHolder.avatar.setImageBitmap(comment.getBitmap());
        viewHolder.likes.setText(comment.getLikes());
        viewHolder.time.setText(comment.getTime());
        viewHolder.name.setText(comment.getAuthor());
        return view;
    }
    private class ViewHolder
    {
        TextView content;
        ImageView avatar;
        TextView name;
        TextView time;
        TextView likes;
    }
}
