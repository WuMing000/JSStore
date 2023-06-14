package com.js.appstore.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.js.appstore.R;
import com.js.appstore.bean.APPLocalBean;
import com.js.appstore.bean.APPServerBean;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class UserRecyclerViewAdapter extends RecyclerView.Adapter<UserRecyclerViewAdapter.MyViewHolder> {

    private Context mContext;
    private List<APPLocalBean> mList;

    private OnItemClickListener onItemClickListener;
    private OnStateClickListener onStateClickListener;

    private TextView btnState;

    public UserRecyclerViewAdapter(Context context, List<APPLocalBean> list) {
        this.mContext = context;
        this.mList = list;
    }

    @NonNull
    @Override
    public UserRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_user, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserRecyclerViewAdapter.MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        String iconId = mList.get(position).getAppIcon();
        String appName = mList.get(position).getAppName();
        String appInformation = mList.get(position).getAppInformation();
        String btnName = mList.get(position).getAppState();
//        holder.ivIcon.setImageResource(iconId);
        Glide.with(mContext).load(iconId).into(holder.ivIcon);
        holder.tvAppName.setText(appName);
        holder.tvAppInformation.setText(appInformation);
        holder.btnState.setText(btnName);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.OnClick(position);
            }
        });
        holder.btnState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                btnState = holder.btnState;
                onStateClickListener.OnClick(position, holder.btnState);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivIcon;
        private TextView tvAppName;
        private TextView tvAppInformation;
        private TextView btnState;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_icon);
            tvAppName = itemView.findViewById(R.id.tv_app_name);
            tvAppInformation = itemView.findViewById(R.id.tv_app_information);
            btnState = itemView.findViewById(R.id.btn_state);
        }
    }

    public void setStateText(String text) {
        btnState.setText(text);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setStateOnClickListener(OnStateClickListener onStateClickListener) {
        this.onStateClickListener = onStateClickListener;
    }

    public interface OnItemClickListener {
        void OnClick(int position);
    }

    public interface OnStateClickListener {
        void OnClick(int position, TextView btnState);
    }
}
