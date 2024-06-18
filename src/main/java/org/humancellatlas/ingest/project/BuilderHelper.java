package org.humancellatlas.ingest.project;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BuilderHelper<T, B> {
  private final B builderInstance;
  private final List<String> builderOperationalFields = List.of("builderHelper");

  public BuilderHelper(B builderInstance) {
    this.builderInstance = builderInstance;
  }

  private static String toSetterName(String fieldName) {
    String capitalisedFieldName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    return "set" + capitalisedFieldName;
  }

  public void copyFieldsFromBuilder(T target, List<String> ignoreFieldsList) {
    Class targetClass = target.getClass();
    Map<String, Field> targetFieldsMap =
        Stream.iterate(targetClass, c -> c.getSuperclass() != null, Class::getSuperclass)
            .map(Class::getDeclaredFields)
            .flatMap(Arrays::stream)
            .filter(f -> !ignoreFieldsList.contains(f.getName()))
            .collect(Collectors.toMap(Field::getName, Function.identity()));
    Arrays.stream(builderInstance.getClass().getDeclaredFields())
        .filter(f -> !ignoreFieldsList.contains(f.getName()))
        .filter(f -> !builderOperationalFields.contains(f.getName()))
        .forEach(
            builderField -> {
              try {
                Field targetField = targetFieldsMap.get(builderField.getName());
                String fieldName = targetField.getName();
                Method setter = getMethod(target, fieldName, targetField);
                setter.invoke(target, builderField.get(this.builderInstance));
              } catch (IllegalAccessException
                  | NoSuchMethodException
                  | InvocationTargetException e) {
                throw new RuntimeException(e);
              }
            });
  }

  private static <T> Method getMethod(T target, String fieldName, Field projectField)
      throws NoSuchMethodException {
    return target.getClass().getMethod(toSetterName(fieldName), projectField.getType());
  }

  public Map<String, Object> asMap(List<String> excludeList) {
    T target = null;
    try {
      Method buildMethod = this.builderInstance.getClass().getMethod("build");
      target = (T) buildMethod.invoke(this.builderInstance);
    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
    return new ObjectToMapConverter<T>().asMap(target, excludeList);
  }

  public static <T> Map<String, Object> asMap(T target) {
    return new ObjectToMapConverter<T>().asMap(target);
  }
}
