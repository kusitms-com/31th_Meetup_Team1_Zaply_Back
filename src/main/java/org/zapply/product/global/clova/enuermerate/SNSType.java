package org.zapply.product.global.clova.enuermerate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.zapply.product.global.clova.dto.ClovaMessage;
import org.zapply.product.global.clova.dto.request.ClovaRequest;

import java.util.List;

@Getter
@RequiredArgsConstructor
public enum SNSType {
    THREADS("쓰레드"),
    INSTAGRAM("인스타그램"),
    FACEBOOK("페이스북"),
    LINKEDIN("링크드인"),
    TWITTER("트위터");

    private final String description;
}
