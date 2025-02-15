package coLaon.ClaonBack.common;

import coLaon.ClaonBack.common.exception.BadRequestException;
import coLaon.ClaonBack.common.exception.ExceptionDto;
import coLaon.ClaonBack.common.exception.InternalServerErrorException;
import coLaon.ClaonBack.common.exception.MethodArgumentNotValidExceptionDto;
import coLaon.ClaonBack.common.exception.UnauthorizedException;
import coLaon.ClaonBack.common.exception.ConflictExceptionDto;
import coLaon.ClaonBack.common.exception.ErrorCode;
import coLaon.ClaonBack.common.exception.NotFoundException;
import coLaon.ClaonBack.common.exception.ServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@Component
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = {BadRequestException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionDto handleBadRequestException(BadRequestException exception) {
        GlobalExceptionHandler.log.error("error message", exception);
        return new ExceptionDto(exception.getErrorCode(), exception.getMessage());
    }

    @ExceptionHandler(value = {MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionDto handleMethodArgumentTypeMismatchException(BadRequestException exception) {
        GlobalExceptionHandler.log.error("error message", exception);
        return new ExceptionDto(exception.getErrorCode(), exception.getMessage());
    }

    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public MethodArgumentNotValidExceptionDto handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        GlobalExceptionHandler.log.error("error message", exception);
        return new MethodArgumentNotValidExceptionDto(ErrorCode.INVALID_PARAMETER, exception.getMessage(), exception);
    }

    @ExceptionHandler(value = {UnauthorizedException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ExceptionDto handleUnauthorizedException(UnauthorizedException exception) {
        GlobalExceptionHandler.log.error("error message", exception);
        return new ExceptionDto(exception.getErrorCode(), exception.getMessage());
    }

    @ExceptionHandler(value = {ServiceUnavailableException.class})
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ExceptionDto handleServiceUnavailableException(ServiceUnavailableException exception) {
        GlobalExceptionHandler.log.error("error message", exception);
        return new ExceptionDto(exception.getErrorCode(), exception.getMessage());
    }

    @ExceptionHandler(value = {InternalServerErrorException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionDto handlerInternalServerErrorException(InternalServerErrorException exception) {
        GlobalExceptionHandler.log.error("error message", exception);
        return new ExceptionDto(exception.getErrorCode(), exception.getMessage());
    }

    @ExceptionHandler(value = {Exception.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionDto unknownException(Exception exception) {
        GlobalExceptionHandler.log.error("error message", exception);
        return new ExceptionDto(ErrorCode.INTERNAL_SERVER_ERROR, exception.getMessage());
    }

    @ExceptionHandler(value = {DataIntegrityViolationException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ExceptionDto handleUniqueConstraintException(Exception exception) {
        GlobalExceptionHandler.log.error("error message", exception);
        return new ConflictExceptionDto("Violate unique constraint - " + exception.getMessage());
    }

    @ExceptionHandler(value = {NotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ExceptionDto handleNotFoundException(NotFoundException exception) {
        GlobalExceptionHandler.log.error("error message", exception);
        return new ExceptionDto(exception.getErrorCode(), exception.getMessage());
    }
}
