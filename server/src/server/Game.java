package server;


import messages.Message;
import messages.MessageType;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

public class Game extends Thread {

    int MaxPlayers;
    int MaxRounds;
    HashSet<ClientHandler> users = new HashSet<>();
    int id;
    String actualPassword;
    int actualUserId = 0;
    String[] passwords;
    boolean inProgress = false;
    boolean gameEnded = false;
    int round = 0;

    long tStart;
    long tEnd;
    long tDelta;
    double elapsedSeconds;

    Iterator UserDrawingIterator;

    int secoundsPassed = 0;

    public Game(int id, int maxplayers, int maxround,String[] passwords) {
        this.passwords=passwords;
        this.id = id;
        MaxPlayers = maxplayers;
        MaxRounds = maxround + 1;
        //this.start();
    }

    @Override
    public void run() {
        System.out.println("Startuje Gre id:" + id);

        UserDrawingIterator = users.iterator();
        randomPassword();
        System.out.println("Round :" + round);
        sendChatToUsers("Runda :" + round);
        System.out.println(getPassword());
        tStart = System.currentTimeMillis();
        pickPlayerToDraw();
        sendChatToUsers("Teraz rysuje player z id:" + actualUserId);
        sendStatusToUsers();
        sendPasswordToUsers();
        do {
            inProgress = true;


            tEnd = System.currentTimeMillis();
            tDelta = tEnd - tStart;
            elapsedSeconds = tDelta / 1000.0;
            if (elapsedSeconds % 1 == 0.0) sendTimeToUsers();

            if (elapsedSeconds == 120) {

                randomPassword();
                if (round != MaxRounds) {
                    sendScoresToUsers();
                    sendChatToUsers("Runda :" + round);
                    sendChatToUsers("Teraz rysuje player z id:" + actualUserId);
                    System.out.println("Game id:" + id + "  round:" + round + " haslo: " + getPassword());
                    }
                    pickPlayerToDraw();
                    sendStatusToUsers();
                    sendPasswordToUsers();
                    tStart = System.currentTimeMillis();


            }


        } while (MaxRounds != round);
        inProgress = false;
        sendChatToUsers("Gra została zakonczona.");
        System.out.println("Game id:" + id + " finished");
        gameEnded = true;

        this.stop();
    }

    public int getGameId() {
        return this.id;
    }

    void sendTimeToUsers() {
        for (ClientHandler ch : users) {
            Message msg = new Message();
            msg.setType(MessageType.TIME);
            final StringBuilder a = new StringBuilder();
            a.append(elapsedSeconds);
            msg.setMsg(a.toString());
            ch.sendMessage(msg);
        }
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    void sendChatToUsers(String a) {
        for (ClientHandler ch : users) {
            Message msg = new Message();
            msg.setType(MessageType.CHAT);
            msg.setMsg(a);
            ch.sendMessage(msg);
        }
    }
    void sendScoresToUsers() {
        for (ClientHandler ch : users) {
            Message msg = new Message();
            msg.setType(MessageType.SCORE);
            final StringBuilder a = new StringBuilder();
            a.append(ch.points);
            msg.setMsg(a.toString());
            ch.sendMessage(msg);
        }
    }

    void sendStatusToUsers() {
        for (ClientHandler ch : users) {
            if (ch.id == actualUserId) {
                Message msg = new Message();
                msg.setType(MessageType.STATUS);
                msg.setMsg("rysujesz");
                ch.sendMessage(msg);
            } else {
                Message msg = new Message();
                msg.setType(MessageType.STATUS);
                msg.setMsg("czekasz");
                ch.sendMessage(msg);
            }
        }
    }

    void sendPasswordToUsers() {
        for (ClientHandler ch : users) {
            if (ch.id == actualUserId) {
                Message msg = new Message();
                msg.setType(MessageType.PASSWORD);
                msg.setMsg(actualPassword);
                ch.sendMessage(msg);
            } else {
                Message msg = new Message();
                msg.setType(MessageType.PASSWORD);
                msg.setMsg(" ");
                ch.sendMessage(msg);
            }

        }
    }

    boolean allReady() {
        for (ClientHandler user : users) {
            if (user.ready == false) {
                return false;
            }
        }
        return true;
    }


    String getPassword() {
        return actualPassword;
    }

    void nextRound() {
        round++;
        tStart = System.currentTimeMillis();
    }

    void randomPassword() {
        nextRound();
        Random r = new Random();
        actualPassword = passwords[r.nextInt(passwords.length)];
    }

    boolean checkPassword(String a,ClientHandler ch) {
        //sendChatToUsers("aktualne haslo:"+actualPassword);
        if(actualUserId!=ch.id) {
            if (actualPassword.equals(a)) {
                nextRound();
                Random r = new Random();
                actualPassword = passwords[r.nextInt(passwords.length)];
                ++ch.points;
                sendChatToUsers("Haslo zgadl User["+ch.id+"]"+". Ma teraz "+ch.points+" punkt/ow");
                sendScoresToUsers();
                if (round != MaxRounds) {
                    sendChatToUsers("Runda :" + round);

                    System.out.println("Game id:" + id + "  round:" + round + " haslo: " + getPassword());
                }
                pickPlayerToDraw();
                sendStatusToUsers();
                sendPasswordToUsers();
                //tStart = System.currentTimeMillis();


                return true;
            }
        }
        return false;
    }


    HashSet<ClientHandler> getUsers() {
        return users;
    }

    void addUser(ClientHandler a) {
        //
        if (inProgress == false) {
            if (users.size() < MaxPlayers) {
                users.add(a);
                sendChatToUsers("Gracz User["+a.id+"]"+" dolaczyl do gry.");
                System.out.println("User id:" + a.id + " joined game : " + id);
            }
        } else {

            System.out.println("You cannot join. Game in progress or full");
        }
    }

    void removeUser(ClientHandler a) {

        //
        if (inProgress == true) {
            sendChatToUsers("User :" + a.id + " wyszedl z gry. Wylaczam gre.");
            gameEnded = true;
            this.stop();
        } else {
            //System.out.println("Gra " + id + " usunieto playera UserId" + a.getId());
            users.remove(a);
        }
    }

    boolean hasFreeSpace() {
        if (users.size() == MaxPlayers) {
            return false;
        }
        return true;
    }

    void pickPlayerToDraw() {

        if (actualUserId == 0) {
            ClientHandler temp = (ClientHandler) UserDrawingIterator.next();
            actualUserId = temp.id;
        } else {
            if (UserDrawingIterator.hasNext()) {
                ClientHandler temp = (ClientHandler) UserDrawingIterator.next();
                actualUserId = temp.id;
            } else {
                UserDrawingIterator = users.iterator();
                ClientHandler temp = (ClientHandler) UserDrawingIterator.next();
                actualUserId = temp.id;
            }
        }

        System.out.println("Teraz rysuje player z id:" + actualUserId);
    }

}
