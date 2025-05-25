package java.com.xipi.common.model.pii.enumeration;

import java.util.List;

public enum PIIFieldType {
    // Enum values representing different field types that PII depends on
    CUSTOMER_ID,
    CUSTOMER_EMAIL;

    public static boolean isFieldTypeNeedRemoveCustomerId(PIIFieldType fieldType) {
        // Check if the field type is CUSTOMER_ID
        return List.of(CUSTOMER_ID).contains(fieldType);
    }
}
