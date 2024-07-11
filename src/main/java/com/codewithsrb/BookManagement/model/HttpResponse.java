package com.codewithsrb.BookManagement.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Data
@Builder
public class HttpResponse {

    protected String timeStamp;
    protected int statusCode;
    protected HttpStatus httpStatus;
    protected String message;
    protected String reason;
    protected List<?> data;
}
