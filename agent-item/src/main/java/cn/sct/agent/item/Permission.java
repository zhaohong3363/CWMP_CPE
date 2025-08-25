package cn.sct.agent.item;


public enum Permission {
    WRITER(1,"可读可写"),
    READ(0,"只允许读"),
    ;


    private final int code;
    private final String des;

    public int getCode() {
        return code;
    }

    public String getDes() {
        return des;
    }

    Permission(int i, String des) {
        this.code = i;
        this.des = des;
    }
}
