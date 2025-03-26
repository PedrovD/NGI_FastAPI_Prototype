package com.han.pwac.pinguins.backend.exceptions;

import com.han.pwac.pinguins.backend.exceptions.global.GlobalExceptionHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.method.MethodValidationResult;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import javax.naming.AuthenticationException;

import java.beans.PropertyEditor;
import java.lang.reflect.Method;
import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GlobalExceptionHandlerTest {
    @Test
    public void test_genericErrorResponse_globalException() {
        // Arrange
        ResponseEntity<Map<String, Object>> expectedResponse = ResponseEntity.status(400).body(new HashMap<>());
        
        GlobalException globalException = mock(GlobalException.class);
        when(globalException.createErrorResponse()).thenReturn(expectedResponse);

        // Act
        ResponseEntity<Map<String, Object>> response = new GlobalExceptionHandler().genericErrorResponse(globalException);

        // Assert
        Assertions.assertEquals(expectedResponse, response);
    }

    public void testMethod(String parameter) {

    }

    @Test
    public void test_validationException_errorResponse() {
        // Arrange

        HandlerMethodValidationException exception = new HandlerMethodValidationException(new MethodValidationResult() {
            @Override
            public Object getTarget() {
                return null;
            }

            @Override
            public Method getMethod() {
                return null;
            }

            @Override
            public boolean isForReturnValue() {
                return false;
            }

            @Override
            public List<ParameterValidationResult> getAllValidationResults() {
                Collection<MessageSourceResolvable> resolvables = new ArrayList<>();
                MessageSourceResolvable messageSourceResolvable = mock(MessageSourceResolvable.class);
                when(messageSourceResolvable.getDefaultMessage()).thenReturn("default");

                resolvables.add(messageSourceResolvable);

                List<ParameterValidationResult> list = new ArrayList<>();
                try {
                    list.add(new ParameterValidationResult(new MethodParameter(GlobalExceptionHandlerTest.class.getMethod("testMethod", String.class), 0), null, resolvables));
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }

                return list;
            }
        });

        // Act
        ResponseEntity<Map<String, Object>> response = new GlobalExceptionHandler().validationException(exception);

        // Assert
        Assertions.assertTrue(response.hasBody());
        Assertions.assertTrue(response.getBody().containsKey("message"));

        Assertions.assertTrue(((String)response.getBody().get("message")).contains("default"));

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void test_validationException2_errorResponse() throws NoSuchMethodException {
        // Arrange

        MethodArgumentNotValidException notValidException = new MethodArgumentNotValidException(new MethodParameter(GlobalExceptionHandlerTest.class.getMethod("testMethod", String.class), 0), new BindingResult() {
            @Override
            public Object getTarget() {
                return null;
            }

            @Override
            public Map<String, Object> getModel() {
                return null;
            }

            @Override
            public Object getRawFieldValue(String field) {
                return null;
            }

            @Override
            public PropertyEditor findEditor(String field, Class<?> valueType) {
                return null;
            }

            @Override
            public PropertyEditorRegistry getPropertyEditorRegistry() {
                return null;
            }

            @Override
            public String[] resolveMessageCodes(String errorCode) {
                return new String[0];
            }

            @Override
            public String[] resolveMessageCodes(String errorCode, String field) {
                return new String[0];
            }

            @Override
            public void addError(ObjectError error) {

            }

            @Override
            public String getObjectName() {
                return null;
            }

            @Override
            public void reject(String errorCode, Object[] errorArgs, String defaultMessage) {

            }

            @Override
            public void rejectValue(String field, String errorCode, Object[] errorArgs, String defaultMessage) {

            }

            @Override
            public List<ObjectError> getGlobalErrors() {
                return List.of(new ObjectError("name", "default"));
            }

            @Override
            public List<FieldError> getFieldErrors() {
                return List.of();
            }

            @Override
            public Object getFieldValue(String field) {
                return null;
            }
        });

        // Act
        ResponseEntity<Map<String, Object>> response = new GlobalExceptionHandler().validationException2(notValidException);

        // Assert
        Assertions.assertTrue(response.hasBody());
        Assertions.assertTrue(response.getBody().containsKey("message"));

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void test_validationException3_errorResponse() {
        // Arrange
        MissingServletRequestParameterException missingServletRequestParameterException = new MissingServletRequestParameterException("paramName", "paramType");

        // Act
        ResponseEntity<Map<String, Object>> response = new GlobalExceptionHandler().validationException3(missingServletRequestParameterException);

        // Assert
        Assertions.assertTrue(response.hasBody());
        Assertions.assertTrue(response.getBody().containsKey("message"));

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void test_validationException4_errorResponse() throws NoSuchMethodException {
        // Arrange
        MethodArgumentTypeMismatchException argumentTypeMismatchException = new MethodArgumentTypeMismatchException("value", String.class, "name", new MethodParameter(GlobalExceptionHandlerTest.class.getMethod("testMethod", String.class), 0), null);

        // Act
        ResponseEntity<Map<String, Object>> response = new GlobalExceptionHandler().validationException4(argumentTypeMismatchException);

        // Assert
        Assertions.assertTrue(response.hasBody());
        Assertions.assertTrue(response.getBody().containsKey("message"));

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void test_validationException4_translationNumber() throws NoSuchMethodException {
        // Arrange
        MethodArgumentTypeMismatchException argumentTypeMismatchException = new MethodArgumentTypeMismatchException("value", Integer.class, "name", new MethodParameter(GlobalExceptionHandlerTest.class.getMethod("testMethod", String.class), 0), null);

        // Act
        ResponseEntity<Map<String, Object>> response = new GlobalExceptionHandler().validationException4(argumentTypeMismatchException);

        // Assert
        Assertions.assertTrue(response.hasBody());
        Assertions.assertTrue(response.getBody().containsKey("message"));

        Assertions.assertTrue(((String)response.getBody().get("message")).contains("nummer"));

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void test_validationException4_translationBoolean() throws NoSuchMethodException {
        // Arrange
        MethodArgumentTypeMismatchException argumentTypeMismatchException = new MethodArgumentTypeMismatchException("value", Boolean.class, "name", new MethodParameter(GlobalExceptionHandlerTest.class.getMethod("testMethod", String.class), 0), null);

        // Act
        ResponseEntity<Map<String, Object>> response = new GlobalExceptionHandler().validationException4(argumentTypeMismatchException);

        // Assert
        Assertions.assertTrue(response.hasBody());
        Assertions.assertTrue(response.getBody().containsKey("message"));

        Assertions.assertTrue(((String)response.getBody().get("message")).contains("ja/nee"));

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void test_validationException4_translationCharacter() throws NoSuchMethodException {
        // Arrange
        MethodArgumentTypeMismatchException argumentTypeMismatchException = new MethodArgumentTypeMismatchException("value", Character.class, "name", new MethodParameter(GlobalExceptionHandlerTest.class.getMethod("testMethod", String.class), 0), null);

        // Act
        ResponseEntity<Map<String, Object>> response = new GlobalExceptionHandler().validationException4(argumentTypeMismatchException);

        // Assert
        Assertions.assertTrue(response.hasBody());
        Assertions.assertTrue(response.getBody().containsKey("message"));

        Assertions.assertTrue(((String)response.getBody().get("message")).contains("karakter"));

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void test_validationException4_translationText() throws NoSuchMethodException {
        // Arrange
        MethodArgumentTypeMismatchException argumentTypeMismatchException = new MethodArgumentTypeMismatchException(1, String.class, "name", new MethodParameter(GlobalExceptionHandlerTest.class.getMethod("testMethod", String.class), 0), null);

        // Act
        ResponseEntity<Map<String, Object>> response = new GlobalExceptionHandler().validationException4(argumentTypeMismatchException);

        // Assert
        Assertions.assertTrue(response.hasBody());
        Assertions.assertTrue(response.getBody().containsKey("message"));

        Assertions.assertTrue(((String)response.getBody().get("message")).contains("tekst"));

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void test_validationException4_translationDate() throws NoSuchMethodException {
        // Arrange
        MethodArgumentTypeMismatchException argumentTypeMismatchException = new MethodArgumentTypeMismatchException(1, Date.class, "name", new MethodParameter(GlobalExceptionHandlerTest.class.getMethod("testMethod", String.class), 0), null);

        // Act
        ResponseEntity<Map<String, Object>> response = new GlobalExceptionHandler().validationException4(argumentTypeMismatchException);

        // Assert
        Assertions.assertTrue(response.hasBody());
        Assertions.assertTrue(response.getBody().containsKey("message"));

        Assertions.assertTrue(((String)response.getBody().get("message")).contains("datum"));

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void test_validationException4_noTranslation() throws NoSuchMethodException {
        // Arrange
        MethodArgumentTypeMismatchException argumentTypeMismatchException = new MethodArgumentTypeMismatchException(1, Method.class, "name", new MethodParameter(GlobalExceptionHandlerTest.class.getMethod("testMethod", String.class), 0), null);

        // Act
        ResponseEntity<Map<String, Object>> response = new GlobalExceptionHandler().validationException4(argumentTypeMismatchException);

        // Assert
        Assertions.assertTrue(response.hasBody());
        Assertions.assertTrue(response.getBody().containsKey("message"));

        Assertions.assertTrue(((String)response.getBody().get("message")).contains("onverwacht"));

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void test_handleAuthenticationException_errorResponse() {
        // Arrange
        AuthenticationException authenticationException = new AuthenticationException("error message");

        // Act
        ResponseEntity<Map<String, Object>> response = new GlobalExceptionHandler().handleAuthenticationException(authenticationException);

        // Assert
        Assertions.assertTrue(response.hasBody());
        Assertions.assertTrue(response.getBody().containsKey("message"));

        Assertions.assertTrue(((String)response.getBody().get("message")).contains("error message"));

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    public void test_validationException_mapErrorTest() {
        // Arrange
        HandlerMethodValidationException exception = new HandlerMethodValidationException(new MethodValidationResult() {
            @Override
            public Object getTarget() {
                return null;
            }

            @Override
            public Method getMethod() {
                return null;
            }

            @Override
            public boolean isForReturnValue() {
                return false;
            }

            @Override
            public List<ParameterValidationResult> getAllValidationResults() {
                Collection<MessageSourceResolvable> resolvables = new ArrayList<>();
                MessageSourceResolvable mockMessage = mock(MessageSourceResolvable.class);
                when(mockMessage.getDefaultMessage()).thenReturn("default");
                MessageSourceResolvable mock2Message = mock(MessageSourceResolvable.class);
                when(mock2Message.getDefaultMessage()).thenReturn("default2");

                when(mockMessage.getArguments()).thenReturn(new Object[] { mock2Message });

                resolvables.add(mockMessage);

                List<ParameterValidationResult> list = new ArrayList<>();
                try {
                    list.add(new ParameterValidationResult(new MethodParameter(GlobalExceptionHandlerTest.class.getMethod("testMethod", String.class), 0), null, resolvables));
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }

                return list;
            }
        });

        // Act
        ResponseEntity<Map<String, Object>> response = new GlobalExceptionHandler().validationException(exception);

        // Assert
        Assertions.assertTrue(response.hasBody());
        Assertions.assertTrue(response.getBody().containsKey("message"));

        Assertions.assertTrue(((String)response.getBody().get("message")).contains("default2"));

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
