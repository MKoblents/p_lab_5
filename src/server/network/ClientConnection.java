package server.network;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.time.Instant;

public class ClientConnection {
    public enum State { READING_LENGTH, READING_PAYLOAD, WRITING }

    private final SocketChannel channel;
    private State state = State.READING_LENGTH;
    private final ByteBuffer lengthBuf = ByteBuffer.allocate(4);
    private ByteBuffer payloadBuf;
    private int expectedLength = -1;
    private ByteBuffer writeBuf;
    private Instant lastHeartbeat = Instant.now();
    private String clientId;

    public ClientConnection(SocketChannel channel) { this.channel = channel; }

    public SocketChannel getChannel() {
        return channel;
    }

    public ByteBuffer getLengthBuf() {
        return lengthBuf;
    }

    public ByteBuffer getPayloadBuf() {
        return payloadBuf;
    }

    public int getExpectedLength() {
        return expectedLength;
    }

    public ByteBuffer getWriteBuf() {
        return writeBuf;
    }

    public State getState() {
        return state;
    }

    public String getClientId() {
        return clientId;
    }

    public Instant getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setExpectedLength(int expectedLength) {
        this.expectedLength = expectedLength;
    }

    public void setLastHeartbeat(Instant lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    public void setPayloadBuf(ByteBuffer payloadBuf) {
        this.payloadBuf = payloadBuf;
    }

    public void setState(State state) {
        this.state = state;
    }

    public void setWriteBuf(ByteBuffer writeBuf) {
        this.writeBuf = writeBuf;
    }

}