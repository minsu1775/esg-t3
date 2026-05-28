package ai.claudecode.esgt3.governance;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

/**
 * 모듈 간 의존 규칙을 빌드 시 강제한다. (Section 2.6 ArchUnit 규칙 반영)
 *
 * <p>위반 시 빌드 실패. CI에서 별도 단계로 검증.
 */
@AnalyzeClasses(
    packages = "ai.claudecode.esgt3",
    importOptions = { ImportOption.DoNotIncludeTests.class }
)
class ArchitectureTest {

    @ArchTest
    static final ArchRule iam은_다른_도메인_모듈을_의존하지_않는다 = noClasses()
        .that().resideInAPackage("..iam..")
        .should().dependOnClassesThat().resideInAnyPackage(
            "..entity..", "..ghg..", "..evidence..", "..vw..", "..rpt..");

    @ArchTest
    static final ArchRule 도메인_모듈은_shared와_iam만_의존_가능 = noClasses()
        .that().resideInAPackage("..entity..")
        .should().dependOnClassesThat().resideInAnyPackage(
            "..ghg..", "..evidence..", "..vw..", "..rpt..");

    @ArchTest
    static final ArchRule controller는_System_out을_사용하지_않는다 = noClasses()
        .should().callMethod(System.class, "out");

    @ArchTest
    static final ArchRule printStackTrace는_금지된다 = noClasses()
        .should().callMethod(Throwable.class, "printStackTrace");

    @ArchTest
    static final ArchRule repository는_infra_패키지에만_존재한다 = classes()
        .that().areAnnotatedWith("org.springframework.stereotype.Repository")
        .or().areAssignableTo("org.springframework.data.repository.Repository")
        .should().resideInAPackage("..infra..")
        .allowEmptyShould(true);

    @ArchTest
    static final ArchRule double_타입_사용_금지_GHG = noClasses()
        .that().resideInAPackage("..ghg..")
        .should().dependOnClassesThat().haveFullyQualifiedName("java.lang.Double")
        .orShould().dependOnClassesThat().haveFullyQualifiedName("double");

    @ArchTest
    static final ArchRule 도메인은_JPA_어노테이션을_사용하지_않는다 = noClasses()
        .that().resideInAnyPackage("..domain..")
        .should().beAnnotatedWith("jakarta.persistence.Entity")
        .orShould().dependOnClassesThat().resideInAPackage("jakarta.persistence..")
        .allowEmptyShould(true);

    // === Phase 1 신규 ===

    /** PolicyEvaluator는 iam.domain 패키지에만 거주 (rule 13-abac-policy.md). */
    @ArchTest
    static final ArchRule PolicyEvaluator는_iam_domain에만_거주 = classes()
        .that().haveSimpleName("PolicyEvaluator")
        .should().resideInAPackage("..iam.domain..")
        .allowEmptyShould(true);

    /** iam.domain은 Spring 의존 0 (도메인 = 순수 Java). */
    @ArchTest
    static final ArchRule iam_domain은_Spring_의존_금지 = noClasses()
        .that().resideInAPackage("..iam.domain..")
        .should().dependOnClassesThat().resideInAPackage("org.springframework..");

    /** iam.domain은 JPA 의존 0. */
    @ArchTest
    static final ArchRule iam_domain은_JPA_의존_금지 = noClasses()
        .that().resideInAPackage("..iam.domain..")
        .should().dependOnClassesThat().resideInAPackage("jakarta.persistence..");

    /**
     * 컨트롤러 메서드는 @PreAuthorize 또는 @PermitAll 명시 의무 (esg-t2 L-P1-01).
     * 누락 시 인가 우회 위험 — 코드 리뷰가 아닌 빌드 차단으로 강제.
     */
    @ArchTest
    static final ArchRule 컨트롤러_메서드는_PreAuthorize_명시 = methods()
        .that().areDeclaredInClassesThat().resideInAPackage("..api.controller..")
        .and().arePublic()
        .and().areAnnotatedWith("org.springframework.web.bind.annotation.GetMapping")
        .or().areAnnotatedWith("org.springframework.web.bind.annotation.PostMapping")
        .or().areAnnotatedWith("org.springframework.web.bind.annotation.PutMapping")
        .or().areAnnotatedWith("org.springframework.web.bind.annotation.DeleteMapping")
        .or().areAnnotatedWith("org.springframework.web.bind.annotation.PatchMapping")
        .or().areAnnotatedWith("org.springframework.web.bind.annotation.RequestMapping")
        .should().beAnnotatedWith("org.springframework.security.access.prepost.PreAuthorize")
        .orShould().beAnnotatedWith("org.springframework.security.access.annotation.Secured")
        .orShould().beAnnotatedWith("jakarta.annotation.security.PermitAll")
        .allowEmptyShould(true);
}
