# FINT Flyt egrunnerverv gateway

Spring Boot WebFlux-tjeneste som kobler eGrunnerverv mot FINT Flyt.

Applikasjonen har to hovedansvar:
1. Ta imot innkommende instanser fra eGrunnerverv (`sak` og `journalpost`) og sende disse inn i Flyt/arkivløpet.
2. Lytte på ferdig arkiverte instanser, bygge kvitteringspayload, og oppdatere ServiceNow-tabeller med resultatinformasjon.

## Hva applikasjonen gjør

### Inngående flyt (eGrunnerverv -> Flyt instance-gateway)

- Eksternt API eksponeres av `EgrunnervervInstanceController`:
  - `POST /api/egrunnerverv/instances/{orgNr}/archive`
  - `POST /api/egrunnerverv/instances/{orgNr}/document?id={saksnummer}`
- Request mappes til `InstanceObject` gjennom:
  - `EgrunnervervSakInstanceMappingService`
  - `EgrunnervervJournalpostInstanceMappingService`
- Ved mapping:
  - E-post normaliseres.
  - Domenesjekk kan håndheves (`novari.flyt.egrunnerverv.checkEmailDomain`).
  - Saksansvarlig/saksbehandler kan valideres mot ressurs-cache.
  - Ved manglende arkivressurs kastes `ArchiveResourceNotFoundException` og Slack-varsling sendes.
- Ferdig mappeobjekt håndteres av `InstanceProcessor` fra `flyt-instance-gateway`.

### Utgående flyt (Flyt/arkiv -> ServiceNow)

- `InstanceConsumerConfiguration` lytter på event `instance-dispatched`.
- `DispatchService` filtrerer på `sourceApplicationId == 2` (eGrunnerverv).
- Relevante headers lagres i DB-tabell `instance_headers_entity`.
- Header konverteres til dispatch-entity (`InstanceReceiptDispatchEntity`) og lagres i `instance_receipt_dispatch_entity`.
- Kvitteringspayload bygges av:
  - `InstanceHeadersEntityToInstanceReceiptDispatchEntityConvertingService`
  - `JournalpostToInstanceReceiptDispatchEntityConvertingService`
  - `CaseRequestService` (request/reply mot `arkiv-noark-sak`)
- `WebClientRequestService` sender PATCH til ServiceNow.
- Retry-oppførsel:
  - `429` og `5xx`: beholdes for senere retry.
  - Andre HTTP-feil: raden slettes (terminal feil).

## Caching av FINT-ressurser

Applikasjonen bygger lokale `FintCache`-cacher for ressurser og kodeverk.

Konfigurasjon:
- `ResourceEntityCacheConfiguration` oppretter cache per ressurs.
- `ResourceEntityConsumersConfiguration` konsumerer entity-topics og fyller cache.

Ressurser som caches:
- `AdministrativEnhetResource`
- `ArkivressursResource`
- `TilgangsrestriksjonResource`
- `SkjermingshjemmelResource`
- `JournalStatusResource`
- `JournalpostTypeResource`
- `PersonalressursResource`
- `PersonResource`

Cachen brukes både ved:
1. Oppslag av arkivressurs fra e-post i innkommende mapping.
2. Oppslag av metadata ved bygging av journalpost-kvittering ut.

## Arkitektur (forenklet)

```mermaid
flowchart LR
    A["eGrunnerverv"] --> B["External API Controller"]
    B --> C["Instance Mapping + Validation"]
    C --> D["Flyt Instance Gateway / Arkiv"]
    D --> E["instance-dispatched event"]
    E --> F["DispatchService + DB queue"]
    F --> G["Case lookup + resource cache enrichment"]
    G --> H["PATCH ServiceNow"]
```

## Konfigurasjon

Viktigste properties i `application.yaml` og profilfiler:

| Property | Beskrivelse |
| --- | --- |
| `fint.application-id` | Applikasjons-ID (brukes blant annet i Kafka-oppsett). |
| `fint.org-id` | Org-id brukt i domenesjekk av e-post og i varslinger. |
| `novari.flyt.egrunnerverv.checkSaksansvarligEpost` | Slår opp/validerer saksansvarlig for `sak`. |
| `novari.flyt.egrunnerverv.checkSaksbehandler` | Slår opp/validerer saksbehandler for `journalpost`. |
| `novari.flyt.egrunnerverv.checkEmailDomain` | Verifiserer at e-postdomene matcher `fint.org-id`. |
| `novari.flyt.egrunnerverv.dispatch.base-url` | Base-URL til ServiceNow table API. |
| `novari.flyt.egrunnerverv.dispatch.token-uri` | OAuth token-endepunkt for ServiceNow. |
| `novari.flyt.egrunnerverv.dispatch.tablenameSak` | ServiceNow-tabell for sakskvittering. |
| `novari.flyt.egrunnerverv.dispatch.tablenameJournalpost` | ServiceNow-tabell for journalpostkvittering. |
| `novari.flyt.egrunnerverv.dispatch.instance-initial-delay` | Initial delay for periodisk dispatch-jobb. |
| `novari.flyt.egrunnerverv.dispatch.instance-fixed-delay` | Fast intervall for periodisk dispatch-jobb. |
| `slack.webhook.url` | Webhook for varsling ved manglende arkivressurs. |

Profiler som inkluderes som standard:
- `flyt-kafka`
- `flyt-logging`
- `flyt-postgres`
- `flyt-resource-server`
- `flyt-file-web-client`
- `flyt-service-now-web-client`

## Database

Flyway migrering i `src/main/resources/db/migration/V1__init.sql` oppretter:
- `instance_headers_entity`
- `instance_receipt_dispatch_entity`

Disse fungerer som en enkel, robust mellomlagring/retry-kø for utgående ServiceNow-dispatch.

## Kjør lokalt

Forutsetninger:
- Java 21+
- Kafka tilgjengelig lokalt (f.eks. `localhost:9092`)
- PostgreSQL
- Tilgang til nødvendige secrets/credentials for OAuth-klienter (ServiceNow og file-service)

### 1) Start PostgreSQL

```bash
./start-postgres
```

Dette starter en lokal container på `localhost:5441`.

### 2) Start applikasjonen

```bash
SPRING_PROFILES_ACTIVE=local-staging ./gradlew bootRun
```

`application-local-staging.yaml` inneholder lokale defaults, blant annet:
- `server.port=8102`
- lokal Postgres-tilkobling
- lokal Kafka bootstrap server

### 3) Kjør tester

```bash
./gradlew test
```

## Deployment

Kustomize-struktur:
- `kustomize/base/` for felles manifest
- `kustomize/overlays/<org>/<env>/` for miljøspesifikke overstyringer

Base-manifest (`kustomize/base/flais.yaml`) setter blant annet:
- health/readiness/liveness-prober
- Prometheus scrape mot `/actuator/prometheus`
- env/secrets for OAuth og Slack webhook

## Observability

- Health: `/actuator/health`
- Readiness: `/actuator/health/readiness`
- Liveness: `/actuator/health/liveness`
- Metrics: `/actuator/prometheus`

## Nyttige filer

- `src/main/java/no/novari/flyt/egrunnerverv/gateway/instance/EgrunnervervInstanceController.java`
- `src/main/java/no/novari/flyt/egrunnerverv/gateway/instance/mapping/EgrunnervervSakInstanceMappingService.java`
- `src/main/java/no/novari/flyt/egrunnerverv/gateway/instance/mapping/EgrunnervervJournalpostInstanceMappingService.java`
- `src/main/java/no/novari/flyt/egrunnerverv/gateway/dispatch/DispatchService.java`
- `src/main/java/no/novari/flyt/egrunnerverv/gateway/dispatch/converting/InstanceHeadersEntityToInstanceReceiptDispatchEntityConvertingService.java`
- `src/main/java/no/novari/flyt/egrunnerverv/gateway/configuration/ResourceEntityConsumersConfiguration.java`
