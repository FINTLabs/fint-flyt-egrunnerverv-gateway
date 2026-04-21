package no.novari.flyt.egrunnerverv.gateway.infrastructure.dispatch.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "instance_headers_entity")
class InstanceHeadersEntity(
    @Id
    @Column(name = "source_application_instance_id")
    var sourceApplicationInstanceId: String = "",
    @Column(name = "source_application_integration_id")
    var sourceApplicationIntegrationId: String = "",
    @Column(name = "archive_instance_id")
    var archiveInstanceId: String = "",
)
