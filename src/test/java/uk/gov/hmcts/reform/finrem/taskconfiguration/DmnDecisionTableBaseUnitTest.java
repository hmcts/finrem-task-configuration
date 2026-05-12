package uk.gov.hmcts.reform.finrem.taskconfiguration;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.dmn.engine.DmnEngine;
import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.impl.DefaultDmnEngineConfiguration;
import org.junit.jupiter.api.BeforeEach;

import java.io.InputStream;
import java.util.Map;

@Slf4j
public abstract class DmnDecisionTableBaseUnitTest {

    protected static DmnDecisionTable currentDmnDecisionTable;
    protected DmnEngine dmnEngine;
    protected DmnDecision decision;

    @BeforeEach
    void setUp() {
        DefaultDmnEngineConfiguration defaultDmnEngineConfiguration
            = (DefaultDmnEngineConfiguration) DmnEngineConfiguration.createDefaultDmnEngineConfiguration();

        dmnEngine = defaultDmnEngineConfiguration.buildEngine();
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = contextClassLoader.getResourceAsStream(currentDmnDecisionTable.getFileName());
        decision = dmnEngine.parseDecision(currentDmnDecisionTable.getKey(), inputStream);
    }

    public DmnDecisionTableResult evaluateDmnTable(Map<String, Object> variables) {
        return dmnEngine.evaluateDecisionTable(decision, variables);
    }
}
