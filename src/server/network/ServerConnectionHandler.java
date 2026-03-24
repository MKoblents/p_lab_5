package server.network;

import server.manager.Invoker;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import shared.utils.SerializationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class ServerConnectionHandler {
    private static final Logger logger = LoggerFactory.getLogger(ServerConnectionHandler.class);
    private static final int BUFFER_SIZE = 16384;  // 16KB

    private final Selector selector;
    private final Invoker invoker;

    public ServerConnectionHandler(Selector selector, Invoker invoker) {
        this.selector = selector;
        this.invoker = invoker;
    }

    /**
     * Принимает новое подключение клиента.
     */
    public void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();

        if (clientChannel != null) {
            clientChannel.configureBlocking(false);
            clientChannel.register(selector, SelectionKey.OP_READ);

            String clientAddress = ((InetSocketAddress) clientChannel.getRemoteAddress()).toString();
            logger.info("Client connected: {}", clientAddress);
        }
    }

    /**
     * Читает запрос от клиента, выполняет команду, отправляет ответ.
     * ВАЖНО: НЕ закрывает соединение после ответа!
     */
    public void handleRead(SelectionKey key) {
        SocketChannel clientChannel = (SocketChannel) key.channel();

        try {
            // 1. Читаем длину сообщения (первые 4 байта)
            ByteBuffer lengthBuffer = ByteBuffer.allocate(4);
            int bytesRead = clientChannel.read(lengthBuffer);

            if (bytesRead == -1) {
                // Клиент закрыл соединение — это нормально
                logger.info("Client disconnected: {}", clientChannel.getRemoteAddress());
                key.cancel();
                clientChannel.close();
                return;
            }

            if (bytesRead < 4) {
                return;  // Ещё не все байты пришли
            }

            lengthBuffer.flip();
            int messageLength = lengthBuffer.getInt();

            // 2. Читаем само сообщение
            ByteBuffer dataBuffer = ByteBuffer.allocate(messageLength);
            while (dataBuffer.hasRemaining()) {
                int read = clientChannel.read(dataBuffer);
                if (read == -1) {
                    logger.warn("Unexpected disconnect while reading message");
                    key.cancel();
                    clientChannel.close();
                    return;
                }
            }

            dataBuffer.flip();
            byte[] data = new byte[messageLength];
            dataBuffer.get(data);

            // 3. Десериализуем запрос
            CommandRequest request = (CommandRequest) SerializationUtil.deserialize(data);
            logger.debug("Received command: {}", request.getCommandKey());

            // 4. Выполняем команду
            CommandResponse response = invoker.runCommand(request);
            logger.debug("Command executed: success={}", response.isSuccess());

            // 5. Сериализуем и отправляем ответ
            byte[] responseData = SerializationUtil.serialize(response);
            ByteBuffer responseBuffer = ByteBuffer.allocate(4 + responseData.length);
            responseBuffer.putInt(responseData.length);
            responseBuffer.put(responseData);
            responseBuffer.flip();

            while (responseBuffer.hasRemaining()) {
                clientChannel.write(responseBuffer);
            }

            logger.trace("Response sent - connection KEPT OPEN for next request");

            // ✅ ВАЖНО: НЕ закрываем соединение! Клиент может отправить ещё запросы.

        } catch (IOException e) {
            // Ошибка сети — закрываем соединение
            logger.error("IO error with client, closing connection", e);
            key.cancel();
            try { clientChannel.close(); } catch (IOException ex) { /* ignore */ }

        } catch (ClassNotFoundException e) {
            // Ошибка десериализации — НЕ закрываем соединение
            logger.error("Deserialization error", e);

        } catch (Exception e) {
            // Любая другая ошибка — НЕ закрываем соединение
            logger.error("Unexpected error", e);
        }
    }
}