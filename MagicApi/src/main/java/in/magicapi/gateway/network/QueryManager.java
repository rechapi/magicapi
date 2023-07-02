package in.magicapi.gateway.network;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import in.magicapi.gateway.R;
import in.magicapi.gateway.Utility;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.Context.NOTIFICATION_SERVICE;


public class QueryManager implements CountingRequestBody.UploadCallbacks {

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .callTimeout(60,TimeUnit.SECONDS)
            .writeTimeout(180, TimeUnit.SECONDS)
            .readTimeout(180, TimeUnit.SECONDS)

            .build();

    private final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public String filePath = "";
    public String fileType = "";
    private CountingRequestBody countingRequestBody;

    private String notification_id = "id";
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder notificationBuilder;

    private static volatile QueryManager instance = null;
    private ProgressDialog progressBar;
    private static Activity mActivity;

    private QueryManager() {
    }

    public static QueryManager getInstance() {
        if (instance == null) {
            synchronized (QueryManager.class) {
                // Double select
                if (instance == null) {
                    instance = new QueryManager();
                }
            }
        }
        return instance;
    }

    public static QueryManager getInstance(Activity activity) {
        mActivity = activity;
        if (instance == null) {
            synchronized (QueryManager.class) {
                // Double select
                if (instance == null) {
                    instance = new QueryManager();
                }
            }
        }
        return instance;
    }

    public void postRequest(Activity activity, String method, String json, final CallbackListener callback) {
        if (Utility.isNetworkConnected(activity)) {

            MultipartBody.Builder builder = new MultipartBody.Builder();
            if (json == null) {
                json = "";
            }

            File file = null;
            String fileName = "";


            if (!filePath.isEmpty()) {
                file = new File(filePath);
                fileName = file.getName();

                if (fileType.equalsIgnoreCase("Video") || fileType.equalsIgnoreCase("Image") || fileType.equalsIgnoreCase("shopImage")) {
                    Log.d("shfssfshh", "postRequest START BODY REQ: ");
                    progressBar = new ProgressDialog(activity);
                    if(fileType.equalsIgnoreCase("Video")) {
                        countingRequestBody = new CountingRequestBody(file, this, "video/*");
                    }
                    else if(fileType.equalsIgnoreCase("Image")) {
                        countingRequestBody = new CountingRequestBody(file, this, "image/*");
                    }
                    else if(fileType.equalsIgnoreCase("shopImage")) {
                        countingRequestBody = new CountingRequestBody(file, this, "image/*");
                    }
                }
            }

            builder.setType(MultipartBody.FORM);
            builder.addFormDataPart("data", json);


            if (file != null) {
                if (fileType.equalsIgnoreCase("Image")) {
                    final MediaType MEDIA_TYPE_IMAGE = MediaType.parse("image/*");
                    //builder1.addFormDataPart("image", fileName, RequestBody.create(MEDIA_TYPE_IMAGE, file)).build();
                    builder.addFormDataPart("image", fileName, countingRequestBody).build();
                } else if (fileType.equalsIgnoreCase("shopImage")) {
                    final MediaType MEDIA_TYPE_IMAGE = MediaType.parse("image/*");
                    //builder1.addFormDataPart("shopImage", fileName, RequestBody.create(MEDIA_TYPE_IMAGE, file)).build();
                    builder.addFormDataPart("shopImage", fileName, countingRequestBody).build();
                } else if (fileType.equalsIgnoreCase("Video")) {
                    final MediaType MEDIA_TYPE_VIDEO = MediaType.parse("video/*");
                    builder.addFormDataPart("kyc", fileName, countingRequestBody).build();
                } else if (fileType.equalsIgnoreCase("Audio")) {
                    final MediaType MEDIA_TYPE_AUDIO = MediaType.parse("audio/*");
                    builder.addFormDataPart("audio", fileName, RequestBody.create(MEDIA_TYPE_AUDIO, file)).build();
                }
            }


            //RequestBody requestBody = RequestBody.create(MediaType.parse("octet-stream"), json);
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);
            String reqUrl = "";

                reqUrl = method;


            Request request = new Request.Builder()
                    .url(reqUrl)

                    .post(requestBody)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull final IOException e) {

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {

                            Log.d("hsdfjhsdjf","failed "+e.toString());
                            callback.onResult(e, "", null);
                            filePath = "";
                            fileType = "";
                        }
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {


                    final String result = Utility.decodeString(response.body().string());

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Gson gson = new Gson();
                                ResponseManager responseManager = gson.fromJson(result, ResponseManager.class);
                                callback.onResult(null, result, responseManager);


                                filePath = "";
                                fileType = "";
                            } catch (Exception e) {
                                Log.d("hsdfjhsdjf", "failed - " + e.toString());
                                callback.onResult(e, "", null);


                                filePath = "";
                                fileType = "";
                            }
                        }
                    });
                }
            });
        }
    }

    public void postRequestforMultipleImages(Activity activity, String method, MultipartBody.Builder builder1, String json, final CallbackListener callback) {
        Log.d("shfssfshh", "request " + filePath);
        //Log.d("shfssfshh","test 1");
        if (Utility.isNetworkConnected(activity)) {

            if (json == null) {
                json = "";
            }

            File file = null;
            String fileName = "";

            if (!filePath.isEmpty()) {
                file = new File(filePath);
                fileName = file.getName();
                if (fileType.equalsIgnoreCase("Video") || fileType.equalsIgnoreCase("Image") || fileType.equalsIgnoreCase("shopImage")) {
                    Log.d("shfssfshh", "postRequest START BODY REQ: ");
                    progressBar = new ProgressDialog(activity);
                    if(fileType.equalsIgnoreCase("Video")) {
                        countingRequestBody = new CountingRequestBody(file, this, "video/*");
                    }
                    else if(fileType.equalsIgnoreCase("Image")) {
                        countingRequestBody = new CountingRequestBody(file, this, "image/*");
                    }
                    else if(fileType.equalsIgnoreCase("shopImage")) {
                        countingRequestBody = new CountingRequestBody(file, this, "image/*");
                    }
                }


            }

            builder1.setType(MultipartBody.FORM);
            builder1.addFormDataPart("data", json);


            if (file != null) {
                if (fileType.equalsIgnoreCase("Image")) {
                    final MediaType MEDIA_TYPE_IMAGE = MediaType.parse("image/*");
                    //builder1.addFormDataPart("image", fileName, RequestBody.create(MEDIA_TYPE_IMAGE, file)).build();
                    builder1.addFormDataPart("image", fileName, countingRequestBody).build();
                } else if (fileType.equalsIgnoreCase("shopImage")) {
                    final MediaType MEDIA_TYPE_IMAGE = MediaType.parse("image/*");
                    //builder1.addFormDataPart("shopImage", fileName, RequestBody.create(MEDIA_TYPE_IMAGE, file)).build();
                    builder1.addFormDataPart("shopImage", fileName, countingRequestBody).build();
                } else if (fileType.equalsIgnoreCase("Video")) {
                    final MediaType MEDIA_TYPE_VIDEO = MediaType.parse("video/*");
                    builder1.addFormDataPart("kyc", fileName, countingRequestBody).build();
                } else if (fileType.equalsIgnoreCase("Audio")) {
                    final MediaType MEDIA_TYPE_AUDIO = MediaType.parse("audio/*");
                    builder1.addFormDataPart("audio", fileName, RequestBody.create(MEDIA_TYPE_AUDIO, file)).build();
                }
            }

            RequestBody requestBody = builder1.build();
            String reqUrl = "";

                reqUrl = method;

            Request request = new Request.Builder()
                    .url(reqUrl)
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull final IOException e) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onResult(e, "", null);
                            filePath = "";
                            fileType = "";
                        }
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {


                    final String result = Utility.decodeString(response.body().string());

                  //  Log.d("psaresult", "onresponse 2 - " + result);
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Gson gson = new Gson();
                                ResponseManager responseManager = gson.fromJson(result, ResponseManager.class);
                                callback.onResult(null, result, responseManager);
                                filePath = "";
                                fileType = "";
                            } catch (Exception e) {
                                callback.onResult(e, "", null);
                                filePath = "";
                                fileType = "";
                            }
                        }
                    });
                }
            });
        }
    }



                    @Override
    public void onProgressUpdate(int percentage) {

        progressBar.setProgress(percentage);
    }

    @Override
    public void onError() {
        if (mActivity != null) {
            Utility.showToastLatest(mActivity, "Something went wrong while upload video. please try again", "ERROR");
        }
    }

    @Override
    public void onFinish() {
        fileType = "";
        progressBar.hide();
        progressBar.dismiss();

    }

    @Override
    public void uploadStart() {
        showUploadProgress();
    }



    private void updateNotification(int downloadItemProgress) {
        notificationBuilder.setProgress(100, downloadItemProgress, false);
        notificationBuilder.setContentText("Upload : " + downloadItemProgress + "%");
        mNotificationManager.notify(0, notificationBuilder.build());
    }

    private void downloadCompleteNotification() {
        notificationBuilder.setProgress(0, 0, false);
        notificationBuilder.setOngoing(false);
        notificationBuilder.setContentText("Video upload successfully");
        notificationBuilder.setTicker("Video upload successfully");
        notificationBuilder.setOngoing(false);
        mNotificationManager.notify(0, notificationBuilder.build());
    }

    private void showUploadProgress() {
        progressBar.setMessage("KYC Video Uploading...");
        progressBar.setCancelable(false);
        progressBar.setCanceledOnTouchOutside(false);
        progressBar.setMax(100);
        progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressBar.setProgress(0);
        progressBar.show();
    }

}
