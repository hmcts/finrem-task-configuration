package uk.gov.hmcts.reform.finrem.taskconfiguration;

import com.fasterxml.jackson.annotation.JsonValue;

public enum DmnDecisionTable {

    WA_TASK_ALLOWED_DAYS_DIVORCE_FINREM_CONTESTED(
        "wa-task-allowed-days-divorce-finrem-contested",
        "wa-task-allowed-days-divorce-finrem-contested.dmn"
    ),
    WA_TASK_CANCELLATION_DIVORCE_FINREM_CONTESTED(
        "wa-task-cancellation-divorce-finrem-contested",
        "wa-task-cancellation-divorce-finrem-contested.dmn"
    ),
    WA_TASK_COMPLETION_DIVORCE_FINREM_CONTESTED(
        "wa-task-completion-divorce-finrem-contested",
        "wa-task-completion-divorce-finrem-contested.dmn"
    ),
    WA_TASK_CONFIGURATION_DIVORCE_FINREM_CONTESTED(
        "wa-task-configuration-divorce-finrem-contested",
        "wa-task-configuration-divorce-finrem-contested.dmn"
    ),
    WA_TASK_INITIATION_DIVORCE_FINREM_CONTESTED(
        "wa-task-initiation-divorce-finrem-contested",
        "wa-task-initiation-divorce-finrem-contested.dmn"
    ),
    WA_TASK_PERMISSIONS_DIVORCE_FINREM_CONTESTED(
        "wa-task-permissions-divorce-finrem-contested",
        "wa-task-permissions-divorce-finrem-contested.dmn"
    ),
    WA_TASK_TYPES_DIVORCE_FINREM_CONTESTED(
        "wa-task-types-divorce-finrem-contested",
        "wa-task-types-divorce-finrem-contested.dmn"
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
