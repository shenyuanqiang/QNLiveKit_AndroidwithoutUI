package com.qiniu.qnnouidemo;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.qiniu.qnnouidemo.uitil.Utils;
import com.qlive.avparam.QCameraParam;
import com.qlive.avparam.QMicrophoneParam;
import com.qlive.avparam.QMixStreaming;
import com.qlive.avparam.QRoomConnectionState;
import com.qlive.core.QInvitationHandlerListener;
import com.qlive.core.QLiveCallBack;
import com.qlive.core.QLiveStatus;
import com.qlive.core.QLiveStatusListener;
import com.qlive.core.been.QExtension;
import com.qlive.core.been.QInvitation;
import com.qlive.core.been.QLiveRoomInfo;
import com.qlive.core.been.QLiveUser;
import com.qlive.linkmicservice.QAudienceMicHandler;
import com.qlive.linkmicservice.QLinkMicMixStreamAdapter;
import com.qlive.linkmicservice.QLinkMicService;
import com.qlive.linkmicservice.QLinkMicServiceListener;
import com.qlive.linkmicservice.QMicLinker;
import com.qlive.playerclient.QPlayerClient;
import com.qlive.qplayer.QPlayerTextureRenderView;
import com.qlive.roomservice.QRoomService;
import com.qlive.rtclive.QPushTextureView;
import com.qlive.sdk.QLive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 观众看直播页面
 */
public class LivePlayerActivity extends AppCompatActivity {
    private QPlayerTextureRenderView playerView;
    private QLiveRoomInfo data;
    private QPlayerClient client;
    private LinearLayout containerView,containerLageView;
    private Button btn_link_mic;

    public void linkMicDialog(QInvitation qInvitation) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setIcon(R.mipmap.ic_launcher);
        dialog.setTitle(qInvitation.initiator.nick+"邀请您连麦");
        dialog.setPositiveButton("同意",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //点击按钮 接受操作
                        client.getService(QLinkMicService.class).getInvitationHandler().accept(qInvitation.invitationID, null, new QLiveCallBack<Void>() {
                            @Override
                            public void onError(int i, String s) {
                                Toast.makeText(LivePlayerActivity.this,i+"=接受连麦失败="+s+",="+qInvitation.invitationID,Toast.LENGTH_SHORT).show();
                                Log.e("==accept",i+"=onError="+s);
                            }

                            @Override
                            public void onSuccess(Void unused) {
                                linkMicChange(true);
                                Toast.makeText(LivePlayerActivity.this,"=接受连麦成功="+qInvitation.invitationID,Toast.LENGTH_SHORT).show();
                                Log.e("==reject","=onSuccess=");
                                //对方接受后调用开始上麦 传摄像头麦克参数 自动开启相应的媒体流
                                client.getService(QLinkMicService.class).getAudienceMicHandler().startLink(null, new QCameraParam(), new QMicrophoneParam(), new QLiveCallBack<Void>() {
                                    @Override
                                    public void onError(int i, String s) {
                                        Toast.makeText(LivePlayerActivity.this,i+"=连麦失败="+s,Toast.LENGTH_SHORT).show();
                                        Log.e("==startLink",i+"=onError="+s);
                                    }

                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.e("==startLink","=onSuccess=");
                                        Toast.makeText(LivePlayerActivity.this,"连麦成功",Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                    }
                });
        dialog.setNegativeButton("拒绝",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 点击按钮  拒绝操作
                        client.getService(QLinkMicService.class).getInvitationHandler().reject(qInvitation.invitationID, null, new QLiveCallBack<Void>() {
                            @Override
                            public void onError(int i, String s) {
                                Toast.makeText(LivePlayerActivity.this,i+"=拒绝失败="+s,Toast.LENGTH_SHORT).show();
                                Log.e("==reject",i+"=onError="+s);
                            }

                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(LivePlayerActivity.this,"=拒绝成功=",Toast.LENGTH_SHORT).show();
                                Log.e("==reject","=onSuccess=");
                            }
                        });
                    }
                });
        dialog.show();// 显示
    }
    //邀请监听
    private QInvitationHandlerListener mInvitationListener=new QInvitationHandlerListener() {
        //收到邀请
        @Override
        public void onReceivedApply(QInvitation qInvitation) {
            linkMicDialog(qInvitation);
//            //todo 显示申请弹窗  qInvitation.getInitiator()获取到申请方资料 显示UI
//            // 点击按钮  拒绝操作
//            client.getService(QLinkMicService.class).getInvitationHandler().reject(qInvitation.invitationID, null, callBack);
//            //点击按钮 接受操作
//            client.getService(QLinkMicService.class).getInvitationHandler().accept(qInvitation.invitationID, null, callBack);
        }
        //收到对方取消
        @Override
        public void onApplyCanceled(QInvitation qInvitation) {}
        //发起超时对方没响应
        @Override
        public void onApplyTimeOut(QInvitation qInvitation) {}
        //对方接受
        @Override
        public void onAccept(QInvitation qInvitation) {
            linkMicChange(true);
            //对方接受后调用开始上麦 传摄像头麦克参数 自动开启相应的媒体流
            client.getService(QLinkMicService.class).getAudienceMicHandler().startLink(null, new QCameraParam(), new QMicrophoneParam(), new QLiveCallBack<Void>() {
                @Override
                public void onError(int i, String s) {
                    Toast.makeText(LivePlayerActivity.this,i+"=连麦失败="+s,Toast.LENGTH_SHORT).show();
                    Log.e("==startLink",i+"=onError="+s);
                }

                @Override
                public void onSuccess(Void unused) {
                    Log.e("==startLink","=onSuccess=");
                    Toast.makeText(LivePlayerActivity.this,"连麦成功",Toast.LENGTH_SHORT).show();
                }
            });
        }
        //对方拒绝
        @Override
        public void onReject(QInvitation qInvitation) {
            Toast.makeText(LivePlayerActivity.this,"对方拒绝了连麦",Toast.LENGTH_SHORT).show();
        }
    };
    private QLinkMicService linkService = null;
    public Map<String, QPushTextureView> containerViewMaps=new HashMap<>();
    //麦位麦位监听
    private QLinkMicServiceListener mQLinkMicServiceListener= new QLinkMicServiceListener() {
        //有人上麦了
        @Override
        public void onLinkerJoin(QMicLinker micLinker) {
            //麦上用户和主播设置这个用户预览 直播拉流用户无需设置
            QPushTextureView preview =new  QPushTextureView(LivePlayerActivity.this);
            //主播用全屏布局
            if (TextUtils.equals(micLinker.user.userId,data.anchor.userId)) {
                preview.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
                //添加到你要添加到的容器
                containerLageView.addView(preview);
            }else {//其他人在上面左右排列
                preview.setLayoutParams(new LinearLayout.LayoutParams(Utils.dip2px(LivePlayerActivity.this,80)
                        ,Utils.dip2px(LivePlayerActivity.this,150)));
                //添加到你要添加到的容器
                containerView.addView(preview);
            }
            containerViewMaps.put(micLinker.user.userId,preview);
            //设置该用户的预览
            linkService.setUserPreview(micLinker.user.userId, preview);
            //跟新连麦UI 如果要显示头像 micLinker里取到上麦者资料
        }
        //有人下麦了
        @Override
        public void onLinkerLeft(QMicLinker micLinker) {
            if (TextUtils.equals(micLinker.user.userId,data.anchor.userId)) {
                containerLageView.removeView(containerViewMaps.get(micLinker.user.userId));
            }else {
                containerView.removeView(containerViewMaps.get(micLinker.user.userId));
            }


            containerViewMaps.remove(micLinker.user.userId);
            //移除已经设置的预览窗口
            //跟新连麦UI 比如去掉麦上头像
        }
        @Override
        public void onLinkerMicrophoneStatusChange(QMicLinker micLinker) {
            //跟新连麦UI
        }
        @Override
        public void  onLinkerCameraStatusChange(QMicLinker micLinker) {
            //跟新连麦UI
        }
        //某个用户被踢麦
        @Override
        public void onLinkerKicked(QMicLinker micLinker,String msg) {}
        @Override
        public void onLinkerExtensionUpdate(QMicLinker micLinker, QExtension extension) {}
    };
    //观众端连麦器监听
    private QAudienceMicHandler.LinkMicHandlerListener mQAudienceMicHandler=new QAudienceMicHandler.LinkMicHandlerListener() {
        @Override
        public void onConnectionStateChanged(QRoomConnectionState qRoomConnectionState) {

        }

        @Override
        public void onRoleChange(boolean isLinker) {
            if (isLinker) {
                containerView.setVisibility(View.VISIBLE);
                //我切换到了连麦模式 -> 使用连麦和麦上用户和主播互动 延迟更低
                for(QMicLinker micLinker:client.getService(QLinkMicService.class).getAllLinker()){
                    //对原来麦上的人设置预览
                    QPushTextureView preview =new QPushTextureView(LivePlayerActivity.this);
                    preview.setLayoutParams(new LinearLayout.LayoutParams(Utils.dip2px(LivePlayerActivity.this,80)
                            ,Utils.dip2px(LivePlayerActivity.this,150)));
                    //主播用全屏布局
                    if (TextUtils.equals(micLinker.user.userId,data.anchor.userId)) {
                        preview.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
                        //添加到你要添加到的容器
                        containerLageView.addView(preview);
                    }else {//其他人在上面左右排列
                        preview.setLayoutParams(new LinearLayout.LayoutParams(Utils.dip2px(LivePlayerActivity.this,80)
                                ,Utils.dip2px(LivePlayerActivity.this,150)));
                        //添加到你要添加到的容器
                        containerView.addView(preview);
                    }
                    containerViewMaps.put(micLinker.user.userId,preview);
                    //设置该用户的预览
                    linkService.setUserPreview(micLinker.user.userId, preview);
                }
            } else {
                containerView.removeAllViews();
                containerLageView.removeAllViews();
                containerViewMaps.clear();
                containerView.setVisibility(View.GONE);
//                //我切换拉流模式
//                for(QMicLinker it:client.getService(QLinkMicService.class).getAllLinker()){
//                    //移除对原来设置麦位移除设置预览view
//                    removePreview(it.user.userId);
//                }
            }
        }
    };
    private boolean isLinkMicing=false;
    public void onClickLinkMic(View view) {
        if(!isLinkMicing){
            linkMicUserList();
        }else{
            client.getService(QLinkMicService.class).getAudienceMicHandler().stopLink(new QLiveCallBack<Void>() {
                @Override
                public void onError(int i, String s) {
                    Toast.makeText(LivePlayerActivity.this,i+"=下麦失败="+s,Toast.LENGTH_SHORT).show();
                    Log.e("==startLink",i+"=onError="+s);
                }

                @Override
                public void onSuccess(Void unused) {
                    linkMicChange(false);
                    Toast.makeText(LivePlayerActivity.this,"=下麦成功=",Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    /**
     * 邀请连麦的列表
     */
    public void linkMicUserList() {
        client.getService(QRoomService.class).getOnlineUser(1, 100, new QLiveCallBack<List<QLiveUser>>() {
            @Override
            public void onError(int i, String s) {
                Toast.makeText(LivePlayerActivity.this,i+"=获取在线用户失败="+s,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(final List<QLiveUser> qLiveUsers) {
                final String[] items = new String[qLiveUsers.size()];
                for(int i=0;i<qLiveUsers.size();i++){
                    QLiveUser qLiveUser=qLiveUsers.get(i);
                    items[i]=qLiveUser.nick;
                }
                AlertDialog.Builder listDialog =
                        new AlertDialog.Builder(LivePlayerActivity.this);
                listDialog.setTitle("这是一个列表Dialog");
                listDialog.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.e("==","你点击了" + items[which]+"="+which);
                        QLiveUser qLiveUser=qLiveUsers.get(which);
                        //点击某个按钮 发起对某个主播申请 或者主播邀请用户
                        client.getService(QLinkMicService.class).getInvitationHandler().apply(10 * 1000, data.liveID, qLiveUser.userId, null, new QLiveCallBack<QInvitation>() {
                            @Override
                            public void onError(int i, String s) {
                                Log.e("==startLink",i+"=onError="+s);
                                Toast.makeText(LivePlayerActivity.this,i+"=邀请失败="+s,Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onSuccess(QInvitation qInvitation) {

                                Log.e("==startLink","=onSuccess=");
                                Toast.makeText(LivePlayerActivity.this,"邀请成功",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                listDialog.show();
            }
        });

    }
    private void linkMicChange(boolean isLinkMic){
        btn_link_mic.setText(isLinkMic?"下麦":"连麦");
        isLinkMicing=isLinkMic;
    }
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_player);
        data=(QLiveRoomInfo)getIntent().getSerializableExtra("data");
        playerView=findViewById(R.id.playerView);
        btn_link_mic=findViewById(R.id.btn_link_mic);
        containerView=findViewById(R.id.containerView);
        containerLageView=findViewById(R.id.containerLageView);
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
//观众设置观众连麦处理监听
        client.getService(QLinkMicService.class).getAudienceMicHandler().addLinkMicListener(mQAudienceMicHandler);
//主播和观众都关心麦位监听
        client.getService(QLinkMicService.class).addMicLinkerListener(mQLinkMicServiceListener);
//注册邀请监听
        client.getService(QLinkMicService.class).getInvitationHandler().addInvitationHandlerListener(mInvitationListener);
        linkService = client.getService(QLinkMicService.class);
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
