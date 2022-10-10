FROM openjdk:17-jdk-slim
RUN mkdir -p /opt/laa-crown-court-proceeding/
WORKDIR /opt/laa-crown-court-proceeding/
COPY ./build/libs/crown-court-proceeding.jar /opt/laa-crown-court-proceeding/app.jar
RUN useradd -r -u 1001 appuser
USER 1001
EXPOSE 8087 8096
ENTRYPOINT ["java","-jar","app.jar"]
