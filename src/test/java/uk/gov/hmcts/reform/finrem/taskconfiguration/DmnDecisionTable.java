package uk.gov.hmcts.reform.finrem.taskconfiguration;

import com.fasterxml.jackson.annotation.JsonValue;

public enum DmnDecisionTable {

    WA_TASK_ALLOWED_DAYS_DIVORCE_FINREM_CONSENTED(
        "wa-task-allowed-days-divorce-finrem-consented",
        "wa-task-allowed-days-divorce-finrem-consented.dmn"
    ),
    WA_TASK_CANCELLATION_DIVORCE_FINREM_CONSENTED(
        "wa-task-cancellation-divorce-finrem-consented",
        "wa-task-cancellation-divorce-finrem-consented.dmn"
    ),
    WA_TASK_COMPLETION_DIVORCE_FINREM_CONSENTED(
        "wa-task-completion-divorce-finrem-consented",
        "wa-task-completion-divorce-finrem-consented.dmn"
    ),
    WA_TASK_CONFIGURATION_DIVORCE_FINREM_CONSENTED(
        "wa-task-configuration-divorce-finrem-consented",
        "wa-task-configuration-divorce-finrem-consented.dmn"
    ),
    WA_TASK_INITIATION_DIVORCE_FINREM_CONSENTED(
        "wa-task-initiation-divorce-finrem-consented",
        "wa-task-initiation-divorce-finrem-consented.dmn"
    ),
    WA_TASK_PERMISSIONS_DIVORCE_FINREM_CONSENTED(
        "wa-task-permissions-divorce-finrem-consented",
        "wa-task-permissions-divorce-finrem-consented.dmn"
    ),
    WA_TASK_TYPES_DIVORCE_FINREM_CONSENTED(
        "wa-task-types-divorce-finrem-consented",
        "wa-task-types-divorce-finrem-consented.dmn"
    );

    @JsonValue
    private final String key;
    private final String fileName;

    DmnDecisionTable(String key, String fileName) {
        this.key = key;
        this.fileName = fileName;
    }

    public String getKey() {
        return key;
    }

    public String getFileName() {
        return fileName;
    }
}
