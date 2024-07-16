FROM mcr.microsoft.com/java/jre:8u192-zulu-alpine as builder

WORKDIR /data/projects/fate-serving/serving-server/

USER root

ARG version

COPY fate-serving-server/target/fate-serving-server-${version}-release.zip ./

RUN unzip fate-serving-server-${version}-release.zip && \
    rm fate-serving-server-${version}-release.zip && \
    ln -s fate-serving-server-*.jar fate-serving-server.jar

FROM mcr.microsoft.com/java/jre:8u192-zulu-centos

WORKDIR /data/projects/fate-serving/serving-server/

COPY --from=builder /data/projects/fate-serving/serving-server  /data/projects/fate-serving/serving-server

USER root

EXPOSE 8000

CMD java -cp conf/:lib/*:extension/*:fate-serving-server.jar com.webank.ai.fate.serving.Bootstrap -c conf/serving-server.properties
