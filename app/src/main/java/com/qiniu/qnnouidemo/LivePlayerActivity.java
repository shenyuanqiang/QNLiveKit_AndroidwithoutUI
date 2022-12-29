package com.qiniu.qnnouidemo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.qlive.core.QLiveCallBack;
import com.qlive.core.QLiveStatus;
import com.qlive.core.QLiveStatusListener;
import com.qlive.core.been.QLiveRoomInfo;
import com.qlive.linkmicservice.QLinkMicService;
import com.qlive.playerclient.QPlayerClient;
import com.qlive.qplayer.QPlayerTextureRenderView;
import com.qlive.sdk.QLive;

import java.util.List;

/**
 * 观众看直播页面
 */
public class LivePlayerActivity extends AppCompatActivity {
    private QPlayerTextureRenderView playerView;
    private QLiveRoomInfo data;
    private QPlayerClient client;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_player);
        data=(QLiveRoomInfo)getIntent().getSerializableExtra("data");
        playerView=findViewById(R.id.playerView);
//用户拉流房间
        client = QLive.createPlayerClient();
        client.addLiveStatusListener(new QLiveStatusListener() {
            @Override
            public void onLiveStatusChanged(QLiveStatus qLiveStatus, String s) {

            }
        });
//加入房间
        client.joinRoom(data.liveID, new QLiveCallBack<QLiveRoomInfo>() {
            @Override
            public void onError(int i, String s) {

            }

            @Override
            public void onSuccess(QLiveRoomInfo qLiveRoomInfo) {
//播放
                client.play(playerView);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        client.destroy();

    }
    public void onClickExit(View view){
        //最好是等退出的成功方法回调以后在finish
        client.leaveRoom(new QLiveCallBack<Void>() {
            @Override
            public void onError(int i, String s) {

            }

            @Override
            public void onSuccess(Void unused) {
                finish();
            }
        });
    }
}
