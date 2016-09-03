package james.com.simplezhihudaily.Model;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import james.com.simplezhihudaily.R;

import static james.com.simplezhihudaily.R.dimen.listView;

public class StoryAdapter extends ArrayAdapter<Story> {
    private int resourceID;
    private View view;
    private ViewHolder viewHolder;
    private int length;
    public StoryAdapter(Context context, int textViewResourceID, List<Story> objects){
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
        Story story = getItem(position);
        if (convertView == null){//如果不为空则直接对convertView进行重用
            view = LayoutInflater.from(getContext()).inflate(resourceID,null);
            viewHolder = new ViewHolder();
            viewHolder.title = (TextView)view.findViewById(R.id.title);
            viewHolder.imageView = (ImageView) view.findViewById(R.id.title_image);
            view.setTag(viewHolder);
            view.setOnTouchListener(new View.OnTouchListener() {
                /*
            通过给listview设置touch listener，
            如果手是向下滑动的(Y比X移动距离大)且滑动距离足够大时，判断是向下反之亦然是向上。
            event move过程会有多次回调，为了保证在一次dowm 向下滑动时，需要在down 时设置标志，
            来保证一次down 向下滑动时只调用动画一次 做显示标题动作。
             */
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    private float lastX;
                    private float lastY;
                    boolean isChange = false;
                    Log.d("onTouch","start");
                    switch (event.getAction())
                    {
                        case MotionEvent.ACTION_DOWN:
                            lastX = event.getX();
                            lastY = event.getY();
                            isChange = false;
                            break;
                        case MotionEvent.ACTION_MOVE:
                            float x = event.getX();
                            float y = event.getY();
                            float xGapDistance = Math.abs(x - lastX);//移动的距离
                            float yGapDistance = Math.abs(y - lastY);
                            boolean isDown = yGapDistance > 4;
                            //没有显示标题时，且是向下的，就显示
                            //boolean isShow = yGapDistance > 8 && xGapDistance < 8 && !mIsShowTitle && isDown;
                            //boolean isHide = yGapDistance > 8 && xGapDistance < 8 && mIsShowTitle && !isDown;
                            boolean isShow = !mIsShowTitle && isDown;
                            boolean isHide = mIsShowTitle && !isDown;
                            Log.d("onTouchShow", String.valueOf(isShow));
                            Log.d("onTouchHide", String.valueOf(isHide));
                            lastX = x;
                            lastY = y;
                            //一次down，只变化一次，防止一次滑动时抖动下，造成某一个的向下时,y比lastY小
                            if (!isChange && mIsfirstVisible && isHide)
                            {
                                // 显示此标题
                                showHideTitle(true, 500);
                                Log.d("onTouchInvokeTrue", "I'm in true");
                                isChange = true;
                            }//显示标题时，且是向上的，就隐蔽
                            else if (!isChange && mIsfirstVisible && isShow)
                            {
                                // 隐蔽标题
                                showHideTitle(false, 500);
                                Log.d("onTouchInvokeFalse", "I'm in false");
                                isChange = true;
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            //if(!mIsTouchHandeled){
                            int position = listView.pointToPosition((int) event.getX(), (int) event.getY());
                            if (position != ListView.INVALID_POSITION)
                            {
                                listView.performItemClick(listView.getChildAt(position - listView.getFirstVisiblePosition()), position, listView.getItemIdAtPosition(position));
                            }
                            break;

                        default:
                            break;
                    }
                    return false;
                }
            });
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
