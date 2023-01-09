package com.qiniu.qnnouidemo;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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
import com.qlive.pushclient.QPusherClient;
import com.qlive.qplayer.QPlayerTextureRenderView;
import com.qlive.roomservice.QRoomService;
import com.qlive.rtclive.QPushTextureView;
import com.qlive.sdk.QLive;
import com.qlive.uikitcore.dialog.LoadingDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 主播直播页面
 */
public class LivePublishActivity extends AppCompatActivity {
    private int mixWidth = 720;
    private int mixHeight = 1280;

    /**
     * 混流每个麦位宽大小
     */
    private int mixMicWidth = 184;

    /**
     * 混流每个麦位高
     */
    private int mixMicHeight = 184;

    /**
     * 混流第一个麦位上间距
     */
    private int mixTopMargin = 174;

    /**
     * 混流参数 每个麦位间距
     */
    private int micBottomMixMargin = 15;

    /**
     * 混流参数 每个麦位右间距
     */
    private int micRightMixMargin = 30 * 3;


    private int uiMicWidth = 0;
    private int uiMicHeight = 0;
    private int uiTopMargin = 0;
    /**
     * 混流换算成屏幕 每个麦位的间距
     */
    private int micBottomUIMargin = 0;
    private int micRightUIMargin = 0;

    //页面宽高
    private int containerWidth = 0;
    private int containerHeight = 0;
    private QPushTextureView playerView;
    private QLiveRoomInfo data;
    private QPusherClient client;
    private Button btn_start;
    private LinearLayout containerView;
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
                                Toast.makeText(LivePublishActivity.this,i+"=接受连麦失败="+s+"，="+qInvitation.invitationID,Toast.LENGTH_SHORT).show();
                                Log.e("==accept",i+"=onError="+s);
                            }

                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(LivePublishActivity.this,"=接受连麦成功"+"，="+qInvitation.invitationID,Toast.LENGTH_SHORT).show();
                                Log.e("==reject","=onSuccess=");
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
                                Toast.makeText(LivePublishActivity.this,i+"=拒绝失败="+s,Toast.LENGTH_SHORT).show();
                                Log.e("==reject",i+"=onError="+s);
                            }

                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(LivePublishActivity.this,"=拒绝成功=",Toast.LENGTH_SHORT).show();
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
//            //对方接受后调用开始上麦 传摄像头麦克参数 自动开启相应的媒体流
//            client.getService(QLinkMicService.class).getAudienceMicHandler().startLink(null, new QCameraParam(), new QMicrophoneParam(), new QLiveCallBack<Void>() {
//                @Override
//                public void onError(int i, String s) {
//                    Toast.makeText(LivePublishActivity.this,i+"=连麦失败="+s,Toast.LENGTH_SHORT).show();
//                    Log.e("==startLink",i+"=onError="+s);
//                }
//
//                @Override
//                public void onSuccess(Void unused) {
//                    Log.e("==startLink","=onSuccess=");
//                    Toast.makeText(LivePublishActivity.this,"连麦成功",Toast.LENGTH_SHORT).show();
//                }
//            });
        }
        //对方拒绝
        @Override
        public void onReject(QInvitation qInvitation) {
            Toast.makeText(LivePublishActivity.this,"对方拒绝了连麦",Toast.LENGTH_SHORT).show();
        }
    };
    private QLinkMicService linkService = null;
    public Map<String,QPushTextureView> containerViewMaps=new HashMap<>();
    //麦位麦位监听
  private QLinkMicServiceListener mQLinkMicServiceListener= new QLinkMicServiceListener() {
      //有人上麦了
      @Override
      public void onLinkerJoin(QMicLinker micLinker) {
          //麦上用户和主播设置这个用户预览 直播拉流用户无需设置
          QPushTextureView preview =new  QPushTextureView(LivePublishActivity.this);
          preview.setLayoutParams(new LinearLayout.LayoutParams(Utils.dip2px(LivePublishActivity.this,80)
          ,Utils.dip2px(LivePublishActivity.this,150)));
          containerViewMaps.put(micLinker.user.userId,preview);
          //添加到你要添加到的容器
          containerView.addView(preview);
          //设置该用户的预览
          linkService.setUserPreview(micLinker.user.userId, preview);
          //跟新连麦UI 如果要显示头像 micLinker里取到上麦者资料
      }
      //有人下麦了
      @Override
      public void onLinkerLeft(QMicLinker micLinker) {
          containerView.removeView(containerViewMaps.get(micLinker.user.userId));
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
      public void onLinkerExtensionUpdate(QMicLinker micLinker,QExtension extension) {}
  };


    //混流适配 房主负责混流
    private QLinkMicMixStreamAdapter mQMixStreamAdapter=new QLinkMicMixStreamAdapter() {
        /**
         * 连麦开始如果要自定义混流画布和背景
         * 返回空则主播推流分辨率有多大就多大默认实现
         * @return
         */
        @Override
        public QMixStreaming.MixStreamParams onMixStreamStart() {
            return null;
        }
        /**
         * 混流布局适配
         */
        @Override
        public List<QMixStreaming.MergeOption> onResetMixParam(List<QMicLinker> micLinkers, QMicLinker target, boolean isJoin) {
            List<QMixStreaming.MergeOption> ops = new ArrayList<>();
//            int lastX =
//                    mixWidth - mixMicWidth - micRightMixMargin;
//            int lastY = mixTopMargin;

            int lastX =0;
            int lastY = 0;
            for(QMicLinker linker:micLinkers){
                //主播 0，0 ， 720 ，1280
                if (TextUtils.equals(linker.user.userId,data.anchor.userId)) {
                    QMixStreaming.MergeOption mergeOption=new QMixStreaming.MergeOption();
                    mergeOption.uid = linker.user.userId;
                    QMixStreaming.CameraMergeOption cameraMergeOption=new QMixStreaming.CameraMergeOption();
                    cameraMergeOption.isNeed = true;
                    cameraMergeOption.x = 0;
                    cameraMergeOption.y = 0;
                    cameraMergeOption.z = 0;
                    cameraMergeOption.width = mixWidth;
                    cameraMergeOption.height = mixHeight;
                    mergeOption.cameraMergeOption=cameraMergeOption;
                    QMixStreaming.MicrophoneMergeOption microphoneMergeOption=new QMixStreaming.MicrophoneMergeOption();
                    microphoneMergeOption.isNeed = true;
                    mergeOption.microphoneMergeOption = microphoneMergeOption;
                    ops.add(mergeOption);
                } else {
                    //用户 每个 右上角依次往下排列
                    QMixStreaming.MergeOption mergeOption=new QMixStreaming.MergeOption();
                    mergeOption.uid = linker.user.userId;
                    QMixStreaming.CameraMergeOption cameraMergeOption=new QMixStreaming.CameraMergeOption();
                    cameraMergeOption.isNeed = linker.isOpenCamera;
                    cameraMergeOption.x = lastX;
                    cameraMergeOption.y = lastY;
                    cameraMergeOption.z = 1;
                    cameraMergeOption.width = mixMicWidth;
                    cameraMergeOption.height = mixMicHeight;
                    mergeOption.cameraMergeOption = cameraMergeOption;
                    lastY += micBottomMixMargin + mixMicHeight;
                    QMixStreaming.MicrophoneMergeOption microphoneMergeOption=new QMixStreaming.MicrophoneMergeOption();
                    microphoneMergeOption.isNeed=true;
                    mergeOption.microphoneMergeOption=microphoneMergeOption;
                    ops.add(mergeOption);
                }
            }
            return ops;
        }
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
                    QPushTextureView preview =new QPushTextureView(LivePublishActivity.this);
                    //添加到你要添加到的容器
                    containerView.addView(preview);
                    //设置该用户的预览
                    linkService.setUserPreview(micLinker.user.userId, preview);
                }
            } else {
                containerView.removeAllViews();
                containerView.setVisibility(View.GONE);
//                //我切换拉流模式
//                for(QMicLinker it:client.getService(QLinkMicService.class).getAllLinker()){
//                    //移除对原来设置麦位移除设置预览view
//                    removePreview(it.user.userId);
//                }
            }
        }
    };

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_publish);
        mixMicWidth = Utils.dip2px(this,80);
        mixMicHeight = Utils.dip2px(this,150);
        playerView=findViewById(R.id.playerView);
        containerView=findViewById(R.id.containerView);
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
//主播设置混流适配
        client.getService(QLinkMicService.class).getAnchorHostMicHandler().setMixStreamAdapter(mQMixStreamAdapter);
//观众设置观众连麦处理监听
        client.getService(QLinkMicService.class).getAudienceMicHandler().addLinkMicListener(mQAudienceMicHandler);
//主播和观众都关心麦位监听
        client.getService(QLinkMicService.class).addMicLinkerListener(mQLinkMicServiceListener);
//注册邀请监听
        client.getService(QLinkMicService.class).getInvitationHandler().addInvitationHandlerListener(mInvitationListener);
        linkService = client.getService(QLinkMicService.class);
    }


    /**
     * 邀请连麦的列表
     */
    public void onClickLinkMic(View view) {
        client.getService(QRoomService.class).getOnlineUser(1, 100, new QLiveCallBack<List<QLiveUser>>() {
            @Override
            public void onError(int i, String s) {
                Toast.makeText(LivePublishActivity.this,i+"=获取在线用户失败="+s,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(final List<QLiveUser> qLiveUsers) {
                final String[] items = new String[qLiveUsers.size()];
                for(int i=0;i<qLiveUsers.size();i++){
                    QLiveUser qLiveUser=qLiveUsers.get(i);
                    items[i]=qLiveUser.nick;
                }
                AlertDialog.Builder listDialog =
                        new AlertDialog.Builder(LivePublishActivity.this);
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
                                Toast.makeText(LivePublishActivity.this,i+"=邀请失败="+s,Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onSuccess(QInvitation qInvitation) {
                                Log.e("==startLink","=onSuccess=");
                                Toast.makeText(LivePublishActivity.this,"邀请成功",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                listDialog.show();
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
