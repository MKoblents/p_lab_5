package shared.dto;

import java.io.Serializable;

public class CommandRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private String commandKey;
    private Object data;
    private String stringArg;
    public CommandRequest(String commandKey,Object data, String stringArg){
        this.commandKey = commandKey;
        this.data = data;
        this.stringArg = stringArg;
    }

    public String getCommandKey() {
        return commandKey;
    }

    public Object getData() {
        return data;
    }

    public String getStringArg() {
        return stringArg;
    }
}
