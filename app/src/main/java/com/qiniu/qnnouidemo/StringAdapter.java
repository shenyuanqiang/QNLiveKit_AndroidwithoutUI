package com.qiniu.qnnouidemo;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.qlive.core.QLiveStatus;
import com.qlive.core.been.QLiveRoomInfo;

import java.util.List;

public class StringAdapter extends RecyclerView.Adapter<StringAdapter.ViewHolder> {
    private List<QLiveRoomInfo>  list;
    private Context context;
    public void setList(List<QLiveRoomInfo>  list){
        this.list=list;
    }
    public StringAdapter(Context context){
        this.context=context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_live, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QLiveRoomInfo item=list.get(position);
        Glide.with(context) //context
                .load(item.coverURL)  //url链接、地址
                .into(holder.iv_cover); //View控件
        holder.tv_name.setText(item.title);
        holder.tv_stutas.setText(getStatusName(item.liveStatus));
        holder.itemView.setOnClickListener(view->{
           Intent intent=new Intent(context, LivePlayerActivity.class);
            intent.putExtra("data",item);
            context.startActivity(intent);
        });
    }
    private String getStatusName(int liveStatus){
        if(liveStatus==QLiveStatus.ANCHOR_ONLINE.ordinal()){
            return "主播上线";
        }else if(liveStatus==QLiveStatus.PREPARE.ordinal()){
            return "房间已创建";
        }else if(liveStatus==QLiveStatus.ON.ordinal()){
            return "房间已发布";
        }else if(liveStatus==QLiveStatus.ANCHOR_OFFLINE.ordinal()){
            return "主播已离线";
        }else if(liveStatus==QLiveStatus.OFF.ordinal()){
            return "房间已关闭";
        }
        return "未知";
    }
    @Override
    public int getItemCount() {
        return list==null?0:list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView iv_cover;
        private TextView tv_name,tv_stutas;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_cover=itemView.findViewById(R.id.iv_cover);
            tv_name=itemView.findViewById(R.id.tv_name);
            tv_stutas=itemView.findViewById(R.id.tv_stutas);
        }
    }
}
