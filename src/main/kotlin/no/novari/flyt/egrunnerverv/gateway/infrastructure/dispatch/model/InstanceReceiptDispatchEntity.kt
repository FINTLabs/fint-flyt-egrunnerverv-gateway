package no.novari.flyt.egrunnerverv.gateway.infrastructure.dispatch.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "instance_receipt_dispatch_entity")
class InstanceReceiptDispatchEntity(
    @Id
    @Column(name = "source_application_instance_id")
    var sourceApplicationInstanceId: String = "",
    @Column(name = "uri")
    var uri: String = "",
    @Column(name = "instance_receipt", columnDefinition = "TEXT")
    var instanceReceipt: String = "",
)
