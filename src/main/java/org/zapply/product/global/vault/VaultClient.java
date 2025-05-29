package org.zapply.product.global.vault;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;
import org.zapply.product.global.security.jasypt.JasyptStringEncryptor;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class VaultClient {

    private final VaultTemplate vaultTemplate;
    private final ObjectMapper objectMapper;
    private final JasyptStringEncryptor jasyptStringEncryptor;

    /**
     * Vault에 시크릿을 저장 (AES256 암호화)
     * @param path  저장할 경로
     * @param key   키
     * @param value 시크릿 값
     */
    public void saveSecret(String path, String key, String value) {
        String encryptedValue = jasyptStringEncryptor.encrypt(value);
        String fullPath = "secret/data/" + path;
        vaultTemplate.write(fullPath, Map.of("data", Map.of(key, encryptedValue)));
    }

    public void savePageSecret(String path, String key, String value) {
        String encryptedValue = jasyptStringEncryptor.encrypt(value);
        String fullPath = "secret/data/" + path;
        vaultTemplate.write(fullPath, Map.of("data", Map.of(key, encryptedValue)));
    }

    /**
     * Vault에서 시크릿을 가져옴 (AES256 복호화)
     * @param path 시크릿 경로
     * @param key  키
     * @return 복호화된 시크릿 값
     */
    public String getSecret(String path, String key) {
        String fullPath = "secret/data/" + path;
        VaultResponse response = vaultTemplate.read(fullPath);

        if (response == null || response.getData() == null) {
            return null;
        }

        JsonNode root = objectMapper.valueToTree(response.getData());
        JsonNode dataNode = root.path("data");
        JsonNode valueNode = dataNode.path(key);

        if (valueNode.isMissingNode()) {
            return null;
        }

        return jasyptStringEncryptor.decrypt(valueNode.asText());
    }

    /**
     * Vault에서 시크릿 데이터를 삭제(soft delete)합니다.
     * 이 호출은 KV v2 엔진의 data 엔드포인트에 DELETE 요청을 보내어
     * 해당 시크릿 경로의 최신 버전을 soft delete 합니다.
     * 버전 전체 soft delete
     * @param path 삭제할 시크릿 경로 (e.g. "my-app/credentials")
     */
    public void deleteSecretData(String path) {
        String dataPath = "secret/data/" + path;
        log.info("Soft-deleting secret data at {}", dataPath);
        vaultTemplate.delete(dataPath);
    }

    /**
     * Vault에서 시크릿 메타데이터를 완전 삭제(permanent delete)합니다.
     * 이 호출은 KV v2 엔진의 metadata 엔드포인트에 DELETE 요청을 보내
     * 해당 시크릿의 모든 버전과 메타정보를 삭제합니다.
     * 그냥 전체(모든 버전) 삭제
     * @param path 삭제할 시크릿 경로 (e.g. "my-app/credentials")
     */
    public void deleteSecretMetadata(String path) {
        String metadataPath = "secret/metadata/" + path;
        log.info("Permanently deleting secret metadata at {}", metadataPath);
        vaultTemplate.delete(metadataPath);
    }

    /**
     * Vault에서 특정 키만 삭제하거나,
     * 나머지 키가 없으면 전체 삭제까지 처리하는 예시 메서드입니다.
     * 해당 버전에서 키에 맞는 값 삭제하고, 버전 올려줌
     * @param path 시크릿 경로
     * @param key  삭제할 데이터 키
     */
    @SuppressWarnings("unchecked")
    public void deleteSecretKey(String path, String key) {
        String fullPath = "secret/data/" + path;
        VaultResponse response = vaultTemplate.read(fullPath);
        if (response == null || response.getData() == null) {
            return;
        }

        Map<String, Object> dataMap = (Map<String, Object>) response.getData().get("data");
        if (!dataMap.containsKey(key)) {
            return;
        }

        Map<String, Object> updated = new HashMap<>(dataMap);
        updated.remove(key);

        if (updated.isEmpty()) {
            // 남은 키가 없으면 soft delete
            vaultTemplate.delete(fullPath);
        } else {
            // 남은 키만 다시 write
            vaultTemplate.write(fullPath, Map.of("data", updated));
        }
    }
}