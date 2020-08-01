package com.example.btl1server.Common;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import com.example.btl1server.Model.Request;
import com.example.btl1server.Model.User;
import com.example.btl1server.Remote.APIService;
import com.example.btl1server.Remote.FCMRetrofitClient;
import com.example.btl1server.Remote.IGeoCoordinates;
import com.example.btl1server.Remote.RetrofitClient;

public class Common {
    public static User currentUser;
    public static Request currentRequest;

    public static final String UPDATE = "Cập nhật";
    public  static final String DELETE = "Xóa";
    public static final String USER_KEY = "User";
    public static final String PWD_KEY = "Password";
    public static final int PICK_IMAGE_REQUEST = 71 ;

    public  static final String baseUrl = "https://maps.googleapis.com";

    public  static final String fcmUrl = "https://fcm.googleapis.com";

    public static String convertCodeToStatus(String status){
        if(status.equals("0"))
            return "Đang xử lí";
        else if (status.equals("1"))
            return "Đang giao";
        else return "Đã giao";
    }

    public static IGeoCoordinates getGeoCodeservice() {
        return RetrofitClient.getClient(baseUrl).create(IGeoCoordinates.class);
    }

    public static APIService getFCMService() {
        return FCMRetrofitClient.getClient(fcmUrl).create(APIService.class);
    }

    public static Bitmap scaleBitmap(Bitmap bitmap, int newWidth, int newHeight) {
        Bitmap scaleBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);

        float scaleX = newWidth / (float)bitmap.getWidth();
        float scaleY = newHeight / (float)bitmap.getHeight();
        float pivotX = 0, pivotY = 0;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(scaleX, scaleY, pivotX, pivotY);

        Canvas canvas = new Canvas(scaleBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmap, 0, 0, new Paint(Paint.FILTER_BITMAP_FLAG));

        return scaleBitmap;
    }
}
