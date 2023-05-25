package com.js.appstore.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.js.appstore.R;

import androidx.annotation.NonNull;

public class UpdateDialog extends Dialog {

    private LinearLayout llUpdate;
    private TextView tvUpdateTitle, tvUpdateMessage, tvUpdateProgress;
    private Button btnUpdate, btnExit;
    private ProgressBar pbUpdateProgress;

    public UpdateDialog(@NonNull Context context) {
        super(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_update, null);

        llUpdate = view.findViewById(R.id.ll_update);
        tvUpdateTitle = view.findViewById(R.id.tv_update_title);
        tvUpdateMessage = view.findViewById(R.id.tv_update_message);
        btnUpdate = view.findViewById(R.id.btn_update);
        btnExit = view.findViewById(R.id.btn_exit);
        pbUpdateProgress = view.findViewById(R.id.pb_update_progress);
        tvUpdateProgress = view.findViewById(R.id.tv_update_progress);

        setContentView(view);
    }

    public void setTitle(String text) {
        tvUpdateTitle.setText(text);
    }

    public void setMessage(String text) {
        tvUpdateMessage.setText(text);
    }

    public void setTitleVisible(int isVisible) {
        tvUpdateTitle.setVisibility(isVisible);
    }

    public void setUpdateOnClickListener(View.OnClickListener onClickListener) {
        btnUpdate.setOnClickListener(onClickListener);
    }

    public void setExitOnClickListener(View.OnClickListener onClickListener) {
        btnExit.setOnClickListener(onClickListener);
    }

    public void setButtonVisible(int isVisible) {
        llUpdate.setVisibility(isVisible);
    }

    public void setProgressVisible(int isVisible) {
        pbUpdateProgress.setVisibility(isVisible);
        tvUpdateProgress.setVisibility(isVisible);
    }

    public void setPbProgress(int progress) {
        pbUpdateProgress.setProgress(progress);
    }

    public void setTvProgress(String progress) {
        tvUpdateProgress.setText(progress + "%");
    }

}
