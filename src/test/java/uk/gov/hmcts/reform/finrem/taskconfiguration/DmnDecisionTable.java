package uk.gov.hmcts.reform.finrem.taskconfiguration;

import com.fasterxml.jackson.annotation.JsonValue;

public enum DmnDecisionTable {

    WA_TASK_ALLOWED_DAYS_DIVORCE_FINANCIALREMEDY(
        "wa-task-allowed-days-divorce-financialremedy",
        "wa-task-allowed-days-divorce-financialremedy.dmn"
    ),
    WA_TASK_CANCELLATION_DIVORCE_FINANCIALREMEDY(
        "wa-task-cancellation-divorce-financialremedy",
        "wa-task-cancellation-divorce-financialremedy.dmn"
    ),
    WA_TASK_COMPLETION_DIVORCE_FINANCIALREMEDY(
        "wa-task-completion-divorce-financialremedy",
        "wa-task-completion-divorce-financialremedy.dmn"
    ),
    WA_TASK_CONFIGURATION_DIVORCE_FINANCIALREMEDY(
        "wa-task-configuration-divorce-financialremedy",
        "wa-task-configuration-divorce-financialremedy.dmn"
    ),
    WA_TASK_INITIATION_DIVORCE_FINANCIALREMEDY(
        "wa-task-initiation-divorce-financialremedy",
        "wa-task-initiation-divorce-financialremedy.dmn"
    ),
    WA_TASK_PERMISSIONS_DIVORCE_FINANCIALREMEDY(
        "wa-task-permissions-divorce-financialremedy",
        "wa-task-permissions-divorce-financialremedy.dmn"
    ),
    WA_TASK_TYPES_DIVORCE_FINANCIALREMEDY(
        "wa-task-types-divorce-financialremedy",
        "wa-task-types-divorce-financialremedy.dmn"
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
