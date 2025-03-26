package com.han.pwac.pinguins.backend.annotations;

import org.antlr.v4.runtime.misc.Pair;
import org.apache.commons.text.StringEscapeUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Aspect
@Component
public class SanitationAspect {

    @Around(value = "@annotation(Sanitation)")
    public Object sanitize(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature)joinPoint.getSignature();
        List<Pair<Integer, Parameter>> insertedParameters = getAllInsertedParameters(methodSignature.getMethod());

        Object[] args = joinPoint.getArgs();
        for (Pair<Integer, Parameter> pair : insertedParameters) {
            int index = pair.a;

            Object value = args[index];
            args[index] = sanitizeObject(value);
        }

        return joinPoint.proceed(args);
    }

    private static List<Pair<Integer, Parameter>> getAllInsertedParameters(Method method) {
        Parameter[] parameters = method.getParameters();
        ArrayList<Pair<Integer, Parameter>> list = new ArrayList<>(parameters.length);
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];

            if (parameter.getAnnotation(RequestBody.class) != null ||
                    parameter.getAnnotation(RequestParam.class) != null ||
                    parameter.getAnnotation(PathVariable.class) != null) {
                list.add(new Pair<>(i, parameter));
            }
        }

        return list;
    }

    // sanitizes all string values within an object
    private static Object sanitizeObject(Object obj) {
        if (obj instanceof String str) {
            String sanitized = Jsoup.clean(
                    StringEscapeUtils.escapeHtml4(str),
                    "",
                    Safelist.basic(),
                    new org.jsoup.nodes.Document.OutputSettings().prettyPrint(false)
            );
            return sanitized.replace("&NewLine;", "\n").trim(); // Convert newlines back to \n
        }
        if (obj == null) {
            return null;
        }

        Class<?> clazz = obj.getClass();
        if (clazz.isRecord()) {
            try {
                Constructor<?> canonicalConstructor = clazz.getDeclaredConstructors()[0];
                canonicalConstructor.setAccessible(true);
                Parameter[] parameters = canonicalConstructor.getParameters();
                Object[] constructorArgs = new Object[parameters.length];

                for (int i = 0; i < parameters.length; i++) {
                    String paramName = parameters[i].getName();
                    Field field = clazz.getDeclaredField(paramName);
                    field.setAccessible(true);
                    constructorArgs[i] = sanitizeObject(field.get(obj));
                }

                return canonicalConstructor.newInstance(constructorArgs);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchFieldException ignored) {
            }
        } else {

            Field[] fieldsArray = clazz.getDeclaredFields();
            Stream<Field> fields = Arrays.stream(fieldsArray).filter(f -> (f.getModifiers() & Modifier.STATIC) == 0);
            Object[] values = new Object[fieldsArray.length];

            AtomicInteger index = new AtomicInteger();
            fields.forEach(field -> {
                try {
                    field.setAccessible(true); // get all fields within an object and sanitize those
                    Object fieldValue = field.get(obj);
                    Object sanitized = sanitizeObject(fieldValue);
                    values[index.get()] = sanitized;
                    field.set(obj, sanitized);
                } catch (IllegalAccessException | IllegalArgumentException | InaccessibleObjectException ignored) {

                }
                index.getAndIncrement();
            });
        }

        return obj;
    }
}
