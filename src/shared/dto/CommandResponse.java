package shared.dto;

import java.io.Serializable;

public class CommandResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private boolean success;
    private Object result;
    private String message;

    public CommandResponse(boolean success,Object result,String message){
        this.success = success;
        this.result= result;
        this.message = message;
    }

    public Object getResult() {
        return result;
    }

    public String getMessage() {
        return message;
    }

    public boolean isSuccess() {
        return success;
    }
}
