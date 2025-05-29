package org.zapply.product.global.clova;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.zapply.product.domain.posting.dto.request.ToneTransferRequest;
import org.zapply.product.domain.posting.dto.response.ToneTransferResponse;
import org.zapply.product.domain.posting.service.ClovaService;
import org.zapply.product.global.clova.dto.ClovaMessage;
import org.zapply.product.global.clova.enuermerate.SNSType;
import org.zapply.product.global.clova.dto.request.ClovaRequest;
import org.zapply.product.global.clova.dto.response.ClovaResponse;
import org.zapply.product.global.clova.enuermerate.ClovaRole;

import java.util.List;

@SpringBootTest
class ClovaAiClientTest {

    @Autowired
    private ClovaAiClient clovaAiClient;

    @Autowired
    private ClovaService clovaService;

    @Value("${cloud.ncp.clova.api-key}")
    private String apiKey;

    @Value("${cloud.ncp.clova.model-name}")
    private String modelName;

    private ClovaRequest clovaRequest;

    @BeforeEach
    public void setUp() {
        clovaRequest = ClovaRequest.from(
                List.of(
                        ClovaMessage.of(ClovaRole.SYSTEM.getValue(), "너는 인스타에서 활동하는 인플루언서야 user의 글을 인스타풍의 느낌으로 바꿔야해"),
                        ClovaMessage.of(ClovaRole.USER.getValue(), "오늘은 정말 기분이 좋다. 날씨도 좋고, 친구들과 함께하는 시간이 너무 즐거워.")
                ));
    }

    @Test
    public void 클로바API_연동_성공() {
        String authorizationHeader = "Bearer " + apiKey;
        ClovaResponse storyLines = clovaAiClient.getCompletions(authorizationHeader, modelName, clovaRequest);
        System.out.println(storyLines);
        System.out.println(storyLines.result().message().content());
    }

    @Test
    public void 쓰레드_글로_변경() {
        ToneTransferRequest toneTransferRequest = new ToneTransferRequest(
                 List.of(SNSType.THREADS),
                "오늘은 정말 기분이 좋다. 날씨도 좋고, 친구들과 함께하는 시간이 너무 즐거워."
        );
        ToneTransferResponse storyLines = clovaService.TransferToSNSTone(toneTransferRequest);
        System.out.println("-------------------------THREADS-------------------------");
        System.out.println(storyLines);
        System.out.println("-------------------------THREADS-------------------------");
    }

    @Test
    public void 인스타_글로_변경() {
        ToneTransferRequest toneTransferRequest = new ToneTransferRequest(
                List.of(SNSType.INSTAGRAM),
                "오늘은 정말 기분이 좋다. 날씨도 좋고, 친구들과 함께하는 시간이 너무 즐거워."
        );
        ToneTransferResponse storyLines = clovaService.TransferToSNSTone(toneTransferRequest);
        System.out.println("-------------------------INSTA-------------------------");
        System.out.println(storyLines);
        System.out.println("-------------------------INSTA-------------------------");
    }

    @Test
    public void 페이스북_글로_변경() {
        ToneTransferRequest toneTransferRequest = new ToneTransferRequest(
                List.of(SNSType.FACEBOOK),
                "오늘은 정말 기분이 좋다. 날씨도 좋고, 친구들과 함께하는 시간이 너무 즐거워."
        );
        ToneTransferResponse storyLines = clovaService.TransferToSNSTone(toneTransferRequest);
        System.out.println("-------------------------FACEBOOK-------------------------");;
        System.out.println(storyLines);
        System.out.println("-------------------------FACEBOOK-------------------------");;
    }

    @Test
    public void 제목_추천() {
        ToneTransferRequest toneTransferRequest = new ToneTransferRequest(
                List.of(SNSType.FACEBOOK),
                "오늘은 정말 기분이 좋다. 날씨도 좋고, 친구들과 함께하는 시간이 너무 즐거워."
        );
        String projectTitle = clovaService.recommendProjectTitle(toneTransferRequest);
        System.out.println(projectTitle);
    }
}