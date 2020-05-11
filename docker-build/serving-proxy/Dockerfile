FROM mcr.microsoft.com/java/jre:8u192-zulu-alpine

WORKDIR /data/projects/fate/serving-proxy/

ARG version

COPY serving-proxy/target/fate-serving-proxy-${version}-release.zip ./

RUN unzip fate-serving-proxy-${version}-release.zip && \
    rm fate-serving-proxy-${version}-release.zip && \
    ln -s fate-serving-proxy-*.jar fate-serving-proxy.jar

EXPOSE 8059 8869 8879

CMD java -Dspring.config.location=/home/vmware/serving/proxy/conf/application.properties -cp conf/:lib/*:fate-serving-proxy.jar com.webank.ai.fate.serving.proxy.bootstrap.Bootstrap -c conf/application.properties
