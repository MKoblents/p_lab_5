package server;

import server.manager.CollectionManager;
import shared.dto.CommandRequest;
import shared.utils.SerializationUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

public class Server {
    private final int port;
    private final CollectionManager collectionManager;
    private final String filePath;

    public Server(int port, CollectionManager collectionManager, String filePath) {
        this.port = port;
        this.collectionManager = collectionManager;
        this.filePath = filePath;
    }

    public void start() throws IOException {
        System.out.println("Server started (stub)");
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(port));
        Selector selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        while (true){
            selector.select();
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            for (SelectionKey selectionKey: selectionKeys){
                if (selectionKey.isAcceptable()){
                    handleAccept(selectionKey);
                } else if (selectionKey.isReadable()) {
                    handleRead(selectionKey);

                }
            }selectionKeys.clear();
        }
    }

    private void handleRead(SelectionKey selectionKey) throws IOException {
        SocketChannel client = (SocketChannel) selectionKey.channel();
        ByteBuffer buffer = ByteBuffer.allocate(4096);
        int bytesRead = client.read(buffer);
        if (bytesRead == -1) {
            client.close();
            selectionKey.cancel();
            System.out.println("✗ Disconnected: " + client.getRemoteAddress());
            return;
        }
        if (bytesRead > 0) {
            buffer.flip();
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);
            try (CommandRequest commandRequest = SerializationUtil.deserialize(data)){
                
            }

            // TODO: Deserialize and process command
        }
    }

    private void handleAccept(SelectionKey selectionKey) throws IOException {
        ServerSocketChannel server = (ServerSocketChannel) selectionKey.channel();
        SocketChannel client = server.accept();
        if (client != null){
            client.configureBlocking( false);
            client.register(selectionKey.selector(), SelectionKey.OP_READ);
        }
    }
}