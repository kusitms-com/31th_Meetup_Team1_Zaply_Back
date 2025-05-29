package org.zapply.product.domain.user.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.zapply.product.domain.user.entity.Member;


@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public record MemberResponse(
        @Schema(description = "회원 ID", example = "1")
        Long memberId,

        @Schema(description = "이름", example = "홍길동")
        String name,

        @Schema(description = "이메일", example = "zaply@gmail.com")
        String email,

        @Schema(description = "휴대폰 번호", example = "010-1234-5678")
        String phoneNumber
) {
    public static MemberResponse of(Member member) {
        return MemberResponse.builder()
                .memberId(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .phoneNumber(member.getPhoneNumber())
                .build();
    }
}