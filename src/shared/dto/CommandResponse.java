package shared.dto;

import java.io.Serializable;

public record CommandResponse(boolean success, Object result, String message, String requestId) implements Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        return "CommandResponse{" +
                "success=" + success +
                ", requestId='" + requestId + '\'' +
                ", message='" + message + '\'' +
                ", result=" + result +
                '}';
    }
}