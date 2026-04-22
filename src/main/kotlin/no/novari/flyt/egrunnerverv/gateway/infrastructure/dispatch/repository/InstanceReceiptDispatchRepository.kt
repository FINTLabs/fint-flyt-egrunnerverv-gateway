package no.novari.flyt.egrunnerverv.gateway.infrastructure.dispatch.repository

import no.novari.flyt.egrunnerverv.gateway.infrastructure.dispatch.model.InstanceReceiptDispatchEntity
import org.springframework.data.jpa.repository.JpaRepository

interface InstanceReceiptDispatchRepository : JpaRepository<InstanceReceiptDispatchEntity, String>
