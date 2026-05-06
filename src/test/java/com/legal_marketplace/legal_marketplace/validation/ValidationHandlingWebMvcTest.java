package com.legal_marketplace.legal_marketplace.validation;

import com.legal_marketplace.legal_marketplace.dto.response.ValidationErrorResponse;
import com.legal_marketplace.legal_marketplace.exception.GlobalExceptionHandler;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.Min;
import org.junit.jupiter.api.Test;
import org.jspecify.annotations.NonNull;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidationHandlingWebMvcTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleMethodArgumentNotValidException_returnsFieldErrors() throws Exception {
        MethodParameter parameter = methodParameter();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new SampleRequest(), "sampleRequest");
        bindingResult.addError(new FieldError("sampleRequest", "title", "must not be blank"));

        ValidationErrorResponse response = handler.handleMethodArgumentNotValidException(
                new MethodArgumentNotValidException(parameter, bindingResult)
        ).getBody();

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.status());
        assertTrue(response.fieldErrors().containsKey("title"));
        assertEquals("must not be blank", response.fieldErrors().get("title").getFirst());
    }

    @Test
    void handleConstraintViolationException_returnsFieldErrors() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            Set<jakarta.validation.ConstraintViolation<SampleConstraintBean>> violations = validator.validate(new SampleConstraintBean());
            ValidationErrorResponse response = handler.handleConstraintViolationException(new ConstraintViolationException(violations)).getBody();

            assertNotNull(response);
            assertEquals(HttpStatus.BAD_REQUEST.value(), response.status());
            assertTrue(response.fieldErrors().containsKey("serialNo"));
            assertEquals("must be greater than or equal to 1", response.fieldErrors().get("serialNo").getFirst());
        }
    }

    @Test
    void notEmptyFileValidatorRejectsEmptyFiles() {
        NotEmptyFile.Validator validator = new NotEmptyFile.Validator();

        assertFalse(validator.isValid(emptyFile(), null));
        assertTrue(validator.isValid(nonEmptyFile(), null));
    }

    private MethodParameter methodParameter() throws NoSuchMethodException {
        Method method = ValidationHandlingWebMvcTest.class.getDeclaredMethod("sampleEndpoint", SampleRequest.class);
        return new MethodParameter(method, 0);
    }

    @SuppressWarnings("unused")
    private void sampleEndpoint(SampleRequest request) {
        // test helper only
    }

    private static MultipartFile emptyFile() {
        return new TestMultipartFile("file", new byte[0]);
    }

    private static MultipartFile nonEmptyFile() {
        return new TestMultipartFile("file", new byte[] {1, 2, 3});
    }

    private static class SampleRequest {
    }

    private static class SampleConstraintBean {
        @Min(1)
        @SuppressWarnings("unused")
        private final Integer serialNo = 0;
    }

    private static class TestMultipartFile implements MultipartFile {
        private final String name;
        private final byte[] content;

        private TestMultipartFile(String name, byte[] content) {
            this.name = name;
            this.content = content;
        }

        @Override
        public @NonNull String getName() {
            return name;
        }

        @Override
        public String getOriginalFilename() {
            return name;
        }

        @Override
        public String getContentType() {
            return "application/octet-stream";
        }

        @Override
        public boolean isEmpty() {
            return content.length == 0;
        }

        @Override
        public long getSize() {
            return content.length;
        }

        @Override
        public byte @NonNull [] getBytes() {
            return content;
        }

        @Override
        public @NonNull InputStream getInputStream() {
            return new ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(@NonNull File dest) {
            throw new UnsupportedOperationException("Not used in tests");
        }

        @Override
        public void transferTo(@NonNull Path dest) {
            throw new UnsupportedOperationException("Not used in tests");
        }
    }
}
