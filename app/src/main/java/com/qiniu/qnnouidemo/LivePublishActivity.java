package com.qiniu.qnnouidemo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.qlive.avparam.QCameraParam;
import com.qlive.avparam.QMicrophoneParam;
import com.qlive.core.QLiveCallBack;
import com.qlive.core.QLiveStatus;
import com.qlive.core.QLiveStatusListener;
import com.qlive.core.been.QLiveRoomInfo;
import com.qlive.playerclient.QPlayerClient;
import com.qlive.pushclient.QPusherClient;
import com.qlive.qplayer.QPlayerTextureRenderView;
import com.qlive.rtclive.QPushTextureView;
import com.qlive.sdk.QLive;
import com.qlive.uikitcore.dialog.LoadingDialog;

/**
 * 主播直播页面
 */
public class LivePublishActivity extends AppCompatActivity {
    private QPushTextureView playerView;
    private QLiveRoomInfo data;
    private QPusherClient client;
    private Button btn_start;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_publish);
        playerView=findViewById(R.id.playerView);
        btn_start=findViewById(R.id.btn_start);
        data=(QLiveRoomInfo)getIntent().getSerializableExtra("data");
// 主播推流
//创建推流client
         client = QLive.createPusherClient();
        QMicrophoneParam microphoneParams =  new QMicrophoneParam();
        microphoneParams.sampleRate=48000;
//启动麦克风模块
        client.enableMicrophone(microphoneParams);
        QCameraParam cameraParam =  new QCameraParam();
        cameraParam.FPS=15;
//启动摄像头模块
        client.enableCamera(cameraParam,playerView);
        //注册房间端监听
        client.addLiveStatusListener(new QLiveStatusListener() {
            @Override
            public void onLiveStatusChanged(QLiveStatus qLiveStatus, String s) {

            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        client.destroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        client.resume();
    }
    @Override
    protected void onStop() {
        super.onStop();
        client.pause();
    }
    private boolean isPublishing;
    public void onClickStart(View view){
        LoadingDialog.INSTANCE.showLoading(getSupportFragmentManager());
        if(isPublishing){
            btn_start.setText("开始");
            //退出直播间，停止推流
            client.leaveRoom(new QLiveCallBack<Void>() {
                @Override
                public void onError(int i, String s) {
                    btn_start.setText("停止");
                    LoadingDialog.INSTANCE.cancelLoadingDialog();
                    Toast.makeText(LivePublishActivity.this,i+",停止推流失败，"+s,Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSuccess(Void unused) {
                    finish();
                    LoadingDialog.INSTANCE.cancelLoadingDialog();
                    Toast.makeText(LivePublishActivity.this,"停止推流",Toast.LENGTH_SHORT).show();
                    isPublishing=false;
                }
            });
        }else {
            btn_start.setText("停止");
            //加入直播间,开始推流
            client.joinRoom(data.liveID, new QLiveCallBack<QLiveRoomInfo>() {
                @Override
                public void onError(int i, String s) {
                    btn_start.setText("开始");
                    LoadingDialog.INSTANCE.cancelLoadingDialog();
                    Toast.makeText(LivePublishActivity.this,i+",推流失败，"+s,Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSuccess(QLiveRoomInfo qLiveRoomInfo) {
                    LoadingDialog.INSTANCE.cancelLoadingDialog();
                    Toast.makeText(LivePublishActivity.this,"推流成功",Toast.LENGTH_SHORT).show();
                    isPublishing=true;
                }
            });
        }
    }
}
