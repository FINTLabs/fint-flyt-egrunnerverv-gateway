package no.novari.flyt.egrunnerverv.gateway.dispatch.repository;

import no.novari.flyt.egrunnerverv.gateway.dispatch.model.InstanceReceiptDispatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstanceReceiptDispatchRepository extends JpaRepository<InstanceReceiptDispatchEntity, String> {
}
