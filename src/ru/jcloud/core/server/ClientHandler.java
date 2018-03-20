package ru.jcloud.core.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import static java.lang.Thread.sleep;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nick;

    private boolean active;

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    while (true) {
                        active = false;
                        Thread t = new Thread(() -> {
                            try {
                                sleep(120*1000);
                                if(active==false) {
                                    try {
                                        socket.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        });
                        t.start();

                        String msg = in.readUTF();

                        active = true;
                        t.stop();

                        if (msg.startsWith("/auth ")) {
                            String[] data = msg.split("\\s");
                            if (data.length == 3) {
                                String newNick = server.getAuthService().getNickByLoginAndPass(data[1], data[2]);
                                if (newNick != null) {
                                    if (!server.isNickBusy(newNick)) {
                                        nick = newNick;
                                        sendMsg("/authok " + newNick);
                                        break;
                                    } else {
                                        sendMsg("Учетная запись занята");
                                    }
                                }
                            }
                        }
                    }
                    while (true) {
                        String msg = in.readUTF();
                        System.out.println(nick + ": " + msg);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public String getNick() {
        return nick;
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
