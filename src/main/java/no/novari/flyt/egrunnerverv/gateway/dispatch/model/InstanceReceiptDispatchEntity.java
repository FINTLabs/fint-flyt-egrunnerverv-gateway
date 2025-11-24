package no.novari.flyt.egrunnerverv.gateway.dispatch.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InstanceReceiptDispatchEntity {
    @Id
    private String sourceApplicationInstanceId;
    private String uri;
    private Class<?> classType;
    @Column(columnDefinition = "TEXT")
    private String instanceReceipt;
}
