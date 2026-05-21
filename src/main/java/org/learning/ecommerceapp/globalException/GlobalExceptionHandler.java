package org.learning.ecommerceapp.globalException;

import org.learning.ecommerceapp.cart.exception.CartEmptyException;
import org.learning.ecommerceapp.order.exception.OrderItemsNotFoundException;
import org.learning.ecommerceapp.order.exception.OrderNotFoundException;
import org.learning.ecommerceapp.order.exception.OrderStatusUpdateException;
import org.learning.ecommerceapp.order.exception.ProductOutOfStockException;
import org.learning.ecommerceapp.category.exception.CategoryAlreadyExistsException;
import org.learning.ecommerceapp.category.exception.CategoryNotFoundException;
import org.learning.ecommerceapp.inventory.exception.InvalidInventoryException;
import org.learning.ecommerceapp.products.exception.NoProductFound;
import org.learning.ecommerceapp.user.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<?> disableAccount(DisabledException ex) {
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                .body(ex.getMessage());
    }

    @ExceptionHandler(CartEmptyException.class)
    public ResponseEntity<?> cartEmptyException(CartEmptyException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> illegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<?> handleAlreadyExists(ResourceAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ex.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<?> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ex.getMessage());
    }

    @ExceptionHandler(PasswordMismatchException.class)
    public ResponseEntity<?> handlePasswordMismatch(PasswordMismatchException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<?> handleCategory(CategoryNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }

    @ExceptionHandler(InvalidInventoryException.class)
    public ResponseEntity<?> handleInvalidInventory(InvalidInventoryException ex) {
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                .body(ex.getMessage());
    }

    @ExceptionHandler(NoProductFound.class)
    public ResponseEntity<?> handleProductNotFound(NoProductFound ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<?> handleOrderNotFound(OrderNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }

    @ExceptionHandler(OrderStatusUpdateException.class)
    public ResponseEntity<?> OrderStatusUpdateException(OrderStatusUpdateException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }

    @ExceptionHandler(OrderItemsNotFoundException.class)
    public ResponseEntity<?> OrderItemsNotFound(OrderItemsNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }

    @ExceptionHandler(ProductOutOfStockException.class)
    public ResponseEntity<?> handleProductOutOfStock(ProductOutOfStockException ex) {
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                .body(ex.getMessage());
    }

    @ExceptionHandler(CategoryAlreadyExistsException.class)
    public ResponseEntity<?> handleCategoryAlreadyExists(CategoryAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                .body(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationErrors(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
}