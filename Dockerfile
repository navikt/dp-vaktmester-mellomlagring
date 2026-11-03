FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-27@sha256:7d09316e4c2f653d7c8a28a76831403862ae23334da32ad3ff1775d5047ef186

ENV TZ="Europe/Oslo"

COPY build/install/*/lib /app/lib

ENTRYPOINT ["java", "-cp", "/app/lib/*", "no.nav.dagpenger.vaktmester.mellomlagring.AppKt"]