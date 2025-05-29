package org.zapply.product.global.clova;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.zapply.product.global.clova.dto.request.ClovaRequest;
import org.zapply.product.global.clova.dto.response.ClovaResponse;

@FeignClient(value = "clova", url = "https://clovastudio.stream.ntruss.com/testapp")
public interface ClovaAiClient {

    @PostMapping("/v1/chat-completions/{modelName}")
    ClovaResponse getCompletions(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable("modelName") String modelName,
            @RequestBody ClovaRequest request
    );
}
