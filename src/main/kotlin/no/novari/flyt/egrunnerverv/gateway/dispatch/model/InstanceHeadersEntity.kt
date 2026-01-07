package no.novari.flyt.egrunnerverv.gateway.dispatch.model

import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
class InstanceHeadersEntity(
    @Id
    var sourceApplicationInstanceId: String = "",
    var sourceApplicationIntegrationId: String = "",
    var archiveInstanceId: String = "",
)
