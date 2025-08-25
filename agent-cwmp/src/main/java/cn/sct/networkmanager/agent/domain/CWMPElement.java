package cn.sct.networkmanager.agent.domain;

import cn.sct.agent.item.Item;
import cn.sct.networkmanager.agent.protocol.cwmp.ProactiveNoticeChangeManager;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public abstract class CWMPElement<T> implements Item<T> {
    private  List<String> accessList=new ArrayList<>();
    private Notification notification = Notification.NONE;
    private boolean isProcessed = true;
    public abstract String getPath();

    /**
     *值改变的时候调用notice方法，
     * 会根据配置Attribute选择通知方式
     */
    public void notice(){
        if (notification == Notification.NONE){
            return;
        }
        if (notification == Notification.PASSIVE_NOTIFICATION){
            isProcessed=false;
        }
        if (notification==Notification.PROACTIVE_NOTIFICATIONS){//主动通知，发生值改变，立即向ACS发送消息
            isProcessed=false;
            ProactiveNoticeChangeManager.notice(getPath(),getValue());
        }

    }



    public enum Notification {
        NONE,//cpe不会主动发送通知
        PASSIVE_NOTIFICATION,//被动通知。CPE 下次建立连接的时候会携带该值
        PROACTIVE_NOTIFICATIONS;//主动通知。每当指定的参数值发生更改时，CPE 必须启动与 ACS 的会话，并在关联的通知消息的 ParameterList 中包含新值


      public   static Notification fromCode(int code){
            switch (code){
                case 0:
                    return NONE;
                case 1:
                    return PASSIVE_NOTIFICATION;
                case 2:
                    return PROACTIVE_NOTIFICATIONS;
            }
            return NONE;
        }
    }
}
