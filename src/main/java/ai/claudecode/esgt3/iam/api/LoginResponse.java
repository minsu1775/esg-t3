package ai.claudecode.esgt3.iam.api;

public record LoginResponse(String accessToken, String refreshToken, long accessTtlSeconds) {}
