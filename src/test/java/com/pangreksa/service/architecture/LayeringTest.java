package com.pangreksa.service.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.data.repository.Repository;
import org.springframework.stereotype.Service;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Architectural rules for pangreksa-service: a simple 3-layer split
 * (model + service, with shared utilities in {@code shared}). Presentation
 * lives in the consumer apps (pangreksa-be, pangreksa-web), never here.
 */
@AnalyzeClasses(packages = "com.pangreksa.service",
        importOptions = ImportOption.DoNotIncludeTests.class)
class LayeringTest {

    @ArchTest
    static final ArchRule no_vaadin =
            noClasses().that().resideInAPackage("com.pangreksa.service..")
                    .should().dependOnClassesThat().resideInAPackage("com.vaadin..");

    @ArchTest
    static final ArchRule no_web_or_security_web_or_poi =
            noClasses().that().resideInAPackage("com.pangreksa.service..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "org.springframework.web..",
                            "org.springframework.security.web..",
                            "org.apache.poi..");

    @ArchTest
    static final ArchRule model_does_not_depend_on_service =
            noClasses().that().resideInAPackage("com.pangreksa.service.model..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("com.pangreksa.service.service..");

    @ArchTest
    static final ArchRule services_are_annotated =
            classes().that().resideInAPackage("com.pangreksa.service.service..")
                    .and().areTopLevelClasses()
                    .and().areNotInterfaces()
                    .should().beAnnotatedWith(Service.class);

    @ArchTest
    static final ArchRule repos_extend_repository =
            classes().that().resideInAPackage("com.pangreksa.service.model.repo..")
                    .and().areInterfaces()
                    .should().beAssignableTo(Repository.class);
}
