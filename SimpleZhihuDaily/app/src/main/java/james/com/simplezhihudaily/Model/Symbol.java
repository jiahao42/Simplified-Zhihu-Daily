package james.com.simplezhihudaily.Model;

public class Symbol{
    public static final int RECEIVE_SUCCESS = 1;
    public static final int RECEIVER_FAILED = 0;
    public static final int RECEIVER_TIMEOUT = -1;
}
/*
Enum类在这里似乎是画蛇添足
public enum Symbol {
    RECEIVE_SUCCESS(1),RECEIVE_FAILED(0),RECEIVER_TIMEOUT(-1);
    private int code;
    private Symbol(int code){
        this.code = code;
    }
    public int toInt(){
        return code;
    }
}
*/