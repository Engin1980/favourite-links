package cz.osu.kip.favouriteLinksBE.exceptions;

import lombok.Getter;

public class ServiceException extends AppException {
    @Getter
    private final Class<?> serviceClass;
    public ServiceException(Object sender, String message) {
        this(sender, message, null);
    }

    public ServiceException(Object sender, String message, Throwable cause) {
        super(message, cause);
        this.serviceClass = sender.getClass();
    }
}
