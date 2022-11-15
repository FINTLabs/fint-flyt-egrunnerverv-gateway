package no.fintlabs;

import no.fintlabs.model.egrunnerverv.EgrunnervervDocument;
import no.fintlabs.model.egrunnerverv.EgrunnervervInstance;
import no.fintlabs.model.egrunnerverv.EgrunnervervInstanceElement;
import no.fintlabs.model.fint.instance.Document;
import no.fintlabs.model.fint.instance.Instance;
import no.fintlabs.model.fint.instance.InstanceField;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class EgrunnervervInstanceMapper {

    private final FileProcessingService fileProcessingService;

    public EgrunnervervInstanceMapper(FileProcessingService fileProcessingService) {
        this.fileProcessingService = fileProcessingService;
    }

    public Instance toInstance(EgrunnervervInstance egrunnervervInstance) {
        return Instance
                .builder()
                .sourceApplicationInstanceUri(egrunnervervInstance.getMetadata().getInstanceUri())
                .fieldPerKey(toFieldPerKey(egrunnervervInstance.getElements()))
                .documents(toDocuments(egrunnervervInstance.getDocuments()))
                .build();
    }

    private Map<String, InstanceField> toFieldPerKey(List<EgrunnervervInstanceElement> egrunnervervInstanceElements) {
        return egrunnervervInstanceElements
                .stream()
                .map(egrunnervervInstanceElement -> InstanceField
                        .builder()
                        .key(egrunnervervInstanceElement.getId())
                        .value(egrunnervervInstanceElement.getValue())
                        .build()
                )
                .collect(Collectors.toMap(
                        InstanceField::getKey,
                        Function.identity()
                ));
    }

    private List<Document> toDocuments(List<EgrunnervervDocument> egrunnervervDocuments) {
        return egrunnervervDocuments
                .stream()
                .map(this::toDocument)
                .collect(Collectors.toList());
    }

    private Document toDocument(EgrunnervervDocument egrunnervervDocument) {
        UUID fileId = fileProcessingService.processFile(egrunnervervDocument);
        return Document
                .builder()
                .name(egrunnervervDocument.getName())
                .type(egrunnervervDocument.getType())
                .encoding(egrunnervervDocument.getEncoding())
                .fileId(fileId)
                .build();
    }

}
