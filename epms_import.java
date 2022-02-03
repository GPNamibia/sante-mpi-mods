package bmt;

import java.sql.Connection;
import java.sql.ResultSet; 
import java.sql.Statement; 

public class epms_import {

    public static void main( String[] args ) throws Exception 
    {
        String sql_url  = "jdbc:sqlserver://172.24.48.36;databaseName=demographic_data";
        String user ="sa";
        String password = "***";

        Connection cn = DriverManager.getConnection(sql_url, user, password);
        
        String fcode = "(10309,10310,10318,10333,10325,10313)";
        //String fcode = "(10313)";
        String ctqry = "Select count(*) as ct from dbo.epms where facilitycode IN "+ fcode;
        Statement st = cn.createStatement();
        ResultSet  rs = st.executeQuery(ctqry);
        rs.next();
        int ct = rs.getInt("ct");

        int batch = 1000;
        int iter = (int) Math.ceil((double) ct / batch);
        
        for (int i=1; i<=iter; i++){
            int offset = (i - 1)  * batch;
            String qry;
            if (i == 1){
                qry = "select top " + batch +" *, REPLACE(artnumberlegacy, '-', '') as 'computed_unique_number', REPLACE(artnumber, '-', '') as 'computed_art_number' from dbo.epms where sex IS NOT NULL and facilitycode IN "+ fcode+ " ORDER BY ident";
                System.out.print(qry);
            }
            else
            {
                qry = "select *, REPLACE(artnumberlegacy, '-', '') as 'computed_unique_number', REPLACE(artnumber, '-', '') as 'computed_art_number'  from dbo.epms where sex IS NOT NULL  and facilitycode IN "+ fcode+ " ORDER BY ident OFFSET " + offset + " ROWS fetch FIRST "+ batch+" ROWS ONLY";
                System.out.print(qry);
            }
             
            epmsThread et = new epmsThread(); 
            et.setConnection(cn);
            et.setSql(qry); 
            et.setIter(i);
            et.start(); 
        }
    }
}
