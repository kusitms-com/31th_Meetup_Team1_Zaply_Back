package org.zapply.product.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.zapply.product.global.BaseTimeEntity;
import org.zapply.product.global.clova.enuermerate.SNSType;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "bigint", nullable = false)
    private Long accountId;

    @Column
    private String accountName;

    @Column
    @Enumerated(EnumType.STRING)
    private SNSType accountType;

    @Column
    private String tokenKey;

    @Column
    private String pageTokenKey;

    @Column
    private String email;

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime tokenExpireAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column
    private String userId;

    @Column(columnDefinition = "TEXT")
    private String profileImageUrl;

    @Builder
    public Account(String accountName, SNSType accountType, String tokenKey, Member member, String email,
                   LocalDateTime tokenExpireAt, String userId, String profileImageUrl, String pageTokenKey) {
        this.accountName = accountName;
        this.accountType = accountType;
        this.tokenKey = tokenKey;
        this.pageTokenKey = pageTokenKey;
        this.member = member;
        this.email = email;
        this.tokenExpireAt = tokenExpireAt;
        this.userId = userId;
        this.profileImageUrl = profileImageUrl;
    }

    public void updateTokenExpireAt(LocalDateTime tokenExpireAt) {
        this.tokenExpireAt = tokenExpireAt;
    }

    public void updateInfo(String accountName, String profileImageUrl) {
        this.accountName = accountName;
        this.profileImageUrl = profileImageUrl;
    }
}
