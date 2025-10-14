package no.fintlabs.dispatch.model;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InstanceHeadersEntity {
    @Id
    private String sourceApplicationInstanceId;
    private String sourceApplicationIntegrationId;
    private String archiveInstanceId;
}
