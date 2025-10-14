package no.fintlabs.dispatch.repository;

import no.fintlabs.dispatch.model.InstanceReceiptDispatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstanceReceiptDispatchRepository extends JpaRepository<InstanceReceiptDispatchEntity, String> {
}
