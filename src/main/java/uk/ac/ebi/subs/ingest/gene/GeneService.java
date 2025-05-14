package uk.ac.ebi.subs.ingest.gene;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GeneService {

    private final GeneRepository geneRepository;

    public Gene findBySymbol(String symbol) {
        return geneRepository.findByName(symbol);
    }

    public Gene findByHGNCId(String hgncId) {
        return geneRepository.findByHgncId(hgncId);
    }
}
