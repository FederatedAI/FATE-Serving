FROM mcr.microsoft.com/java/jre:8u192-zulu-alpine

WORKDIR /data/projects/fate/serving-proxy/

USER root

ARG version

COPY fate-serving-proxy/target/fate-serving-proxy-${version}-release.zip ./

RUN unzip fate-serving-proxy-${version}-release.zip && \
    rm fate-serving-proxy-${version}-release.zip && \
    ln -s fate-serving-proxy-*.jar fate-serving-proxy.jar

EXPOSE 8059 8869 8879

CMD java -Dspring.config.location=/data/projects/fate/serving-proxy/conf/application.properties -cp conf/:lib/*:fate-serving-proxy.jar com.webank.ai.fate.serving.proxy.bootstrap.Bootstrap -c conf/application.properties
