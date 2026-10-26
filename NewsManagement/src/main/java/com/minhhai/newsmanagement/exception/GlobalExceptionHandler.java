package com.minhhai.newsmanagement.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError e : ex.getBindingResult().getFieldErrors()) {
            errors.put(e.getField(), e.getDefaultMessage());
        }
        String detail = errors.isEmpty() ? "Dữ liệu gửi lên không hợp lệ" : String.join("; ", errors.values());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        pd.setTitle("Validation Failed");
        pd.setProperty("timestamp", Instant.now());
        pd.setProperty("errors", errors);
        return ResponseEntity.badRequest().body(pd);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleDenied(AccessDeniedException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "Bạn không có quyền thực hiện thao tác này");
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ProblemDetail handleLock(ObjectOptimisticLockingFailureException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "Bài viết vừa được người khác thay đổi, vui lòng tải lại bài rồi thử lại");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleIntegrity(DataIntegrityViolationException ex) {
        log.warn("Vi phạm ràng buộc dữ liệu", ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Dữ liệu bị trùng hoặc vi phạm ràng buộc");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleOther(Exception ex) {
        log.error("Lỗi nội bộ", ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi máy chủ");
    }
}
