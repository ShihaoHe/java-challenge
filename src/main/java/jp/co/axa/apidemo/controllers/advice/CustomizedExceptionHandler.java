package jp.co.axa.apidemo.controllers.advice;

import jp.co.axa.apidemo.controllers.ControllerScanBase;
import jp.co.axa.apidemo.exception.ResourceNotFoundException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletRequest;

/**
 * global exception handler
 *
 * handle exceptions and log them
 *
 * unify the format of error response as
 * {
 *     msg: exception information
 * }
 */
@Slf4j
@RestControllerAdvice(basePackageClasses = {ControllerScanBase.class})
public class CustomizedExceptionHandler {

    private static final HttpHeaders httpHeaders = new HttpHeaders();

    static {
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
    }

    /**
     * handler for ResourceNotFoundException
     * response status would be 404
     *
     * @param ex {@link ResourceNotFoundException}
     * @param webRequest {@link WebRequest}
     * @return {@link ResponseEntity<Object>}
     */
    @ExceptionHandler({ ResourceNotFoundException.class })
    public ResponseEntity<Object> resourceNotFound(ResourceNotFoundException ex, WebRequest webRequest) {
        log.warn(formLog(HttpStatus.NOT_FOUND, webRequest), ex);
        return responseEntity(HttpStatus.NOT_FOUND, ex);
    }

    /**
     * handler for other exceptions
     * response status would be 500
     *
     * @param ex {@link Exception}
     * @param webRequest {@link WebRequest}
     * @return {@link ResponseEntity<Object>}
     */
    @ExceptionHandler({ Exception.class })
    public ResponseEntity<Object> internalError(Exception ex, WebRequest webRequest) {
        log.error(formLog(HttpStatus.INTERNAL_SERVER_ERROR, webRequest), ex);
        return responseEntity(HttpStatus.INTERNAL_SERVER_ERROR, ex);
    }


    private ResponseEntity<Object> responseEntity(HttpStatus status, Exception ex) {
        return ResponseEntity.status(status)
                .headers(httpHeaders)
                .body(new Msg(ex.getMessage()));
    }

    private String formLog(HttpStatus status, WebRequest webRequest) {
        final HttpServletRequest request = ((ServletWebRequest) webRequest).getRequest();
        return status.toString() + " : [" + request.getMethod() + "] " + request.getRequestURI();
    }

    @AllArgsConstructor
    @Getter
    public static class Msg {
        private String msg;
    }
}
