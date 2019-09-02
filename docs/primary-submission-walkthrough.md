## Pre-requisites

### Set-up a service account

You must have a valid token identifying your service in order to use the Ingest API.

The DCP uses Google service accounts for authenticating service-to-service requests. To request a service account and associated RSA key-pair, create an issue in [Fusillade](https://github.com/HumanCellAtlas/fusillade), the DCP's auth service.

Use [this](https://github.com/HumanCellAtlas/dcplib/blob/master/dcplib/security/dcp_service_account.py) library with your key file to sign a JWT token identifying your service.

### Structure metadata

Metadata submitted to the DCP must adhere to standards described in the [metadata-schema project](https://github.com/HumanCellAtlas/metadata-schema/tree/master/json_schema).

## Discover 
* Query the collection managing submission envelopes, along with all other types of objects
```
curl -X GET \
  https://api.ingest.dev.data.humancellatlas.org/ \
```
* Returns `_links.submissionEnvelopes.href` -> /submissionEnvelopes

## Create Submission Envelope
```
curl -X POST \
  http://api.ingest.dev.data.humancellatlas.org/submissionEnvelopes \
  -H 'Authorization: Bearer $token \
  -H 'content-type: application/json' \
  -d '{}'
```

* Returns submission URL in `_links.self.href` -> `$sub_url`

## Get Submission Envelope for URLs to add metadata
```
curl -X GET \
  $sub_url \
```
e.g.
```
curl -X GET \
  http://api.ingest.dev.data.humancellatlas.org/submissionEnvelopes/5aa4ec8a1b41fe298594e531
```

* Example:
    * Return biomaterials URL: `_links.biomaterials.href` -> `/submissionEnvelopes/{$sub_id}/biomaterials`
    * Return processes URL: `_links.processes.href` -> `/submissionEnvelopes/{$sub_id}/processes`
    * Return files URL: `_links.files.href` -> `/submissionEnvelopes/{$sub_id}/files`
    * Return project URL: `_links.projects.href` -> `/submissionEnvelopes/{$sub_id}/projects`
    * Return protocols URL: `_links.protocols.href` -> `/submissionEnvelopes/{$sub_id}/protocols`

## Add Project to Submission Envelope
```
curl -X POST \
  http://api.ingest.dev.data.humancellatlas.org/submissionEnvelopes/$sub_url/projects \
  -H 'Authorization: Bearer $token' \
  -H 'content-type: application/json' \
  -d '{
    "describedBy" : "https://schema.humancellatlas.org/type/project/5.0.1/project",
    "schema_version" : "5.0.1",
    "schema_type" : "project",
    "project_core" : {
      "project_shortname" : "Q4_DEMO-project_PRJNA248302",
      "project_title" : "Q4_DEMO-Single cell RNA-seq of primary human glioblastomas",
      "project_description" : "Q4_DEMO-We report transcriptomes from 430 single glioblastoma cells isolated from 5 individual tumors and 102 single cells from gliomasphere cells lines generated using SMART-seq. In addition, we report population RNA-seq from the five tumors as well as RNA-seq from cell lines derived from 3 tumors (MGH26, MGH28, MGH31) cultured under serum free (GSC) and differentiated (DGC) conditions. This dataset highlights intratumoral heterogeneity with regards to the expression of de novo derived transcriptional modules and established subtype classifiers. Overall design: Operative specimens from five glioblastoma patients (MGH26, MGH28, MGH29, MGH30, MGH31) were acutely dissociated, depleted for CD45+ inflammatory cells and then sorted as single cells (576 samples). Population controls for each tumor were isolated by sorting 2000-10000 cells and processed in parallel (5 population control samples). Single cells from two established cell lines, GBM6 and GBM8, were also sorted as single cells (192 samples). SMART-seq protocol was implemented to generate single cell full length transcriptomes (modified from Shalek, et al Nature 2013) and sequenced using 25 bp paired end reads. Single cell cDNA libraries for MGH30 were resequenced using 100 bp paired end reads to allow for isoform and splice junction reconstruction (96 samples, annotated MGH30L). Cells were also cultured in serum free conditions to generate gliomasphere cell lines for MGH26, MGH28, and MGH31 (GSC) which were then differentiated using 10% serum (DGC). Population RNA-seq was performed on these samples (3 GSC, 3 DGC, 6 total). The initial dataset included 875 RNA-seq libraries (576 single glioblastoma cells, 96 resequenced MGH30L, 192 single gliomasphere cells, 5 tumor population controls, 6 population libraries from GSC and DGC samples). Data was processed as described below using RSEM for quantification of gene expression. 5,948 genes with the highest composite expression either across all single cells combined (average log2(TPM)>4.5) or within a single tumor (average log2(TPM)>6 in at least one tumor) were included. Cells expressing less than 2,000 of these 5,948 genes were excluded. The final processed dataset then included 430 primary single cell glioblastoma transcriptomes, 102 single cell transcriptomes from cell lines(GBM6,GBM8), 5 population controls (1 for each tumor), and 6 population libraries from cell lines derived from the tumors (GSC and DGC for MGH26, MGH28 and MGH31). The final matrix (GBM_data_matrix.txt) therefore contains 5948 rows (genes) quantified in 543 samples (columns). Please note that the samples which are not included in the data processing are indicated in the sample description field.",
      "describedBy" : "https://schema.humancellatlas.org/core/project/5.0.0/project_core",
      "schema_version" : "5.0.0"
    }
  }'
```
* Returns project URL in `_links.self.href` -> `$project_url`

## Add Biomaterial to Submission Envelope
```
curl -X POST \
  http://api.ingest.dev.data.humancellatlas.org/submissionEnvelopes/5aa4ec8a1b41fe298594e531/biomaterials \
  -H 'content-type: application/json' \
  -d '{
    "describedBy" : "https://schema.humancellatlas.org/type/biomaterial/5.0.0/donor_organism",
    "schema_version" : "5.0.0",
    "schema_type" : "biomaterial",
    "biomaterial_core" : {
      "biomaterial_id" : "Q4_DEMO-donor_MGH30",
      "biomaterial_name" : "Q4 DEMO donor MGH30",
      "ncbi_taxon_id" : [ 9606 ],
      "describedBy" : "https://schema.humancellatlas.org/core/biomaterial/5.0.0/biomaterial_core",
      "schema_version" : "5.0.0"
    },
    "genus_species" : [ {
      "describedBy" : "https://schema.humancellatlas.org/module/ontology/5.0.0/species_ontology",
      "text" : "Homo sapiens"
    } ],
    "is_living" : true,
    "biological_sex" : "unknown"
  }'
```
* Returns biomaterial URL in `_links.self.href` -> `$biomaterial_url`

## Add Process to Submission Envelope
```
curl -X POST \
  http://api.ingest.dev.data.humancellatlas.org/submissionEnvelopes/5aa4ec8a1b41fe298594e531/processes \
  -H 'content-type: application/json' \
  -d '{
    "describedBy" : "https://schema.humancellatlas.org/type/process/sequencing/5.0.0/library_preparation_process",
    "schema_version" : "5.0.0",
    "schema_type" : "process",
    "process_core" : {
      "process_id" : "preparation1",
      "describedBy" : "https://schema.humancellatlas.org/core/process/5.0.0/process_core",
      "schema_version" : "5.0.0"
    },
    "input_nucleic_acid_molecule" : {
      "describedBy" : "https://schema.humancellatlas.org/module/ontology/5.0.0/biological_macromolecule_ontology",
      "text" : "polyA RNA"
    },
    "library_construction_approach" : "Smart-seq2",
    "end_bias" : "5 prime end bias",
    "strand" : "unstranded",
    "process_type" : {
      "describedBy" : "https://schema.humancellatlas.org/module/ontology/5.0.0/process_type_ontology",
      "text" : "nucleic acid librarly construction process"
    }
  }'
```
* Returns process URL in `_links.self.href` -> `$process_url`

## Add Protocol to Submission Envelope
```
curl -X POST \
  http://api.ingest.dev.data.humancellatlas.org/submissionEnvelopes/5aa4ec8a1b41fe298594e531/processes \
  -H 'content-type: application/json' \
  -d '{
    "describedBy" : "https://schema.humancellatlas.org/type/process/sequencing/5.0.0/library_preparation_process",
    "schema_version" : "5.0.0",
    "schema_type" : "process",
    "process_core" : {
      "process_id" : "preparation1",
      "describedBy" : "https://schema.humancellatlas.org/core/process/5.0.0/process_core",
      "schema_version" : "5.0.0"
    },
    "input_nucleic_acid_molecule" : {
      "describedBy" : "https://schema.humancellatlas.org/module/ontology/5.0.0/biological_macromolecule_ontology",
      "text" : "polyA RNA"
    },
    "library_construction_approach" : "Smart-seq2",
    "end_bias" : "5 prime end bias",
    "strand" : "unstranded",
    "process_type" : {
      "describedBy" : "https://schema.humancellatlas.org/module/ontology/5.0.0/process_type_ontology",
      "text" : "nucleic acid librarly construction process"
    }
  }'
```
* Returns protocol URL in `_links.self.href` -> `$protocol_url`

## Add File to Submission Envelope
```
curl -X POST \
  http://api.ingest.dev.data.humancellatlas.org/submissionEnvelopes/5aa4ec8a1b41fe298594e531/files/R1.fastq.gz \
  -H 'content-type: application/json' \
  -d '{
    "describedBy" : "https://schema.humancellatlas.org/type/file/5.0.0/sequence_file",
    "schema_version" : "5.0.0",
    "schema_type" : "file",
    "file_core" : {
      "file_name" : "R1.fastq.gz",
      "file_format" : "fastq.gz",
      "describedBy" : "https://schema.humancellatlas.org/core/file/5.0.0/file_core",
      "schema_version" : "5.0.0"
    },
    "read_index" : "read1",
    "lane_index" : 1,
    "read_length" : 187
  }'
```
* Returns protocol URL in `_links.self.href` -> `$protocol_url`

# Finally, submit the Submission Envelope
```
curl -X PUT \
  http://api.ingest.dev.data.humancellatlas.org/submissionEnvelopes/5aa4ec8a1b41fe298594e531/submit \
  -H 'content-type: application/json'
```

# Privacy
This API require limited processing of personal data. For more information, please read our [privacy policy](http://www.ebi.ac.uk/data-protection/privacy-notice/human-cell-atlas-ingest-access-service).
