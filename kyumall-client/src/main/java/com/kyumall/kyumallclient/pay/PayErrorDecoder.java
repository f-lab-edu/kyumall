package com.kyumall.kyumallclient.pay;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyumall.kyumallcommon.exception.ErrorCode;
import com.kyumall.kyumallcommon.exception.KyumallException;
import feign.Response;
import feign.codec.ErrorDecoder;
import java.io.IOException;
import java.io.Reader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PayErrorDecoder implements ErrorDecoder {
  private final ObjectMapper objectMapper;

  @Override
  public Exception decode(String methodKey, Response response) {
    if (response.status() == 400) {
      PayResponse  payResponse = extractPayResponse(response);
      if (payResponse.getResult().equals(PayResult.LACK_OF_AMOUNT)) {
        throw new KyumallException(ErrorCode.FAIL_PAY_BECAUSE_LACK_AMOUNT);
      }
    }
    throw new KyumallException(ErrorCode.ORDER_PAY_FAILS);
  }

  private PayResponse extractPayResponse(Response response) {
    try(Reader reader = response.body().asReader()) {
      return objectMapper.readValue(reader, PayResponse.class);
    } catch (IOException e) {
      throw new KyumallException(ErrorCode.INTERNAL_SERVER_ERROR);
    }
  }
}
