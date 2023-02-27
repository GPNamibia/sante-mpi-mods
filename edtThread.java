package bmt;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection; 
import java.sql.ResultSet; 
import java.sql.Statement;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.valueset.AddressUseEnum;
import ca.uhn.fhir.model.dstu2.valueset.AdministrativeGenderEnum;
import ca.uhn.fhir.model.dstu2.valueset.BundleTypeEnum;
import ca.uhn.fhir.model.dstu2.valueset.ContactPointSystemEnum;
import ca.uhn.fhir.model.dstu2.valueset.HTTPVerbEnum; 
import ca.uhn.fhir.model.primitive.DateDt; 

public class edtThread extends Thread {
    
    private String sql; 
    private Connection cn; 
    private int iter; 

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public void setIter(int iter) {
        this.iter = iter;
    }

    public Connection getConnection() {
        return cn;
    }

    public void setConnection(Connection cn) {
        this.cn = cn;
    }

    public void run()
    {
        FhirContext ctx = FhirContext.forDstu2();    
        Bundle bundle = new Bundle(); 
        try (
            Statement st = cn.createStatement()) {
            ResultSet  rs = st.executeQuery(sql);

            bundle.setType(BundleTypeEnum.TRANSACTION);

            if (!rs.isBeforeFirst() ) {    
                System.out.println("No data"); 
            } 
            else
            {
                while(rs.next())
                {
                    Patient ourPatient = new Patient();

                    String fname = rs.getString("first_name");
                    String lname = rs.getString("last_name");

                    ourPatient.addName().addFamily(lname).addGiven(fname);
                    
                    String gender = rs.getString("gender").toUpperCase();
                    ourPatient.setGender(AdministrativeGenderEnum.valueOf(gender)); 

                    String dob = rs.getString("date_of_birth");
                    dob = dob.substring(0, 10);

                    DateDt dt = new DateDt(dob); 
                    ourPatient.setBirthDate(dt);

                    String country = "Namibia";
                    String address = rs.getString("address");
                    String town = rs.getString("town");
                    ourPatient.addAddress().setUse(AddressUseEnum.HOME).setCountry(country).setState(town).setPostalCode(address);
                    
                    String mobile_number = rs.getString("mobile_number");
                    ourPatient.addTelecom().setSystem(ContactPointSystemEnum.PHONE).setValue(mobile_number); 

                    String art_number = rs.getString("art_number");
                    String unique_number = rs.getString("unique_number");
                    String computed_unique_number = rs.getString("computed_unique_number");
                    String computed_art_number = rs.getString("computed_art_number");

                    ourPatient.addIdentifier().setSystem("urn:oid:3.1").setValue(art_number);
                    ourPatient.addIdentifier().setSystem("urn:oid:3.4").setValue(unique_number);
                    ourPatient.addIdentifier().setSystem("urn:oid:3.5").setValue(computed_unique_number);
                    ourPatient.addIdentifier().setSystem("urn:oid:3.6").setValue(computed_art_number);
                    
                    bundle.addEntry()
                    .setFullUrl(ourPatient.getIdElement().getValue())
                    .setResource(ourPatient)
                    .getRequest().setMethod(HTTPVerbEnum.POST);
                }
                String encoded = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle);
                
                try 
                {   
                    String fn = "edt/bundle_"+iter+".json";
                    FileWriter myWriter = new FileWriter(fn);
                    myWriter.write(encoded);
                    myWriter.close();
                    System.out.println("Successfully wrote to the file.");
                } 
                catch (IOException e) 
                {
                    System.out.println("An error occurred.");
                    e.printStackTrace();
                }

            }
        } catch (Exception e1) { 
            e1.printStackTrace();
        }
    
    }

}