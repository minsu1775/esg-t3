package ai.claudecode.esgt3.iam.domain;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 정책 when 절 매칭 로직. 순수 함수, 상태 없음.
 *
 * <p>지원 키 prefix: {@code subject.}, {@code resource.}, {@code action}, {@code env.}.
 * 값 형태:
 * <ul>
 *   <li>스칼라 ({@code String}, {@code Number}): 평등 비교</li>
 *   <li>리스트: in 매처 (값이 리스트의 한 원소와 일치)</li>
 *   <li>Map: {@code contains} 또는 {@code in} 키 보유 - 컬렉션 매처</li>
 *   <li>{@code "${resource.entityId}"} 형태: 변수 치환 후 비교</li>
 * </ul>
 *
 * <p>매칭 실패 시 즉시 false 반환 (단락 평가).
 */
final class WhenClauseMatcher {

    private static final Pattern VAR = Pattern.compile("\\$\\{([\\w.]+)}");

    private WhenClauseMatcher() {}

    static boolean matches(Map<String, Object> when, PolicyContext ctx) {
        for (var entry : when.entrySet()) {
            if (!matchOne(entry.getKey(), entry.getValue(), ctx)) {
                return false;
            }
        }
        return true;
    }

    private static boolean matchOne(String key, Object expected, PolicyContext ctx) {
        Object actual = resolveAttribute(key, ctx);
        Object resolvedExpected = resolveVariable(expected, ctx);

        if (resolvedExpected instanceof Map<?, ?> matcher) {
            return matchWithMatcher(actual, matcher, ctx);
        }
        if (resolvedExpected instanceof Collection<?> list) {
            return list.stream().anyMatch(v -> equalsCoerced(actual, resolveVariable(v, ctx)));
        }
        return equalsCoerced(actual, resolvedExpected);
    }

    private static Object resolveAttribute(String key, PolicyContext ctx) {
        return switch (firstToken(key)) {
            case "subject" -> subjectAttr(rest(key), ctx.subject());
            case "resource" -> resourceAttr(rest(key), ctx.resource());
            case "action" -> ctx.action().name();
            case "env" -> envAttr(rest(key), ctx.environment());
            default -> null;
        };
    }

    private static Object subjectAttr(String name, Subject s) {
        return switch (name) {
            case "userId" -> s.userId() != null ? s.userId().toString() : null;
            case "role" -> s.role();
            case "tenantId" -> s.tenantId().toString();
            case "assignedEntityIds" -> s.assignedEntityIds();
            case "departmentId" -> s.departmentId() != null ? s.departmentId().toString() : null;
            default -> null;
        };
    }

    private static Object resourceAttr(String name, Resource r) {
        return switch (name) {
            case "type" -> r.type();
            case "tenantId" -> r.tenantId() != null ? r.tenantId().toString() : null;
            default -> r.attributes().get(name);
        };
    }

    private static Object envAttr(String name, PolicyContext.Environment e) {
        return switch (name) {
            case "requestIp" -> e.requestIp();
            case "mfaVerified" -> e.mfaVerified();
            case "lockedTenantIds" -> System.getProperty("esg.lockdown.tenants", "");
            default -> null;
        };
    }

    private static Object resolveVariable(Object value, PolicyContext ctx) {
        if (!(value instanceof String s)) return value;
        Matcher m = VAR.matcher(s);
        if (!m.matches()) return value;
        return resolveAttribute(m.group(1), ctx);
    }

    private static boolean matchWithMatcher(Object actual, Map<?, ?> matcher, PolicyContext ctx) {
        for (var e : matcher.entrySet()) {
            String op = String.valueOf(e.getKey());
            Object v = resolveVariable(e.getValue(), ctx);
            switch (op) {
                case "contains" -> {
                    if (!(actual instanceof Collection<?> coll)) return false;
                    if (coll.stream().noneMatch(x -> equalsCoerced(x, v))) return false;
                }
                case "in" -> {
                    if (v instanceof Collection<?> coll) {
                        if (coll.stream().noneMatch(x -> equalsCoerced(actual, resolveVariable(x, ctx)))) return false;
                    } else if (v instanceof String s && !s.isEmpty()) {
                        // env.lockedTenantIds 처럼 CSV 문자열인 경우
                        String[] parts = s.split(",");
                        boolean any = false;
                        for (String part : parts) {
                            if (equalsCoerced(actual, part.trim())) { any = true; break; }
                        }
                        if (!any) return false;
                    } else {
                        return false;
                    }
                }
                default -> { return false; }
            }
        }
        return true;
    }

    private static boolean equalsCoerced(Object a, Object b) {
        if (a == null || b == null) return a == b;
        if (a instanceof UUID ua) return ua.toString().equals(b.toString());
        if (b instanceof UUID ub) return ub.toString().equals(a.toString());
        return a.toString().equals(b.toString());
    }

    private static String firstToken(String key) {
        int dot = key.indexOf('.');
        return dot < 0 ? key : key.substring(0, dot);
    }

    private static String rest(String key) {
        int dot = key.indexOf('.');
        return dot < 0 ? "" : key.substring(dot + 1);
    }
}
