package server;


import messages.Message;
import messages.MessageType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashSet;
import java.util.Iterator;

public class Main {

    static HashSet<ClientHandler> users = new HashSet<ClientHandler>();
    static HashSet<Game> games = new HashSet<Game>();
    static Thread updateThread;

    static int Maxplayers = 4;
    static int Maxrounds = 6;
    static String[] passwords;
    private static ServerSocket server;

    private static void initServer() {
        try {
            server = new ServerSocket(3000);
            readXML();
            System.out.println("init server socket");
        } catch (Exception err) {
            System.out.println(err);
        }
    }

    private static void updateUsers() {

        updateThread = new Thread(() -> {
            while (true) {
                /*
                for (ClientHandler ch : users) {
                    if (ch.s.isClosed()) {
                        users.remove(ch);
                    }
                }*/

                for (Game game : games) {
                    if (game.gameEnded == true) {
                        games.remove(game);
                    }
                }

                try {
                    updateThread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });
        updateThread.start();


    }

    public static int maxUserId() {
        Iterator<ClientHandler> it = users.iterator();
        int c1 = 1;
        int c2 = 1;
        while (it.hasNext()) {
            c1 = it.next().id;
            if (c1 > c2) {
                c2 = c1;
            }
            ;
        }
        ;
        return c2 + 1;
    }

    public static int maxGameId() {
        Iterator<Game> it = games.iterator();
        int c1 = 1;
        int c2 = 1;
        while (it.hasNext()) {
            c1 = it.next().id;
            if (c1 > c2) {
                c2 = c1;
            }
            ;
        }
        ;
        return c2 + 1;
    }


    public static void main(String[] args) throws IOException {
        initServer();
        updateUsers();
        createGame();
        while (true) {
                users.add(new ClientHandler(server.accept(), maxUserId()));

        }
    }

    public static int createGame() {
        System.out.println("Created game with id:" + maxGameId());
        Game temp = new Game(maxGameId(), Maxplayers, Maxrounds,passwords);
        //temp.startGame();
        games.add(temp);
        return temp.getGameId();
    }

    public static int joinGame(int id, ClientHandler ch) {
        for (Game game : games) {
            if (game.id == id) {
                if(game.inProgress==false) {
                    if (game.hasFreeSpace()) {
                        game.addUser(ch);
                        return 1;
                    }
                }
            }
        }
        return 0;
    }

    public static void drawPointsToGame(int id, double a, double b, double c, double d) {
        for (Game game : games) {
            if (game.getGameId() == id) {
                double length= Math.sqrt(Math.pow((a-c),2)-Math.pow((b-d),2));
                //System.out.println(game.getId());
                Iterator iterator = game.users.iterator();
                while (iterator.hasNext()) {
                    ClientHandler element = (ClientHandler) iterator.next();
                    /*if(length<50.0) {*/
                    element.sendMessage(new Message(MessageType.POINTS, a + "," + b + "," + c + "," + d));
                    /*}else{
                        System.out.println("length:"+length);
                    };*/
                }

                //System.out.println(game.users.size());


            }
        }
    }
    public static void erasePointsToGame(int id, double a, double b, double c, double d) {
        for (Game game : games) {
            if (game.getGameId() == id) {
                Iterator iterator = game.users.iterator();
                while (iterator.hasNext()) {
                    ClientHandler element = (ClientHandler) iterator.next();
                    element.sendMessage(new Message(MessageType.ERASE, a + "," + b + "," + c + "," + d));
                }
            }
        }
    }
    public static void eraseCanvasToGame(int id) {
        for (Game game : games) {
            if (game.getGameId() == id) {
                Iterator iterator = game.users.iterator();
                while (iterator.hasNext()) {
                    ClientHandler element = (ClientHandler) iterator.next();
                    element.sendMessage(new Message(MessageType.ERASE, "canvas"));
                }
            }
        }
    }

    public static void sendChatToGame(ClientHandler ch, String a) {
        for (Game game : games) {
            if (game.getGameId() == ch.gameid) {
                Iterator iterator = game.users.iterator();
                while (iterator.hasNext()) {
                    ClientHandler element = (ClientHandler) iterator.next();
                    Message msg = new Message(MessageType.CHAT, a);
                    msg.setUserId(ch.id);
                    element.sendMessage(msg);
                }
            }
        }
    }

    public static  Game getGame(ClientHandler ch) {
        for (Game game : games) {
            if (game.id == ch.gameid) {
                return game;
            }
        }
        return null;
    }

    public static void leaveGame(ClientHandler ch, int id) {
        for (Game game : games) {
            if (id == game.id) {
                System.out.println("Leave user id:" + ch.id + " from game id: " + game.id);
                ch.points=0;
                game.removeUser(ch);
                game.gameEnded=true;
                System.out.println("People left:" + game.users.size());
            }
        }

    }

    public static void startGame(int id) {
        for (Game game : games) {
            if (id == game.id) {
                if (game.allReady() && game.users.size() >= 2) {
                    System.out.println("StartGame");
                    game.start();
                }
                ;
                //System.out.println("Game started:"+game.users.size());
            }
        }

    }
    static void readXML(){
        try {
            System.out.println("Wczytuje XML");

            File fXmlFile = new File("config.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);

            doc.getDocumentElement().normalize();

            System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
            NodeList nList = doc.getElementsByTagName("game");
            System.out.println("----------------------------");
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    Maxplayers=Integer.parseInt(eElement.getElementsByTagName("maxplayers").item(0).getTextContent());
                    Maxrounds=Integer.parseInt((eElement.getElementsByTagName("maxrounds").item(0).getTextContent()));
                    System.out.println("MaxPlayers : " + eElement.getElementsByTagName("maxplayers").item(0).getTextContent());
                    System.out.println("MaxRounds : " + eElement.getElementsByTagName("maxrounds").item(0).getTextContent());

                    passwords = new String[eElement.getElementsByTagName("password").getLength()];
                    for(int i=0;i<eElement.getElementsByTagName("password").getLength();i++){
                        passwords[i]=eElement.getElementsByTagName("password").item(i).getTextContent();
                    System.out.println("Password : " + eElement.getElementsByTagName("password").item(i).getTextContent());}
                }
            }
            System.out.println("");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
