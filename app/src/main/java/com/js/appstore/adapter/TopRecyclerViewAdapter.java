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

public class TopRecyclerViewAdapter extends RecyclerView.Adapter<TopRecyclerViewAdapter.MyViewHolder> {

    private Context mContext;
    private List<APPLocalBean> mList;

    private OnItemClickListener onItemClickListener;

    public TopRecyclerViewAdapter(Context context, List<APPLocalBean> list) {
        this.mContext = context;
        this.mList = list;
    }

    @NonNull
    @Override
    public TopRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_top_text, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TopRecyclerViewAdapter.MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        if (mList.size() != 0) {
            String i = mList.get(position % mList.size()).getAppPicture().split(",")[0];
//            holder.ivRecommend.setImageResource(i);
            Glide.with(mContext).load(i).into(holder.ivRecommend);
            String iconId = mList.get(position % mList.size()).getAppIcon();
            String appName = mList.get(position % mList.size()).getAppName();
            String appInformation = mList.get(position % mList.size()).getAppInformation();
            Glide.with(mContext).load(iconId).into(holder.ivIcon);
            holder.tvAppName.setText(appName);
            holder.tvAppInformation.setText(appInformation);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.OnClick(position % mList.size());
            }
        });
    }

    @Override
    public int getItemCount() {
        return Integer.MAX_VALUE;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivRecommend;
        private ImageView ivIcon;
        private TextView tvAppName;
        private TextView tvAppInformation;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            ivRecommend = itemView.findViewById(R.id.iv_recommend);
            ivIcon = itemView.findViewById(R.id.iv_icon);
            tvAppName = itemView.findViewById(R.id.tv_app_name);
            tvAppInformation = itemView.findViewById(R.id.tv_app_information);
        }
    }

    public void setOnItemClickListener(TopRecyclerViewAdapter.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void OnClick(int position);
    }
}
