package org.zapply.product.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.zapply.product.domain.user.dto.request.AuthRequest;
import org.zapply.product.domain.user.dto.response.MemberResponse;
import org.zapply.product.domain.user.entity.Credential;
import org.zapply.product.domain.user.entity.Member;
import org.zapply.product.domain.user.repository.CredentialRepository;
import org.zapply.product.domain.user.repository.MemberRepository;
import org.zapply.product.global.apiPayload.exception.CoreException;
import org.zapply.product.global.apiPayload.exception.GlobalErrorType;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final CredentialRepository credentialRepository;

    /**
     * Credential을 생성하고 저장하는 메서드
     * @param credential
     * @param authRequest
     * @return Member
     */
    public Member createMember(Credential credential, AuthRequest authRequest) {
        memberRepository.findByEmailAndDeletedAtIsNull(authRequest.email()).ifPresent(existingMember -> {
           throw new CoreException(GlobalErrorType.MEMBER_ALREADY_EXISTS);
        });
        Member member = authRequest.toMember(credential);
        return memberRepository.save(member);
    }

    /**
     * 이메일로 사용자를 조회하는 메서드
     * @param email
     * @return Member
     */
    public Member getMemberByEmail(String email) {
        return memberRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new CoreException(GlobalErrorType.MEMBER_NOT_FOUND));
    }

    /**
     * 사용자 이름을 수정하는 메서드
     * @param member
     * @param name
     * @return MemberResponse
     */
    public MemberResponse updateMemberName(Member member, String name) {
        member.updateName(name);
        memberRepository.save(member);
        return MemberResponse.builder()
                .name(member.getName())
                .build();
    }

    /**
     * 사용자 비밀번호를 수정하는 메서드
     * @param member
     * @param password
     * @return MemberResponse
     */
    public void updateMemberPassword(Member member, String password) {
        member.getCredential().updatePassword(password);
        credentialRepository.save(member.getCredential());
    }
}