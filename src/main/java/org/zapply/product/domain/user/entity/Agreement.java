package org.zapply.product.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Agreement {

    @Column(nullable = false, columnDefinition = "boolean")
    private Boolean termsOfServiceAgreed;

    @Column(nullable = false, columnDefinition = "boolean")
    private Boolean privacyPolicyAgreed;

    @Column(columnDefinition = "boolean")
    private Boolean marketingAgreed;

    @Builder
    public Agreement(Boolean termsOfServiceAgreed, Boolean privacyPolicyAgreed, Boolean marketingAgreed) {
        this.termsOfServiceAgreed = termsOfServiceAgreed;
        this.privacyPolicyAgreed = privacyPolicyAgreed;
        this.marketingAgreed = marketingAgreed;
    }
}