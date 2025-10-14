package no.fintlabs.dispatch.converting;

import no.fint.model.felles.basisklasser.Begrep;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.administrasjon.personal.PersonalressursResource;
import no.fint.model.resource.arkiv.kodeverk.JournalStatusResource;
import no.fint.model.resource.arkiv.kodeverk.JournalpostTypeResource;
import no.fint.model.resource.arkiv.kodeverk.SkjermingshjemmelResource;
import no.fint.model.resource.arkiv.kodeverk.TilgangsrestriksjonResource;
import no.fint.model.resource.arkiv.noark.*;
import no.fint.model.resource.felles.PersonResource;
import no.fintlabs.cache.FintCache;
import no.fintlabs.dispatch.model.JournalpostReceipt;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static no.fintlabs.ResourceLinkUtil.getOptionalFirstLink;
import static no.fintlabs.dispatch.converting.InstanceHeadersEntityToInstanceReceiptDispatchEntityConvertingService.EGRUNNERVERV_DATETIME_FORMAT;

@Service
public class JournalpostToInstanceReceiptDispatchEntityConvertingService {

    private final FintCache<String, AdministrativEnhetResource> administrativEnhetResourceCache;
    private final FintCache<String, ArkivressursResource> arkivressursResourceCache;
    private final FintCache<String, JournalStatusResource> journalStatusResourceCache;
    private final FintCache<String, JournalpostTypeResource> journalpostTypeResourceCache;
    private final FintCache<String, TilgangsrestriksjonResource> tilgangsrestriksjonResourceCache;
    private final FintCache<String, SkjermingshjemmelResource> skjermingshjemmelResourceCache;
    private final FintCache<String, PersonalressursResource> personalressursResourceCache;
    private final FintCache<String, PersonResource> personResourceCache;

    public JournalpostToInstanceReceiptDispatchEntityConvertingService(
            FintCache<String, AdministrativEnhetResource> administrativEnhetResourceCache,
            FintCache<String, ArkivressursResource> arkivressursResourceCache,
            FintCache<String, JournalStatusResource> journalStatusResourceCache,
            FintCache<String, JournalpostTypeResource> journalpostTypeResourceCache,
            FintCache<String, TilgangsrestriksjonResource> tilgangsrestriksjonResourceCache,
            FintCache<String, SkjermingshjemmelResource> skjermingshjemmelResourceCache,
            FintCache<String, PersonalressursResource> personalressursResourceCache,
            FintCache<String, PersonResource> personResourceCache
    ) {
        this.administrativEnhetResourceCache = administrativEnhetResourceCache;
        this.arkivressursResourceCache = arkivressursResourceCache;
        this.journalStatusResourceCache = journalStatusResourceCache;
        this.journalpostTypeResourceCache = journalpostTypeResourceCache;
        this.tilgangsrestriksjonResourceCache = tilgangsrestriksjonResourceCache;
        this.skjermingshjemmelResourceCache = skjermingshjemmelResourceCache;
        this.personalressursResourceCache = personalressursResourceCache;
        this.personResourceCache = personResourceCache;
    }


    public JournalpostReceipt map(SakResource sakResource, Long journalpostNummer) {

        Optional<PersonalressursResource> saksansvarligPersonalressursResource =
                getOptionalFirstLink(sakResource::getSaksansvarlig)
                        .flatMap(arkivressursResourceCache::getOptional)
                        .flatMap(arkivressurs -> getOptionalFirstLink(arkivressurs::getPersonalressurs))
                        .flatMap(personalressursResourceCache::getOptional);

        Optional<PersonResource> saksansvarligPersonResource =
                saksansvarligPersonalressursResource
                        .flatMap(personalressursResource -> getOptionalFirstLink(personalressursResource::getPerson))
                        .flatMap(personResourceCache::getOptional);

        JournalpostResource journalpostResource = sakResource.getJournalpost()
                .stream()
                .filter(journalpost -> Objects.equals(journalpost.getJournalPostnummer(), journalpostNummer))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException("No journalpost with journalpostNummer=" + journalpostNummer)
                );

        Optional<AdministrativEnhetResource> administrativEnhetResource =
                getOptionalFirstLink(journalpostResource::getAdministrativEnhet)
                        .flatMap(administrativEnhetResourceCache::getOptional);

        Optional<JournalStatusResource> journalStatusResource =
                getOptionalFirstLink(journalpostResource::getJournalstatus)
                        .flatMap(journalStatusResourceCache::getOptional);

        Optional<JournalpostTypeResource> journalpostTypeResource =
                getOptionalFirstLink(journalpostResource::getJournalposttype)
                        .flatMap(journalpostTypeResourceCache::getOptional);

        Optional<SkjermingResource> skjermingResource = Optional.ofNullable(journalpostResource.getSkjerming());

        Optional<TilgangsrestriksjonResource> tilgangsrestriksjonResource =
                skjermingResource.flatMap(sr -> getOptionalFirstLink(sr::getTilgangsrestriksjon))
                        .flatMap(tilgangsrestriksjonResourceCache::getOptional);

        Optional<SkjermingshjemmelResource> skjermingshjemmelResource =
                skjermingResource.flatMap(sr -> getOptionalFirstLink(sr::getSkjermingshjemmel))
                        .flatMap(skjermingshjemmelResourceCache::getOptional);

        JournalpostReceipt.JournalpostReceiptBuilder builder =
                JournalpostReceipt
                        .builder()
                        .journalpostnr(
                                sakResource.getMappeId().getIdentifikatorverdi() +
                                        "-" +
                                        journalpostResource.getJournalPostnummer().toString()
                        )
                        .tittel(journalpostResource.getTittel())
                        .dokumentdato(
                                Optional.ofNullable(
                                        journalpostResource.getOpprettetDato()
                                ).map(opprettetDato -> opprettetDato
                                        .toInstant()
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDateTime()
                                        .format(DateTimeFormatter.ofPattern(EGRUNNERVERV_DATETIME_FORMAT))
                                ).orElse(null)
                        );

        journalStatusResource
                .map(Begrep::getSystemId)
                .map(Identifikator::getIdentifikatorverdi)
                .ifPresent(builder::statusId);

        tilgangsrestriksjonResource
                .map(Begrep::getKode)
                .ifPresent(builder::tilgangskode);

        skjermingshjemmelResource
                .map(Begrep::getSystemId)
                .map(Identifikator::getIdentifikatorverdi)
                .ifPresent(builder::hjemmel);

        journalpostTypeResource
                .map(Begrep::getSystemId)
                .map(Identifikator::getIdentifikatorverdi)
                .ifPresent(builder::dokumenttypeid);

        journalpostTypeResource
                .map(Begrep::getNavn)
                .ifPresent(builder::dokumenttypenavn);

        saksansvarligPersonalressursResource
                .map(PersonalressursResource::getBrukernavn)
                .map(Identifikator::getIdentifikatorverdi)
                .ifPresent(builder::saksansvarligbrukernavn);

        saksansvarligPersonResource
                .map(resource -> Stream.of(
                                        resource.getNavn().getFornavn(),
                                        resource.getNavn().getMellomnavn(),
                                        resource.getNavn().getEtternavn()
                                ).filter(Objects::nonNull)
                                .collect(Collectors.joining(" "))
                )
                .ifPresent(builder::saksansvarlignavn);

        administrativEnhetResource
                .map(AdministrativEnhetResource::getSystemId)
                .map(Identifikator::getIdentifikatorverdi)
                .ifPresent(builder::adminenhetkortnavn);

        administrativEnhetResource
                .map(AdministrativEnhetResource::getNavn)
                .ifPresent(builder::adminenhetnavn);

        return builder.build();
    }

}
