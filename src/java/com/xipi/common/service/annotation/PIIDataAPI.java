package java.com.xipi.common.service.annotation;

import java.com.xipi.common.model.pii.provider.PIIAdditionalConditionProvider;
import java.com.xipi.common.model.pii.enumeration.PIIDatalevel;
import java.lang.annotation.*;

/**
 * Annotation to mark methods that handle PII data.
 * @see PIIDatalevel
 * 1. level == REQUEST will remove customerid from request
 * 2. level == RESPONSE will encode data from response
 * 3. level == ALL will remove customerid from request and encode data from response
 * @see PIIAdditionalConditionProvider
 * if you want to add additional conditions for encoding, you can implement PIIAdditionalConditionProvider
 * and add it into spring container then set it in conditionalProvider
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface PIIDataAPI {
    PIIDatalevel level();
    Class<? extends PIIAdditionalConditionProvider> conditionProvider() default PIIAdditionalConditionProvider.class;
}
