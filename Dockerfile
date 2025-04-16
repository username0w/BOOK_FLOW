# 1. 베이스 이미지로 OpenJDK 21 사용
FROM openjdk:21-jdk-slim

# 2. 애플리케이션 JAR 파일을 컨테이너에 복사
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

# 3. 컨테이너가 시작될 때 실행할 명령어
ENTRYPOINT ["java", "-jar", "/app.jar"]
