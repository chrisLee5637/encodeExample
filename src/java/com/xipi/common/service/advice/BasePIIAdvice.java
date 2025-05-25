package java.com.xipi.common.service.advice;

import com.google.common.collect.ImmutableList;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;

import java.com.xipi.common.model.pii.provider.PIIAdditionalConditionProvider;
import java.com.xipi.common.model.pii.enumeration.PIIDatalevel;
import java.com.xipi.common.service.annotation.PIIDataAPI;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class BasePIIAdvice {
    @Autowired
    private ApplicationContext applicationContext;
    private static  final ImmutableList<String> excludePackages = ImmutableList.of(
            "java.",
            "javax.",
            "org.slf4j.",
            "ch.qos."
    );

    public boolean isEncodingApplicable(MethodParameter methodParameter, PIIDatalevel excludeLevel) {
        PIIDataAPI piiDataAPI = methodParameter.getMethodAnnotation(PIIDataAPI.class);
        if (piiDataAPI == null || piiDataAPI.level() == excludeLevel) {
            return false;
        }
        Class<? extends PIIAdditionalConditionProvider> conditionProviderClazz = piiDataAPI.conditionProvider();
        if (conditionProviderClazz != null) {
            try {
                PIIAdditionalConditionProvider conditionProvider = applicationContext.getBean(conditionProviderClazz);
                return conditionProvider.isEncodingApplicable();
            } catch (Exception e) {
                // Log the exception or handle it as needed
                return true;
            }

        }
        return true;
    }
    public static boolean isCustomClass(Class<?> clazz, Field field) {
        if (clazz == null || clazz.getClassLoader() == null || field == null || field.getType().isEnum()) {
            return false;
        }

        return isNotInExcludePackages(clazz);
    }

    public static boolean isNotInExcludePackages(Class<?> clazz) {
        for (String excludePackage : excludePackages) {
            if (clazz.getName().startsWith(excludePackage)) {
                return false;
            }
        }
        return true;
    }

    public Field[] getAllFieldsInCurrentOrParentClass(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null && clazz != Object.class) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields.toArray(new Field[0]);
    }
}
