package shared.dto;
import java.io.Serializable;

public record HandshakeRequest(
        String clientId,
        String parentClientId
) implements Serializable {
    private static final long serialVersionUID = 1L;
}