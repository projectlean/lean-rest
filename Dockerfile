FROM tomee:9.0.0.RC1-alpine
LABEL maintainer="Lean Team"

# RUN apt update && apt install -y zip unzip

RUN rm -rf /usr/local/tomee/webapps/ROOT
COPY target/lean-rest-0.0.2-SNAPSHOT.war /usr/local/tomee/webapps/ROOT.war

# RUN cd /usr/local/tomee/webapps/ && unzip ROOT.war
RUN mkdir /config
RUN mkdir /lean

COPY docker/leanrest.properties /config