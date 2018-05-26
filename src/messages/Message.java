package messages;

import java.io.Serializable;

public class Message implements Serializable {

    private int userId;
    private MessageType type = null;
    private int gameid;
    private String msg = null;


    public Message() {

    }

    public Message(MessageType a, String b) {
        type = a;
        msg = b;
    }


    public int getUserId() {
        return userId;
    }

    public void setUserId(int id) {
        this.userId = id;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setGameid(int name) {
        this.gameid = name;
    }

    public int getGameId() {
        return gameid;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }


}