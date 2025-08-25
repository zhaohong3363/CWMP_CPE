package cn.sct.networkmanager.agent.domain.enums;

public enum CWMPClientState {
    /**
     * 客户端初始状态，尚未进行任何连接操作
     */
    INITIAL,

    /**
     * 正在尝试连接到服务器
     */
    CONNECTING,

    /**
     * 连接已建立，可以正常通信
     */
    CONNECTED,

    /**
     * 连接已断开
     */
    DISCONNECTED,

    /**
     * 连接过程中发生错误
     */
    ERROR,

    /**
     * 客户端正在关闭
     */
    CLOSING,

    /**
     * 客户端已完全关闭
     */
    CLOSED

}
