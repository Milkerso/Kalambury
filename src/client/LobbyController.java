package client;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import messages.Message;
import messages.MessageType;

import java.net.URL;
import java.util.ResourceBundle;

public class LobbyController implements Initializable {
    public Main main;
    @FXML
    private Button create;
    @FXML
    private Button exit;
    @FXML
    private Button refresh;
    @FXML
    private Label iduser;
    @FXML
    private VBox vbox;

    public void setMainApp(Main mainApp) {
        this.main = mainApp;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        create.setOnMousePressed(event -> {
            Message msg = new Message(MessageType.CREATE, "CREATE");
            main.sendMessage(msg);
        });
        exit.setOnMousePressed(event -> {
            if (!main.s.isClosed()) {
                Message msg = new Message();
                msg.setType(MessageType.DISCONNECTED);
                main.sendMessage(msg);
            }
            main.disconnect();
        });

        refresh.setOnAction(event -> {
            Message msg = new Message();
            msg.setType(MessageType.GAMES);
            main.sendMessage(msg);
            //updateServers();
        });

    }

    void setUserId(int a) {
        iduser.setText(a + "");
    }
    void updateServers(String servers){
        vbox.getChildren().clear();
        if(servers.length()!=0) {
        String[] temp = servers.split(",");
        HBox box= new HBox();
        int row= temp.length/5;
        int index=0;

            for (int x = 0; x < row; x++) {
                box = new HBox();
                for (int i = 0; i < 5; i++) {
                /*if (row / 5 == 1) {
                    row = 0; */
                    //row++;
                    box.setAlignment(Pos.BASELINE_CENTER);
                    box.getChildren().add(createButton(temp[index]));
                    index++;
                }
                ;
                vbox.getChildren().add(box);
            }

            vbox.setAlignment(Pos.BASELINE_CENTER);
            box = new HBox();
            for (int y = 0; y < (temp.length - (row * 5)); y++) {
                if (row == 0) index = y;
                box.setAlignment(Pos.BASELINE_CENTER);
                box.getChildren().add(createButton(temp[index]));
            }
            vbox.getChildren().add(box);
        }

    }
    Button createButton(String name){
        final String[] temp= name.split("\\(");

        Button btn = new Button("Game " + name);
        btn.setOnAction(event -> {
            Message msg = new Message();
            msg.setType(MessageType.JOIN);
            msg.setGameid(Integer.parseInt(temp[0]));
            main.sendMessage(msg);
            main.GameController.clearChat();
        });

        return btn;
    };

}
