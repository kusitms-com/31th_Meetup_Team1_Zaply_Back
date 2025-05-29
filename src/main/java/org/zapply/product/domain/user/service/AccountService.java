package org.zapply.product.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.zapply.product.domain.user.dto.response.AccountInfo;
import org.zapply.product.domain.user.dto.response.AccountsInfoResponse;
import org.zapply.product.domain.user.entity.Account;
import org.zapply.product.domain.user.entity.Member;
import org.zapply.product.global.clova.enuermerate.SNSType;
import org.zapply.product.domain.user.repository.AccountRepository;
import org.zapply.product.domain.user.repository.MemberRepository;
import org.zapply.product.global.apiPayload.exception.CoreException;
import org.zapply.product.global.apiPayload.exception.GlobalErrorType;
import org.zapply.product.global.snsClients.facebook.service.FacebookClient;
import org.zapply.product.global.snsClients.facebook.service.FacebookMediaClient;
import org.zapply.product.global.snsClients.instagram.InstagramBusinessResponse;
import org.zapply.product.global.snsClients.instagram.InstagramClient;
import org.zapply.product.global.snsClients.facebook.FacebookProfile;
import org.zapply.product.global.snsClients.facebook.FacebookToken;
import org.zapply.product.global.snsClients.instagram.InstagramProfile;
import org.zapply.product.global.snsClients.linkedin.LinkedinClient;
import org.zapply.product.global.snsClients.linkedin.LinkedinToken;
import org.zapply.product.global.snsClients.linkedin.LinkedinUserInfo;
import org.zapply.product.global.snsClients.threads.ThreadsClient;
import org.zapply.product.global.snsClients.threads.ThreadsProfile;
import org.zapply.product.global.snsClients.threads.ThreadsToken;
import org.zapply.product.global.vault.VaultClient;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final ThreadsClient threadsClient;
    private final VaultClient vaultClient;
    private final LinkedinClient linkedinClient;
    private final MemberRepository memberRepository;
    private final InstagramClient instagramClient;
    private final FacebookClient facebookClient;
    private final FacebookMediaClient facebookMediaClient;

    @Value("${spring.security.oauth2.client.registration.facebook.redirect-uri}")
    private String facebookRedirectUrl;
    @Value("${spring.security.oauth2.client.registration.threads.redirect-uri}")
    private String threadsRedirectUrl;
    @Value("${spring.security.oauth2.client.registration.linkedin.redirect-uri}")
    private String linkedinRedirectUrl;

    @Value("${spring.cloud.vault.facebook-path}")
    private String facebookPath;
    @Value("${spring.cloud.vault.threads-path}")
    private String threadsPath;
    @Value("${spring.cloud.vault.linkedin-path}")
    private String linkedinPath;
    @Value("${spring.cloud.vault.instagram-path}")
    private String instagramPath;
    @Value("${spring.cloud.vault.facebook-page-path}")
    private String facebookPagePath;


    /**
     * 페이스북 계정 연동
     *
     * @param code
     * @param memberId
     * @return Redis Key
     */
    public String linkFacebook(String code, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CoreException(GlobalErrorType.MEMBER_NOT_FOUND));

        log.info("linkFacebook called with code: {}, memberId: {}", code);
        // 페이스북에 있는 사용자 정보 반환
        FacebookProfile facebookProfile = facebookClient.getMemberInfo(code);
        // 페이스북으로 액세스 토큰 요청하기
        FacebookToken longFacebookAccessToken = facebookClient.getLongLivedToken(code);
        log.info("Long-lived Facebook access token: {}", longFacebookAccessToken.accessToken());
        String pageId =  facebookMediaClient.getPageId(longFacebookAccessToken.accessToken());
        FacebookToken longFacebookPageAccessToken = facebookClient.getLongLivedPageAccessToken(pageId, longFacebookAccessToken.accessToken());

        // 반환된 정보의 이메일 추출
        String email = facebookProfile.email();
        if (email == null) {
            throw new CoreException(GlobalErrorType.EMAIL_NOT_FOUND);
        }

        // key 생성
        String key = "facebook:" + "client:" + generateKey(member.getId(), email);
        String keyForPage = "facebook:" + "page:" + generateKey(member.getId(), pageId);

        // business logic: account 정보가 이미 있다면 확인 후 해당 account 정보를 반환하고, 없다면 새로운 account 정보를 생성하여 반환
        Account account = accountRepository.findByEmailAndAccountTypeAndMember(email, SNSType.FACEBOOK, member)
                .map(existingAccount -> {
                    // 토큰 만료일 갱신
                    existingAccount.updateTokenExpireAt(LocalDateTime.now().plusDays(60));
                    existingAccount.updateInfo(facebookProfile.name(), String.valueOf(facebookProfile.picture()));
                    return accountRepository.save(existingAccount);
                })
                .orElseGet(() -> {
                    // 계정이 없다면 새 계정 생성
                    Account newAccount = Account.builder()
                            .accountName(facebookProfile.name())
                            .email(email)
                            .accountType(SNSType.FACEBOOK)
                            .tokenKey(key)
                            .pageTokenKey(keyForPage)
                            .member(member)
                            .tokenExpireAt(LocalDateTime.now().plusDays(60))
                            .userId(facebookProfile.id())
                            .profileImageUrl(String.valueOf(facebookProfile.picture()))
                            .build();
                    return accountRepository.save(newAccount);
                });
        vaultClient.saveSecret(facebookPath, key, longFacebookAccessToken.accessToken());
        vaultClient.savePageSecret(facebookPagePath, keyForPage, longFacebookPageAccessToken.accessToken());

        return key;
    }

    /**
     * 스레드 계정 연동
     *
     * @param code
     * @param memberId
     * @return Redis Key
     */
    public String linkThreads(String code, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CoreException(GlobalErrorType.MEMBER_NOT_FOUND));
        // 스레드로 액세스 토큰 요청하기
        ThreadsToken shortThreadsToken = threadsClient.getThreadsAccessToken(code, threadsRedirectUrl);
        // 스레드에서 장기 액세스 토큰 요청하기
        ThreadsToken longThreadsToken = threadsClient.getLongLivedToken(shortThreadsToken.accessToken());
        // 스레드에 있는 사용자 정보 반환
        ThreadsProfile profile = threadsClient.getThreadsProfile(longThreadsToken.accessToken());
        // 반환된 정보의 username 추출, key 생성
        String key = "threads:" + "client:" + generateKey(member.getId(), profile.username());
        //bussiness logic: account 정보가 이미 있다면 확인 후 해당 account 정보를 반환하고, 없다면 새로운 account 정보를 생성하여 반환
        Account account = accountRepository.findByAccountNameAndAccountTypeAndMember(profile.username(), SNSType.THREADS, member)
                .map(existingAccount -> {
                    // 토큰 만료일 갱신
                    existingAccount.updateTokenExpireAt(LocalDateTime.now().plusDays(60));
                    existingAccount.updateInfo(profile.name(), profile.profilePictureUrl());
                    return accountRepository.save(existingAccount);
                })
                .orElseGet(() -> {
                    // 계정이 없다면 새로 저장
                    Account newAccount = Account.builder()
                            .accountName(profile.username())
                            .email("")
                            .accountType(SNSType.THREADS)
                            .tokenKey(key)
                            .member(member)
                            .tokenExpireAt(LocalDateTime.now().plusDays(60))
                            .userId(profile.id())
                            .profileImageUrl(profile.profilePictureUrl())
                            .build();
                    return accountRepository.save(newAccount);
                });
        vaultClient.saveSecret(threadsPath, key, longThreadsToken.accessToken());
        return key;
    }


    public String linkLinkedin(String code, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CoreException(GlobalErrorType.MEMBER_NOT_FOUND));

        LinkedinToken token = linkedinClient.getLinkedinAccessToken(code);
        String accessToken = token.accessToken();
        LinkedinUserInfo profile = linkedinClient.getLinkedinProfile(accessToken);
        String key = "linkedin:client:" + generateKey(member.getId(), profile.sub());

        Account account = accountRepository
                .findByAccountNameAndAccountTypeAndMember(profile.name(), SNSType.LINKEDIN, member)
                .map(existing -> {
                    existing.updateTokenExpireAt(LocalDateTime.now().plusDays(60));
                    return accountRepository.save(existing);
                })
                .orElseGet(() -> {
                    Account newAcc = Account.builder()
                            .accountName(profile.name())
                            .email(profile.email())  // 이메일 별도 조회 로직이 필요하면 추가
                            .accountType(SNSType.LINKEDIN)
                            .tokenKey(key)
                            .member(member)
                            .tokenExpireAt(LocalDateTime.now().plusDays(60))
                            .userId(profile.sub())
                            .build();
                    return accountRepository.save(newAcc);
                });

        // 5) Vault에 토큰 저장
        vaultClient.saveSecret(linkedinPath, key, accessToken);
        return key;
    }

    /**
     * Key 생성
     *
     * @param memberId
     * @param email
     * @return Key
     */
    public String generateKey(Long memberId, String email) {
        try {
            String rawKey = memberId + ":" + email;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawKey.getBytes());
            return Base64.getUrlEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new CoreException(GlobalErrorType.SHA256_GENERATION_ERROR);
        }
    }

    /**
     * 사용자의 Vault에 저장된 액세스 토큰을 가져옴
     *
     * @param member
     * @param accountType
     * @return 액세스 토큰
     */
    public String getAccessToken(Member member, SNSType accountType) {
        Account account = accountRepository.findByAccountTypeAndMember(accountType, member)
                .orElseThrow(() -> new CoreException(GlobalErrorType.ACCOUNT_TOKEN_KEY_NOT_FOUND));

        String vaultPath;
        switch (accountType) {
            case FACEBOOK -> vaultPath = facebookPath;
            case THREADS -> vaultPath = threadsPath;
            case LINKEDIN -> vaultPath = linkedinPath;
            case INSTAGRAM -> vaultPath = instagramPath;
            default -> throw new CoreException(GlobalErrorType.SNS_TYPE_NOT_FOUND);
        }

        // 토큰 만료일 체크
        if (isTokenExpired(account)) {
            throw new CoreException(GlobalErrorType.TOKEN_INVALID);
        }

        // Vault에서 액세스 토큰 가져오기
        String accessToken = vaultClient.getSecret(vaultPath, account.getTokenKey());
        if (accessToken == null || accessToken.isBlank()) {
            throw new CoreException(GlobalErrorType.VAULT_TOKEN_NOT_FOUND);
        }

        return accessToken;
    }

    public final String getPageToken(Member member){
        Account account = accountRepository.findByAccountTypeAndMember(SNSType.FACEBOOK, member)
                .orElseThrow(() -> new CoreException(GlobalErrorType.ACCOUNT_TOKEN_KEY_NOT_FOUND));

        String vaultPath = facebookPagePath;

        // 토큰 만료일 체크
        if (isTokenExpired(account)) {
            throw new CoreException(GlobalErrorType.TOKEN_INVALID);
        }

        // Vault에서 페이지 액세스 토큰 가져오기
        String pageAccessToken = vaultClient.getSecret(vaultPath, account.getPageTokenKey());
        if (pageAccessToken == null || pageAccessToken.isBlank()) {
            throw new CoreException(GlobalErrorType.VAULT_TOKEN_NOT_FOUND);
        }

        return pageAccessToken;
    }

    /**
     * 토큰 갱신 날짜 유효성 체크
     * @param account
     * @return true: 만료됨, false: 만료되지 않음
     */
    public boolean isTokenExpired(Account account) {
        return account.getTokenExpireAt() == null || LocalDateTime.now().isAfter(account.getTokenExpireAt());
    }

    /**
     * member에게 연결된 account 조회
     * @param member
     */
    public AccountsInfoResponse getAccountsInfo(Member member) {
        List<Account> accounts = accountRepository.findAllByMember(member);
        return AccountsInfoResponse.of(
                accounts.stream()
                        .map(AccountInfo::of)
                        .collect(Collectors.toList())
        );
    }

    /**
     * 계정 삭제
     * @param snsType
     * @param member
     */
    public void unlinkService(SNSType snsType, Member member) {
        Account account = accountRepository.findByAccountTypeAndMember(snsType, member)
                .orElseThrow(() -> new CoreException(GlobalErrorType.ACCOUNT_NOT_FOUND));

        String vaultPath;
        switch (snsType) {
            case FACEBOOK -> vaultPath = facebookPath;
            case THREADS -> vaultPath = threadsPath;
            case LINKEDIN  -> vaultPath = linkedinPath;
            case INSTAGRAM -> vaultPath = instagramPath;
            default -> throw new CoreException(GlobalErrorType.SNS_TYPE_NOT_FOUND);
        }
        vaultClient.deleteSecretKey(vaultPath, account.getTokenKey());
        accountRepository.delete(account);
    }

    /**
     * 인스타그램 계정 연동
     * @param accessToken
     * @param member
     */
    public String linkInstagram(String accessToken, Member member){

        String longAccessToken = instagramClient.getLongLivedToken(accessToken);

        // 사용자 비즈니스 계정 정보 조회
        List<InstagramBusinessResponse.PageData> pageData = instagramClient.getAccountId(longAccessToken);
        if (pageData.isEmpty()) {
            throw new CoreException(GlobalErrorType.INSTAGRAM_BUSINESS_ACCOUNT_NOT_FOUND);
        }

        // 인스타그램 비즈니스 계정 ID
        String userId = pageData.get(0).instagramBusinessAccount().id();

        // 인스타그램 비즈니스 프로필 정보 조회
        InstagramProfile instagramProfile = instagramClient.getInstagramProfile(userId, longAccessToken);

        // key 생성
        String key = "instagram:" + "client:" + generateKey(member.getId(), instagramProfile.id());

        //bussiness logic: account 정보가 이미 있다면 확인 후 해당 account 정보를 반환하고, 없다면 새로운 account 정보를 생성하여 반환
        Account account = accountRepository.findByUserIdAndAccountTypeAndMember(instagramProfile.id(), SNSType.INSTAGRAM, member)
                .map(existingAccount -> {
                    // 토큰 만료일 갱신
                    existingAccount.updateTokenExpireAt(LocalDateTime.now().plusDays(60));
                    existingAccount.updateInfo(instagramProfile.username(), instagramProfile.profilePictureUrl());
                    return accountRepository.save(existingAccount);
                })
                .orElseGet(() -> {
                    // 계정이 없다면 새로 저장
                    Account newAccount = Account.builder()
                            .accountName(instagramProfile.username())
                            .email("")
                            .accountType(SNSType.INSTAGRAM)
                            .tokenKey(key)
                            .member(member)
                            .tokenExpireAt(LocalDateTime.now().plusDays(60))
                            .userId(instagramProfile.id())
                            .profileImageUrl(instagramProfile.profilePictureUrl())
                            .build();
                    return accountRepository.save(newAccount);
                });
        vaultClient.saveSecret(instagramPath, key, accessToken);

        return key;
    }

}
