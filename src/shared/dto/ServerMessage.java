package shared.dto;

import java.io.Serializable;
public record ServerMessage(
        MessageType type,
        CommandResponse response,
        CommandRequest forwardedRequest ) implements Serializable {

    public enum MessageType {
        RESPONSE,
        FORWARD}

    public static ServerMessage forResponse(CommandResponse r) {
        return new ServerMessage(MessageType.RESPONSE, r, null);
    }

    public static ServerMessage forForward(CommandRequest r) {
        return new ServerMessage(MessageType.FORWARD, null, r);
    }

    public boolean isResponse() { return type == MessageType.RESPONSE; }
    public boolean isForward() { return type == MessageType.FORWARD; }
}