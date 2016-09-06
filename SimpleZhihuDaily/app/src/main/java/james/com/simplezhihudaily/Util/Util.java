package james.com.simplezhihudaily.Util;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import james.com.simplezhihudaily.Model.DeviceInfo;

public class Util {
    /**
     * 获取设备的屏幕信息
     *
     * @param activity
     * @return
     */
    public static DeviceInfo getDevicesPix(Activity activity) {
        DisplayMetrics metric = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metric);
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.width = metric.widthPixels;  // 屏幕宽度（像素）
        deviceInfo.height = metric.heightPixels;  // 屏幕高度（像素）
        deviceInfo.density = metric.density;  // 屏幕密度（0.75 / 1.0 / 1.5）
        deviceInfo.densityDpi = metric.densityDpi;  // 屏幕密度DPI（120 / 160 / 240）

        Log.i("TAG", "deviceInfo.width" + deviceInfo.width);
        Log.i("TAG", "deviceInfo.height" + deviceInfo.height);
        Log.i("TAG", "deviceInfo.density" + deviceInfo.density);
        Log.i("TAG", "deviceInfo.densityDpi" + deviceInfo.densityDpi);

        return deviceInfo;
        /*
        for (Element img : elementImgs) {
				img.attr("width", (int)(deviceInfo.width/deviceInfo.density) + "px");//设置width属性
			}
         */
    }

    public static String parseDate(String date) {
        String year = date.substring(0, 4);
        String month = date.substring(4, 6);
        String day = date.substring(6, 8);
        Log.d("date", year + month + day);
        return (year + "年" + month + "月" + day + "日");
    }

    /**
     * MD5加密函数
     * @param string    明文
     * @return      密文
     */
    public static String getMD5(String string) {

        byte[] hash;
        try
        {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException("Huh, MD5 should be supported?", e);
        } catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException("Huh, UTF-8 should be supported?", e);
        }
        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash)
        {
            if ((b & 0xFF) < 0x10) hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }

    /**
     * 检测当的网络（WLAN、3G/2G）状态
     *
     * @param context Context
     * @return true 表示网络可用
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null)
        {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected())
            {
                // 当前网络是连接的
                if (info.getState() == NetworkInfo.State.CONNECTED)
                {
                    // 当前所连接的网络可用
                    return true;
                }
            }
        }
        return false;
    }
    public static int safeLongToInt(long l) {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException
                    (l + " cannot be cast to int without changing its value.");
        }
        return (int) l;
    }
    public static class ListUtils {
        public static void setDynamicHeight(ListView mListView) {
            ListAdapter mListAdapter = mListView.getAdapter();
            if (mListAdapter == null) {
                // when adapter is null
                return;
            }
            int height = 0;
            int desiredWidth = View.MeasureSpec.makeMeasureSpec(mListView.getWidth(), View.MeasureSpec.UNSPECIFIED);
            for (int i = 0; i < mListAdapter.getCount(); i++) {
                View listItem = mListAdapter.getView(i, null, mListView);
                listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
                height += listItem.getMeasuredHeight();
            }
            ViewGroup.LayoutParams params = mListView.getLayoutParams();
            params.height = height + (mListView.getDividerHeight() * (mListAdapter.getCount() - 1));
            Log.d("Comment","Height" + params.height);
            mListView.setLayoutParams(params);
            mListView.requestLayout();
        }
    }
    public static String paserTime(int time){
        System.setProperty("user.timezone", "Asia/Shanghai");
        TimeZone tz = TimeZone.getTimeZone("Asia/Shanghai");
        TimeZone.setDefault(tz);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String times = format.format(new Date(time * 1000L));
        System.out.print("日期格式---->" + times);
        return times;
    }

}
