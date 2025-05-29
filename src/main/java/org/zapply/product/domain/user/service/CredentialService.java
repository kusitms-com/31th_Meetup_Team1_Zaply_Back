package org.zapply.product.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.zapply.product.domain.user.dto.request.AuthRequest;
import org.zapply.product.domain.user.entity.Credential;
import org.zapply.product.domain.user.entity.Member;
import org.zapply.product.domain.user.repository.CredentialRepository;
import org.zapply.product.global.apiPayload.exception.CoreException;
import org.zapply.product.global.apiPayload.exception.GlobalErrorType;

@Service
@RequiredArgsConstructor
public class CredentialService {

    private final PasswordEncoder passwordEncoder;
    private final CredentialRepository credentialRepository;

    /**
     * Credential을 생성하고 저장하는 메서드
     * @param authRequest
     * @return Credential
     */
    public Credential createCredential(AuthRequest authRequest) {

        String hashedPassword = passwordEncoder.encode(authRequest.password());
        Credential credential = Credential.builder()
                .hashedPassword(hashedPassword)
                .build();

        return credentialRepository.save(credential);
    }

    /**
     * 비밀번호를 확인하는 메서드
     * @param member
     * @param password
     * @return boolean
     */
    public boolean checkPassword(Member member, String password) {
        Credential credential = member.getCredential();

        if(!passwordEncoder.matches(password, credential.getPassword())) {
            throw new CoreException(GlobalErrorType.PASSWORD_MISMATCH);
        }
        return true;
    }
}
