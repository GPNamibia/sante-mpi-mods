package bmt;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet; 



public class edt_import {
    public static void main( String[] args ) throws Exception 
    {
        String sql_url  = "jdbc:sqlserver://172.24.48.36;databaseName=demographic_data";
        String user ="sa";
        String password = "***";

        Connection cn = DriverManager.getConnection(sql_url, user, password);
        
        String fcode = "('Katutura Health Centre','Katutura Hospital','Okuryangava Clinic','Windhoek Central Hospital','Robert Mugabe Clinic','Khomasdal Health Centre')";
        //String fcode = "('Khomasdal Health Centre')";
        String ctqry = "Select count(*) as ct from dbo.edt where Health_facility IN "+ fcode;
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
                qry = "select top " + batch +" *, REPLACE(unique_number, '-', '') as 'computed_unique_number', REPLACE(art_number, '-', '') as 'computed_art_number' from dbo.edt where gender in ('Male','Female') and date_of_birth is not null and Health_facility IN "+ fcode+ " ORDER BY ident";
                System.out.print(qry);
            }
            else
            {
                qry = "select *, REPLACE(unique_number, '-', '') as 'computed_unique_number', REPLACE(art_number, '-', '') as 'computed_art_number'  from dbo.edt where gender in ('Male','Female') and date_of_birth is not null and Health_facility IN "+ fcode+ " ORDER BY ident OFFSET " + offset + " ROWS fetch FIRST "+ batch+" ROWS ONLY";
                System.out.print(qry);
            }
             
            edtThread dt = new edtThread(); 
            dt.setConnection(cn);
            dt.setSql(qry); 
            dt.setIter(i);
            dt.start(); 
            
        }
        //cn.close();
        
    }
}
