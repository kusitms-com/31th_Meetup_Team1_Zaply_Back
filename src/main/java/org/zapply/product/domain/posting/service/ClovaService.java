package org.zapply.product.domain.posting.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zapply.product.domain.posting.dto.request.ToneTransferRequest;
import org.zapply.product.domain.posting.dto.response.ToneTransferResponse;
import org.zapply.product.global.clova.dto.ClovaMessage;
import org.zapply.product.global.clova.enuermerate.ClovaRole;
import org.zapply.product.global.clova.enuermerate.SNSType;
import org.zapply.product.global.apiPayload.exception.CoreException;
import org.zapply.product.global.apiPayload.exception.GlobalErrorType;
import org.zapply.product.global.clova.ClovaAiClient;
import org.zapply.product.global.clova.dto.request.ClovaRequest;
import org.zapply.product.global.clova.dto.response.ClovaResponse;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Log4j2
public class ClovaService {

    @Value("${cloud.ncp.clova.api-key}")
    private String apiKey;

    @Value("${cloud.ncp.clova.model-name}")
    private String modelName;

    @Value("${prompt.title.system}")
    private String titleSystemPrompt;

    @Value("${prompt.snstype.threads}")
    private String threadsPrompt;

    @Value("${prompt.snstype.instagram}")
    private String instagramPrompt;

    @Value("${prompt.snstype.facebook}")
    private String facebookPrompt;

    @Value("${prompt.snstype.linkedin}")
    private String linkedinPrompt;

    @Value("${prompt.snstype.twitter}")
    private String twitterPrompt;

    private final ClovaAiClient clovaAiClient;

    /**
     * SNS 글 분위기 변환
     * @param toneTransferRequest 사용자 입력 콘텐츠와 SNS 타입을 포함하는 요청 객체
     * @return 변환된 콘텐츠를 포함하는 ToneTransferResponse 객체
     * @param toneTransferRequest
     * @return ToneTransferResponse 객체
     */
    public ToneTransferResponse TransferToSNSTone(ToneTransferRequest toneTransferRequest) {
        String authorizationHeader = "Bearer " + apiKey;
        List<SNSType> snsTypes = toneTransferRequest.snsTypes();
        String userPrompt = toneTransferRequest.userPrompt();

        List<ToneTransferResponse.ToneTransferResponseItem> responseItems = new ArrayList<>();

        for (SNSType snsType : snsTypes) {
            String systemPrompt;

            // snsType별로 정확하게 매칭
            switch (snsType) {
                case THREADS -> systemPrompt = threadsPrompt;
                case INSTAGRAM -> systemPrompt = instagramPrompt;
                case FACEBOOK -> systemPrompt = facebookPrompt;
                case LINKEDIN -> systemPrompt = linkedinPrompt;
                case TWITTER -> systemPrompt = twitterPrompt;
                default -> throw new CoreException(GlobalErrorType.SNS_TYPE_NOT_FOUND);
            }

            try {
                ClovaRequest clovaRequest = ClovaRequest.from(List.of(
                        ClovaMessage.of(ClovaRole.SYSTEM.getValue(), systemPrompt),
                        ClovaMessage.of(ClovaRole.USER.getValue(), userPrompt)
                ));
                ClovaResponse clovaResponse = clovaAiClient.getCompletions(authorizationHeader, modelName, clovaRequest);
                String content = clovaResponse.result().message().content();

                ToneTransferResponse.ToneTransferResponseItem responseItem =
                        ToneTransferResponse.ToneTransferResponseItem.builder()
                                .snsType(snsType)
                                .content(content)
                                .build();

                responseItems.add(responseItem);
            } catch (Exception e) {
                throw new CoreException(GlobalErrorType.CLOVA_API_ERROR);
            }
        }

        return ToneTransferResponse.builder()
                .toneTransferResponseItems(responseItems)
                .build();
    }

    /**
     * 글에 맞는 컨텐츠 제목 추천
     * @param toneTransferRequest 사용자 입력 콘텐츠를 포함하는 요청 객체
     * @return 추천된 제목 문자열
     */
    public String recommendProjectTitle(ToneTransferRequest toneTransferRequest) {
        String authorizationHeader = "Bearer " + apiKey;
        String userPrompt = toneTransferRequest.userPrompt();
        String systemPrompt = titleSystemPrompt;
        try{
            ClovaRequest clovaRequset = ClovaRequest.from(List.of(
                    ClovaMessage.of(ClovaRole.SYSTEM.getValue(), systemPrompt),
                    ClovaMessage.of(ClovaRole.USER.getValue(),   userPrompt)
            ));
            ClovaResponse clovaResponse = clovaAiClient.getCompletions(authorizationHeader, modelName, clovaRequset);
            return clovaResponse.result().message().content();
        } catch (Exception e) {
            log.error("변환 중 오류 발생 요청 데이터: {}", userPrompt, e);
            throw new CoreException(GlobalErrorType.CLOVA_API_ERROR);
        }
    }

}
