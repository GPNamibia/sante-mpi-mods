using Hl7.Fhir.Model;
using SanteDB.Core.Model;
using SanteDB.Messaging.FHIR.Util;
using System;
using System.Collections.Generic; 
using SanteDB.Messaging.FHIR.Extensions;
using SanteDB.Core.Model.Interfaces;
using System.Linq;
using SanteDB.Core.Extensions;

namespace NPRSValidationExtension
{
    public class NPRSValidationExtension : IFhirExtensionHandler
    {
        private Guid MY_EXTENSION_TYPE_UUID = new Guid("b80807d9-da19-4c4a-97ff-9ad5b8601883");

        /// <summary>
        /// Gets the resource type this applies to
        /// </summary>
        public ResourceType? AppliesTo => ResourceType.Patient;

        /// <summary>
        /// Gets the profile definition
        /// </summary>
        public Uri ProfileUri => this.Uri;

        /// <summary>
        /// Gets the URI of this extension
        /// </summary>
        public Uri Uri => new Uri("urn:validationproject:nprsStatus");
        
        /// <summary>
        /// Construct the extension
        /// </summary>

        public IEnumerable<Extension> Construct(IIdentifiedEntity modelObject)
        {
            if (modelObject is SanteDB.Core.Model.Roles.Patient patient)
            {
                var extension = patient.LoadProperty(o => o.Extensions).FirstOrDefault(o => o.ExtensionTypeKey == MY_EXTENSION_TYPE_UUID);
                if (extension != null)
                {
                    yield return new Extension(this.Uri.ToString(), (DataType)extension.GetValue());
                }
            }
        }

        /// <summary>
        /// Parse the extension
        /// </summary>
        public bool Parse(Extension fhirExtension, IdentifiedData modelObject)
        {
            if (fhirExtension.Value is CodeableConcept nprs_status && modelObject is SanteDB.Core.Model.Roles.Patient patient)
            {
                var santeDbConcept = DataTypeConverter.ToConcept(nprs_status);
                patient.AddExtension(MY_EXTENSION_TYPE_UUID, typeof(ReferenceExtensionHandler), santeDbConcept);
                return true;
            }
            return false;
        }

    }
}

