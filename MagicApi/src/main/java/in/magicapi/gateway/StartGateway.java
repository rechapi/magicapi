package in.magicapi.gateway;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.cashfree.pg.CFPaymentService;
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;
import com.paytm.pgsdk.TransactionManager;
import com.payu.base.models.ErrorResponse;
import com.payu.base.models.PayUPaymentParams;
import com.payu.checkoutpro.PayUCheckoutPro;
import com.payu.checkoutpro.utils.PayUCheckoutProConstants;
import com.payu.ui.model.listeners.PayUCheckoutProListener;
import com.payu.ui.model.listeners.PayUHashGenerationListener;
import com.payu.upisdk.Upi;
import com.payu.upisdk.bean.UpiConfig;
import com.payu.upisdk.callbacks.PayUUPICallback;
import com.payu.upisdk.generatepostdata.PaymentParamsUpiSdk;
import com.payu.upisdk.generatepostdata.PostDataGenerate;
import com.payu.upisdk.upi.IValidityCheck;
import com.payu.upisdk.util.UpiConstant;
import com.view.circulartimerview.CircularTimerListener;
import com.view.circulartimerview.CircularTimerView;
import com.view.circulartimerview.TimeFormatEnum;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import in.magicapi.gateway.network.CallbackListener;
import in.magicapi.gateway.network.QueryManager;
import in.magicapi.gateway.network.ResponseManager;

import static com.cashfree.pg.CFPaymentService.PARAM_APP_ID;
import static com.cashfree.pg.CFPaymentService.PARAM_CUSTOMER_EMAIL;
import static com.cashfree.pg.CFPaymentService.PARAM_CUSTOMER_NAME;
import static com.cashfree.pg.CFPaymentService.PARAM_CUSTOMER_PHONE;
import static com.cashfree.pg.CFPaymentService.PARAM_NOTIFY_URL;
import static com.cashfree.pg.CFPaymentService.PARAM_ORDER_AMOUNT;
import static com.cashfree.pg.CFPaymentService.PARAM_ORDER_CURRENCY;
import static com.cashfree.pg.CFPaymentService.PARAM_ORDER_ID;
import static com.cashfree.pg.CFPaymentService.PARAM_ORDER_NOTE;

public class StartGateway extends AppCompatActivity {
    private Dialog timmerDialog;

    private ProgressHelper progressHelper;
    private CircularTimerView progressBar;

    final int RES_UPI_INTENT = 101;
    String hash="";
    String status="";
    String errorCode="";
    String reason="";
    String orderId="";
    Activity activity;
    String amount="";
    String response="";
    Map<String, String> hashMap = new HashMap<>();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.library_activity_main);

        hash = getIntent().getStringExtra("hash");
        activity=StartGateway.this;

        progressHelper = new ProgressHelper(this);
        if (hash != null) {

            Map<String, String> map1 = new HashMap<>();

            map1.put("hash", hash); //

            try {
                JSONObject json = new JSONObject();
                for (Map.Entry<String, String> entry : map1.entrySet()) {
                    json.put(entry.getKey(), entry.getValue());
                }
                String jsonString = json.toString();

                byte[] jsonBytes = jsonString.getBytes();

                String base64Encoded = Base64.encodeToString(jsonBytes, Base64.DEFAULT);
                getHashDetails(base64Encoded);
            }
            catch (Exception e){

                status="FAILED";
                errorCode="1200001";
                reason=e.toString();
            }


        }
        else{
            status="FAILED";
            errorCode="1200000";
            reason="No hash received";
            callForResult();
        }
    }
    private static MagicApiResultListener resultListener;

    public static void setMagicApiResultListener(MagicApiResultListener listener) {
        resultListener = listener;
    }

    private void callForResult(){
        try {
            hashMap.put("status", status);
            hashMap.put("reason", reason);
            hashMap.put("orderId", orderId);
            hashMap.put("amount", amount);
            hashMap.put("errorCode", errorCode);

            //Log.d("sdhfkdsfsd","statys "+status);
            resultListener.onMagicApiResult(hashMap);



        }
        catch (Exception e){
            //Log.d("sdhfkdsfsd",e.toString());
        }
        finish();
    }

    private void callgethashApi(String request){
        QueryManager.getInstance().postRequest(this, "https://api.magicapi.in/hashDetails.php", request, new CallbackListener() {
            @Override
            public void onResult(Exception e, String result,
                                 ResponseManager responseManager) {
                //parseResponse(result, e);
                progressHelper.dismiss();
                try {
                    JSONObject data = new JSONObject(result);

                    String gatewayType = data.getString("gatewayType");
                    orderId=data.getString("orderId");
                    amount=data.getString("amount");
                    if (gatewayType.equals("upiIntent")) {
                        call_upiIntent(data);
                    } else if (gatewayType.equals("razorpay")) {
                        call_razorpay(data);
                    } else if (gatewayType.equals("internalBroadcast")) {
                        call_internalBroadcast(data);
                    } else if (gatewayType.equals("cashfree")) {
                        call_cashfree(data);
                    } else if (gatewayType.equals("upiRequest")) {
                        call_webRequestUpi(data);
                    } else if (gatewayType.equals("paytm")) {
                        call_paytm(data);
                    } else if (gatewayType.equals("payu")) {
                        call_payu(data);
                    } else if (gatewayType.equals("payuUpiIntent")) {
                        call_payu_upiIntent(data);
                    } else if (gatewayType.equals("cashfreeUpiIntent")) {
                        call_cashfree_upiIntent(data);
                    }
                }
                catch (Exception e2){
                    status="FAILED";
                    errorCode="1200002";
                    reason=e2.toString();
                    callForResult();
                }
            }

        });
    }
    private void getHashDetails(String request){
        progressHelper.show();

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
            callgethashApi(request);
            }
        },4500);



    }
    private void call_payu_upiIntent(JSONObject data){

        try {
            String openWithPackage=data.getString("packageName");
            PaymentParamsUpiSdk mPaymentParamsUpiSdk = new PaymentParamsUpiSdk();
            mPaymentParamsUpiSdk.setKey(data.getString("merchantKey")); //Your Merchant Key
            mPaymentParamsUpiSdk.setProductInfo(data.getString("note"));
            mPaymentParamsUpiSdk.setFirstName(data.getString("customerFirstName")); //Customer First name
            mPaymentParamsUpiSdk.setEmail(data.getString("customerEmail")); //Customer Email
            mPaymentParamsUpiSdk.setTxnId(data.getString("orderId")); //Your transaction id
            mPaymentParamsUpiSdk.setAmount(amount); //Your transaction Amount(In Double as String)
            mPaymentParamsUpiSdk.setSurl(data.getString("successUrl"));
            mPaymentParamsUpiSdk.setFurl(data.getString("failedUrl"));
            mPaymentParamsUpiSdk.setUdf1("");
            mPaymentParamsUpiSdk.setUdf2("");
            mPaymentParamsUpiSdk.setUdf3("");
            mPaymentParamsUpiSdk.setUdf4("");
            mPaymentParamsUpiSdk.setUdf5("");
            //mPaymentParamsUpiSdk.setVpa(""); //In case of UPI Collect set customer vpa here
            mPaymentParamsUpiSdk.setUserCredentials(data.getString("userCredential"));
            mPaymentParamsUpiSdk.setOfferKey("");
            mPaymentParamsUpiSdk.setPhone(data.getString("customerMobile"));//Customer Phone Number
            mPaymentParamsUpiSdk.setHash(data.getString("checkSum"));//Your Payment Hash

            String postDataFromUpiSdk = new PostDataGenerate.PostDataBuilder(this).
                    setPaymentMode(UpiConstant.UPI_INTENT).setPaymentParamUpiSdk(mPaymentParamsUpiSdk).
                    build().toString();

            ///Log.d("skjgfssd","payusting "+postDataFromUpiSdk);


            UpiConfig upiConfig = new UpiConfig();
            upiConfig.setMerchantKey(data.getString("merchantKey"));
            //upiConfig.setPaymentType("upi");
            if(!openWithPackage.isEmpty()){
                upiConfig.setPackageNameForSpecificApp(openWithPackage);

            }

            upiConfig.setPayuPostData(postDataFromUpiSdk);// that we generate above
//In order to set CustomProgress View use below settings
            //upiConfig.setProgressDialogCustomView(false);
            upiConfig.setDisableIntentSeamlessFailure(UpiConfig.TRUE);
            Upi upi = Upi.getInstance();
            upi.makePayment(payUUpiSdkCallbackUpiSdk, this, upiConfig);
        }
        catch (Exception e){
            status="FAILED";
            errorCode="6755433";
            reason=e.toString();
            callForResult();
        }
    }

    PayUUPICallback payUUpiSdkCallbackUpiSdk = new PayUUPICallback() {

        @Override
        public void onPaymentFailure(String payuResult, String merchantResponse) {
            super.onPaymentFailure(payuResult, merchantResponse);

            status="FAILED";
            errorCode="875543";
            reason=payuResult;
            callForResult();
//Payment failed
        }
        @Override
        public void onPaymentSuccess(String payuResult, String merchantResponse) {
            super.onPaymentSuccess(payuResult, merchantResponse);
            status="SUCCESS";
            errorCode="200";
            reason=payuResult+"|"+merchantResponse;
            callForResult();
        }
        @Override
        public void onVpaEntered(String vpa, IValidityCheck iValidityCheck) {
            super.onVpaEntered(vpa, iValidityCheck);
            String input = "payu merchant key" + "|validateVPA|" + vpa + "|" + "payu merchant salt";
            //iValidityCheck.verifyVpa(calculateHash(input));

        }
        @Override
        public void onUpiErrorReceived(int code, String errormsg) {
            super.onUpiErrorReceived(code, errormsg);
            //Log.d("skjgfssd","Failed"+errormsg);
//Any error on upisdk
            callForResult();
        }
    };

    private void call_cashfree_upiIntent(JSONObject data){
        try {

            String checkSum = data.getString("checkSum");
            CFPaymentService cfPaymentService = CFPaymentService.getCFPaymentServiceInstance();
            cfPaymentService.setOrientation(0);
            try {

                String openWithPackage = data.getString("packageName");
                if(!openWithPackage.equals("")){
                    cfPaymentService.selectUpiClient(openWithPackage);

                }
            }
            catch (Exception e){
                status="FAILED";
                errorCode="5674543";
                reason=e.toString();
                callForResult();
            }

            cfPaymentService.upiPayment(this, getInputParams(data),
                    checkSum, "PROD");
        }
        catch (Exception e){
            status="FAILED";
            errorCode="6786554";
            reason=e.toString();
            callForResult();
        }
    }
    private void call_internalBroadcast(JSONObject data){

    }

    private void call_paytm(JSONObject data){


        try{
            String mid=data.getString("mid");
            String txnToken=data.getString("txnToken");
            String cbUrl=data.getString("cbUrl");
            PaytmOrder paytmOrder = new PaytmOrder(orderId, mid, txnToken, amount, cbUrl);


            TransactionManager transactionManager = new TransactionManager(paytmOrder, new PaytmPaymentTransactionCallback() {
                @Override
                public void onTransactionResponse(Bundle bundle) {

                    if(bundle!=null) {


                        String dataRes = "";
                        Set<String> keys = bundle.keySet();
                        Iterator<String> it = keys.iterator();

                        while (it.hasNext()) {
                            String key = it.next();
                            dataRes += "" + key + "=" + bundle.get(key) + "&";


                        }

                        status="PENDING";
                        errorCode="201";
                        reason=dataRes;
                        callForResult();
                    }else{
                        status="PENDING";
                        errorCode="201";
                        reason="No Response Received";
                        callForResult();
                    }
                }

                @Override
                public void networkNotAvailable() {

                    status="PENDING";
                    errorCode="201";
                    reason="Network not available";
                    callForResult();
                }

                @Override
                public void onErrorProceed(String s) {

                    status="FAILED";
                    errorCode="85455434";
                    reason=s.toString();
                    callForResult();
                }

                @Override
                public void clientAuthenticationFailed(String inErrorMessage) {

                    status="FAILED";
                    errorCode="56854435";
                    reason=inErrorMessage;
                    callForResult();
                }

                @Override
                public void someUIErrorOccurred(String inErrorMessage) {

                    status="FAILED";
                    errorCode="8955454";
                    reason=inErrorMessage;
                    callForResult();
                }

                @Override
                public void onErrorLoadingWebPage(int iniErrorCode, String inErrorMessage, String inFailingUrl) {

                    status="FAILED";
                    errorCode="67543433";
                    reason=inErrorMessage;
                    callForResult();
                }

                @Override
                public void onBackPressedCancelTransaction() {

                    status="FAILED";
                    errorCode="856443";
                    reason="Back Pressed";
                    callForResult();
                }

                @Override
                public void onTransactionCancel(String inErrorMessage, Bundle inResponse) {

                    status="FAILED";
                    errorCode="67453443";
                    reason=inErrorMessage;
                    callForResult();
                }
            });

            transactionManager.setAppInvokeEnabled(false);
            transactionManager.startTransaction(this, 102);
        }
        catch (Exception e){

        }



    }

    private void call_webRequestUpi(JSONObject data){

        showTimmerDialog(orderId);


    }



    SharedPreferences prefs;
    private void showTimmerDialog(String orderId) {

        View timmerView = LayoutInflater.from(this).inflate(R.layout.timmer_layout, null);


        timmerView.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (timmerDialog != null)
                    timmerDialog.dismiss();
            }
        });

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setView(timmerView);

        alertDialog.setCancelable(false);

        progressBar = timmerView.findViewById(R.id.progress_circular);
        progressBar.setProgress(0);

        timmerDialog = alertDialog.create();
        timmerDialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.orange_border_white_fill_bg));

        timmerDialog.show();

        progressBar.setCircularTimerListener(new CircularTimerListener() {
            @Override
            public String updateDataOnTick(long remainingTimeInMs) {

                prefs = activity.getSharedPreferences(
                        "AutoSms", Context.MODE_PRIVATE);
                int second = (int) Math.ceil((remainingTimeInMs / 1000.f));

                if (second % 5 == 0) {

                    int timeNow = (int) ((System.currentTimeMillis() / 1000) % 3600);
                    int pastTime = prefs.getInt("time", 0);
                    int difference = (3600 + timeNow - pastTime) % 3600;

                    if (difference > 4) {
                        checkPaymentStatus(orderId);
                        Log.d("AddFundActivity ", " second " + second + " diff " + difference);

                        prefs.edit().putInt("time", timeNow).apply();
                    }
                }

                return Utility.getDurationString(second);
            }

            @Override
            public void onTimerFinished() {
                if (timmerDialog != null)
                    timmerDialog.dismiss();
                Utility.showToastLatest(activity, "Error 052173 "+"Timeout -could not get any response from user.", "ERROR");
                progressBar.setPrefix("");
                progressBar.setSuffix("");
                progressBar.setText("Time out!");
                callForResult();
            }
        }, 5, TimeFormatEnum.MINUTES, 10);

        progressBar.startTimer();


    }

    private void checkPaymentStatus(String orderId) {


        Map<String, String> map1 = new HashMap<>();


        map1.put("orderId", orderId);
        map1.put("hash",hash);

        try {
            JSONObject json = new JSONObject();
            for (Map.Entry<String, String> entry : map1.entrySet()) {
                json.put(entry.getKey(), entry.getValue());
            }
            String jsonString = json.toString();

            byte[] jsonBytes = jsonString.getBytes();

            String base64Encoded = Base64.encodeToString(jsonBytes, Base64.DEFAULT);

            if (Utility.isNetworkConnected(this)) {


                QueryManager.getInstance().postRequest(this, "https://api.magicapi.in/paymentStatus.php", base64Encoded, new CallbackListener() {
                    @Override
                    public void onResult(Exception e, String result,
                                         ResponseManager responseManager) {
                        //parseResponse(result, e);

                        try {
                            JSONObject data = new JSONObject(result);

                            String paymentStatus = data.getString("status");

                            if(paymentStatus.equals("SUCCESS")) {
                                status="SUCCESS";

                                if (progressBar != null)
                                    progressBar.stopTimer();

                                if (timmerDialog != null)
                                    timmerDialog.dismiss();

                                callForResult();
                            }

                        }
                        catch (Exception e2){


                        }
                    }

                });

            } else {
                Utility.showToastLatest(this, "Error 764214 - Internet not found", "");
            }
        }
        catch (Exception e){

        }


    }

    private void call_razorpay(JSONObject data){
/*
        final Activity activity = this;

        final Checkout co = new Checkout();




        try {
            co.setKeyID(data.getString("mid"));

            JSONObject options = new JSONObject() ;
            options.put("name", data.getString("customerName"));
            options.put("description", data.getString("note"));
            //You can omit the image option to fetch the image from dashboard
            //options.put("image", response.getData().getWebsite());
            options.put("currency", "INR");
            options.put("amount", amount);
            options.put("order_id", orderId);

            JSONObject preFill = new JSONObject();
            preFill.put("email", data.getString("customerEmail"));
            preFill.put("contact", data.getString("customerMobile"));


            options.put("prefill", preFill);

            co.open(activity, options);
        } catch (Exception e) {
            Toast.makeText(activity, "Error in payment: " + e.getMessage(), Toast.LENGTH_SHORT)
                    .show();
            status="FAILED";
            errorCode="3845623";
            reason=e.toString();
            callForResult();
            e.printStackTrace();
        }

 */
    }

    private void call_upiIntent(JSONObject data){

        try {
            String upiString = data.getString("upiString");
            String openWithPackage=data.getString("packageName");

            if(openWithPackage.equals("")){
                Uri uri = Uri.parse(upiString);
                Intent upiPayIntent = new Intent(Intent.ACTION_VIEW);
                upiPayIntent.setData(uri);

                // will always show a dialog to user to choose an app
                Intent chooser = Intent.createChooser(upiPayIntent, "Pay with");
                // select if intent resolves
                if (null != chooser.resolveActivity(getPackageManager())) {
                    startActivityForResult(chooser, RES_UPI_INTENT);

                } else {
                    Utility.showToastLatest(activity, "No UPI app found, please install one to continue", "ERROR");
                }
            }
            else{

                if (Utility.isPackageInstalled(openWithPackage, activity.getPackageManager())) {

                    try {
                        Uri uri = Uri.parse(upiString);
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(uri);
                        i.setPackage(openWithPackage); //The package name of the app to which intent is to be sent
                        //i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivityForResult(i, RES_UPI_INTENT);

                        //someActivityResultLauncher.launch(i);

                    } catch (ActivityNotFoundException e) {
                        showInstallationDialig(openWithPackage);

                        status="FAILED";
                        errorCode="3845623";
                        reason=e.toString();
                        callForResult();
                    } catch (Exception e) {
                        Utility.showToastLatest(activity, "Error 467443 " + e.toString(), "ERROR");
                        status="FAILED";
                        errorCode="3845623";
                        reason=e.toString();
                        callForResult();
                    }
                }
                else{
                    showInstallationDialig(openWithPackage);
                }

            }


        }
        catch (Exception e){
            status="FAILED";
            errorCode="86564";
            reason=e.toString();

            Utility.showToastLatest(activity,"Error 86744 "+e.toString(),"ERROR");
            callForResult();
        }
    }


    private void showInstallationDialig(String openWithPackage) {

        AlertDialog.Builder gotoPlaystore = new AlertDialog.Builder(this);
        gotoPlaystore.setMessage("Required Application not available, download it to countinue.");

        gotoPlaystore.setPositiveButton("Download", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + openWithPackage)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + openWithPackage)));
                }

            }
        });

        gotoPlaystore.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        gotoPlaystore.setTitle("Device");
        gotoPlaystore.setCancelable(false);

        gotoPlaystore.show();


    }
    private void initUiSdk(PayUPaymentParams payUPaymentParams, JSONObject data) {

        PayUCheckoutPro.open(
                this,
                payUPaymentParams,
                new PayUCheckoutProListener() {

                    @Override
                    public void onPaymentSuccess(Object response) {
                        payuResponseCallback(response);
                    }

                    @Override
                    public void onPaymentFailure(Object response) {
                        payuResponseCallback(response);
                    }

                    @Override
                    public void onPaymentCancel(boolean isTxnInitiated) {
                        callForResult();
                    }

                    @Override
                    public void onError(ErrorResponse errorResponse) {
                        String errorMessage = errorResponse.getErrorMessage();
                        callForResult();
                    }

                    @Override
                    public void setWebViewProperties(@Nullable WebView webView, @Nullable Object o) {
                        //For setting webview properties, if any. Check Customized Integration section for more details on this
                    }

                    @Override
                    public void generateHash(HashMap<String, String> valueMap, PayUHashGenerationListener hashGenerationListener) {
                        String hashName = valueMap.get(PayUCheckoutProConstants.CP_HASH_NAME);
                        String hashData = valueMap.get(PayUCheckoutProConstants.CP_HASH_STRING);
                        String hashType = valueMap.get(PayUCheckoutProConstants.CP_HASH_TYPE);

                        //Log.d("fhsssdhhd",""+hashType+"  hasName "+hashName+" hashData "+hashData);
                        if (!TextUtils.isEmpty(hashName) && !TextUtils.isEmpty(hashData)) {
                            //Generate Hash from your backend here
                            String prodSalt="";
                            String secretKey="";
                            try {
                                prodSalt = data.getString("salt");
                                secretKey=data.getString("secretKey");

                            }
                            catch (Exception e){

                            }

                            String salt = prodSalt;
                            if (valueMap.containsKey(PayUCheckoutProConstants.CP_POST_SALT))
                                salt = salt + "" + (valueMap.get(PayUCheckoutProConstants.CP_POST_SALT));
                            //

                            String hash = null;
                            if (hashName.equalsIgnoreCase(PayUCheckoutProConstants.CP_LOOKUP_API_HASH)) {
                                //Calculate HmacSHA1 HASH for calculating Lookup API Hash
                                ///Do not generate hash from local, it needs to be calculated from server side only. Here, hashString contains hash created from your server side.

                                hash = Utility.calculateHmacSHA1Hash(hashData, secretKey);
                            } else if (hashType!=null && hashType.equalsIgnoreCase(PayUCheckoutProConstants.CP_V2_HASH)){
                                hash = Utility.calculateHmacSha256(hashData,prodSalt);

                            }else {

                                //Log.d("fhsssdhhd","has calulate  "+hashName+" - "+hashData+" salt "+salt);
                                //Calculate SHA-512 Hash here
                                hash = Utility.calculateHash(hashData + salt);


                            }

                            HashMap<String, String> dataMap = new HashMap<>();
                            dataMap.put(hashName, hash);
                            hashGenerationListener.onHashGenerated(dataMap);
                        }
                    }
                }
        );
    }
    private PayUPaymentParams preparePayUBizParams(JSONObject data) {

        PayUPaymentParams.Builder builder = new PayUPaymentParams.Builder();
        try {
            builder.setAmount(amount)
                    .setIsProduction(true)
                    .setProductInfo(data.getString("note"))
                    .setKey(data.getString("merchantKey"))
                    .setPhone(data.getString("customerMobile"))
                    .setTransactionId(String.valueOf(System.currentTimeMillis()))
                    .setFirstName(data.getString("customerFirstName"))
                    .setEmail(data.getString("customerEmail"))
                    .setSurl(data.getString("successUrl"))
                    .setFurl(data.getString("failedUrl"))
                    .setUserCredential(data.getString("userCredential"));

        }
        catch (Exception e){
            Utility.showToastLatest(this,"Error 421476 "+e.toString(),"ERROR");
        }
        PayUPaymentParams payUPaymentParams = builder.build();
        return payUPaymentParams;
    }
    private void payuResponseCallback(Object response) {
        HashMap<String, Object> result = (HashMap<String, Object>) response;

        String payuResponse=result.get(PayUCheckoutProConstants.CP_PAYU_RESPONSE)+"<|>"+result.get(
                PayUCheckoutProConstants.CP_MERCHANT_RESPONSE
        );



    }


    private void call_payu(JSONObject data) {

        initUiSdk(preparePayUBizParams(data),data);
    }

    private Map<String, String> getInputParams(JSONObject data) {

        /*
         * appId will be available to you at CashFree Dashboard. This is a unique
         * identifier for your app. Please replace this appId with your appId.
         * Also, as explained below you will need to change your appId to prod
         * credentials before publishing your app.
         */
        Map<String, String> params = new HashMap<>();
        try {


            params.put(PARAM_APP_ID, data.getString("appId"));
            params.put(PARAM_ORDER_ID, orderId);
            params.put(PARAM_ORDER_AMOUNT, amount);
            params.put(PARAM_ORDER_NOTE, data.getString("note"));
            params.put(PARAM_CUSTOMER_NAME, data.getString("customerName"));
            params.put(PARAM_CUSTOMER_PHONE, data.getString("customerPhone"));
            params.put(PARAM_CUSTOMER_EMAIL, data.getString("customerEmail"));
            params.put(PARAM_ORDER_CURRENCY, "INR");
            params.put(PARAM_NOTIFY_URL, data.getString("callbackUrl"));


        }
        catch (Exception e){
            Utility.showToastLatest(this,"Error 2459784 "+e.toString(),"ERROR");
        }

        return params;
    }

    private void call_cashfree(JSONObject data){
        //Utility.showToast(AddfundActivity.this,"called");

        try {
            String checkSum = data.getString("checkSum");


            CFPaymentService cfPaymentService = CFPaymentService.getCFPaymentServiceInstance();
            cfPaymentService.setOrientation(0);

            cfPaymentService.doPayment(this,
                    getInputParams(data),
                    checkSum,
                    "PROD",
                    "#784BD2",
                    "#FFFFFF",
                    false);

        }
        catch (Exception e){
            Utility.showToastLatest(this,"Error 0784532 "+e.toString(),"ERROR");
        }


    }
    @Override
    public void onBackPressed() {
        // Handle the back button click event here
        // For example, you can display a dialog or perform any necessary actions
        //Toast.makeText(this, "Back button clicked", Toast.LENGTH_SHORT).show();
        resultListener.onMagicApiResult(hashMap);
        // If you want to perform the default back button behavior, call super.onBackPressed()
         super.onBackPressed();
    }
}
