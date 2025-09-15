package cz.osu.kip.favouriteLinksBE.model.dto;

import cz.osu.kip.favouriteLinksBE.model.ErrorMessageType;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public record ErrorDto(int status,
                       String messageType,
                       String messageParameter,
                       LocalDateTime timestamp) {

    public ErrorDto(HttpStatus status, ErrorMessageType messageType, String messageParameter) {
        this(status.value(), messageType == null ? null : messageType.toString(), messageParameter, LocalDateTime.now());
    }

    public ErrorDto(HttpStatus status, ErrorMessageType messageType) {
        this(status, messageType, null);
    }

    public ErrorDto(HttpStatus status) {
        this(status, null, null);
    }
}