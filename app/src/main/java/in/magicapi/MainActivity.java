package in.magicapi;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

import in.magicapi.gateway.MagicApiResultListener;
import in.magicapi.gateway.StartGateway;
import in.magicapi.gateway.Utility;

public class MainActivity extends AppCompatActivity  {

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Intent intent = new Intent(MainActivity.this, StartGateway.class);
        intent.putExtra("hash","");
        startActivity(intent);


        StartGateway.setMagicApiResultListener(new MagicApiResultListener() {
            @Override
            public void onMagicApiResult(Map<String, String> map) {
                try{
                    Log.d("sdhfkdsfsd","response "+map.get("status"));
                    Utility.showToastLatest(MainActivity.this,"yess "+map.get("reason"),"");
                    //Utility.showToastLatest(getApplicationContext(),"status "+map.get("status"),"");
                }
                catch (Exception e){
                    Log.d("sdhfkdsfsd",e.toString());
                }
            }


        });


    }

}
