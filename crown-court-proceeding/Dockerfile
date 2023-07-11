FROM amazoncorretto:17
RUN mkdir -p /opt/laa-crown-court-proceeding/
WORKDIR /opt/laa-crown-court-proceeding/
COPY ./build/libs/crown-court-proceeding.jar /opt/laa-crown-court-proceeding/app.jar
#RUN addgroup -S appgroup && adduser -u 1001 -S appuser -G appgroup
USER 1001
EXPOSE 8087 8096 8081
CMD java -jar app.jar
