# SanteMPI Modifications for Namibia UPID #

This repository contains scripts that 

1. Extract patient demographic data from source. This source can be EDT, EPMS or PTracker.

2. Generate patient bundles. Each bundle contains 1000 patients.  Refer to the scripts in EDTBundleCreation, EPMSBundleCreation or PTrackerBundleCreation.

3. Load each of the patient bundles into SanteMPI. Refer to the scripts in EDTBundleImport, EPMSBundleImport or PTrackerBundleImport folders.

4. SanteMPI scripts that generate a Health ID when a patient is created. Refer to the scripts in CustomHealthID folder.

4. SanteMPI scripts that creates a FHIR extension to store the NPRS validation status. Refer to the scripts in NPRSStatusExtension folder.

Additionally, a dummy dataset of 2 million patients has been added to this repository. Refer to the DummyDataset folder.
