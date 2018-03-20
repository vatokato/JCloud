package ru.jcloud.core.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Vector;

public class Server {
    private Vector<ClientHandler> clients;
    AuthService authService;

    public AuthService getAuthService() {
        return authService;
    }

    public Server() {
        try (ServerSocket serverSocket = new ServerSocket(8189)){
            clients = new Vector<>();
            authService = new AuthService();
            authService.connect();
            System.out.println("Server started... Waiting clients...");
            while(true){
                //ожидаем подключения
                Socket socket = serverSocket.accept();

                System.out.println("Client connected" + socket.getInetAddress() + " " + socket.getPort() + " " + socket.getLocalPort());

                new ClientHandler(this, socket);
                System.out.println(new ClientHandler(this, socket) );
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException | ClassNotFoundException e){
            System.out.println("Не удалось запустить сервис авторизации");
        }finally {
            authService.disconnect();
        }
    }

    public boolean isNickBusy(String nick){
        for (ClientHandler o: clients) {
            if (o.getNick().equals(nick))
                return true;
        }
        return false;
    }
}
