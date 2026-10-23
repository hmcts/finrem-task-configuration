package uk.gov.hmcts.reform.finrem.taskconfiguration;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.dmn.engine.DmnEngine;
import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.impl.DefaultDmnEngineConfiguration;
import org.camunda.bpm.dmn.feel.impl.scala.ScalaFeelEngine;
import org.camunda.bpm.dmn.feel.impl.scala.function.CustomFunctionTransformer;
import org.camunda.bpm.engine.variable.context.VariableContext;
import org.camunda.feel.valuemapper.ValueMapper;
import org.junit.jupiter.api.BeforeEach;
import uk.gov.hmcts.reform.finrem.taskconfiguration.utils.MockFeelClock;

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

        // Setting a ScalaFeelEngineFactory adapter so that the engine can be created to use the MockFeelClock.
        defaultDmnEngineConfiguration.setFeelEngineFactory(new org.camunda.bpm.dmn.feel.impl.scala.ScalaFeelEngineFactory() {
            @Override
            public org.camunda.bpm.dmn.feel.impl.FeelEngine createInstance() {
                return new ScalaFeelEngine(this.customFunctionProviders){

                    @Override
                    public <T> T evaluateSimpleExpression(String s, VariableContext variableContext) {
                        return super.evaluateSimpleExpression(s, variableContext);
                    }

                    @Override
                    public boolean evaluateSimpleUnaryTests(String s, String s1, VariableContext variableContext) {
                        return super.evaluateSimpleUnaryTests(s, s1, variableContext);
                    }

                    @Override
                    protected org.camunda.feel.FeelEngine buildFeelEngine(CustomFunctionTransformer transformer, ValueMapper.CompositeValueMapper valueMapper) {
                        return new org.camunda.feel.FeelEngine.Builder()
                            .functionProvider(transformer)
                            .valueMapper(valueMapper)
                            .enableExternalFunctions(false)
                            .clock(new MockFeelClock())
                            .build();
                    }
                };
            }
        });

        dmnEngine = defaultDmnEngineConfiguration.buildEngine();
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = contextClassLoader.getResourceAsStream(currentDmnDecisionTable.getFileName());
        decision = dmnEngine.parseDecision(currentDmnDecisionTable.getKey(), inputStream);
    }

    public DmnDecisionTableResult evaluateDmnTable(Map<String, Object> variables) {
        return dmnEngine.evaluateDecisionTable(decision, variables);
    }
}
