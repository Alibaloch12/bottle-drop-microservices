package com.example.customer_service.common;


import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CustomerApiExceptionHandler {

    @ExceptionHandler(
            CustomerDependencyException.class
    )
    ProblemDetail handleDependencyFailure(
            CustomerDependencyException exception
    ) {
        ProblemDetail problem =
                ProblemDetail.forStatus(
                        HttpStatus.SERVICE_UNAVAILABLE
                );

        problem.setTitle(
                "Dependent service unavailable"
        );

        problem.setDetail(
                exception.getMessage()
        );

        return problem;
    }

    @ExceptionHandler(
            CustomerUpstreamException.class
    )
    ProblemDetail handleUpstreamResponse(
            CustomerUpstreamException exception
    ) {
        ProblemDetail problem =
                ProblemDetail.forStatus(
                        HttpStatusCode.valueOf(
                                exception.getStatusCode()
                        )
                );

        problem.setTitle(
                "Order request rejected"
        );

        problem.setDetail(
                exception.getMessage()
        );

        return problem;
    }

    @ExceptionHandler(
            IllegalArgumentException.class
    )
    ProblemDetail handleInvalidRequest(
            IllegalArgumentException exception
    ) {
        ProblemDetail problem =
                ProblemDetail.forStatus(
                        HttpStatus.BAD_REQUEST
                );

        problem.setTitle("Invalid request");
        problem.setDetail(
                exception.getMessage()
        );

        return problem;
    }
}