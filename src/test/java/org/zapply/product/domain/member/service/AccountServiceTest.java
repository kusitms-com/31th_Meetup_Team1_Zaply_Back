package org.zapply.product.domain.member.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.zapply.product.domain.user.dto.response.AccountsInfoResponse;
import org.zapply.product.domain.user.entity.Account;
import org.zapply.product.domain.user.entity.Member;
import org.zapply.product.domain.user.repository.AccountRepository;
import org.zapply.product.domain.user.service.AccountService;
import org.zapply.product.global.apiPayload.exception.CoreException;
import org.zapply.product.global.apiPayload.exception.GlobalErrorType;
import org.zapply.product.global.clova.enuermerate.SNSType;
import org.zapply.product.global.vault.VaultClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock private AccountRepository accountRepository;
    @Mock private VaultClient vaultClient;

    @InjectMocks private AccountService accountService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(accountService, "facebookPath", "facebook/path");
        ReflectionTestUtils.setField(accountService, "threadsPath",  "threads/path");
        ReflectionTestUtils.setField(accountService, "linkedinPath", "linkedin/path");
    }

    @Test
    void getAccountsInfo_returnsResponse() {
        Member member = Member.builder().email("u@e.com").name("U").build();
        Account acc = Account.builder()
                .accountType(SNSType.FACEBOOK)
                .accountName("fb_user")
                .email("fb@e.com")
                .tokenKey("key")
                .member(member)
                .tokenExpireAt(LocalDateTime.now().plusDays(1))
                .userId("uid").build();
        // set createdAt to avoid NPE in mapping
        LocalDateTime now = LocalDateTime.now();
        ReflectionTestUtils.setField(acc, "createdAt", now);

        given(accountRepository.findAllByMember(member)).willReturn(List.of(acc));

        AccountsInfoResponse res = accountService.getAccountsInfo(member);
        assertThat(res.totalCount()).isEqualTo(1);
        assertThat(res.accounts()).hasSize(1);
        assertThat(res.accounts().getFirst().accountName()).isEqualTo("fb_user");

        verify(accountRepository).findAllByMember(member);
    }

    @Test
    @DisplayName("unlinkService: existing account triggers delete and vault delete")
    void unlinkService_success() {
        Member member = Member.builder().email("t@e").name("T").build();
        Account acc = Account.builder()
                .accountType(SNSType.THREADS)
                .accountName("thr_user")
                .tokenKey("tok").member(member)
                .tokenExpireAt(LocalDateTime.now().plusDays(1))
                .userId("uid").build();

        given(accountRepository.findByAccountTypeAndMember(SNSType.THREADS, member))
                .willReturn(Optional.of(acc));

        assertDoesNotThrow(() -> accountService.unlinkService(SNSType.THREADS, member));
        verify(vaultClient).deleteSecretKey("threads/path", "tok");
        verify(accountRepository).delete(acc);
    }

    @Test
    @DisplayName("unlinkService: absent account throws ACCOUNT_NOT_FOUND")
    void unlinkService_notFound() {
        Member member = Member.builder().email("x@e").name("X").build();
        given(accountRepository.findByAccountTypeAndMember(SNSType.FACEBOOK, member))
                .willReturn(Optional.empty());

        CoreException ex = assertThrows(CoreException.class,
                () -> accountService.unlinkService(SNSType.FACEBOOK, member));
        assertThat(ex.getErrorType()).isEqualTo(GlobalErrorType.ACCOUNT_NOT_FOUND);
        verify(vaultClient, never()).deleteSecretKey(any(), any());
        verify(accountRepository, never()).delete(any());
    }

    @Test
    void getAccessToken_success() {
        Member member = Member.builder().email("a@e").name("A").build();
        Account acc = Account.builder()
                .accountType(SNSType.LINKEDIN)
                .tokenKey("k").member(member)
                .tokenExpireAt(LocalDateTime.now().plusHours(1))
                .build();
        given(accountRepository.findByAccountTypeAndMember(SNSType.LINKEDIN, member))
                .willReturn(Optional.of(acc));
        given(vaultClient.getSecret("linkedin/path", "k")).willReturn("secret");

        String token = accountService.getAccessToken(member, SNSType.LINKEDIN);
        assertThat(token).isEqualTo("secret");
    }

    @Test
    void getAccessToken_accountNotFound_throws() {
        Member member = Member.builder().email("b@e").name("B").build();
        given(accountRepository.findByAccountTypeAndMember(SNSType.FACEBOOK, member))
                .willReturn(Optional.empty());

        CoreException ex = assertThrows(CoreException.class,
                () -> accountService.getAccessToken(member, SNSType.FACEBOOK));
        assertThat(ex.getErrorType()).isEqualTo(GlobalErrorType.ACCOUNT_TOKEN_KEY_NOT_FOUND);
    }

    @Test
    void getAccessToken_tokenExpired_throws() {
        Member member = Member.builder().email("c@e").name("C").build();
        Account acc = Account.builder().accountType(SNSType.FACEBOOK)
                .tokenKey("k").member(member)
                .tokenExpireAt(LocalDateTime.now().minusMinutes(1))
                .build();
        given(accountRepository.findByAccountTypeAndMember(SNSType.FACEBOOK, member))
                .willReturn(Optional.of(acc));

        CoreException ex = assertThrows(CoreException.class,
                () -> accountService.getAccessToken(member, SNSType.FACEBOOK));
        assertThat(ex.getErrorType()).isEqualTo(GlobalErrorType.TOKEN_INVALID);
    }

    @Test
    void getAccessToken_vaultMissing_throws() {
        Member member = Member.builder().email("d@e").name("D").build();
        Account acc = Account.builder().accountType(SNSType.THREADS)
                .tokenKey("k").member(member)
                .tokenExpireAt(LocalDateTime.now().plusMinutes(10))
                .build();
        given(accountRepository.findByAccountTypeAndMember(SNSType.THREADS, member))
                .willReturn(Optional.of(acc));
        given(vaultClient.getSecret("threads/path", "k")).willReturn(null);

        CoreException ex = assertThrows(CoreException.class,
                () -> accountService.getAccessToken(member, SNSType.THREADS));
        assertThat(ex.getErrorType()).isEqualTo(GlobalErrorType.VAULT_TOKEN_NOT_FOUND);
    }

    @Test
    void isTokenExpired_behavior() {
        Account future = Account.builder().tokenExpireAt(LocalDateTime.now().plusSeconds(5)).build();
        Account past = Account.builder().tokenExpireAt(LocalDateTime.now().minusSeconds(5)).build();
        assertFalse(accountService.isTokenExpired(future));
        assertTrue(accountService.isTokenExpired(past));
    }

    @Test
    void generateKey_consistentHash() throws Exception {
        String key1 = accountService.generateKey(1L, "email@e.com");
        String key2 = accountService.generateKey(1L, "email@e.com");
        assertEquals(key1, key2);
        // check length of base64-encoded SHA256
        byte[] decoded = Base64.getUrlDecoder().decode(key1);
        assertEquals(32, decoded.length);
    }
}