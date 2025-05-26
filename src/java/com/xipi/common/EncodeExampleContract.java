package java.com.xipi.common.service.annotation;

import java.com.xipi.common.model.pii.provider.PIIAdditionalConditionProvider;
import java.com.xipi.common.model.pii.enumeration.PIIDatalevel;
import java.lang.annotation.*;


@Tag(name = "EncodeExampleContract", description = "Contract for encoding examples in PII handling")
public interface EncodeExampleContract {

    @Operation(
        summary = "Encode Example",
        description = "Encodes a given input string based on the specified PII data level and additional conditions."
    )
    ResponseEntity<CustomerDetailsPO> encodeExample(
        @Parameter(description = "The input string to be encoded") String input
    );

    @Operation(
            summary = "Decode Example",
            @Parameter(description = "The CusomerDetailPO cusomerDetailPO to be encoded") CusomerDetailPO cusomerDetailPO
    )
    ResponseEntity<CustomerDetailsPO> decodeExample(
            @Parameter(description = "The input string to be decoded") String input
    );

}
