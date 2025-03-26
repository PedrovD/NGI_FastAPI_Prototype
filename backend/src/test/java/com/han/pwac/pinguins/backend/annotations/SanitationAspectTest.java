package com.han.pwac.pinguins.backend.annotations;

import org.apache.commons.text.StringEscapeUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SanitationAspectTest {
    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature signature;

    @Mock
    private Method method;

    @Mock
    private Parameter parameter1;

    @Mock
    private Parameter parameter2;

    @Mock
    private Parameter parameter3;

    @Mock
    private Parameter parameter4;

    @Mock
    private Parameter parameter5;

    @Mock
    private Parameter parameter6;

    public record Record(String value) {}
    public static class Clazz {
        public String value;

        public Clazz(String value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            return obj instanceof Clazz instance
                    && instance.value.equals(this.value);
        }
    }

    private record PrivateRecord(String value) {}

    private static final String VALUE1 = "value1   ";
    private static final String VALUE2 = "<h2>value2</h2>";
    private static final Record VALUE3 = new Record("value3  \n");
    private static final Clazz VALUE4 = new Clazz("value4\t");
    private static final String VALUE5INITIAL = "<h5>value4</h5>";
    private static final Clazz VALUE5 = new Clazz(VALUE5INITIAL);

    @BeforeEach
    public void setup() {
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(method);

        when(parameter1.getAnnotation(RequestBody.class)).thenReturn(new RequestBody() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return null;
            }

            @Override
            public boolean required() {
                return false;
            }
        });

        when(parameter2.getAnnotation(RequestBody.class)).thenReturn(null);
        when(parameter2.getAnnotation(RequestParam.class)).thenReturn(new RequestParam() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return null;
            }

            @Override
            public String value() {
                return null;
            }

            @Override
            public String name() {
                return null;
            }

            @Override
            public boolean required() {
                return false;
            }

            @Override
            public String defaultValue() {
                return null;
            }
        });

        when(parameter3.getAnnotation(RequestBody.class)).thenReturn(null);

        when(parameter3.getAnnotation(RequestParam.class)).thenReturn(null);
        when(parameter3.getAnnotation(PathVariable.class)).thenReturn(new PathVariable() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return null;
            }

            @Override
            public String value() {
                return null;
            }

            @Override
            public String name() {
                return null;
            }

            @Override
            public boolean required() {
                return false;
            }
        });

        when(parameter4.getAnnotation(RequestBody.class)).thenReturn(null);
        when(parameter4.getAnnotation(RequestParam.class)).thenReturn(null);
        when(parameter4.getAnnotation(PathVariable.class)).thenReturn(null);

        when(parameter5.getAnnotation(RequestBody.class)).thenReturn(null);
        when(parameter5.getAnnotation(RequestParam.class)).thenReturn(new RequestParam() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return null;
            }

            @Override
            public String value() {
                return null;
            }

            @Override
            public String name() {
                return null;
            }

            @Override
            public boolean required() {
                return false;
            }

            @Override
            public String defaultValue() {
                return null;
            }
        });

        when(parameter6.getAnnotation(RequestBody.class)).thenReturn(new RequestBody() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return null;
            }

            @Override
            public boolean required() {
                return false;
            }
        });

        when(joinPoint.getArgs()).thenReturn(new Object[] { VALUE1, VALUE2, VALUE3, VALUE4, VALUE5, null });

        when(method.getParameters()).thenReturn(new Parameter[] { parameter1, parameter2, parameter3, parameter4, parameter5, parameter6 });
    }

    @Test
    public void test_sanitize_valid() throws Throwable {
        // Arrange
        ArgumentCaptor<Object[]> objectsCaptor = ArgumentCaptor.forClass(Object[].class);

        String returnValue = "returnValue";
        when(joinPoint.proceed(objectsCaptor.capture())).thenReturn(returnValue);

        // Act
        Object value = new SanitationAspect().sanitize(joinPoint);

        // Assert
        Assertions.assertInstanceOf(String.class, value);
        Assertions.assertEquals(returnValue, value);

        Object[] capturedObjects = objectsCaptor.getValue();
        Assertions.assertEquals(6, capturedObjects.length);

        Assertions.assertEquals(VALUE1.trim(), capturedObjects[0]);
        Assertions.assertEquals(Jsoup.clean(StringEscapeUtils.escapeHtml4(VALUE2), Safelist.basic()), capturedObjects[1]);
        Assertions.assertEquals(new Record(VALUE3.value().trim()), capturedObjects[2]);
        Assertions.assertEquals(VALUE4, capturedObjects[3]);
        Assertions.assertEquals(VALUE5, capturedObjects[4]);
        Assertions.assertEquals(new Clazz(Jsoup.clean(StringEscapeUtils.escapeHtml4(VALUE5INITIAL), Safelist.basic())), capturedObjects[4]);
        Assertions.assertNull(capturedObjects[5]);
    }
}
