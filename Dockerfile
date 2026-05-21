ARG APP_INSIGHTS_AGENT_VERSION=3.5.4

# Application image

FROM hmctsprod.azurecr.io/base/java:21-distroless

COPY lib/AI-Agent.xml /opt/app/
COPY build/libs/finrem-task-configuration.jar /opt/app/

EXPOSE 4558
CMD [ "finrem-task-configuration.jar" ]
