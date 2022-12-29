package com.qiniu.qnnouidemo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.qiniu.qnnouidemo.uitil.BZUser;
import com.qiniu.qnnouidemo.uitil.SpUtil;
import com.qlive.core.QLiveCallBack;
import com.qlive.core.been.QCreateRoomParam;
import com.qlive.core.been.QLiveRoomInfo;
import com.qlive.core.been.QLiveStatistics;
import com.qlive.giftservice.QGift;
import com.qlive.giftservice.QGiftStatistics;
import com.qlive.jsonutil.JsonUtils;
import com.qlive.qnlivekit.uitil.OKHttpManger;
import com.qlive.qnlivekit.uitil.UserManager;
import com.qlive.sdk.QLive;
import com.qlive.sdk.QRooms;
import com.qlive.sdk.QUserInfo;
import com.qlive.uikitcore.dialog.LoadingDialog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.Response;
import okio.Buffer;

/**
 * 直播列表
 */
public class MainActivity extends AppCompatActivity {
    private RecyclerView rv;
    private StringAdapter adapter;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rv=findViewById(R.id.rv);
        adapter=new StringAdapter(this);
        rv.setAdapter(adapter);
        QLive.getRooms().listRoom(1, 100, new QLiveCallBack<List<QLiveRoomInfo>>() {
            @Override
            public void onError(int i, String s) {
                Log.e("==",i+"=e="+s);
            }

            @Override
            public void onSuccess(List<QLiveRoomInfo> qLiveRoomInfos) {
                Log.e("==",qLiveRoomInfos.size()+"=");
                adapter.setList(qLiveRoomInfos);
                adapter.notifyDataSetChanged();
                for(QLiveRoomInfo item:qLiveRoomInfos){
                    Log.e("==",item.title+"="+item);
                }
            }
        });
    }
    public void onClickLive(View view){
//创建房间
        QCreateRoomParam param = new QCreateRoomParam();
        param.title="testlive-"+formatTime(System.currentTimeMillis());
        QLive.getRooms().createRoom(param, new QLiveCallBack<QLiveRoomInfo>() {
            @Override
            public void onError(int i, String s) {

            }

            @Override
            public void onSuccess(QLiveRoomInfo qLiveRoomInfo) {
                Intent intent=new Intent(MainActivity.this, LivePublishActivity.class);
                intent.putExtra("data",qLiveRoomInfo);
                startActivity(intent);
            }
        });
    }

    private String formatTime(long timeMillisl) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(timeMillisl);
        return simpleDateFormat.format(date);
    }

}
