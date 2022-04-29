FROM mcr.microsoft.com/java/jre:8u192-zulu-alpine

WORKDIR /data/projects/fate/serving-server/

USER root

ARG version

COPY fate-serving-server/target/fate-serving-server-${version}-release.zip ./

RUN unzip fate-serving-server-${version}-release.zip && \
    rm fate-serving-server-${version}-release.zip && \
    ln -s fate-serving-server-*.jar fate-serving-server.jar

EXPOSE 8000

CMD java -cp conf/:lib/*:extension/*:fate-serving-server.jar com.webank.ai.fate.serving.Bootstrap -c conf/serving-server.properties
