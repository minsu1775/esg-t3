package ai.claudecode.esgt3.governance;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * 코딩 컨벤션 검사 — 이벤트 네이밍, 클래스 위치 등.
 */
@AnalyzeClasses(
    packages = "ai.claudecode.esgt3",
    importOptions = { ImportOption.DoNotIncludeTests.class }
)
class ConventionTest {

    /**
     * 모든 도메인 이벤트는 DomainEvent 마커를 구현해야 한다.
     * (Section 4.4 Event Catalog 네이밍 컨벤션)
     */
    @ArchTest
    static final ArchRule 도메인_이벤트는_DomainEvent_마커를_구현 = classes()
        .that().haveSimpleNameEndingWith("Created")
        .or().haveSimpleNameEndingWith("Updated")
        .or().haveSimpleNameEndingWith("Deleted")
        .or().haveSimpleNameEndingWith("Approved")
        .or().haveSimpleNameEndingWith("Rejected")
        .and().resideInAPackage("..event..")
        .should().beAssignableTo("ai.claudecode.esgt3.shared.event.DomainEvent")
        .allowEmptyShould(true);

    @ArchTest
    static final ArchRule 컨트롤러는_controller_패키지에 = classes()
        .that().haveSimpleNameEndingWith("Controller")
        .should().resideInAPackage("..api.controller..")
        .orShould().resideInAPackage("..api..")
        .allowEmptyShould(true);
}
