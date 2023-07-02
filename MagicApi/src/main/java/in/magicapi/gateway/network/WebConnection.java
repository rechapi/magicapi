package in.magicapi.gateway.network;

/**
 * Created by rajdeep on 29/07/16.
 */

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by San on 10-05-2016.
 */

public class WebConnection {
    private static final String TAG = "WebConnection";
    private static final String PROTOCOL = "http://";
    private static final boolean displayLog = false;
    private String url;
    Context context;
    public WebConnection(Context context){

    }
    public WebConnection(Context context, String url){
        this(context);
        this.context=context;
        this.url=url;//dont prefix https
    }
    public String[] sendGet(String para) {
        this.url=url+"?"+para;

        String[] ret=new String[2];
        URL obj = null;
        try {
            obj = new URL(PROTOCOL+this.url);
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.e(TAG,e.getMessage());
            return ret;
        }
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) obj.openConnection();
            con.setConnectTimeout(5000);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // optional default is GET
        try {
            con.setRequestMethod("GET");
        } catch (ProtocolException e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
        }
        //add request header
        //con.setRequestProperty("User-Agent", USER_AGENT);
        //con.setRequestProperty("Content-Type", "application/json");
        //con.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml");

        int responseCode = 0;
        try {
            responseCode = con.getResponseCode();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Log.w(TAG,"response is-"+responseCode);
        if(displayLog) {
            Log.w(TAG, "\nSending 'GET' request to URL : " + url);
            Log.w(TAG, "Response Code : " + responseCode);
        }
        BufferedReader in = null;
        StringBuffer response = null;
        try {
            in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if(displayLog) {
            Log.w(TAG, "WebConnection : " + response.toString());
        }
        ret[0]=""+responseCode;
        if(response!=null){
            ret[1]=response.toString();
        }
        return ret;
    }

    public String[] sendPost(String urlParameters) throws Exception {
        String[] ret=new String[2];
        if(this.url == null){
            return ret;
        }

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setConnectTimeout(5000);

        //
        //add reuqest header
        con.setRequestMethod("POST");
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        //String urlParameters = "sn=C02G8416DRJM&cn=&locale=&caller=&num=12345";

        // Send post request
        con.setDoOutput(true);
        if(urlParameters!=null){
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();
        }

        int responseCode = con.getResponseCode();
        //System.out.println("\nSending 'POST' request to URL : " + url);
        //System.out.println("Post parameters : " + urlParameters);
        //System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        ret[0]=""+responseCode;
        ret[1]=response.toString();
        //print result
        //System.out.println(response.toString());
        return ret;
    }
}

