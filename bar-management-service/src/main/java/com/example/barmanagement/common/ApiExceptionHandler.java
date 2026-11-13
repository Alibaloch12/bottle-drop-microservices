package com.example.barmanagement.common;

import com.example.barmanagement.domain.exception.ExternalServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail handleNotFound(IllegalArgumentException exception) {
        ProblemDetail problem = ProblemDetail.forStatus(
                HttpStatus.BAD_REQUEST
        );
        problem.setTitle("Invalid request");
        problem.setDetail(exception.getMessage());
        return problem;
    }

    @ExceptionHandler(IllegalStateException.class)
    ProblemDetail handleDomainRule(IllegalStateException exception) {
        ProblemDetail problem = ProblemDetail.forStatus(
                HttpStatus.CONFLICT
        );
        problem.setTitle("Domain rule violated");
        problem.setDetail(exception.getMessage());
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(
            MethodArgumentNotValidException exception
    ) {
        ProblemDetail problem = ProblemDetail.forStatus(
                HttpStatus.BAD_REQUEST
        );
        problem.setTitle("Validation failed");
        problem.setDetail(
                exception.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(error ->
                                error.getField() + ": " + error.getDefaultMessage()
                        )
                        .findFirst()
                        .orElse("Invalid request")
        );
        return problem;
    }
    @ExceptionHandler(ExternalServiceException.class)
    ProblemDetail handleExternalService(
            ExternalServiceException exception
    ) {
        ProblemDetail problem =
                ProblemDetail.forStatus(
                        HttpStatus.SERVICE_UNAVAILABLE
                );

        problem.setTitle(
                "Dependent microservice unavailable"
        );

        problem.setDetail(
                exception.getMessage()
        );

        return problem;
    }
}