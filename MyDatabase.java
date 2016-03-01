/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mydatabase;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Writer;
import static java.lang.Character.toUpperCase;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MyDatabase {
    private static Scanner sc;
    static RandomAccessFile writeFile;
    //static ArrayList <dbAttributes> pharmaValues = new ArrayList<dbAttributes>();
    //static ArrayList pharmaValues = new ArrayList();
    static ArrayList<Long> OffsetValue=new ArrayList<Long>();
    static RandomAccessFile file1;
    static RandomAccessFile file2;
    static Map<String, List<String>> selectMap = new HashMap<String, List<String>>();
    static Writer insert_file = null;
    static byte double_blind_mask      = 8;    // binary 0000 1000
    static byte controlled_study_mask  = 4;    // binary 0000 0100
    static byte govt_funded_mask       = 2;    // binary 0000 0010
    static byte fda_approved_mask      = 1;    // binary 0000 0001
    final static byte double_blind_const      = 8;    // binary 0000 1000
    final static byte controlled_study_const  = 4;    // binary 0000 0100
    final static byte govt_funded_const       = 2;    // binary 0000 0010
    final static byte fda_approved_const      = 1;    // binary 0000 0001
    static byte delete_byte = 16;                     // exist means 1
    final static byte delete_byte_mask = 16; 
    
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        sc = new Scanner(System.in);
        //System.out.println("in"+ sc);
        String options;
        while(true){
	   System.out.println("Enter Your Choice, any of the following:");
	   System.out.println("Import");
	   System.out.println("Query");
	   System.out.println("Insert");
	   System.out.println("Delete");
	   System.out.println("Exit");
	   //System.out.println(options.length());
           
           options = sc.nextLine();
           String[] opts = options.split(" ");
           //System.out.println(options.length());
           String opt = opts[0].toUpperCase();
           String input = "";
           String input_file = "";
           
           switch(opt){
	   case "IMPORT":
               input = opts[1]; 
               input_file = input.replaceFirst("[.][^.]+$", "");
               //System.out.println("import");
               import_file(input, input_file);
               break;
           case "SELECT":

               input = opts[3]+".csv";
               input_file = opts[3];
               String where_variable = opts[5]; //variable to search
               String where_sign = "";
               String where_value = "";
               if(opts[6].equalsIgnoreCase("not")){
                  where_sign = "NOT " + opts[7];   //sign of comparison 
                  if (opts[8].charAt(opts[8].length()-1)==';'){
                      where_value = opts[8].substring(0, opts[8].length()-1);
                  }
                  else{
                  where_value = opts[8];}
               }
               else{
                   where_sign = opts[6];   //sign of comparison
                   if (opts[7].charAt(opts[7].length()-1)==';'){
                       where_value = opts[7].substring(0, opts[7].length()-1);
                   }
                   else{
                   where_value = opts[7];}
               }       
                select_record(input, input_file, where_variable, where_sign, where_value);
               break;
           
           case "INSERT":
               
               String insert_comm = options.replace(",", "");
               insert_comm = insert_comm.replace("(", "");
               insert_comm = insert_comm.replace(")", "");
               insert_comm = insert_comm.replace(";", "");
               insert_comm = insert_comm.replace("'", "");
               String[] insert_opts = insert_comm.split(" ");
               input = insert_opts[2]+".csv";
               
               input_file = insert_opts[2];
               String v_id = insert_opts[4];
               String v_com = insert_opts[5];
               String v_did = insert_opts[6];
               String v_tr = insert_opts[7];
               String v_pa = insert_opts[8];
               String v_dos = insert_opts[9];
               String v_read = insert_opts[10];
               String v_doub = insert_opts[11];
               String v_cs = insert_opts[12];
               String v_gf = insert_opts[13];
               String v_fa = insert_opts[14];
               
               insert_record(input, input_file, v_id, v_com, v_did, v_tr, v_pa, v_dos, v_read, v_doub, v_cs, v_gf, v_fa);
               import_file(input, input_file);
               System.out.println("Record Inserted Successfully!");
               break;
           
           case "DELETE":
               input = opts[2]+".csv";
               input_file = opts[2];
               String delete_field = opts[4];
               String delete_operator = "";
               String delete_value = "";
               if (opts[5].equalsIgnoreCase("not")){
                       delete_operator = "NOT "+ opts[6];
                       if (opts[7].charAt(opts[7].length()-1)==';')
                       {
                           delete_value = opts[7].substring(0, opts[7].length()-1);
                       }
                       else{
                           delete_value = opts[7];
                       }
               }
               else{
                   delete_operator = opts[5];
                   if (opts[6].charAt(opts[6].length()-1)==';')
                   {
                       delete_value = opts[6].substring(0, opts[6].length()-1);
                   }
                   else{
                        delete_value = opts[6]; 
                   }
               }              
               delete_record(input, input_file, delete_field, delete_value, delete_operator);
               System.out.println("Record successfully deleted!");
               break;
               
           case "EXIT":
		   System.exit(0);
           default:
               System.out.println("Bad Input!");
	   }      
          }
        }

    public static void import_file(String input_file, String file_name) throws IOException {
        BufferedReader br = null;
        long bytes_pLine = 0L;
        long bytes_pLine1 = 0L;
        String line = "";
        try {
            br = new BufferedReader(new FileReader(input_file));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MyDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }
        byte commonByte = 0x00;               //  binary 0000 0000
        //delete byte
        byte deleteByte = 0x00;
        
        RandomAccessFile raf = new RandomAccessFile(file_name +".db","rw");
        
        BufferedWriter out_id = new BufferedWriter(new FileWriter(file_name+".id.ndx"));
        TreeMap<Integer, String> in_id=new TreeMap<Integer, String>();
        
        BufferedWriter out_company = new BufferedWriter(new FileWriter(file_name+".company.ndx"));
        TreeMap<String, List<String>> in_company=new TreeMap<String, List<String>>();
        
        BufferedWriter out_drugId = new BufferedWriter(new FileWriter(file_name+".drugId.ndx"));
        TreeMap<String, List<String>> in_drugId=new TreeMap<String, List<String>>();
        
        BufferedWriter out_trials = new BufferedWriter(new FileWriter(file_name+".trials.ndx"));
        TreeMap<Short, List<String>> in_trials=new TreeMap<Short, List<String>>();
        
        BufferedWriter out_patients = new BufferedWriter(new FileWriter(file_name+".patients.ndx"));
        TreeMap<Short, List<String>> in_patients=new TreeMap<Short, List<String>>();
        
        BufferedWriter out_dosageMg = new BufferedWriter(new FileWriter(file_name+".dosageMg.ndx"));
        TreeMap<Short, List<String>> in_dosageMg=new TreeMap<Short, List<String>>();
        
        BufferedWriter out_reading = new BufferedWriter(new FileWriter(file_name+".reading.ndx"));
        TreeMap<Float, List<String>> in_reading=new TreeMap<Float, List<String>>();
        
        BufferedWriter out_doubleBlind = new BufferedWriter(new FileWriter(file_name+".doubleBlind.ndx"));
        TreeMap<Boolean, List<String>> in_doubleBlind=new TreeMap<Boolean, List<String>>();
        List<String> list_doubleBlind_true = new ArrayList<String>();
        List<String> list_doubleBlind_false = new ArrayList<String>();
        
        BufferedWriter out_controlledStudy = new BufferedWriter(new FileWriter(file_name+".controlledStudy.ndx"));
        TreeMap<Boolean, List<String>> in_controlledStudy=new TreeMap<Boolean, List<String>>();
        List<String> list_controlledStudy_true = new ArrayList<String>();
        List<String> list_controlledStudy_false = new ArrayList<String>();
        
        BufferedWriter out_govtFunded = new BufferedWriter(new FileWriter(file_name+".govtFunded.ndx"));
        TreeMap<Boolean, List<String>> in_govtFunded=new TreeMap<Boolean, List<String>>();
        List<String> list_govtFunded_true = new ArrayList<String>();
        List<String> list_govtFunded_false = new ArrayList<String>();
        
        BufferedWriter out_fdaApproved = new BufferedWriter(new FileWriter(file_name+".fdaApproved.ndx"));
        TreeMap<Boolean, List<String>> in_fdaApproved=new TreeMap<Boolean, List<String>>();
        //HashMap<Boolean, List<String>> in_fdaApproved=new HashMap<Boolean, List<String>>();
        List<String> list_fdaApproved_true = new ArrayList<String>();
        List<String> list_fdaApproved_false = new ArrayList<String>();
        
        br.readLine();
        while ((line = br.readLine()) != null) { 
            //dbAttributes detailObj= new dbAttributes(); 
            String[] values = line.replaceAll("^\"", "").split("\"?(,|$)(?=(([^\"]*\"){2})*[^\"]*$) *\"?"); 
            //System.out.println(values);            
            OffsetValue.add(null);
            OffsetValue.add((long) 0);
            bytes_pLine1 = raf.getFilePointer();
            //RandomAccessFile raf = new RandomAccessFile(input_file +".db","rw");
            raf.writeInt(Integer.parseInt(values[0]));  //id
            //pharmaValues.add(null)
           
            //bytes_pLine = bytes_pLine + 4;
            
            int vlen = values[1].length();
            
            byte varchar_company = (byte) values[1].length();
            raf.write(values[1].length());
            //bytes_pLine = bytes_pLine + 1;
            
            raf.writeBytes(values[1]);
            
            //bytes_pLine = bytes_pLine + values[1].length();
            //System.out.println(values[1]);
            
            raf.writeBytes(values[2]);
            //bytes_pLine = bytes_pLine + 6;
            raf.writeShort(Short.parseShort(values[3]));   // trials
            //bytes_pLine = bytes_pLine + 2;
            raf.writeShort(Short.parseShort(values[4]));   //patients
            //bytes_pLine = bytes_pLine + 2;
            raf.writeShort(Short.parseShort(values[5]));   //dos
            //bytes_pLine = bytes_pLine + 2;
            raf.writeFloat(Float.parseFloat(values[6]));   //reading
            //bytes_pLine = bytes_pLine + 4;
            if("true".equalsIgnoreCase(values[7])){
                double_blind_mask = 8;
            }else{
                double_blind_mask = 0;
            }
            if("true".equalsIgnoreCase(values[8])){
                controlled_study_mask = 4;
            }else{
                controlled_study_mask = 0;
            }
            if("true".equalsIgnoreCase(values[9])){
                govt_funded_mask = 2;
            }else{
                govt_funded_mask = 0;
            }
            if("true".equalsIgnoreCase(values[10])){
                fda_approved_mask = 1;
            }else{
                fda_approved_mask = 0;
            }
            commonByte = 0x00;               //  binary 0000 0000
            commonByte = (byte)(commonByte | double_blind_mask);
            commonByte = (byte)(commonByte | controlled_study_mask);
            commonByte = (byte)(commonByte | govt_funded_mask);
            commonByte = (byte)(commonByte | fda_approved_mask);
            commonByte = (byte)(commonByte | delete_byte_mask);
            raf.write(commonByte);
            
            //bytes_pLine = bytes_pLine + 1;
            //delete byte
            //raf.writeByte(1);
            OffsetValue.add(bytes_pLine); 
            //pharmaValues.add(values);
            //add index values
            in_id.put(Integer.parseInt(values[0]), Long.toString(bytes_pLine1));
            //in_company.put(values[1], Long.toString(bytes_pLine));
            List<String> currentValue_company = in_company.get(values[1]);
            if (currentValue_company == null){
                currentValue_company = new ArrayList<String>();
                in_company.put(values[1], currentValue_company);
            }
            currentValue_company.add(Long.toString(bytes_pLine1));
            //in_drugId.put(values[2], Long.toString(bytes_pLine));
            List<String> currentValue_drugId = in_drugId.get(values[2]);
            if (currentValue_drugId == null){
                currentValue_drugId = new ArrayList<String>();
                in_drugId.put(values[2], currentValue_drugId);    
            }
            currentValue_drugId.add(Long.toString(bytes_pLine1));
            //in_trials.put((Short.parseShort(values[3])), Long.toString(bytes_pLine));
            List<String> currentValue_trials = in_trials.get(Short.parseShort(values[3]));
            if (currentValue_trials == null){
                currentValue_trials = new ArrayList<String>();
                in_trials.put(Short.parseShort(values[3]), currentValue_trials);    
            }
            currentValue_trials.add(Long.toString(bytes_pLine1));
            //in_patients.put((Short.parseShort(values[4])), Long.toString(bytes_pLine));
            List<String> currentValue_patients = in_patients.get(Short.parseShort(values[4]));
            if (currentValue_patients == null){
                currentValue_patients = new ArrayList<String>();
                in_patients.put(Short.parseShort(values[4]), currentValue_patients);    
            }
            currentValue_patients.add(Long.toString(bytes_pLine1));
            //in_dosageMg.put((Short.parseShort(values[5])), Long.toString(bytes_pLine));
            List<String> currentValue_dosageMg = in_dosageMg.get(Short.parseShort(values[5]));
            if (currentValue_dosageMg == null){
                currentValue_dosageMg = new ArrayList<String>();
                in_dosageMg.put(Short.parseShort(values[5]), currentValue_dosageMg);    
            }
            currentValue_dosageMg.add(Long.toString(bytes_pLine1));
            //in_reading.put((Float.parseFloat(values[6])), Long.toString(bytes_pLine));
            List<String> currentValue_reading = in_reading.get(Float.parseFloat(values[6]));
            if (currentValue_reading == null){
                currentValue_reading = new ArrayList<String>();
                in_reading.put(Float.parseFloat(values[6]), currentValue_reading);    
            }
            currentValue_reading.add(Long.toString(bytes_pLine1));
            //in_doubleBlind.put((Boolean.parseBoolean(values[7])), Long.toString(bytes_pLine));
            if (Boolean.parseBoolean(values[7]) == true){
                list_doubleBlind_true.add(Long.toString(bytes_pLine1));    
            }
            if (Boolean.parseBoolean(values[7]) == false){
                list_doubleBlind_false.add(Long.toString(bytes_pLine1));
            }
            //in_controlledStudy.put((Boolean.parseBoolean(values[8])), Long.toString(bytes_pLine));
            if (Boolean.parseBoolean(values[8]) == true){
                list_controlledStudy_true.add(Long.toString(bytes_pLine1));    
            }
            if (Boolean.parseBoolean(values[8]) == false){
                list_controlledStudy_false.add(Long.toString(bytes_pLine1));
            }
            //in_govtFunded.put((Boolean.parseBoolean(values[9])), Long.toString(bytes_pLine));
            if (Boolean.parseBoolean(values[9]) == true){
                list_govtFunded_true.add(Long.toString(bytes_pLine1));    
            }
            if (Boolean.parseBoolean(values[9]) == false){
                list_govtFunded_false.add(Long.toString(bytes_pLine1));
            }
            //in_fdaApproved.put((Boolean.parseBoolean(values[10])), Long.toString(bytes_pLine));
            if (Boolean.parseBoolean(values[10]) == true){
                list_fdaApproved_true.add(Long.toString(bytes_pLine1));    
            }
            if (Boolean.parseBoolean(values[10]) == false){
                list_fdaApproved_false.add(Long.toString(bytes_pLine1));
            }
        }
        raf.close();
        //System.out.println(in_fdaApproved);
        //write id index
        for(int s:in_id.keySet()){
            out_id.write(s+"\t\t");
            out_id.write(in_id.get(s));
            out_id.newLine();
	}
	out_id.close();
        //write company index
        for(String s:in_company.keySet()){
            out_company.write(s+"\t\t");
            //out_company.write(in_company.get(s));
            for (int i =0; i < in_company.get(s).size(); i++){
                out_company.write(in_company.get(s).get(i));
                out_company.write("\t");
            }
            out_company.newLine();
        }
         out_company.close();
        //write drug id index
        for(String s:in_drugId.keySet()){
            out_drugId.write(s+"\t\t");
            //out_drugId.write(in_drugId.get(s));
            for (int i =0; i < in_drugId.get(s).size(); i++){
                out_drugId.write(in_drugId.get(s).get(i));
                out_drugId.write("\t");
            }
            out_drugId.newLine();
        }
        out_drugId.close(); 
        //write trials index
        for(Short s:in_trials.keySet()){
            out_trials.write(s+"\t\t");
            //out_trials.write(in_trials.get(s));
            for (int i =0; i < in_trials.get(s).size(); i++){
                out_trials.write(in_trials.get(s).get(i));
                out_trials.write("\t");
            }
            out_trials.newLine();
        }
        out_trials.close();
        //write patients index
        for(Short s:in_patients.keySet()){
            out_patients.write(s+"\t\t");
            //out_patients.write(in_patients.get(s));
            for (int i =0; i < in_patients.get(s).size(); i++){
                out_patients.write(in_patients.get(s).get(i));
                out_patients.write("\t");
            }
            out_patients.newLine();
        }
        out_patients.close();
        //write dosageMg index
        for(Short s:in_dosageMg.keySet()){
            out_dosageMg.write(s+"\t\t");
            //out_dosageMg.write(in_dosageMg.get(s));
            for (int i =0; i < in_dosageMg.get(s).size(); i++){
                out_dosageMg.write(in_dosageMg.get(s).get(i));
                out_dosageMg.write("\t");
            }
            out_dosageMg.newLine();
        }
        out_dosageMg.close();
        //write reading index
        for(Float s:in_reading.keySet()){
            out_reading.write(s+"\t\t");
            //out_reading.write(in_reading.get(s));
            for (int i =0; i < in_reading.get(s).size(); i++){
                out_reading.write(in_reading.get(s).get(i));
                out_reading.write("\t");
            }
            out_reading.newLine();
        }
        out_reading.close();
        //write doubleBlind index
        in_doubleBlind.put(true, list_doubleBlind_true);
        in_doubleBlind.put(false, list_doubleBlind_false);
        for(Boolean s:in_doubleBlind.keySet()){
            out_doubleBlind.write(s+"\t\t");
            //out_doubleBlind.write(in_doubleBlind.get(s));
            for (int i =0; i < in_doubleBlind.get(s).size(); i++){
                out_doubleBlind.write(in_doubleBlind.get(s).get(i));
                out_doubleBlind.write("\t");
            }
            out_doubleBlind.newLine();
        }
        out_doubleBlind.close();
        //write controlledStudy index
        in_controlledStudy.put(true, list_controlledStudy_true);
        in_controlledStudy.put(false, list_controlledStudy_false);
        for(Boolean s:in_controlledStudy.keySet()){
            out_controlledStudy.write(s+"\t\t");
            //out_controlledStudy.write(in_controlledStudy.get(s));
            for (int i =0; i < in_controlledStudy.get(s).size(); i++){
                out_controlledStudy.write(in_controlledStudy.get(s).get(i));
                out_controlledStudy.write("\t");
            }
            out_controlledStudy.newLine();
        }
        out_controlledStudy.close();
        //write govtfunded index
        in_govtFunded.put(true, list_govtFunded_true);
        in_govtFunded.put(false, list_govtFunded_false);
        for(Boolean s:in_govtFunded.keySet()){
            out_govtFunded.write(s+"\t\t");
            //out_govtFunded.write(in_govtFunded.get(s));
            for (int i =0; i < in_govtFunded.get(s).size(); i++){
                out_govtFunded.write(in_govtFunded.get(s).get(i));
                out_govtFunded.write("\t");
            }
            out_govtFunded.newLine();
        }
        out_govtFunded.close();
        //write fdaApproved index
        in_fdaApproved.put(true, list_fdaApproved_true);
        in_fdaApproved.put(false, list_fdaApproved_false);
        for(Boolean s:in_fdaApproved.keySet()){
            out_fdaApproved.write(s+"\t\t");
            //out_fdaApproved.write(in_fdaApproved.get(s));
            for (int i =0; i < in_fdaApproved.get(s).size(); i++){
                out_fdaApproved.write(in_fdaApproved.get(s).get(i));
                out_fdaApproved.write("\t");
            }
            out_fdaApproved.newLine();
        }
        out_fdaApproved.close();       
}

    private static void select_record(String input, String file_name, String where_variable, String where_sign, String where_value) throws FileNotFoundException, IOException {
               
        try {
            file1 = new RandomAccessFile(file_name +".db","rw");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MyDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Selected data:");
        System.out.println("ID  Company  Drug_id  Trials  Patients  Dosage_Mg  Reading  Double_Blind  Controlled_Study  Govt_Funded  FDA_Approved");
        
        if (where_variable.equals("id")){  
                file2 = new RandomAccessFile(file_name +".id.ndx", "rw");
                //System.out.println("in id");
                show_records(file1, file2, where_sign, where_value);            
        }
        else if(where_variable.equals("company")){
            file2 = new RandomAccessFile(file_name +".company.ndx", "rw");
            show_records(file1, file2, where_sign, where_value);           
        }
        else if (where_variable.equals("drug_id")){
            file2 = new RandomAccessFile(file_name +".drugId.ndx", "rw");
            show_records(file1, file2, where_sign, where_value);          
        }
        else if (where_variable.equals("trials")){
            file2 = new RandomAccessFile(file_name +".trials.ndx", "rw");
            show_records(file1, file2, where_sign, where_value);    
        }
        else if (where_variable.equals("patients")){
            file2 = new RandomAccessFile(file_name +".patients.ndx", "rw");
            show_records(file1, file2, where_sign, where_value);    
        }
        else if(where_variable.equals("dosage_mg")){
            file2 = new RandomAccessFile(file_name +".dosageMg.ndx", "rw");
            show_records(file1, file2, where_sign, where_value);    
        }
        else if(where_variable.equals("reading")){
            file2 = new RandomAccessFile(file_name +".reading.ndx", "rw");
            show_records(file1, file2, where_sign, where_value);    
        }
        else if (where_variable.equals("double_blind")){
            file2 = new RandomAccessFile(file_name +".doubleBlind.ndx", "rw");
            show_records(file1, file2, where_sign, where_value);    
        }
        else if(where_variable.equals("controlled_study")){
            file2 = new RandomAccessFile(file_name +".controlledStudy.ndx", "rw");
            show_records(file1, file2, where_sign, where_value);
            
        }
        else if(where_variable.equals("govt_funded")){
            file2 = new RandomAccessFile(file_name +".govtFunded.ndx", "rw");
            show_records(file1, file2, where_sign, where_value);
            
        }
        else if (where_variable.equals("fda_approved")){
            file2 = new RandomAccessFile(file_name +".fdaApproved.ndx", "rw");
            show_records(file1, file2, where_sign, where_value);
            
        }
    }

    private static void show_records(RandomAccessFile file1, RandomAccessFile file2, String where_sign, String where_value) throws IOException {
        String result1, result2;
        List<String> position = new ArrayList<String>();
       
        result1 =file2.readLine();
                while (result1 != null){                   
                    List<String> values = Arrays.asList(result1.split("[\\s,]+"));
                    //System.out.println(values.get(0));
                    if (where_sign.equals("=")){
                        if (values.get(0).equals(where_value)){
                            position = values;
                            //System.out.println(position);
                            display_selected_records(position, file1);
                            break;
                        }
                    }
                    if (where_sign.equals("NOT =")){
                        if (!values.get(0).equals(where_value)){
                            position = values;
                            //System.out.println(position);
                            display_selected_records(position, file1);
                            //break;
                        }
                    }
                    if (where_sign.equals(">")){
                        if (values.get(0).compareTo(where_value) > 0){
                            position = values;
                            //System.out.println(position);
                            display_selected_records(position, file1);
                            //break;
                        }
                    }
                    //result1 =file2.readLine();
                    if (where_sign.equals("<")){
                        if (values.get(0).compareTo(where_value) < 0){
                            position = values;
                            //System.out.println(position);
                            display_selected_records(position, file1);
                            //break;
                        }
                    }
                    if (where_sign.equals("<=")){
                        if (values.get(0).compareTo(where_value) < 0 || values.get(0).equals(where_value)){
                            position = values;
                            //System.out.println(position);
                            display_selected_records(position, file1);
                            //break;
                        }
                    }
                    if (where_sign.equals(">=")){
                        if (values.get(0).compareTo(where_value) > 0 || values.get(0).equals(where_value)){
                            position = values;
                            //System.out.println(position);
                            display_selected_records(position, file1);
                            //break;
                        }
                    }
                    if (where_sign.equals(" NOT >")){
                        if (values.get(0).compareTo(where_value) <= 0){
                            position = values;
                            //System.out.println(position);
                            display_selected_records(position, file1);
                            //break;
                        }
                    }
                    if (where_sign.equals(" NOT <")){
                        if (values.get(0).compareTo(where_value) >= 0){
                            position = values;
                            //System.out.println(position);
                            display_selected_records(position, file1);
                            //break;
                        }
                    }
                    result1 =file2.readLine();
                }
    }
    private static void display_selected_records(List<String> position, RandomAccessFile file1) throws IOException {
        //get record from file1 with record as in position
        //StringBuilder stringBuilder = new StringBuilder();
        for (int i = 1; i < position.size(); i++) {

            file1.seek(Integer.parseInt(position.get(i)));
            System.out.print(file1.readInt() + "\t");   // id
            
            int vlen = file1.readByte();
            byte[] b = new byte[vlen];
            for(int x=0;x<=vlen-1;x++){
              b[x]=  file1.readByte();
            }
            System.out.print(new String(b) + "\t");     //company
            
            byte[] b1 = new byte[6];
            for (int j=0; j<6; j++ ){
               b1[j] = file1.readByte();
            }
            System.out.print(new String(b1) + "\t");

            System.out.print(file1.readShort() + "\t");
            System.out.print(file1.readShort() + "\t");
            System.out.print(file1.readShort() + "\t");
            System.out.print(file1.readFloat() + "\t");
 
            byte commonByte = 0x00;               //  binary 0000 0000
            commonByte = (byte)file1.read();
            
            System.out.print((double_blind_const == (byte) (commonByte & double_blind_const)) + "\t");
            System.out.print((controlled_study_const == (byte) (commonByte & controlled_study_const)) + "\t");
            System.out.print((govt_funded_const == (byte) (commonByte & govt_funded_const)) + "\t");
            System.out.print((fda_approved_const == (byte) (commonByte & fda_approved_const)) + "\t");
            System.out.println();
            //System.out.println("commonByte"+ commonByte);
            //System.out.println((byte)(commonByte & delete_byte_mask));
            if (delete_byte_mask == (byte) (commonByte & delete_byte_mask))
            {
               System.out.println("Record exists in table"); 
            }
            else{
                System.out.println("Record is deleted");
            }
        }
    }


    private static void insert_record(String input, String input_file, String v_id, String v_com, String v_did, String v_tr, String v_pa, String v_dos, String v_read, String v_doub, String v_cs, String v_gf, String v_fa) throws IOException {
            insert_file = new BufferedWriter(new FileWriter(input, true));
            insert_file.append(v_id+","+ v_com+","+ v_did+","+ v_tr+","+ v_pa+","+v_dos+","+ v_read+","+v_doub+","+v_cs+","+ v_gf+","+v_fa);
            insert_file.close();
    }

    private static void delete_record(String input, String file_name, String delete_field, String delete_value, String delete_operator) throws IOException {
        try {
            file1 = new RandomAccessFile(file_name +".db","rw");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MyDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if (delete_field.equals("id")){ 
                //System.out.println("del id---");
                file2 = new RandomAccessFile(file_name +".id.ndx", "rw");
                delete_selected_rec(file1, file2, delete_field, delete_operator, delete_value);
        }
        else if(delete_field.equals("company")){
            file2 = new RandomAccessFile(file_name +".company.ndx", "rw");
            delete_selected_rec(file1, file2, delete_field, delete_operator, delete_value);
        }
        else if (delete_field.equals("drug_id")){
            file2 = new RandomAccessFile(file_name +".drugId.ndx", "rw");
            delete_selected_rec(file1, file2, delete_field, delete_operator, delete_value);
        }
        else if (delete_field.equals("trials")){
            file2 = new RandomAccessFile(file_name +".trials.ndx", "rw");
            delete_selected_rec(file1, file2, delete_field, delete_operator, delete_value);
        }
        else if (delete_field.equals("patients")){
            file2 = new RandomAccessFile(file_name +".patients.ndx", "rw");
            delete_selected_rec(file1, file2, delete_field, delete_operator, delete_value);
        }
        else if(delete_field.equals("dosage_mg")){
            file2 = new RandomAccessFile(file_name +".dosageMg.ndx", "rw");
            delete_selected_rec(file1, file2, delete_field, delete_operator, delete_value);
        }
        else if(delete_field.equals("reading")){
            file2 = new RandomAccessFile(file_name +".reading.ndx", "rw");
            delete_selected_rec(file1, file2, delete_field, delete_operator, delete_value);
        }
        else if (delete_field.equals("double_blind")){
            file2 = new RandomAccessFile(file_name +".doubleBlind.ndx", "rw");
            delete_selected_rec(file1, file2, delete_field, delete_operator, delete_value);
        }
        else if(delete_field.equals("controlled_study")){
            file2 = new RandomAccessFile(file_name +".controlledStudy.ndx", "rw");
            delete_selected_rec(file1, file2, delete_field, delete_operator, delete_value);
            
        }
        else if(delete_field.equals("govt_funded")){
            file2 = new RandomAccessFile(file_name +".govtFunded.ndx", "rw");
            delete_selected_rec(file1, file2, delete_field, delete_operator, delete_value);
            
        }
        else if (delete_field.equals("fda_approved")){
            file2 = new RandomAccessFile(file_name +".fdaApproved.ndx", "rw");
            delete_selected_rec(file1, file2, delete_field, delete_operator, delete_value);
            
        }
        file1.close();
        file2.close();
    }

    
    private static void delete_selected_rec(RandomAccessFile file1, RandomAccessFile file2, String delete_field, String delete_operator, String delete_value) throws IOException {
        String result1;
        List<String> position = new ArrayList<String>();
        result1 =file2.readLine();
        while (result1 != null){                   
            List<String> values = Arrays.asList(result1.split("[\\s,]+"));
            //System.out.println(values.get(0));
            if (delete_operator.equals("=")){
                //System.out.println(values.get(0));
                if (values.get(0).equals(delete_value)){
                    position = values;
                    //System.out.println(position);
                    soft_delete_rec(position, file1);
                    break;
                }
            }
            if (delete_operator.equals("NOT =")){
                if (!values.get(0).equals(delete_value)){
                    position = values;
                    //System.out.println(position);
                    soft_delete_rec(position, file1);
                    //break;
                }
            }
            if (delete_operator.equals(">")){
                if (values.get(0).compareTo(delete_value) > 0){
                    position = values;
                    //System.out.println(position);
                    soft_delete_rec(position, file1);
                    //break;
                }
            }
                    //result1 =file2.readLine();
            if (delete_operator.equals("<")){
                if (values.get(0).compareTo(delete_value) < 0){
                    position = values;
                    //System.out.println(position);
                    soft_delete_rec(position, file1);
                    //break;
                }
            }
            if (delete_operator.equals("<=")){
                if (!(values.get(0).compareTo(delete_value) > 0)){
                    position = values;
                    //System.out.println(position);
                    soft_delete_rec(position, file1);
                    //break;
                }
            }
            if (delete_operator.equals(">=")){
                if (!(values.get(0).compareTo(delete_value) < 0)){
                    position = values;
                    //System.out.println(position);
                    soft_delete_rec(position, file1);
                    //break;
                }
            }
            if (delete_operator.equals(" NOT >")){
                if (values.get(0).compareTo(delete_value) <= 0){
                    position = values;
                    //System.out.println(position);
                    soft_delete_rec(position, file1);
                    //break;
                }
            }
            if (delete_operator.equals(" NOT <")){
                if (values.get(0).compareTo(delete_value) >= 0){
                    position = values;
                    //System.out.println(position);
                    soft_delete_rec(position, file1);
                    //break;
                }
            }
            result1 =file2.readLine();
        }
    }

    private static void soft_delete_rec(List<String> position, RandomAccessFile file1) throws IOException {
        for (int i = 1; i < position.size(); i++) {
            long bytes_pLine = 0L;
            file1.seek(Integer.parseInt(position.get(i)));
           
            file1.readInt();
            int vlen = file1.readByte();
            byte[] b = new byte[vlen];
            for(int x=0;x<=vlen-1;x++){
              b[x]=  file1.readByte();
            }
            
            byte[] b1 = new byte[6];
            for (int j=0; j<6; j++ ){
               b1[j] = file1.readByte();
            }
            file1.readShort();
            file1.readShort();
            file1.readShort();
            file1.readFloat();
            byte commonByte = 0x00;
            
            bytes_pLine = file1.getFilePointer();
            
            //System.out.println(bytes_pLine);
            commonByte = (byte)file1.read();
            //System.out.println(commonByte);
            commonByte = (byte)(commonByte & ~delete_byte_mask);
            //System.out.println(commonByte);
            //System.out.println(bytes_pLine);
            //System.out.println(commonByte);
            file1.seek(bytes_pLine);
            //update commonByte
            file1.write(commonByte);
    }

    }
}

    
