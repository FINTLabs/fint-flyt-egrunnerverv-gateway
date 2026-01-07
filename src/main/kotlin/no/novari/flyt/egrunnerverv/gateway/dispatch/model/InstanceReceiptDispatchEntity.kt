package no.novari.flyt.egrunnerverv.gateway.dispatch.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
class InstanceReceiptDispatchEntity(
    @Id
    var sourceApplicationInstanceId: String = "",
    var uri: String = "",
    var classType: Class<*>? = null,
    @Column(columnDefinition = "TEXT")
    var instanceReceipt: String = "",
)
