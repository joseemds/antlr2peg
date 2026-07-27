# syntax=docker/dockerfile:1

FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /src
COPY converter/ converter/

RUN cd converter \
    && chmod +x ./gradlew \
    && ./gradlew dependencies --no-daemon \
    && ./gradlew shadowJar --no-daemon

FROM eclipse-temurin:21-jdk-jammy AS dev

RUN apt-get update && apt-get install -y --no-install-recommends \
        lua5.3 \
        liblua5.3-dev \
        luarocks \
        build-essential \
        make \
        git \
        python3 \
        python3-pip \
    && luarocks install lpeglabel \
    && pip3 install --no-cache-dir grammarinator==26.1 \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /opt/antlr2peg

COPY . .

RUN chmod +x ./antlr2peg converter/gradlew \
    && make deps \
    && make jar

ENV PATH="/opt/antlr2peg:${PATH}"

CMD ["/bin/bash"]

FROM eclipse-temurin:21-jre-jammy AS runtime

RUN apt-get update && apt-get install -y --no-install-recommends \
        lua5.3 \
        liblua5.3-dev \
        luarocks \
        build-essential \
        make \
        python3 \
        python3-pip \
    && luarocks install lpeglabel \
    && pip3 install --no-cache-dir grammarinator==26.1 \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /opt/antlr2peg

COPY --from=build /src/converter/build/libs/antlr2peg.jar converter/build/libs/antlr2peg.jar

COPY antlr2peg ./antlr2peg
RUN chmod +x ./antlr2peg

COPY Makefile .
COPY scripts/ scripts/

ENV PATH="/opt/antlr2peg:${PATH}"

WORKDIR /data
ENTRYPOINT ["/opt/antlr2peg/antlr2peg"]
CMD ["--help"]
