package cz.osu.kip.favouriteLinksBE.controllers.errorHandling;

import cz.osu.kip.favouriteLinksBE.exceptions.EntityNotFoundException;
import cz.osu.kip.favouriteLinksBE.exceptions.UnauthorizedException;
import cz.osu.kip.favouriteLinksBE.model.ErrorMessageType;
import cz.osu.kip.favouriteLinksBE.model.dto.ErrorDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;

@RestControllerAdvice  // = @ControllerAdvice + @ResponseBody
public class GlobalExceptionHandler {

  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<ErrorDto> handleEntityNotFound(EntityNotFoundException ex) {
    String msg = ex.getClass().getSimpleName();
    if (msg.endsWith("Entity"))
      msg = msg.substring(0, msg.length() - 6);
    if (ex.getIdentifier() != null)
      msg += " (" + ex.getIdentifier() + ")";
    ErrorDto error = new ErrorDto(
        HttpStatus.NOT_FOUND,
        ErrorMessageType.ENTITY_NOT_FOUND,
        msg);
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

  @ExceptionHandler(NoHandlerFoundException.class)
  public ResponseEntity<ErrorDto> handleNotFound(NoHandlerFoundException ex) {
    ErrorDto error = new ErrorDto(
        HttpStatus.NOT_FOUND
    );
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorDto> handleValidationErrors(MethodArgumentNotValidException ex) {
    List<String> errors = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(f -> f.getField() + ": " + f.getDefaultMessage())
        .toList();

    String errorText = String.join(";;; ", errors);

    ErrorDto error = new ErrorDto(
        HttpStatus.INTERNAL_SERVER_ERROR,
        ErrorMessageType.INTERNAL_ERROR,
        "Error validating arguments: " + errorText);
    return ResponseEntity.badRequest().body(error);
  }

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<ErrorDto> handleUnauthorized(UnauthorizedException ex) {
    ErrorDto error = new ErrorDto(HttpStatus.UNAUTHORIZED);
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorDto> handleGeneric(Exception ex) {
    ErrorDto error = new ErrorDto(
        HttpStatus.INTERNAL_SERVER_ERROR);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
  }
}


