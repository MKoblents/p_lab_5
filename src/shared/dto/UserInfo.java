package shared.dto;

import java.io.Serializable;

public record UserInfo(String name, String password) implements Serializable {
    private static final long serialVersionUID = 1L;
}
