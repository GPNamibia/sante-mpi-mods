/// <reference path="../.ref/js/santedb-bre.js" />
/// <reference path="../.ref/js/santedb-model.js" />
/// <reference path="../.ref/js/santedb.js" /> 

/**
 * Health MPI / SanteMPI Rules for Generating new ID
 * --
 */

/**
 * @summary Generates a Health Card ID
 */
/**
 * @summary Generates a Health Card ID
 */

function generateHealthId() {
    var res = SanteHealthIDRuleObject.GetNextSequenceValue("health_ident_seq");  
    return res;
};



/**
 * @summary Business rule function
 */
function appendPatientID(patient) {

    // Does patient have no identifiers?
    if (!patient.identifier)
        patient.identifier = {};

    // If operating in a server environment
    //if (SanteDBBre.Environment == ExecutionEnvironment.Server) {
        // Append ID only an existing one doesn't exist.
        if (!patient.identifier.Health_ID){
            patient.identifier.Health_ID= { value: generateHealthId() }; 
        }
            
    //}
    return patient;
}

// Bind the business rules
SanteDBBre.AddBusinessRule("elb.rule.id", "Patient", "BeforeInsert", { "deceasedDate": "null" }, appendPatientID);

// Add identifier generators
if(SanteDB.application) 
    SanteDB.application.addIdentifierGenerator("Health_ID", generateHealthId());