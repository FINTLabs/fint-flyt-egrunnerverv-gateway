package no.novari.flyt.egrunnerverv.gateway.configuration

import no.fint.model.resource.administrasjon.personal.PersonalressursResource
import no.fint.model.resource.arkiv.kodeverk.JournalStatusResource
import no.fint.model.resource.arkiv.kodeverk.JournalpostTypeResource
import no.fint.model.resource.arkiv.kodeverk.SkjermingshjemmelResource
import no.fint.model.resource.arkiv.kodeverk.TilgangsrestriksjonResource
import no.fint.model.resource.arkiv.noark.AdministrativEnhetResource
import no.fint.model.resource.arkiv.noark.ArkivressursResource
import no.fint.model.resource.felles.PersonResource
import no.novari.cache.FintCache
import no.novari.cache.FintCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.Locale

@Configuration
class ResourceEntityCacheConfiguration(
    private val fintCacheManager: FintCacheManager,
) {
    @Bean
    fun administrativEnhetResourceCache(): FintCache<String, AdministrativEnhetResource> =
        createCache(AdministrativEnhetResource::class.java)

    @Bean
    fun arkivressursResourceCache(): FintCache<String, ArkivressursResource> =
        createCache(ArkivressursResource::class.java)

    @Bean
    fun tilgangsrestriksjonResourceCache(): FintCache<String, TilgangsrestriksjonResource> =
        createCache(TilgangsrestriksjonResource::class.java)

    @Bean
    fun skjermingshjemmelResourceCache(): FintCache<String, SkjermingshjemmelResource> =
        createCache(SkjermingshjemmelResource::class.java)

    @Bean
    fun journalstatusResourceCache(): FintCache<String, JournalStatusResource> =
        createCache(JournalStatusResource::class.java)

    @Bean
    fun journalposttypeResourceCache(): FintCache<String, JournalpostTypeResource> =
        createCache(JournalpostTypeResource::class.java)

    @Bean
    fun personalressursResourceCache(): FintCache<String, PersonalressursResource> =
        createCache(PersonalressursResource::class.java)

    @Bean
    fun personResourceCache(): FintCache<String, PersonResource> = createCache(PersonResource::class.java)

    private fun <V> createCache(resourceClass: Class<V>): FintCache<String, V> =
        fintCacheManager.createCache(
            resourceClass.name.lowercase(Locale.ROOT),
            String::class.java,
            resourceClass,
        )
}
