package in.magicapi.gateway;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.view.circulartimerview.CircularTimerListener;
import com.view.circulartimerview.CircularTimerView;
import com.view.circulartimerview.TimeFormatEnum;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import es.dmoral.toasty.Toasty;

import static android.util.Base64.NO_WRAP;

public class Utility {
    public  static String calculateHmacSHA1Hash(String data, String key) {
        String HMAC_SHA1_ALGORITHM = "HmacSHA1";
        String result = null;

        try {
            Key signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(data.getBytes());
            result = getHexString(rawHmac);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
    private static String getHexString(byte[] array) {
        StringBuilder hash = new StringBuilder();
        for (byte hashByte : array) {
            hash.append(Integer.toString((hashByte & 0xff) + 0x100, 16).substring(1));
        }
        return hash.toString();
    }

    public static String getHashDetails(String hash){

        return null;
    }
    public static String calculateHmacSha256( String hashString,  String salt) {
        String HMAC_SHA256_ALGORITHM = "HmacSHA256";
        String result = null;
        try {
            SecretKeySpec secret = new SecretKeySpec(salt.getBytes(StandardCharsets.UTF_8), HMAC_SHA256_ALGORITHM);
            Mac mac= Mac.getInstance(HMAC_SHA256_ALGORITHM);
            mac.init(secret);
            byte[] bytes = mac.doFinal(hashString.getBytes(StandardCharsets.UTF_8));
            result = Base64.encodeToString(bytes, NO_WRAP);
        } catch (Exception e){
            result = null;
        }
        return  result;
    }
    public static String calculateHash(String hashString) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-512");
            messageDigest.update(hashString.getBytes());
            byte[] mdbytes = messageDigest.digest();
            return getHexString(mdbytes);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static void showToastLatest(Context context, String msg, String resType) {
        if (!msg.equalsIgnoreCase("SUCCESS")) {
            //Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            if (resType.equals("SUCCESS") || resType.equals("200")) {
                Toasty.success(context, msg, Toast.LENGTH_LONG, true).show();
                /*
                SuperActivityToast.create(context, new Style(), Style.TYPE_BUTTON)
                        .setProgressBarColor(Color.WHITE)
                        .setText(msg)
                        .setDuration(Style.DURATION_LONG)
                        .setFrame(Style.FRAME_LOLLIPOP)
                        .setGravity(Gravity.CENTER)
                        .setColor(PaletteUtils.getSolidColor(PaletteUtils.MATERIAL_GREEN))
                        .setGravity(Gravity.CENTER_HORIZONTAL, 0, 0)
                        .setAnimations(Style.ANIMATIONS_POP).show();
                        */
            } else if (resType.equals("INFO")) {
                Toasty.info(context, msg, Toast.LENGTH_LONG, true).show();
                /*
                SuperActivityToast.create(context, new Style(), Style.TYPE_BUTTON)
                        .setProgressBarColor(Color.WHITE)
                        .setText(msg)
                        .setDuration(Style.DURATION_LONG)
                        .setGravity(Gravity.CENTER)
                        .setFrame(Style.FRAME_LOLLIPOP)
                        .setColor(PaletteUtils.getSolidColor(PaletteUtils.MATERIAL_BLUE))
                        .setGravity(Gravity.CENTER_HORIZONTAL, 0, 0)
                        .setAnimations(Style.ANIMATIONS_POP).show();
                        */
            } else {
                Log.d("sdhfkdsfsd","1");
                AlertDialog.Builder alert = new AlertDialog.Builder(context);
                View wv = LayoutInflater.from(context).inflate(R.layout.error_dialog, null, false);
                Button gotit = wv.findViewById(R.id.back_btn);
                Log.d("sdhfkdsfsd","2");
                TextView textView = wv.findViewById(R.id.error_msg);
                if (!msg.isEmpty())
                    textView.setText(msg);
                alert.setView(wv);
                alert.setCancelable(false);
                Log.d("sdhfkdsfsd","3");
                final Dialog dialog = alert.create();
                if (context != null) {
                    dialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.orange_border_white_fill_bg));
                    Log.d("sdhfkdsfsd","4");
                    try {
                        dialog.show();
                    } catch (Exception ignored) {
                        Log.d("sdhfkdsfsd","5" +ignored.toString());
                    }
                }

                gotit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.cancel();
                    }
                });

                //Toast.makeText(context,msg,Toast.LENGTH_LONG).show();
//                Toasty.error(context, msg, Toast.LENGTH_LONG, true).show();
                //openErrorToastDialog(context,msg);
                /*
                SuperActivityToast.create(context, new Style(), Style.TYPE_BUTTON)

                        .setProgressBarColor(Color.WHITE)

                        .setText(msg)

                        .setDuration(Style.DURATION_LONG)

                        .setFrame(Style.FRAME_LOLLIPOP)

                        .setGravity(Gravity.CENTER)
                        .setGravity(Gravity.CENTER_HORIZONTAL, 0, 0)
                        .setColor(PaletteUtils.getSolidColor(PaletteUtils.MATERIAL_INDIGO))

                        .setAnimations(Style.ANIMATIONS_POP)
                        .show();


                // Create layout inflator object to inflate toast.xml file
                //LayoutInflater inflater = getLayoutInflater();

                LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                // Call toast.xml file for toast layout
                View toast = inflater.inflate(R.layout.toast, null);
                toast.setBackgroundResource(R.color.red_light);
                toast.setPadding(10,10,10,10);


                Toast toast1 = new Toast(context);

                // Set layout to toast
                toast1.setView(toast);

                toast1.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL,
                        0, 0);
                toast1.setDuration(Toast.LENGTH_LONG);


                toast1.show();
*/
                //showAlertDialog("Alert", msg , context);
            }
        }
    }

    public static boolean isPackageInstalled(String packageName, PackageManager packageManager) {
        try {
            packageManager.getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
    private static void showTimmerDialog(String orderId, Activity activity, Dialog timmerDialog, CircularTimerView progressBar) {
        final SharedPreferences[] prefs = new SharedPreferences[1];
        prefs[0] = activity.getSharedPreferences(
                "AutoSms", Context.MODE_PRIVATE);
        View timmerView = LayoutInflater.from(activity).inflate(R.layout.timmer_layout, null);


        Dialog finalTimmerDialog = timmerDialog;
        timmerView.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (finalTimmerDialog != null)
                    finalTimmerDialog.dismiss();
            }
        });

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
        alertDialog.setView(timmerView);

        alertDialog.setCancelable(false);

        progressBar = timmerView.findViewById(R.id.progress_circular);
        progressBar.setProgress(0);

        timmerDialog = alertDialog.create();
        timmerDialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(activity, R.drawable.orange_border_white_fill_bg));

        timmerDialog.show();

        Dialog finalTimmerDialog1 = timmerDialog;
        CircularTimerView finalProgressBar = progressBar;
        progressBar.setCircularTimerListener(new CircularTimerListener() {
            @Override
            public String updateDataOnTick(long remainingTimeInMs) {

                prefs[0] = activity.getSharedPreferences(
                        "AutoSms", Context.MODE_PRIVATE);
                int second = (int) Math.ceil((remainingTimeInMs / 1000.f));

                if (second % 5 == 0) {

                    int timeNow = (int) ((System.currentTimeMillis() / 1000) % 3600);
                    int pastTime = prefs[0].getInt("time", 0);
                    int difference = (3600 + timeNow - pastTime) % 3600;

                    if (difference > 4) {
                        //checkPaymentStatus(orderId);
                        Log.d("AddFundActivity ", " second " + second + " diff " + difference);

                        prefs[0].edit().putInt("time", timeNow).apply();
                    }
                }

                return getDurationString(second);
            }

            @Override
            public void onTimerFinished() {
                if (finalTimmerDialog1 != null)
                    finalTimmerDialog1.dismiss();
                Utility.showToastLatest(activity, "Error 052173 "+"Timeout -could not get any response from user.", "ERROR");
                finalProgressBar.setPrefix("");
                finalProgressBar.setSuffix("");
                finalProgressBar.setText("Time out!");
            }
        }, 5, TimeFormatEnum.MINUTES, 10);

        progressBar.startTimer();


    }


    public static String decodeString(String enc) {

        if (enc.length() > 25) {
            String substr = enc.substring(12, 22);

            enc = enc.replace(substr, "");
        }
        String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        StringBuilder enc_rot = new StringBuilder();
        for (int i = 0; i < enc.length(); i++) {
            char c = enc.charAt(i);
            if (c >= 'a' && c <= 'm') c += 13;
            else if (c >= 'A' && c <= 'M') c += 13;
            else if (c >= 'n' && c <= 'z') c -= 13;
            else if (c >= 'N' && c <= 'Z') c -= 13;
            enc_rot.append(c);
        }

        try {
            byte[] valueDecoded = Base64.decode(enc_rot.toString().getBytes(), Base64.NO_WRAP);
            enc = new String(valueDecoded);
        } catch (Exception ignored) {

        }

        enc_rot = new StringBuilder("");
        for (int i = 0; i < enc.length(); i++) {
            char c = enc.charAt(i);
            if (c >= 'a' && c <= 'm') c += 13;
            else if (c >= 'A' && c <= 'M') c += 13;
            else if (c >= 'n' && c <= 'z') c -= 13;
            else if (c >= 'N' && c <= 'Z') c -= 13;
            enc_rot.append(c);
        }
        //Log.d("decode value ","- "+enc_rot.toString());

        return enc_rot.toString();
    }
    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            Boolean isConnected;


                if (cm != null && cm.getActiveNetworkInfo() != null) {
                    isConnected = true;
                } else {
                    Utility.showToastLatest(context, "Internet Connection Not Found","ERROR");
                    isConnected = false;
                }

            return isConnected;

        } else {
            Utility.showToastLatest(context, "Internet Context Not Found","ERROR");
            return false;
        }
    }

    static String getDurationString(int seconds) {

        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        seconds = seconds % 60;

        return twoDigitString(minutes) + " : " + twoDigitString(seconds);
    }

    private static String twoDigitString(int number) {

        if (number == 0) {
            return "00";
        }

        if (number / 10 == 0) {
            return "0" + number;
        }

        return String.valueOf(number);
    }
}
