package bmt;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
 
import org.json.JSONObject;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap; 
import java.util.Map;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import ca.uhn.fhir.context.FhirContext; 
import ca.uhn.fhir.model.dstu2.resource.Patient; 
import ca.uhn.fhir.model.dstu2.valueset.AddressUseEnum;
import ca.uhn.fhir.model.dstu2.valueset.AdministrativeGenderEnum;
import ca.uhn.fhir.model.dstu2.valueset.ContactPointSystemEnum; 
import ca.uhn.fhir.model.primitive.DateDt;

public class issueHealthId {

    private final static HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();


    public static HttpResponse<String> authenticateClient() throws Exception {

        // form parameters
        Map<Object, Object> data;
        data = new HashMap<>();
        data.put("grant_type", "client_credentials");
        data.put("client_id", "fiddler");
        data.put("client_secret", "***");

        HttpRequest request = HttpRequest.newBuilder()
                .POST(buildFormDataFromMap(data))
                .uri(URI.create("http://localhost:8080/auth/oauth2_token"))
                .setHeader("User-Agent", "Java 11 HttpClient Bot") // add request header
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());


        // print status code
        System.out.println(response.statusCode());

        // print response body
        System.out.println(response.body());
        return response;

    }

    HttpResponse<String> refreshToken(String refreshToken) throws Exception {

        // form parameters
        Map<Object, Object> data;
        data = new HashMap<>();
        data.put("grant_type", "refresh_token");
        data.put("refresh_token", refreshToken);
        data.put("client_id", "fiddler");
        data.put("client_secret", "***");

        HttpRequest request = HttpRequest.newBuilder()
                .POST(buildFormDataFromMap(data))
                .uri(URI.create("http://localhost:8080/auth/oauth2_token"))
                .setHeader("User-Agent", "Java 11 HttpClient Bot") // add request header
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());


        // print status code
        System.out.println(response.statusCode());

        // print response body
        System.out.println(response.body());
        return response;

    }

    private static HttpRequest.BodyPublisher buildFormDataFromMap(Map<Object, Object> data) {
        var builder = new StringBuilder();
        for (Map.Entry<Object, Object> entry : data.entrySet()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
            builder.append("=");
            builder.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
        }
        System.out.println(builder.toString());
        return HttpRequest.BodyPublishers.ofString(builder.toString());
    }


    public static void main( String[] args ) throws Exception
    {
        String db_url = "jdbc:postgresql://localhost:5432/santedb";
        String user = "santedb";
        String password = "***";

        String mp = "e942eb20-dc4a-4b0d-b919-ec21b5c60c16";

        try (Connection con = DriverManager.getConnection(db_url, user, password);
                
                Statement st_guid_ident = con.createStatement();
                ResultSet rs_guid_ident = st_guid_ident.executeQuery("SELECT distinct src_ent_id, trg_ent_id, dob, UPPER(gn.val), tel_val , cd.val FROM public.ent_rel_tbl er inner join public.ent_vrsn_tbl ev ON er.src_ent_id = ev.ent_id inner join public.psn_tbl ps on  ev.ent_vrsn_id = ps.ent_vrsn_id left join public.cd_name_tbl cd  on er.cls_cd_id = cd.cd_id and cd.obslt_vrsn_seq_id is null left join public.cd_name_tbl gn ON gn.cd_id = gndr_cd_id left join public.ent_tel_tbl tel on tel.ent_id = er.src_ent_id WHERE er.obslt_vrsn_seq_id is null and trg_ent_id in ('"+mp+"') and ev.rplc_vrsn_id IS NULL ORDER BY cd.val ASC")) 
                {
                    ArrayList<identifier> identifiers_list = getIdentifiers(mp, con);

                    ArrayList<address> add_list = getAddress(mp, con);

                    while(rs_guid_ident.next())
                    {
                        String src_uuid = rs_guid_ident.getString(1);
                        String trg_uuid = rs_guid_ident.getString(2);
                        System.out.println(trg_uuid);

                        String dob = rs_guid_ident.getString(3);
                        String gender = rs_guid_ident.getString(4).toUpperCase();
                        String tel = rs_guid_ident.getString(5);
                        String cl = rs_guid_ident.getString(6);

                        Patient ourPatient = new Patient();
                        ourPatient.setId(src_uuid);
                        
                        String link = "";
                        if (cl.contains("Verified"))
                        {
                            link = "http://127.0.0.1:8080/fhir/Patient/"+src_uuid;
                        }
                        else if (cl.contains("Linked"))
                        {
                            link = "http://127.0.0.1:8080/fhir/Patient/"+trg_uuid; 
                        }

                        System.out.println(cl);
                        System.out.println(link);

                        String source = trg_uuid.replaceAll("[a-zA-Z-0]", "").substring(0, 8);
                        String[] source_char = source.split("");
                        int[] source_num = new int[source_char.length];

                        for (int i = 0; i < source_char.length; i++) 
                        {
                            source_num[i] = Integer.parseInt(source_char[i]);
                        }

                        String key = "987654321"; 

                        int[] src_idx = new int[source_num.length];

                        for (int i=0; i<source_num.length; i++){
                            int k_index = key.indexOf( String.valueOf(source_num[i])); 
                            src_idx[i] = k_index; 
                        }

                        int seed = Arrays.stream(src_idx).reduce(0,(a, b)  -> a + b);

                        int checkDigit  = (97 - seed + 1 ) % 97;

                        int retVal = Integer.valueOf(source) + checkDigit;

                        /*System.out.println(source);
                        System.out.println(Arrays.toString(src_idx));
                        System.out.println(seed);
                        System.out.println(checkDigit);
                        System.out.println(retVal);*/

                        
                        FhirContext ctx = FhirContext.forDstu2();

                        ourPatient.addTelecom().setSystem(ContactPointSystemEnum.PHONE).setValue(tel); 
                        ourPatient.setGender(AdministrativeGenderEnum.valueOf(gender)); 

                        DateDt dt = new DateDt(dob); 
                        ourPatient.setBirthDate(dt);

                        String country = "";
                        String state = "";
                        String address = "";

                        for(address a : add_list){

                            
                            if (a.uuid.equals(src_uuid))
                            {
                                if (a.add_type.contains("country")){
                                    country = a.add_val;
                                }
    
                                if (a.add_type.contains("state")){
                                    state = a.add_val;
                                }
    
                                if (a.add_type.contains("PostalCode")){
                                    address = a.add_type;
                                }  
                            }                               
                        }
                        ourPatient.addAddress().setUse(AddressUseEnum.HOME).setCountry(country).setState(state).setPostalCode(address);

 
                        for(identifier i : identifiers_list){
                            
                            if (i.uuid.equals(src_uuid))
                            {  
                                ourPatient.addIdentifier().setSystem(i.ident_name).setValue(i.ident_val);
                            }
                            
                        }

                        String h_id = "NAM_" + retVal;
                        
                        ourPatient.addIdentifier().setSystem("http://ohie.org/Health_ID").setValue(h_id);

                        String encoded = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(ourPatient);

                        System.out.println(encoded);

                        HttpResponse<String> auth_response = authenticateClient();
                        JSONObject token = new JSONObject(auth_response.body());
                        
                        String accessToken = (String) token.get("access_token"); 
                        
                        String auth = "Bearer " + accessToken;

                        postResource(link, encoded, auth);
 
                    }

                } catch (SQLException ex) {
                
                    Logger lgr = Logger.getLogger(issueHealthId.class.getName());
                    lgr.log(Level.SEVERE, ex.getMessage(), ex);
                }   

    }

    private static void postResource(String link, String encoded, String auth)
            throws MalformedURLException, IOException, ProtocolException {
        URL url = new URL(link);
        HttpURLConnection http = (HttpURLConnection)url.openConnection();
        http.setRequestMethod("PUT");
        http.setDoOutput(true);
        http.setRequestProperty("Authorization", auth);
        http.setRequestProperty("Accept", "text/html,application/fhir+xml,application/xml;q=0.9,*/*;q=0.8");
               
        http.setRequestProperty("Accept", "application/fhir+json");
        http.setRequestProperty("Content-Type", "application/fhir+json");

        String data = encoded;

        byte[] out = data.getBytes(StandardCharsets.UTF_8);

        OutputStream stream = http.getOutputStream();
        stream.write(out);

        System.out.println(http.getResponseCode() + " " + http.getResponseMessage());
        http.disconnect();
    }

    private static ArrayList<address> getAddress(String mp, Connection con) throws SQLException {
        String sql_add = "SELECT DISTINCT src_ent_id,cd.val as add_type, ent_add_val.val as add_val from public.ent_addr_tbl ent_add inner join public.ent_rel_tbl er on ent_add.ent_id = er.src_ent_id left join public.ent_addr_cmp_tbl ent_add_comp on ent_add_comp.addr_id = ent_add.addr_id left join public.ent_addr_cmp_val_tbl ent_add_val on ent_add_val.val_seq_id = ent_add_comp.val_seq_id LEFT JOIN public.cd_name_tbl cd ON cd.cd_id = ent_add_comp.typ_cd_id where trg_ent_id = '"+mp+"'";

        Statement st_add = con.createStatement();
        ResultSet rs_add = st_add.executeQuery(sql_add);
        ArrayList<address> add_list = new ArrayList<address>();
        while(rs_add.next())
        {
            String uuid = rs_add.getString(1);
            String add_type = rs_add.getString(2);
            String add_val = rs_add.getString(3);
           
            add_list.add(new address(uuid, add_type, add_val));   
        }
        return add_list;
    }

    private static ArrayList<identifier> getIdentifiers(String mp, Connection con) throws SQLException {
        String sql_ident = "SELECT DISTINCT src_ent_id,url, id_val FROM public.ent_id_tbl  ent inner join public.ent_rel_tbl er on ent.ent_id = er.src_ent_id inner JOIN public.asgn_aut_tbl asg on ent.aut_id=asg.aut_id WHERE trg_ent_id = '" + mp+ "'  and ent.obslt_vrsn_seq_id is null";

        Statement st_ident = con.createStatement();
        ResultSet rs_ident = st_ident.executeQuery(sql_ident);
        ArrayList<identifier> identifiers_list = new ArrayList<identifier>();
        while(rs_ident.next())
        {
            String uuid = rs_ident.getString(1);
            String ident_name = rs_ident.getString(2);
            String ident_val = rs_ident.getString(3);
           
            if (!ident_name.contains("Health_ID") && (!ident_val.contains("NULL")))
            {
                identifiers_list.add(new identifier(uuid, ident_name, ident_val));   
            }
            
        }
        return identifiers_list;
    }
}
