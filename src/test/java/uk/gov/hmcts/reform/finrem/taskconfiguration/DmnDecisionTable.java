package uk.gov.hmcts.reform.finrem.taskconfiguration;

import com.fasterxml.jackson.annotation.JsonValue;

public enum DmnDecisionTable {

    WA_TASK_ALLOWED_DAYS_DIVORCE_FINANCIALREMEDYMVP2(
        "wa-task-allowed-days-divorce-financialremedymvp2",
        "wa-task-allowed-days-divorce-financialremedymvp2.dmn"
    ),
    WA_TASK_CANCELLATION_DIVORCE_FINANCIALREMEDYMVP2(
        "wa-task-cancellation-divorce-financialremedymvp2",
        "wa-task-cancellation-divorce-financialremedymvp2.dmn"
    ),
    WA_TASK_COMPLETION_DIVORCE_FINANCIALREMEDYMVP2(
        "wa-task-completion-divorce-financialremedymvp2",
        "wa-task-completion-divorce-financialremedymvp2.dmn"
    ),
    WA_TASK_CONFIGURATION_DIVORCE_FINANCIALREMEDYMVP2(
        "wa-task-configuration-divorce-financialremedymvp2",
        "wa-task-configuration-divorce-financialremedymvp2.dmn"
    ),
    WA_TASK_INITIATION_DIVORCE_FINANCIALREMEDYMVP2(
        "wa-task-initiation-divorce-financialremedymvp2",
        "wa-task-initiation-divorce-financialremedymvp2.dmn"
    ),
    WA_TASK_PERMISSIONS_DIVORCE_FINANCIALREMEDYMVP2(
        "wa-task-permissions-divorce-financialremedymvp2",
        "wa-task-permissions-divorce-financialremedymvp2.dmn"
    ),
    WA_TASK_TYPES_DIVORCE_FINANCIALREMEDYMVP2(
        "wa-task-types-divorce-financialremedymvp2",
        "wa-task-types-divorce-financialremedymvp2.dmn"
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
