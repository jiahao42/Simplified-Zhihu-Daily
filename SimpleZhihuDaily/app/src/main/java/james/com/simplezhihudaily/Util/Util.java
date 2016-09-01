package james.com.simplezhihudaily.Util;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.util.Log;

import james.com.simplezhihudaily.Model.DeviceInfo;

public class Util {
    /**
     * 获取设备的屏幕信息
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
    public static String analyzeDate(String date){
        String year = date.substring(0,4);
        String month = date.substring(4,6);
        String day = date.substring(6,8);
        Log.d("date",year+month+day);
        return (year + "年" + month + "月" + day + "日");
    }
}
