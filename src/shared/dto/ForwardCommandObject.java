package shared.dto;

public record ForwardCommandObject (String parentId, String childId, String commandKey) {
    private static final long serialVersionUID = 1L;
}
