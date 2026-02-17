package Exceptions;

public class UnavailableHealthException extends RuntimeException {
    public UnavailableHealthException(String message) {
        super(message);
    }
}
