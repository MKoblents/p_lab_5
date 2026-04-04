package shared.dto;

import java.io.Serializable;

public record ForwardCommandObject (String parentId, String childId, String commandKey) implements Serializable {
    private static final long serialVersionUID = 1L;
}
