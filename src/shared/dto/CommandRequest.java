package shared.dto;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

public record CommandRequest(String commandType, @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class") Object args, String requestId, String clientId, UserInfo userInfo) implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final String CMD_HEARTBEAT = "heartbeat";

    @Override
    public String toString() {
        return "CommandRequest{" +
                "commandType='" + commandType + '\'' +
                ", requestId='" + requestId + '\'' +
                ", args=" + args +'\''+
                ", User=" + userInfo.name()+
                '}';
    }
}