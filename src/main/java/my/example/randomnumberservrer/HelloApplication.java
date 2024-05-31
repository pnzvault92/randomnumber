package my.example.randomnumberservrer;

import org.glassfish.tyrus.server.Server;


public class HelloApplication {
    public static void main(String[] args)  {
        Server server = new Server(WebSocketServer.class);
        try {
            server.start();
            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            server.stop();
        }
    }
}