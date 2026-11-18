FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-27@sha256:5b89b27479236d36d96096e4666874574699b9bbb3edc2ea6029a21956d38321

ENV TZ="Europe/Oslo"

COPY build/install/*/lib /app/lib

ENTRYPOINT ["java", "-cp", "/app/lib/*", "no.nav.dagpenger.vaktmester.mellomlagring.AppKt"]