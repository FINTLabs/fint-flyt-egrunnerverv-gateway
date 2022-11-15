package no.fintlabs.caseinfo;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.administrasjon.personal.PersonalressursResource;
import no.fint.model.resource.arkiv.kodeverk.SaksstatusResource;
import no.fint.model.resource.arkiv.noark.AdministrativEnhetResource;
import no.fint.model.resource.arkiv.noark.ArkivressursResource;
import no.fint.model.resource.arkiv.noark.SakResource;
import no.fint.model.resource.felles.PersonResource;
import no.fintlabs.cache.FintCache;
import no.fintlabs.cache.exceptions.NoSuchCacheEntryException;
import no.fintlabs.codelist.links.NoSuchLinkException;
import no.fintlabs.model.fint.caseinfo.AdministrativeUnit;
import no.fintlabs.model.fint.caseinfo.CaseInfo;
import no.fintlabs.model.fint.caseinfo.CaseManager;
import no.fintlabs.model.fint.caseinfo.CaseStatus;
import org.springframework.stereotype.Service;

import static no.fintlabs.codelist.links.ResourceLinkUtil.getFirstLink;

@Slf4j
@Service
public class CaseInfoMappingService {

    private final FintCache<String, SaksstatusResource> saksstatusResourceCache;
    private final FintCache<String, AdministrativEnhetResource> administrativEnhetResourceCache;
    private final FintCache<String, ArkivressursResource> arkivressursResourceCache;
    private final FintCache<String, PersonalressursResource> personalressursResourceCache;
    private final FintCache<String, PersonResource> personResourceCache;

    public CaseInfoMappingService(
            FintCache<String, SaksstatusResource> saksstatusResourceCache,
            FintCache<String, AdministrativEnhetResource> administrativEnhetResourceCache,
            FintCache<String, ArkivressursResource> arkivressursResourceCache,
            FintCache<String, PersonalressursResource> personalressursResourceCache,
            FintCache<String, PersonResource> personResourceCache
    ) {
        this.saksstatusResourceCache = saksstatusResourceCache;
        this.administrativEnhetResourceCache = administrativEnhetResourceCache;
        this.arkivressursResourceCache = arkivressursResourceCache;
        this.personalressursResourceCache = personalressursResourceCache;
        this.personResourceCache = personResourceCache;
    }

    public CaseInfo toCaseInfo(String sourceApplicationInstanceId, SakResource caseResource) {
        return CaseInfo
                .builder()
                .sourceApplicationInstanceId(sourceApplicationInstanceId)
                .archiveCaseId(caseResource.getMappeId().getIdentifikatorverdi())
                .caseManager(getCaseManager(caseResource))
                .administrativeUnit(getAdministrativeUnit(caseResource))
                .status(getCaseStatus(caseResource))
                .build();
    }

    private CaseManager getCaseManager(SakResource caseResource) {
        try {
            ArkivressursResource archiveResourceResource = arkivressursResourceCache.get(
                    getFirstLink(
                            caseResource::getSaksansvarlig,
                            caseResource,
                            "Saksansvarlig"
                    )
            );
            PersonalressursResource personalResourceResource = personalressursResourceCache.get(
                    getFirstLink(
                            archiveResourceResource::getPersonalressurs,
                            archiveResourceResource,
                            "Personalressurs"
                    )
            );
            PersonResource personResource = personResourceCache.get(
                    getFirstLink(
                            personalResourceResource::getPerson,
                            personalResourceResource,
                            "Person"
                    )
            );
            return CaseManager
                    .builder()
                    .firstName(personResource.getNavn().getFornavn())
                    .middleName(personResource.getNavn().getMellomnavn())
                    .lastName(personResource.getNavn().getEtternavn())
                    .email(personResource.getKontaktinformasjon().getEpostadresse())
                    .phone(personResource.getKontaktinformasjon().getMobiltelefonnummer())
                    .build();
        } catch (NoSuchLinkException | NoSuchCacheEntryException e) {
            log.warn("No case manager for case with mappeId='" + caseResource.getMappeId().getIdentifikatorverdi() + "'", e);
            return null;
        }
    }

    private AdministrativeUnit getAdministrativeUnit(SakResource caseResource) {
        try {
            AdministrativEnhetResource administrativeUnitResource = administrativEnhetResourceCache.get(
                    getFirstLink(
                            caseResource::getAdministrativEnhet,
                            caseResource,
                            "AdministrativEnhet"
                    )
            );
            return AdministrativeUnit
                    .builder()
                    .name(administrativeUnitResource.getNavn())
                    .build();
        } catch (NoSuchLinkException | NoSuchCacheEntryException e) {
            log.warn("No administrative unit for case with mappeId='" + caseResource.getMappeId().getIdentifikatorverdi() + "'", e);
            return null;
        }
    }

    private CaseStatus getCaseStatus(SakResource caseResource) {
        try {
            SaksstatusResource caseStatusResource = saksstatusResourceCache.get(
                    getFirstLink(
                            caseResource::getSaksstatus,
                            caseResource,
                            "Saksstatus"
                    )
            );
            return CaseStatus
                    .builder()
                    .name(caseStatusResource.getNavn())
                    .code(caseStatusResource.getKode())
                    .build();
        } catch (NoSuchLinkException | NoSuchCacheEntryException e) {
            log.warn("No status for case with mappeId='" + caseResource.getMappeId().getIdentifikatorverdi() + "'", e);
            return null;
        }
    }

}
