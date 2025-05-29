package org.zapply.product.global.vault;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.vault.authentication.TokenAuthentication;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.core.VaultTemplate;

@Configuration
public class VaultConfig {

    @Value("${spring.cloud.vault.token}")
    private String vaultToken;

    @Value("${spring.cloud.vault.uri}")
    private String vaultEndpointUri;

    /**
     * VaultTemplate Bean 설정
     */
    @Bean
    public VaultTemplate vaultTemplate() {

        TokenAuthentication tokenAuthentication = new TokenAuthentication(vaultToken);

        return new VaultTemplate(VaultEndpoint.from(vaultEndpointUri), tokenAuthentication);
    }
}