package client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import messages.Message;
import messages.MessageType;

import java.io.*;
import java.net.Socket;

public class Main extends Application {

    static boolean connected = false;
    public GameController GameController;
    public LobbyController LobbyController;
    Socket s;
    ObjectInputStream r;
    ObjectOutputStream w;
    int userId;
    int actualGamedId;
    private Stage primaryStage;
    private Scene gameScene;
    private Scene lobbyScene;
    private Thread recivingThread;

    public static void main(String[] args) {
        launch(args);

    }

    // 12.02.2018r 12:00 LAB C

    @Override
    public void start(Stage primaryStage) throws Exception {

        this.primaryStage = primaryStage;
        connect();

    }

    public void initGame() {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("client/game.fxml"));
            //loader.setLocation(Main.class.getResource("client/game.fxml"));

            Parent root = loader.load();

            gameScene = new Scene(root, 800, 620);
            gameScene.setRoot(root);

            GameController = loader.getController();
            GameController.setMainApp(this);


            //System.out.println("game layout init");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initLobby() {

        try {

            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("client/lobby.fxml"));
            //loader.setLocation(Main.class.getResource("client/game.fxml"));

            Parent root = loader.load();

            lobbyScene = new Scene(root, 520, 350);
            lobbyScene.setRoot(root);

            LobbyController = loader.getController();
            LobbyController.setMainApp(this);

            //System.out.println("lobby layout init");


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void setLobbyScene() {
        primaryStage.setTitle("Lobby");
        primaryStage.setResizable(true);
        primaryStage.setScene(lobbyScene);
        primaryStage.show();
        primaryStage.setOnCloseRequest(e -> disconnect());
    }

    void setGameScene() {
        primaryStage.setTitle("Gra");
        primaryStage.setResizable(false);
        primaryStage.setScene(gameScene);
        primaryStage.show();
        primaryStage.setOnCloseRequest(e -> disconnect());
    }

    private void connect() {

        while (!connected) {
            try {
                s = new Socket("localhost", 3000);
                r = new ObjectInputStream(s.getInputStream());
                w = new ObjectOutputStream(s.getOutputStream());
                connected = true;
                intRecivingThread();
                initLobby();
                initGame();
                setLobbyScene();
                recivingThread.start();
            } catch (IOException err) {
                System.err.println("You are not connected , retrying....");
                connected = false;

                //disconnect();
            }
        }

    }

    public void disconnect() {
        try {

            Message msg = new Message();
            msg.setType(MessageType.LEAVE);
            msg.setGameid(actualGamedId);
            sendMessage(msg);
            msg = new Message();
            msg.setType(MessageType.DISCONNECTED);
            sendMessage(msg);
            //recivingThread.stop();

            s.close();
            r.close();
            w.close();
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void intRecivingThread() {
        recivingThread = new Thread(() -> {
            System.out.println("Started reciving packets...");
            while (true) {
                if (!s.isClosed()) {
                    Message msg = readMessage();
                    validateMessages(msg);
                } else {
                    recivingThread.stop();
                    break;
                }

                try {
                    recivingThread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        });

    }

    public void validateMessages(Message msg) {

        System.out.println(msg.getType() + " : " + msg.getMsg());
        if(msg.getType()!=null) {
            if (msg.getType().equals(MessageType.CHAT)) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        GameController.echoChat("User[" + msg.getUserId() + "]: " + msg.getMsg());
                    }
                });

            } else if (msg.getType().equals(MessageType.STATUS)) {
                if (msg.getMsg().equals("rysujesz")) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            GameController.setStatus(msg.getMsg());
                            GameController.startDrawing();
                        }
                    });

                } else if (msg.getMsg().equals("czekasz")) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            GameController.setStatus(msg.getMsg());
                            GameController.stopDrawing();
                        }
                    });

                }
            } else if (msg.getType().equals(MessageType.POINTS)) {
                String[] temp = msg.getMsg().split(",");
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {

                        GameController.drawPoints(Double.parseDouble(temp[0]),
                                Double.parseDouble(temp[1]), Double.parseDouble(temp[2]),
                                Double.parseDouble(temp[3]));
                    }
                });

            }else if (msg.getType().equals(MessageType.ERASE)) {

                String[] temp = msg.getMsg().split(",");
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if(msg.getMsg().equals("canvas")){ GameController.clearCanvas();}
                        else {
                            GameController.clearRectangular(Double.parseDouble(temp[0]),
                                    Double.parseDouble(temp[1]), Double.parseDouble(temp[2]),
                                    Double.parseDouble(temp[3]));
                        }
                    }
                });

            }else if (msg.getType() == MessageType.SCORE) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        GameController.setScore(Integer.parseInt(msg.getMsg()));
                    }
                });

            } else if (msg.getType().equals(MessageType.CREATE)) {
                if (msg.getMsg().equals("1")) {
                    //System.out.println("Laduje layout gry");
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            //setGameScene();
                            //actualGamedId = msg.getGameId();
                            System.out.println("Create game with id=" + msg.getGameId());
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Lobby");
                            alert.setHeaderText("Lobby created succefully");
                            alert.setContentText("You can join lobby with id:" + msg.getGameId());
                            alert.showAndWait();
                        }
                    });
                }
            } else if (msg.getType().equals(MessageType.PASSWORD)) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        GameController.setPassword(msg.getMsg());
                    }
                });

            } else if (msg.getType().equals(MessageType.TIME)) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        GameController.setTime(msg.getMsg());
                    }
                });

            } else if (msg.getType().equals(MessageType.JOIN)) {
                if (msg.getMsg().equals("1")) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            actualGamedId = msg.getGameId();
                            setGameScene();
                        }
                    });

                } else if (msg.getMsg().equals("0")) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Alert");
                            alert.setHeaderText("LOBBY ERROR");
                            alert.setContentText("The lobby you want to join is unavaliable or full");
                            alert.showAndWait();
                        }
                    });
                }
            } else if (msg.getType().equals(MessageType.CONNECTED)) {
                System.out.println("Connected + your ID:" + Integer.parseInt(msg.getMsg()));
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        LobbyController.setUserId(Integer.parseInt(msg.getMsg()));
                        GameController.setIdlabel(Integer.parseInt(msg.getMsg()));
                        userId = Integer.parseInt(msg.getMsg());
                    }
                });

            } else if (msg.getType().equals(MessageType.GAMES)) {
                //System.out.println("Downloaded games");
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        LobbyController.updateServers(msg.getMsg());
                    }
                });

            }
        }
    }


    public Stage getPrimaryStage() {
        return this.primaryStage;
    }

    Message readMessage() {
        try {
            Object obj= r.readObject();
            if(obj instanceof Message){ return (Message) obj;};
            return new Message();
        } catch (EOFException a) {
            System.err.println("EOF czytanie");
            System.err.println(a);
            return new Message();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return new Message();
        }catch (ClassCastException e){
            System.err.println(e);
            return new Message();
        }catch (StreamCorruptedException e){
            System.out.println(e);
            //connect();
            return new Message();
        } catch (IOException e) {
            e.printStackTrace();
            return new Message();
        }
    }

    void sendMessage(Message msg) {
        try {
            msg.setUserId(userId);
            w.writeObject(msg);
            w.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
