FROM dev.chuyuwater.cn:32003/cicd/openjdk:17-builder AS builder

# 设置工作目录
WORKDIR /app

# 复制 Maven 配置文件
COPY pom.xml .
# 复制源代码
COPY api ./api
COPY gateway ./gateway
COPY global ./global
COPY sdk ./sdk

# 编译打包
RUN mvn package -DskipTests

# 第二阶段：运行环境
FROM dev.chuyuwater.cn:32003/cicd/openjdk:17

# 设置工作目录
WORKDIR /app

# OTEL可观测性配置
ENV OTEL_SERVICE_NAME="auth-center"
ENV OTEL_EXPORTER_OTLP_ENDPOINT="https://dev.chuyuwater.cn:32007/api/default"
ENV OTEL_EXPORTER_OTLP_HEADERS="Authorization=Basic cm9vdEBleGFtcGxlLmNvbTpQYmx5bEExbXJ2OFhXYzE5"

# Maven中央仓库下载otel-agent
RUN curl https://repo1.maven.org/maven2/io/opentelemetry/javaagent/opentelemetry-javaagent/2.25.0/opentelemetry-javaagent-2.25.0.jar -o /app/opentelemetry-javaagent.jar

# 从 builder 阶段复制 jar 包
COPY --from=builder /app/api/target/*.jar app.jar

# 暴露端口
EXPOSE 8080

# 运行应用，可以被docker-compose.yaml或k8s中的启动参数覆盖
ENTRYPOINT ["java", "-javaagent:/app/opentelemetry-javaagent.jar", "-jar", "app.jar"]