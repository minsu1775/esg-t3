package ai.claudecode.esgt3.governance;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

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
}
