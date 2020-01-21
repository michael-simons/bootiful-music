FROM openjdk:11-jre-slim

RUN addgroup --system neo4j && adduser --no-create-home --system --home /var/lib/neo4j --group neo4j

ENV TINI_VERSION=v0.18.0
ADD https://github.com/krallin/tini/releases/download/${TINI_VERSION}/tini /usr/bin/tini
RUN chmod +x /usr/bin/tini

ENV NEO4J_SHA256=8d02047f3196c23849f7b6dda605b98098dbdc694747f1708e119715af3d3e06 \
    NEO4J_TARBALL=neo4j-enterprise-3.5.14-unix.tar.gz \
    NEO4J_EDITION=enterprise
ARG NEO4J_URI=http://dist.neo4j.org/neo4j-enterprise-3.5.14-unix.tar.gz
ARG NEO4J_APOC_URI=https://github.com/neo4j-contrib/neo4j-apoc-procedures/releases/download/3.5.0.7/apoc-3.5.0.7-all.jar
ARG NEO4J_ALGO_URI=https://github.com/neo4j-contrib/neo4j-graph-algorithms/releases/download/3.5.4.0/graph-algorithms-algo-3.5.4.0.jar

COPY ./local-package/* /tmp/
COPY ./install-apoc-and-algo.sh /tmp/

RUN apt-get update
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get install -y -q --no-install-recommends gosu bash curl \
    && curl --fail --silent --show-error --location --remote-name ${NEO4J_URI} \
    && curl --fail --silent --show-error --location -o /tmp/apoc.jar ${NEO4J_APOC_URI} \
    && curl --fail --silent --show-error --location -o /tmp/algo.jar ${NEO4J_ALGO_URI} \
    && echo "${NEO4J_SHA256}  ${NEO4J_TARBALL}" | sha256sum -cw - \
    && tar --extract --file ${NEO4J_TARBALL} --directory /var/lib \
    && mv /var/lib/neo4j-* /var/lib/neo4j \
    && rm ${NEO4J_TARBALL} \
    && mv /var/lib/neo4j/data /data \
    && chown -R neo4j:neo4j /data \
    && chmod -R 777 /data \
    && chown -R neo4j:neo4j /var/lib/neo4j \
    && chmod -R 777 /var/lib/neo4j \
    && ln -s /data /var/lib/neo4j/data \
    && apt-get purge -y curl

ENV PATH /var/lib/neo4j/bin:$PATH

WORKDIR /var/lib/neo4j

VOLUME /data

COPY docker-entrypoint.sh /docker-entrypoint.sh

EXPOSE 7474 7473 7687

ENTRYPOINT ["/usr/bin/tini", "-g", "--", "/docker-entrypoint.sh"]
CMD ["neo4j"]