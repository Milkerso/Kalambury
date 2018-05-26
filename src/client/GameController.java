package client;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import messages.Message;
import messages.MessageType;

import java.awt.event.MouseListener;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.ResourceBundle;

public class GameController implements Initializable {
    boolean permitDrawing = false;
    boolean permitErasing = false;
    GraphicsContext gc;
    EventHandler handleMP;
    EventHandler handleMD;
    EventHandler handleMR;
    String consoleText;

    private Main main;
    @FXML
    private Button goBack;
    @FXML
    private Button eraser;
    @FXML
    private Button cleaner;
    @FXML
    private Button marker;
    @FXML
    private Label password;
    @FXML
    private Label time;
    @FXML
    private Label idlabel;
    @FXML
    private Label status;
    @FXML
    private Label score;
    @FXML
    private Button ready;
    @FXML
    private Canvas canvas;
    @FXML
    private TextArea console;
    @FXML
    private TextField input;
    @FXML
    private ListView users;
    @FXML
    private AnchorPane anchorPane;


    public void setMainApp(Main mainApp) {
        this.main = mainApp;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        anchorPane.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, null, new BorderWidths(5))));

        gc = canvas.getGraphicsContext2D();
        gc.setStroke(Color.BLACK);
        startDrawing();
        console.setEditable(false);
        console.textProperty().addListener(new ChangeListener<Object>() {
            @Override
            public void changed(ObservableValue<?> observable, Object oldValue,
                                Object newValue) {
                console.setScrollTop(Double.MAX_VALUE);
            }
        });
        input.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ENTER) {
                    final Message msg = new Message(MessageType.CHAT, input.getText());
                    msg.setGameid(main.actualGamedId);
                    main.sendMessage(msg);
                    input.setText("");
                }
            }
        });
        ready.setOnAction(event -> {
            Message msg = new Message();
            msg.setType(MessageType.READY);
            msg.setUserId(main.userId);
            msg.setGameid(main.actualGamedId);
            main.sendMessage(msg);
            ready.setText("You are ready!");
        });
        eraser.setOnAction(event -> {
            permitDrawing=false;
            permitErasing=true;
            initEraser();

        });
        marker.setOnAction(event -> {
            permitDrawing=true;
            permitErasing=false;
            initCanvasDrawing();
        });
        cleaner.setOnAction(event -> {
            Message msg = new Message();
            msg.setType(MessageType.ERASE);
            msg.setUserId(main.userId);
            msg.setGameid(main.actualGamedId);
            msg.setMsg("canvas");
            main.sendMessage(msg);

        });


        goBack.setOnMousePressed(event -> {

            clearCanvas();
            clearChat();
            Message msg = new Message();
            msg.setType(MessageType.LEAVE);
            msg.setGameid(main.actualGamedId);
            msg.setUserId(main.userId);
            main.sendMessage(msg);

            setTime("");
            setPassword("");
            setStatus("");
            setScore(0);
            main.setLobbyScene();

        });
    }

    public void clearCanvas() {
        gc.clearRect(0, 0, 500, 400);
    }
    public void clearRectangular(double x1, double y1, double x2, double y2) {
        gc.clearRect(x1, y1, x2, y2);
    }

    void setStatus(String a) {
        status.setText("Status:" + a);
    }
    void setIdlabel(int a) {
        idlabel.setText("Twoje id to:" + a);
    }
    void setScore(int a) {
        score.setText("Punkty:" + a);
    }

    void setTime(String a) {
        time.setText("Czas:" + a);
    }

    void setPassword(String a) {
        password.setText("Haslo:" + a);
    }

    public void startDrawing() {

        //initCanvasDrawing();
        clearCanvas();
        initCanvasDrawing();
        //echoChat("Twoja kolei");
        permitDrawing=true;

    }

    public void stopDrawing() {

        //closeCanvasDrawing();
        permitDrawing=false;
        clearCanvas();
        closeCanvasDrawing();
        //echoChat("Koniec rysowania");
    }


    void drawPoints(double x1, double y1, double x2, double y2) {
        gc.beginPath();
        gc.moveTo(x1, y1);
        gc.stroke();
        gc.lineTo(x2, y2);
        gc.stroke();
        gc.closePath();
    }

    void echoChat(String echo) {
        console.appendText(echo + "\n");
    }

    void clearChat() {
        console.setText(" ");
    }

    private void initEraser(){
        gc.setLineWidth(4);
        handleMP = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                double x = event.getX();
                double y = event.getY();
                //gc.beginPath();
                //gc.moveTo(event.getX(), event.getY());
                //setStatus(event.getX()+":"+event.getY()+"");
                //queuePoints.add(event.getX()+","+ event.getY());
                if(permitErasing==true){
                    final Message msg = new Message(MessageType.ERASE, (x-15) + "," + (y-15)+","+30+","+30);
                    msg.setGameid(main.actualGamedId);
                    main.sendMessage(msg);}

                // gc.stroke();
            }
        };
        handleMD = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                double x = event.getX();
                double y = event.getY();
                //setPassword(x+":"+y+"");
                //queuePoints.add(x+","+y);

                if(permitErasing==true){
                    final Message msg = new Message(MessageType.ERASE, (x-15) + "," + (y-15)+","+30+","+30);
                    msg.setGameid(main.actualGamedId);
                    main.sendMessage(msg); }
                /*
                gc.lineTo(event.getX(), event.getY());
                gc.stroke();
                gc.closePath();
                gc.beginPath();
                gc.moveTo(event.getX(), event.getY()); */
            }
        };
        handleMR = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                double x = event.getX();
                double y = event.getY();
                //queuePoints.add(x+","+y);

                if(permitErasing==true){
                    final Message msg = new Message(MessageType.ERASE, (x-15) + "," + (y-15)+","+30+","+30);
                    msg.setGameid(main.actualGamedId);
                    main.sendMessage(msg); };
/*
                gc.lineTo(event.getX(), event.getY());
                gc.stroke();
                gc.closePath(); */

            }
        };

        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, handleMP);
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, handleMD);
        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, handleMR);

    }
    private void closeEraser() {

        canvas.removeEventHandler(MouseEvent.MOUSE_PRESSED, handleMP);
        canvas.removeEventHandler(MouseEvent.MOUSE_DRAGGED, handleMD);
        canvas.removeEventHandler(MouseEvent.MOUSE_RELEASED, handleMR);

    }
    private void initCanvasDrawing() {

        gc.setLineWidth(4);
        handleMP = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                double x = event.getX();
                double y = event.getY();
                //gc.beginPath();
                //gc.moveTo(event.getX(), event.getY());
                //setStatus(event.getX()+":"+event.getY()+"");
                //queuePoints.add(event.getX()+","+ event.getY());
                if(permitDrawing==true){
                 Message msg = new Message(MessageType.POINTS, x + "," + y);
                msg.setGameid(main.actualGamedId);
                main.sendMessage(msg);}

                // gc.stroke();
            }
        };
        handleMD = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                double x = event.getX();
                double y = event.getY();
                //setPassword(x+":"+y+"");
                //queuePoints.add(x+","+y);

                if(permitDrawing==true){
                 Message msg = new Message(MessageType.POINTS, x + "," + y);
                msg.setGameid(main.actualGamedId);
                main.sendMessage(msg);}
                /*
                gc.lineTo(event.getX(), event.getY());
                gc.stroke();
                gc.closePath();
                gc.beginPath();
                gc.moveTo(event.getX(), event.getY()); */
            }
        };
        handleMR = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                double x = event.getX();
                double y = event.getY();
                //queuePoints.add(x+","+y);

                if(permitDrawing==true){
                    Message msg = new Message(MessageType.POINTS, x + "," + y);
                msg.setGameid(main.actualGamedId);
                main.sendMessage(msg);}
/*
                gc.lineTo(event.getX(), event.getY());
                gc.stroke();
                gc.closePath(); */

            }
        };

        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, handleMP);
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, handleMD);
        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, handleMR);


    }

    private void closeCanvasDrawing() {

        canvas.removeEventHandler(MouseEvent.MOUSE_PRESSED, handleMP);
        canvas.removeEventHandler(MouseEvent.MOUSE_DRAGGED, handleMD);
        canvas.removeEventHandler(MouseEvent.MOUSE_RELEASED, handleMR);

    }
}
