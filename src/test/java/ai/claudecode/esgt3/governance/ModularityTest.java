package ai.claudecode.esgt3.governance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

import ai.claudecode.esgt3.EsgT3Application;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Spring Modulith 모듈 경계 자동 검증 + Event Catalog 문서 생성.
 *
 * <p>esg-t2 L-P0-06: 직하위 패키지는 @ApplicationModule 없어도 자동 모듈 인식.
 * esg-t3는 8개 모듈 모두 @ApplicationModule 명시.
 */
class ModularityTest {

    private static final ApplicationModules MODULES = ApplicationModules.of(EsgT3Application.class);

    @Test
    @DisplayName("모듈 8개가 등록된다")
    void 모듈_8개가_등록된다() {
        // iam, entity, audit, ghg, evidence, vw, rpt, shared
        assertThat(MODULES.stream().count()).isEqualTo(8L);
    }

    @Test
    @DisplayName("모듈 간 의존성 규칙을 위반하지 않는다")
    void 모듈_경계가_유효하다() {
        MODULES.verify();
    }

    @Test
    @DisplayName("Modulith documenter가 PlantUML/AsciiDoc Event Catalog를 생성한다")
    void event_catalog_문서가_생성된다() {
        new Documenter(MODULES)
            .writeModulesAsPlantUml()
            .writeIndividualModulesAsPlantUml()
            .writeDocumentation();
        // target/modulith-docs/ 에 산출물 생성됨
    }
}
