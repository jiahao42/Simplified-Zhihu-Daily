package james.com.simplezhihudaily.Model;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import james.com.simplezhihudaily.R;

public class NewsAdapter extends ArrayAdapter<NewsInfo> {
    private int resourceID;
    public NewsAdapter(Context context, int textViewResourceID, List<NewsInfo> objects){
        super(context,textViewResourceID,objects);
        resourceID = textViewResourceID;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        NewsInfo newsInfo = getItem(position);
        View view;
        ViewHolder viewHolder;
        if (convertView == null){
            view = LayoutInflater.from(getContext()).inflate(resourceID,null);
            viewHolder = new ViewHolder();
            viewHolder.title = (TextView)view.findViewById(R.id.title);
            viewHolder.imageView = (ImageView) view.findViewById(R.id.title_image);
            view.setTag(viewHolder);
        }else {
            view = convertView;
            viewHolder = (ViewHolder)view.getTag();
        }
        viewHolder.title.setText(newsInfo.getTitle());
        viewHolder.imageView.setImageBitmap(newsInfo.getBitmap());
        return view;
    }
    private class ViewHolder
    {
        TextView title;
        ImageView imageView;
    }
}
