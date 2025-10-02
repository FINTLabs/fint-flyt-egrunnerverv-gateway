package no.fintlabs.dispatch.model;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

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
