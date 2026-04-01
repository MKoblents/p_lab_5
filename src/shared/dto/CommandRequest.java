package shared.dto;

import java.io.Serializable;

public record CommandRequest(String commandType, Object args, String requestId, String clientId) implements Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        return "CommandRequest{" +
                "commandType='" + commandType + '\'' +
                ", requestId='" + requestId + '\'' +
                ", args=" + args +
                '}';
    }
}