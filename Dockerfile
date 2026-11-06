FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-27@sha256:e6ddefb805d6b56791f736c12e99d883b7a511f11dfd2d55cfb2532a9ae2e6c5

ENV TZ="Europe/Oslo"

COPY build/install/*/lib /app/lib

ENTRYPOINT ["java", "-cp", "/app/lib/*", "no.nav.dagpenger.vaktmester.mellomlagring.AppKt"]