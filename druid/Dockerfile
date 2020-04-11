FROM openjdk:8

ENV DRUID_VERSION 0.17.1

# Get Druid
RUN mkdir -p /tmp \
    && cd /tmp/ \
    && curl -fsLS "https://downloads.apache.org/druid/$DRUID_VERSION/apache-druid-$DRUID_VERSION-bin.tar.gz" | tar xvz \
    && mv apache-druid-$DRUID_VERSION /opt/druid

WORKDIR /opt/druid/

# Expose ports:
# - 8888: HTTP (router)
# - 8081: HTTP (coordinator)
# - 8082: HTTP (broker)
# - 8083: HTTP (historical)
# - 8090: HTTP (overlord)
# - 2181 2888 3888: ZooKeeper
EXPOSE 8888
EXPOSE 8081
EXPOSE 8082
EXPOSE 8083
EXPOSE 8090
EXPOSE 2181 2888 3888

ENTRYPOINT ./bin/start-nano-quickstart
