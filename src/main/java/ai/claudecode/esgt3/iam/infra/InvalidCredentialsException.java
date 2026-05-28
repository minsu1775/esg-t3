package ai.claudecode.esgt3.iam.infra;

import ai.claudecode.esgt3.shared.exception.EsgException;

/**
 * 로그인·리프레시 실패 시 발생.
 *
 * <p>errorCode:
 * <ul>
 *   <li>{@code INVALID_CREDENTIALS}: 이메일·비밀번호 불일치, 사용자 미존재</li>
 *   <li>{@code REFRESH_TOKEN_REVOKED}: refresh JTI가 블랙리스트에 등록</li>
 *   <li>{@code INVALID_TOKEN}: JWT 서명·만료 오류</li>
 * </ul>
 */
public class InvalidCredentialsException extends EsgException {

    public InvalidCredentialsException(String errorCode, String message) {
        super(errorCode, message);
    }

    public InvalidCredentialsException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
