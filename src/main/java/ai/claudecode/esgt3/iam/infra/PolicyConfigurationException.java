package ai.claudecode.esgt3.iam.infra;

import ai.claudecode.esgt3.shared.exception.EsgException;

/**
 * 정책 YAML 로드·파싱 실패 시 발생.
 *
 * <p>errorCode 컨벤션:
 * <ul>
 *   <li>{@code POLICY_LOAD_FAILED}: classpath에서 정책 디렉터리 스캔 실패</li>
 *   <li>{@code POLICY_PARSE_FAILED}: 개별 YAML 파일 파싱 실패</li>
 *   <li>{@code POLICY_FIELD_MISSING}: 필수 필드(id/effect) 누락</li>
 * </ul>
 */
public class PolicyConfigurationException extends EsgException {

    public PolicyConfigurationException(String errorCode, String message) {
        super(errorCode, message);
    }

    public PolicyConfigurationException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
