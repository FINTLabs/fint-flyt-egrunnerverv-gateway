package no.novari.flyt.egrunnerverv.gateway.dispatch.repository;

import no.novari.flyt.egrunnerverv.gateway.dispatch.model.InstanceHeadersEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstanceHeadersRepository extends JpaRepository<InstanceHeadersEntity, String> {
}
