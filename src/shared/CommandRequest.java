package shared;

import java.io.Serializable;

public class CommandRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private String commandKey;
    private Object data;
    private String xmlString;
    public CommandRequest(String commandKey,Object data, String xmlString){
        this.commandKey = commandKey;
        this.data = data;
        this.xmlString = xmlString;
    }

    public String getCommandKey() {
        return commandKey;
    }

    public Object getData() {
        return data;
    }

    public String getXmlString() {
        return xmlString;
    }
}
