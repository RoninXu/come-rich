package com.finance.planner.ai.agent.tool;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.*;

@Component
public class ToolSchemaGenerator {

    public Map<String, Object> buildToolSchema(Tool tool) {
        Map<String, Object> function = new LinkedHashMap<>();
        function.put("name", tool.getName());
        function.put("description", tool.getDescription());
        function.put("parameters", buildParametersSchema(tool.getParameterClass()));

        Map<String, Object> toolSchema = new LinkedHashMap<>();
        toolSchema.put("type", "function");
        toolSchema.put("function", function);
        return toolSchema;
    }

    private Map<String, Object> buildParametersSchema(Class<?> paramClass) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new LinkedHashMap<>();
        List<String> required = new ArrayList<>();

        for (Field field : paramClass.getDeclaredFields()) {
            String name = field.getName();
            Map<String, Object> prop = new LinkedHashMap<>();
            String type = mapJsonType(field.getType());
            prop.put("type", type);

            if (field.getType().equals(LocalDate.class)) {
                prop.put("format", "date");
            }

            Size size = field.getAnnotation(Size.class);
            if (size != null && "string".equals(type)) {
                if (size.min() > 0) {
                    prop.put("minLength", size.min());
                }
                if (size.max() > 0) {
                    prop.put("maxLength", size.max());
                }
            }

            Min min = field.getAnnotation(Min.class);
            if (min != null) {
                prop.put("minimum", min.value());
            }
            Max max = field.getAnnotation(Max.class);
            if (max != null) {
                prop.put("maximum", max.value());
            }

            if (field.isAnnotationPresent(NotNull.class) || field.isAnnotationPresent(NotBlank.class)) {
                required.add(name);
            }

            properties.put(name, prop);
        }

        schema.put("properties", properties);
        if (!required.isEmpty()) {
            schema.put("required", required);
        }
        return schema;
    }

    private String mapJsonType(Class<?> clazz) {
        if (clazz.equals(String.class) || clazz.equals(LocalDate.class)) {
            return "string";
        }
        if (Number.class.isAssignableFrom(clazz) || clazz.isPrimitive()) {
            if (clazz.equals(Integer.class) || clazz.equals(Long.class) || clazz.equals(Short.class) || clazz.equals(int.class) || clazz.equals(long.class) || clazz.equals(short.class)) {
                return "integer";
            }
            return "number";
        }
        if (clazz.equals(Boolean.class) || clazz.equals(boolean.class)) {
            return "boolean";
        }
        if (Collection.class.isAssignableFrom(clazz)) {
            return "array";
        }
        return "object";
    }
}
