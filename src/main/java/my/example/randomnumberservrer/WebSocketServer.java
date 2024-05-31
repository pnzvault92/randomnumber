package my.example.randomnumberservrer;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import org.glassfish.tyrus.core.TyrusSession;

@ServerEndpoint("/websocket")
public class WebSocketServer {
    /**
     * активные сессии
     */
    private static final Set<Session> clients = Collections.synchronizedSet(new HashSet<>());
    /**
     * коллекция, куда записываются выданные клиентам числа
     */
    private final Set<BigInteger> used = new HashSet<>();
    /**
     * класс для получения случайного числа
     */
    private final Random random = new Random();


    /**
     * Функция для проверки новой сессии и добавления её в список
     * @param session Сессия
     */
    @OnOpen
    public void onOpen(Session session) throws IOException {
        if (clientAlreadyConnected(session)) {
            session.getBasicRemote().sendText("Only 1 session for IP address is available");
            session.close();
        } else {
            clients.add(session);
        }
    }

    /**
     * Функция для удаления сессии при закрытии клиентом
     * @param session Сессия
     */
    @OnClose
    public void onClose(Session session) {
        clients.remove(session);
    }

    /**
     * Функция вызывается, когда клиент отправляет сообщение. В качестве ответа отправляет случайное число
     * @param message Сообщение клиента (Решено, что ответ не зависит от текста сообщения, поэтому не используется)
     * @param session Сессия клиента
     */
    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        BigInteger uniqueRandomNumber = getUniqueRandomNumber();
        session.getBasicRemote().sendText(String.valueOf(uniqueRandomNumber));
    }

    /**
     * @param newSession Новое подключение
     * @return Признак, есть ли уже активная сессия с такого же IP адреса
     */
    private boolean clientAlreadyConnected(Session newSession) {
        synchronized (clients) {
            for (Session client : clients) {
                if (client instanceof TyrusSession tyrusSession
                        && newSession instanceof TyrusSession newTyrusSession
                        && Objects.equals(tyrusSession.getRequestURI(), newTyrusSession.getRequestURI()))
                {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Функция получает случайное число, проверяет, было ли оно получено ранее,
     * и так до тех пор, пока не найдётся ранее не использованное
     * @return Случайное уникальное число BigInteger
     */
    private BigInteger getUniqueRandomNumber() {
        synchronized (used) {
            BigInteger randomNumber = getRandomNumber();
            while (used.contains(randomNumber)) {
                randomNumber = getRandomNumber();
            }
            used.add(randomNumber);
            return randomNumber;
        }
    }

    /**
     * @return Случайное число BigInteger
     */
    private BigInteger getRandomNumber() {
        short numBits = 128;  // Битовая длина. Подобрана таким образом, чтобы выходить за пределы значений long
        return new BigInteger(numBits, random);
    }

}
