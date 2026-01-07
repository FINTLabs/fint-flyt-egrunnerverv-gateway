package no.novari.flyt.egrunnerverv.gateway.dispatch.repository

import no.novari.flyt.egrunnerverv.gateway.dispatch.model.InstanceHeadersEntity
import org.springframework.data.jpa.repository.JpaRepository

interface InstanceHeadersRepository : JpaRepository<InstanceHeadersEntity, String>
