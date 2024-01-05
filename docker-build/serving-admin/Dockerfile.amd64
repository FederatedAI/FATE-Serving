FROM mcr.microsoft.com/java/jre:8u192-zulu-alpine as builder


WORKDIR /data/projects/fate-serving/serving-admin/
USER root

ARG version

COPY fate-serving-admin/target/fate-serving-admin-${version}-release.zip ./

RUN unzip fate-serving-admin-${version}-release.zip && \
    rm fate-serving-admin-${version}-release.zip && \
    ln -s fate-serving-admin-*.jar fate-serving-admin.jar

FROM mcr.microsoft.com/java/jre:8u192-zulu-centos

WORKDIR /data/projects/fate-serving/serving-admin/

COPY --from=builder /data/projects/fate-serving/serving-admin  /data/projects/fate-serving/serving-admin

USER root

EXPOSE 8350

CMD java -Dspring.config.location=conf/application.properties -cp "conf/:lib/*:fate-serving-admin.jar" com.webank.ai.fate.serving.admin.Bootstrap