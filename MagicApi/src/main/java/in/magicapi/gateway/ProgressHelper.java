package in.magicapi.gateway;


import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;


public class ProgressHelper {

    private Dialog dialog;
    private Activity mActivity;
    private TextView progressBarTextView;

    private static volatile ProgressHelper instance = null;

    public ProgressHelper(Activity activity) {
        this.mActivity = activity;
        dialog = new Dialog(mActivity);
        dialog.setCancelable(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_progess);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

    }


    /**
     * Show progress dialog when web service call
     */
    public void show() {

        if (dialog == null) {
            dialog = new Dialog(mActivity);
            dialog.setCancelable(false);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_progess);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        }
        try {
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        /*progressBar.getIndeterminateDrawable()
                .setColorFilter(0xFFFFFFFF, android.graphics.PorterDuff.Mode.MULTIPLY);*/


    }

    public void show(String value) {
        dialog = new Dialog(mActivity);
        dialog.setCancelable(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_progess);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        progressBarTextView = dialog.findViewById(R.id.progressBarTextView);
        progressBarTextView.setText(value);
        ProgressBar progressBar = dialog.findViewById(R.id.progressBar);

        /*progressBar.getIndeterminateDrawable()
                .setColorFilter(0xFFFFFFFF, android.graphics.PorterDuff.Mode.MULTIPLY);*/

        dialog.show();
        //Log.d("Progress ","dialog show");
    }

    public void setTextValue(String value) {
        if (dialog != null) {
            progressBarTextView = dialog.findViewById(R.id.progressBarTextView);
            progressBarTextView.setText(value);
        }
    }

    /**
     * Dismiss progress dialog
     */
    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            try {
                dialog.dismiss();
                Log.d("LoginActivity", "Dialog dismissed");
            } catch (Exception e) {
                Log.d("LoginActivity", "Dialog error" + e.toString());
            }
        }
    }

    public boolean isShowing() {
        if (dialog != null)
            return dialog.isShowing();
        else
            return false;
    }
}

