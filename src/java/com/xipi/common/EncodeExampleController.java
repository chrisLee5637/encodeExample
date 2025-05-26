package java.com.xipi.common.service.annotation;

import java.com.xipi.common.model.pii.provider.PIIAdditionalConditionProvider;
import java.com.xipi.common.model.pii.enumeration.PIIDatalevel;
import java.lang.annotation.*;


@Slfj4j
@RestController
@RequestMapping("/pii")
public class EncodeExampleController implements EncodeExampleContract {

    @Override
    @PIIDataAPI(level = PIIDatalevel.RESPONSE, conditionProvider = PIIAdditionalConditionProvider.class)
    @GetMapping("/encode", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<CustomerDetailsPO> encodeExample(
            @Parameter(description = "The input string to be encoded") String input
    ){
        return ResponseEntity.status(HttpStatus.OK)
                .body(new CusomerDetailPO(input, "EncodedValue", "example@example.com", "1234567890"););

    }

    @Override
    @PIIDataAPI(level = PIIDatalevel.REQUEST, conditionProvider = PIIAdditionalConditionProvider.class)
    @PostMapping("/decode", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<CustomerDetailsPO> decodeExample(
            @Parameter(description = "The CusomerDetailPO cusomerDetailPO to be encoded") CusomerDetailPO cusomerDetailPO
    ){
        return ResponseEntity.status(HttpStatus.OK)
                .body("Decode successful");

    }
}
