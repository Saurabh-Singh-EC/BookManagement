package util;

import com.auth0.jwt.exceptions.InvalidClaimException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.codewithsrb.BookManagement.exception.ApiException;
import com.codewithsrb.BookManagement.model.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;

import java.io.OutputStream;

import static java.time.LocalDateTime.now;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
public class ExceptionUtils {

    private static final String CUSTOM_ERROR_MESSAGE = "An error occurred. Please try again.";

    public static void processError(HttpServletResponse response, Exception exception) {
        HttpResponse httpResponse;
        if (exception instanceof ApiException
                || exception instanceof InvalidClaimException
                || exception instanceof TokenExpiredException
                || exception instanceof BadCredentialsException) {
            httpResponse = getHttpResponse(response, exception.getMessage(), BAD_REQUEST);
        } else {
            httpResponse = getHttpResponse(response, CUSTOM_ERROR_MESSAGE, INTERNAL_SERVER_ERROR);
        }
        writeResponse(response, httpResponse);
        log.error(exception.getMessage());
    }

    private static HttpResponse getHttpResponse(HttpServletResponse response, String message, HttpStatus httpStatus) {
        HttpResponse httpResponse = HttpResponse.builder()
                .timeStamp(now().toString())
                .reason(message)
                .statusCode(httpStatus.value())
                .httpStatus(httpStatus)
                .build();

        response.setContentType(APPLICATION_JSON_VALUE);
        response.setStatus(httpStatus.value());

        return httpResponse;
    }

    private static void writeResponse(HttpServletResponse response, HttpResponse httpResponse) {
        try {
            OutputStream outputStream = response.getOutputStream();
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(outputStream, httpResponse);
            outputStream.flush();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}