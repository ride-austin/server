package com.rideaustin.report.adapter;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ReflectionUtils;

import com.rideaustin.report.FormatAs;
import com.rideaustin.report.ReportField;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultReportAdapter<T> implements ReportAdapter<T> {

  private final Class<T> entryClass;
  private final Map<String, Object> context;

  @Override
  public Map<String, Object> getReportContext() {
    return Collections.unmodifiableMap(context);
  }

  @Override
  public Function<T, String[]> getRowMapper() {
    return this::createRowMapper;
  }

  @Override
  public String[] getHeaders() {
    Map<Integer, String> result = new TreeMap<>();
    processMethods(entryClass, m -> extractHeaderOnMethod(m, result));
    processFields(entryClass, f -> extractHeaderOnField(f, result));
    return toArray(result);
  }

  private String[] createRowMapper(T entry) {
    Map<Integer, String> result = new TreeMap<>();
    processMethods(entryClass, m -> mapMethodValue(entry, m, result));
    processFields(entryClass, f -> mapFieldValue(entry, f, result));
    return toArray(result);
  }

  private void processFields(Class entryClass, Consumer<Field> fieldConsumer) {
    ReflectionUtils.doWithFields(
      entryClass,
      fieldConsumer::accept,
      field -> field.isAnnotationPresent(ReportField.class)
    );
  }

  private void processMethods(Class entryClass, Consumer<Method> methodConsumer) {
    ReflectionUtils.doWithMethods(
      entryClass,
      methodConsumer::accept,
      method -> method.isAnnotationPresent(ReportField.class) && method.getDeclaringClass().equals(entryClass)
    );
  }

  private void mapFieldValue(T object, Field field, Map<Integer, String> map) {
    ReportField annotation = field.getAnnotation(ReportField.class);
    try {
      mapValue(PropertyUtils.getProperty(object, field.getName()), annotation.format(), annotation.order(), map);
    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
      throw new FieldMappingException(ex);
    }
  }

  private void mapMethodValue(T object, Method method, Map<Integer, String> map) {
    ReportField annotation = method.getAnnotation(ReportField.class);
    try {
      mapValue(method.invoke(object), annotation.format(), annotation.order(), map);
    } catch (IllegalAccessException | InvocationTargetException ex) {
      throw new MethodMappingException(ex);
    }
  }

  private void mapValue(Object value, FormatAs format, int order, Map<Integer, String> map) {
    FormatAs useFormat = format;
    Optional<Object> optional = Optional.ofNullable(value);
    Object safeValue = optional.orElse("");
    if (!optional.isPresent()) {
      useFormat = FormatAs.NONE;
    }
    if (FormatAs.NONE.equals(format)) {
      useFormat = Optional.ofNullable(FormatAs.formatFor(safeValue.getClass())).orElse(FormatAs.NONE);
    }
    map.put(order, useFormat.toString(safeValue));
  }

  private void extractHeaderOnMethod(Method method, Map<Integer, String> map) {
    ReportField annotation = method.getAnnotation(ReportField.class);
    map.put(annotation.order(), extractHeader(annotation.name(), method.getName()));
  }

  private void extractHeaderOnField(Field field, Map<Integer, String> map) {
    ReportField annotation = field.getAnnotation(ReportField.class);
    map.put(annotation.order(), extractHeader(annotation.name(), field.getName()));
  }

  private String extractHeader(String value, String name) {
    if (StringUtils.isNotEmpty(value)) {
      return value;
    }
    String[] words = StringUtils.splitByCharacterTypeCamelCase(name.replaceAll("^get", ""));
    for (int i = 0; i < words.length; i++) {
      if (i == 0) {
        words[i] = StringUtils.capitalize(words[i]);
      } else {
        words[i] = StringUtils.uncapitalize(words[i]);
      }
    }
    return StringUtils.join(words, " ");
  }

  private String[] toArray(Map<Integer, String> result) {
    return result.values().toArray(new String[0]);
  }

  private static class MethodMappingException extends RuntimeException {
    MethodMappingException(Exception source) {
      super(source);
    }
  }

  private static class FieldMappingException extends RuntimeException {
    FieldMappingException(Exception source) {
      super(source);
    }
  }
}
