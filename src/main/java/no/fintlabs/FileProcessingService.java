package no.fintlabs;

import no.fintlabs.model.egrunnerverv.EgrunnervervDocument;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FileProcessingService {

    public UUID processFile(EgrunnervervDocument egrunnervervDocument) {
        return UUID.randomUUID();
    }
}
