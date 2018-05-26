package server;

import messages.Message;
import messages.MessageType;
import messages.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Iterator;

public class ClientHandler extends Thread {

    String tempPointX;
    String tempPointY;

    ObjectInputStream r;
    ObjectOutputStream w;
    Socket s;

    boolean ready = false;
    int gameid;
    int id;
    int points=0;

    public ClientHandler(Socket socket, int id) throws IOException {
        this.id = id;
        s = socket;
        w = new ObjectOutputStream(socket.getOutputStream());
        r = new ObjectInputStream(socket.getInputStream());
        System.out.println("Created handler User Id= " + id);
        this.start();
    }

    Message readMessage() {
        try {

            return (Message) r.readObject();
        } catch (IOException e) {
            System.err.println(e);
            return new Message();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return new Message();
        }

    }

    void sendMessage(Message msg) {
        try {
            w.writeObject(msg);
            w.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void commandValidator(Message msg) {

        System.out.println(msg.getType() + ":" + msg.getMsg() + ":" + msg.getGameId());
        if (msg.getType() == MessageType.CONNECTED) {

        } else if (msg.getType() == MessageType.CHAT) {
            Main.sendChatToGame(this, msg.getMsg());
            Main.getGame(this).checkPassword(msg.getMsg(),this);
        }else if (msg.getType() == MessageType.SCORE) {
            Message a = new Message();
            a.setType(MessageType.SCORE);
            StringBuilder stringBuilder=new StringBuilder();
            stringBuilder.append(points);
            a.setMsg(stringBuilder.toString());
            a.setUserId(id);
            sendMessage(a);
        } else if (msg.getType() == MessageType.POINTS) {
            String[] temp = msg.getMsg().split(",");
            if (tempPointX == null || tempPointY == null) {
                tempPointX = temp[0];
                tempPointY = temp[1];
            } else {

                //sendMessage(new Message(MessageType.POINTS,tempPointX+","+tempPointY+","+temp[0]+","+temp[1]));
                Main.drawPointsToGame(gameid, Double.parseDouble(tempPointX), Double.parseDouble(tempPointY)
                        , Double.parseDouble(temp[0]), Double.parseDouble(temp[1]));
                tempPointX = null;
                tempPointY = null;
            }

        } else if (msg.getType() == MessageType.ERASE) {
            if(msg.getMsg().equals("canvas")){
                Main.eraseCanvasToGame(msg.getGameId());
            }else {
                String[] temp = msg.getMsg().split(",");
                Main.erasePointsToGame(msg.getGameId(), Double.parseDouble(temp[0]),
                        Double.parseDouble(temp[1]), Double.parseDouble(temp[2]),
                        Double.parseDouble(temp[3]));
            }

        } else if (msg.getType() == MessageType.READY) {
            ready = true;
            Main.startGame(gameid);
        } else if (msg.getType() == MessageType.JOIN) {
            int b = Main.joinGame(msg.getGameId(), this);
            if (b == 1) {
                Message a = new Message();
                a.setType(MessageType.JOIN);
                a.setMsg("1");
                a.setGameid(msg.getGameId());
                gameid = msg.getGameId();
                sendMessage(a);
            } else {
                Message a = new Message();
                a.setType(MessageType.JOIN);
                a.setMsg("0");
                sendMessage(a);
            }
        } else if (msg.getType() == MessageType.CREATE) {
            if (msg.getMsg().equals("CREATE")) {
                final int b = Main.createGame();
                final Message a = new Message();
                a.setType(MessageType.CREATE);
                a.setMsg("1");
                a.setGameid(b);
                sendMessage(a);
            }
        } else if (msg.getType() == MessageType.LEAVE) {
            ready = false;
            Main.leaveGame(this, msg.getGameId());
        }else if (msg.getType() == MessageType.GAMES) {
            Iterator a = Main.games.iterator();
            StringBuilder stringBuilder= new StringBuilder();
            while(a.hasNext()){
                Game game= (Game) a.next();
                stringBuilder.append(game.id+"("+game.users.size()+"/"+game.MaxPlayers+")"+",");
            }
            final Message b = new Message();
            b.setType(MessageType.GAMES);
            b.setUserId(id);
            b.setMsg(stringBuilder.toString());
            sendMessage(b);
        }

    }

    @Override
    public void run() {
        try {

            Message msg = new Message();
            msg.setType(MessageType.CONNECTED);
            msg.setMsg(id + "");
            sendMessage(msg);
            do {
                msg = readMessage();
                commandValidator(msg);

                if (s.isClosed()) {
                    Main.leaveGame(this, gameid);
                    break;
                }
                if (msg.getType().equals(MessageType.DISCONNECTED)) {
                    break;
                }
                //this.sleep(1);
            } while (!s.isClosed());
            w.close();
            r.close();
            s.close();
            System.out.println("Closing handler User Id=" + id);
        } catch (NullPointerException a) {
            System.err.println("Null point");
            Main.leaveGame(this, gameid);
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}
