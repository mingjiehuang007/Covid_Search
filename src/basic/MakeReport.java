package basic;


import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.python.util.PythonInterpreter;

import javax.xml.ws.Response;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MakeReport {

    private void combineFiles() throws Exception{

        String output_path = ".\\data\\final_output\\final_output.txt";
        try {
            File file_output = new File(output_path);        //文件路径（路径+文件名）
            if (!file_output.exists()) {    //文件不存在则创建文件，先创建目录
                File dir_output = new File(file_output.getParent());
                dir_output.mkdirs();
                file_output.createNewFile();
            }

            String sourceString = "key1\t" + "key2\t" + "doc_id\t" + "sen_text\t" + "text_field\n";
            FileOutputStream outStream = new FileOutputStream(file_output);    //文件输出流用于将数据写入文件
            byte[] sourceByte = sourceString.getBytes();
            outStream.write(sourceByte);
            outStream.close();    //关闭文件输出流

        } catch (Exception e) {
            e.printStackTrace();
        }

        int count = 0;
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(output_path, true), "utf-8"));
        ArrayList<String> source_list = new ArrayList<>();

        File dir = new File(".\\data\\output");            // ouput的存放地址
        File[] files = dir.listFiles();
        for(File f : files){
            System.out.println(f.getName());
            if(!f.isFile()){
                File[] list_of_files = f.listFiles();
                for (File ff : list_of_files){
                    String source_string = "";

                    FileInputStream fis = new FileInputStream(ff.getPath());
                    BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                    String line_temp = br.readLine();
                    while((line_temp = br.readLine()) != null){
                        count++;
                        if(count%100000 == 0){
                            System.out.println(count);
                        }

                        String temp1 = line_temp.substring(0, line_temp.indexOf("\t"));
                        if (temp1.contains("\"")){
                            temp1 = temp1.replace("\"", "");
                        }
                        source_string = temp1 + line_temp.substring(line_temp.indexOf("\t")) + "\n";

                        source_list.add(source_string);
                        if (source_list.size() >= 1000000){        // 满1000000条就输出
                            System.out.println("outputing...");
                            for (String s : source_list){
                                pw.write(s);
                                pw.flush();
                            }
                            source_list.clear();
                            System.out.println("output over");
                        }
                    }
                    br.close();
                    fis.close();
                }
            }
        }
        if (source_list.size() != 0){
            System.out.println("outputing...");
            for (String s : source_list){
                pw.write(s);
                pw.flush();
            }
            source_list.clear();
            System.out.println("output over");
        }
        pw.close();
    }

    private void combineJsonToText(String inputPath, String outputPath, String field) throws Exception{
        try {
            File file_output = new File(outputPath);        //文件路径（路径+文件名）
            if (!file_output.exists()) {    //文件不存在则创建文件，先创建目录
                File dir_output = new File(file_output.getParent());
                dir_output.mkdirs();
                file_output.createNewFile();
            }

            String sourceString = "paper_id\t" + field + "\n";
            FileOutputStream outStream = new FileOutputStream(file_output);    //文件输出流用于将数据写入文件
            byte[] sourceByte = sourceString.getBytes();
            outStream.write(sourceByte);
            outStream.close();    //关闭文件输出流

        } catch (Exception e) {
            e.printStackTrace();
        }
        ArrayList<String> string_list = new ArrayList<>();
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(outputPath, true), "utf-8"));

        int count = 0;
        File dir = new File(inputPath);            // text的存放地址
        File[] files = dir.listFiles();
        for(File f : files){
            count++;
            if (count%10000 == 0){
                System.out.println(count);
            }
            String source_string = "";
            FileInputStream fis1 = new FileInputStream(f.getPath());
            BufferedReader br1 = new BufferedReader(new InputStreamReader(fis1));
            String doc_id = f.getName();
            doc_id = doc_id.substring(0, doc_id.indexOf(".txt"));
            String line_temp = "";
            while((line_temp = br1.readLine()) != null){
                source_string += "paper_id= " + doc_id + "\t" + field + "= " + line_temp + "\n";
            }
            string_list.add(source_string);
            if (string_list.size() == 10000){
                System.out.println("outputing...");
                for (String s : string_list){
                    pw.write(s);
                    pw.flush();
                }
                string_list.clear();
                System.out.println("output over");
            }
            br1.close();
            fis1.close();
        }
        if (string_list.size() != 0 ){
            System.out.println("outputing...");
            for (String s : string_list){
                pw.write(s);
                pw.flush();
            }
            string_list.clear();
            System.out.println("output over");
        }
        pw.close();
    }

    private void createFile(String outputPath, String sourceString) throws Exception{
        File newFile = new File(outputPath);
        if (newFile.exists()){
            newFile.delete();
        }
        //目录不存在 则创建
        if (!newFile.getParentFile().exists()) {
            boolean mkdir = newFile.getParentFile().mkdirs();
            if (!mkdir) {
                throw new RuntimeException("创建目标文件所在目录失败！");
            }
        }
        newFile.createNewFile();
        FileOutputStream outStream = new FileOutputStream(outputPath);    //文件输出流用于将数据写入文件
        byte[] sourceByte = sourceString.getBytes();
        outStream.write(sourceByte);
        outStream.close();    //关闭文件输出流
    }

    private ArrayList<ArrayList<String>> getDiseaseName(String filePath) throws Exception{
        // 收集疾病及其同义词至列表
        System.out.println("get disease name");
        ArrayList<ArrayList<String>> keyword_list = new ArrayList<>();
        FileInputStream fileInputStream = new FileInputStream(filePath);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
        String line = bufferedReader.readLine();
        while((line = bufferedReader.readLine()) != null) {
            String[] arr_temp = line.split("\t");
            ArrayList<String> keywords = new ArrayList<>();
            keywords.add(arr_temp[1].replace("\"", ""));
            if (arr_temp.length >= 3) {
                String[] syn_arr = arr_temp[2].split(";");
                for (String syn : syn_arr) {
                    keywords.add(syn.replace("\"", ""));
                }
            }
            keyword_list.add(keywords);
        }
        fileInputStream.close();
        bufferedReader.close();
        System.out.println("get disease name finished");
        return keyword_list;

    }

    private ArrayList<String> getDiseaseNameInOutput(String outputPath) throws Exception{
        System.out.println("get disease name in output");
        File outputFile = new File(outputPath);
        ArrayList<String> d_in_output = new ArrayList<>();
        LineIterator lit0 = FileUtils.lineIterator(outputFile, "UTF-8");
        String l_temp = lit0.nextLine();
        while (lit0.hasNext()){
            l_temp = lit0.nextLine();
            String[] k_arr = l_temp.split("\t");
            if (!d_in_output.contains(k_arr[0])){
                d_in_output.add(k_arr[0]);
            }
        }
        lit0.close();
        System.out.println("get disease name in output finished");
        return d_in_output;
    }

    private void setDocPart1(String outputPath, String newPath) throws Exception{
        String sourceString = "key1\t" + "doc_id\t" + "sen_text\t" + "text_field\n";
        this.createFile(newPath, sourceString);
        ArrayList<ArrayList<String>> keyword_list = this.getDiseaseName(".\\data\\relations\\DiseaseInfo.txt");

        // 对文档进行去重
        ArrayList<String> source_list = new ArrayList<>();
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(newPath, true), "utf-8"));
        // 获取ouput文档中的disease name
        ArrayList<String> disease_in_output = this.getDiseaseNameInOutput(outputPath);

        FileInputStream fis = new FileInputStream(outputPath);
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
        for(int i = 0; i < keyword_list.size(); i++) {              // 以disease name循环
            ArrayList<String> sen_text_list = new ArrayList<>();
            String key1 = keyword_list.get(i).get(0);
            System.out.println((i + 1) + "|6494\t" + key1);
            ArrayList<String> source_temp_list = new ArrayList<>();
            for (String s : keyword_list.get(i)) {                  // 对disease name及同义词进行循环
                if (disease_in_output.contains(s)) {
                    fis = new FileInputStream(outputPath);
                    br = new BufferedReader(new InputStreamReader(fis));
                    String line_temp = br.readLine();
                    while ((line_temp = br.readLine()) != null) {

                        String[] line_arr = line_temp.split("\t");
                        if (keyword_list.get(i).contains(line_arr[0])) {
                            String sen_temp = line_arr[3];
                            String new_line = key1 + "\t" + line_arr[2] + "\t" + sen_temp + "\t" + line_arr[4] + "\n";
                            if (!sen_text_list.contains(sen_temp)) {
                                sen_text_list.add(sen_temp);
                                source_temp_list.add(new_line);
                            }
                        }
                    }
                    source_list.addAll(source_temp_list);
                    if (source_list.size() >= 1000) {
                        System.out.println("outputing...");
                        for (String s2 : source_list) {
                            pw.write(s2);
                            pw.flush();
                        }
                        source_list.clear();
                        System.out.println("output over");
                    }
                }
            }
        }
        if (source_list.size() != 0) {
            System.out.println("outputing...");
            for (String s2 : source_list) {
                pw.write(s2);
                pw.flush();
            }
            source_list.clear();
            System.out.println("output over");
        }
        pw.close();
        br.close();
        fis.close();
    }

    private void setDoc(String inputPath, String newPath) throws Exception{
        // 对文档二次去重
        String sourceString = "key1\t" + "doc_id\t" + "sen_text\t" + "text_field\n";
        this.createFile(newPath, sourceString);

        // 给doc_id	和 pmc_id建立关系表
        String docInfo_path = ".\\data\\relations\\DocInfo.txt";
        FileInputStream fis_pmc_doc = new FileInputStream(docInfo_path);
        BufferedReader br_pmc_doc = new BufferedReader(new InputStreamReader(fis_pmc_doc));
        HashMap<String, String> pmc_doc_map = new HashMap<>();   // 初始化一个map
        String line_pmc_doc = br_pmc_doc.readLine();
        while ((line_pmc_doc = br_pmc_doc.readLine()) != null){
            String[] pmc_doc_arr = line_pmc_doc.split("\t");
            pmc_doc_map.put(pmc_doc_arr[3], pmc_doc_arr[0]);            // 把doc_PMC数据存入map
        }
        System.out.println("pmc_doc_map build finished");
        br_pmc_doc.close();
        fis_pmc_doc.close();

        // 需要处理数据的文件位置
        FileReader fileReader = new FileReader(new File(inputPath));
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(newPath, true), "utf-8"));
        String readLine = bufferedReader.readLine();

        int count = 0;
        Set<String> lines_set = new LinkedHashSet<>();
        while ((readLine = bufferedReader.readLine()) != null) {
            String line = readLine.trim();
            String key1_temp = line.substring(0, line.indexOf("\t"));       // key1字符串
            String doc_id_to_end = line.substring(line.indexOf("\t") + 1);  // 从doc_id到整行结束
            String doc_id_old = doc_id_to_end.substring(0, doc_id_to_end.indexOf("\t"));    // 截取doc_id
            if (doc_id_old.substring(0,3).equals("PMC")){
                String new_doc_id = pmc_doc_map.get(doc_id_old);
                line = line.replace(doc_id_old, new_doc_id);
            }
            lines_set.add(line);
            if (lines_set.size() >= 1500000){
                System.out.println("outputing...");
                for (String s : lines_set){
                    pw.write(s + "\n");
                    pw.flush();
                }
                lines_set.clear();
                System.out.println("output over");
            }

            count++;
            if(count%100000 == 0){
                System.out.println(count);
            }
        }
        for (String s : lines_set){
            pw.write(s + "\n");
            pw.flush();
        }
        System.out.println("set finished");

        pw.close();
        bufferedReader.close();
        fileReader.close();
    }

    private void createIndex(String srcFilePath, String index_path) throws Exception {
        Directory directory = FSDirectory.open(new File(index_path).toPath());
        IndexWriter indexWriter = new IndexWriter(directory, new IndexWriterConfig());
        indexWriter.deleteAll();

        int count = 0;
        File dir = new File(srcFilePath);
        File[] files = dir.listFiles();
        for (File f : files) {
            if(!f.isFile()){
                File[] list_of_files = f.listFiles();
                for (File ff : list_of_files){
                    String path = ff.getPath();
                    String line_temp;
                    FileInputStream fileInputStream = new FileInputStream(path);
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
                    line_temp = bufferedReader.readLine();
                    while ((line_temp = bufferedReader.readLine()) != null) {
                        count++;
                        if (count%10000 == 0){
                            System.out.println(count);
                        }
                        String id = line_temp.substring(9,line_temp.indexOf("\t")).trim();
                        String text = line_temp.substring(line_temp.indexOf("\t") + 1).trim();
                        Field field1 = new TextField("doc_id", id, Field.Store.YES);
                        Field field2 = new TextField("doc_text", text, Field.Store.YES);
                        Document document = new Document();
                        document.add(field1);
                        document.add(field2);
                        indexWriter.addDocument(document);
                    }
                }
            }
        }
        indexWriter.close();
        System.out.println("index build finished");
    }

    private void createIndexForOutPut(String srcFilePath, String index_path) throws Exception {
        Directory directory = FSDirectory.open(new File(index_path).toPath());
        IndexWriter indexWriter = new IndexWriter(directory, new IndexWriterConfig());
        indexWriter.deleteAll();

        int count = 0;
        String line_temp;
        FileInputStream fileInputStream = new FileInputStream(srcFilePath);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
        line_temp = bufferedReader.readLine();
        while ((line_temp = bufferedReader.readLine()) != null) {
            count++;
            if (count % 10000 == 0) {
                System.out.println(count);
            }
            String[] line_arr = line_temp.split("\t");
            if (line_arr.length >= 4) {
                String key1 = line_arr[0].trim();
                String doc_id = line_arr[1].trim();
                String sen_text = line_arr[2].trim();
                String text_field = line_arr[3].trim();
                Field field1 = new TextField("key1", key1, Field.Store.YES);
                Field field2 = new TextField("doc_id", doc_id, Field.Store.YES);
                Field field3 = new TextField("sen_text", sen_text, Field.Store.YES);
                Field field4 = new TextField("text_field", text_field, Field.Store.YES);
                Document document = new Document();
                document.add(field1);
                document.add(field2);
                document.add(field3);
                document.add(field4);
                indexWriter.addDocument(document);
            }
        }
        indexWriter.close();
        System.out.println("index build finished");
    }

    public void reportPart1(String test_file, String reportFileName) throws Exception{
        String first_line = "disease_name\t" + "DOID\t" + "pubmed_id\t" + "doi\t" + "title\t" + "publish_time\t"
                + "authors\t" + "journal\t" + "pairs_count_abstract\t" + "pairs_count_body_text\t"
                + "count_disease_abstract\t" + "count_disease_body\t" + "count_covid_abstract\t"
                + "count_covid_body\t" + "match_sens_in_abstract\t" + "match_sens_in_body_text\t" + "doc_id\n";


        try {
            File file_output = new File(reportFileName);        //文件路径（路径+文件名）
            if (!file_output.exists()) {    //文件不存在则创建文件，先创建目录
                File dir_output = new File(file_output.getParent());
                dir_output.mkdirs();
                file_output.createNewFile();
            }

            FileOutputStream outStream = new FileOutputStream(file_output);    //文件输出流用于将数据写入文件
            byte[] sourceByte = first_line.getBytes();
            outStream.write(sourceByte);
            outStream.close();    //关闭文件输出流

        } catch (Exception e) {
            e.printStackTrace();
        }


        String diseaseInfo_path = ".\\data\\relations\\DiseaseInfo.txt";
        String docInfo_path = ".\\data\\relations\\DocInfo.txt";
        String covid_syn_path = ".\\data\\relations\\COVID-19_synonym";
        String doc_sen_path = ".\\data\\relations\\doc sen relations.txt";


        // 给disease信息建hashmap, DOID	Disease_NAME	Synonym
        FileInputStream fis_disease = new FileInputStream(diseaseInfo_path);
        BufferedReader br_disease = new BufferedReader(new InputStreamReader(fis_disease));
        HashMap<String, ArrayList<String>> disease_map = new HashMap<>();   // 初始化一个map
        String line_disease = br_disease.readLine();
        ArrayList<String> keywords = new ArrayList<>();         // 把disease name作为输出报告的主关键词，存入列表中

        while((line_disease = br_disease.readLine()) != null){
            String[] arr_temp = line_disease.split("\t");
            String key_temp = arr_temp[1];
            if (key_temp.contains("\"")){
                key_temp = key_temp.substring(1, key_temp.length()-1);
            }
            key_temp = key_temp.replace("\"", "");
            keywords.add(key_temp);                          // 把disease作为主要关键词存入列表
            ArrayList<String> value_list = new ArrayList<>();
            value_list.add(arr_temp[0]);
            if(arr_temp.length >= 3) {
                String value_temp = arr_temp[2];
                value_temp = value_temp.replace("\"", "");
                value_list.add(value_temp);
            }
            disease_map.put(arr_temp[1], value_list);           // 把DiseaseInFo的数据存入map
        }
        System.out.println("disease_map build finished");
        br_disease.close();
        fis_disease.close();

//        for (Map.Entry<String, ArrayList<String>> test:disease_map.entrySet()){   // 查看map内容
//            System.out.println("key="+test.getKey()+",values="+test.getValue());
//        }


        // 给doc信息建立hashmap, doc_id	pubmed_id	doi	pmc_id	title	publish_time	authors	journal
        FileInputStream fis_docInfo = new FileInputStream(docInfo_path);
        BufferedReader br_docInfo = new BufferedReader(new InputStreamReader(fis_docInfo));
        HashMap<String, ArrayList<String>> docInfo_map = new HashMap<>();   // 初始化一个map
        String line_docInfo = br_docInfo.readLine();
        while ((line_docInfo = br_docInfo.readLine()) != null){
            ArrayList<String> doc_Info_detail = new ArrayList<>();

            String[] doc_arr = line_docInfo.split("\t");
            for(int i = 1; i < doc_arr.length; i++){
                doc_Info_detail.add(doc_arr[i]);
            }
            docInfo_map.put(doc_arr[0], doc_Info_detail);       // 把docInfo数据存入map,关键词为doc_id
        }
        System.out.println("docInfo_map build finished");
        br_docInfo.close();
        fis_docInfo.close();


        // 给doc_id	和 pmc_id建立关系表
        FileInputStream fis_pmc_doc = new FileInputStream(docInfo_path);
        BufferedReader br_pmc_doc = new BufferedReader(new InputStreamReader(fis_pmc_doc));
        HashMap<String, String> pmc_doc_map = new HashMap<>();   // 初始化一个map
        String line_pmc_doc = br_pmc_doc.readLine();
        while ((line_pmc_doc = br_pmc_doc.readLine()) != null){
            String[] pmc_doc_arr = line_pmc_doc.split("\t");
            pmc_doc_map.put(pmc_doc_arr[3], pmc_doc_arr[0]);            // 把doc_PMC数据存入map
        }
        System.out.println("pmc_doc_map build finished");
        br_pmc_doc.close();
        fis_pmc_doc.close();


        // 把covid-19近义词存入列表
        FileInputStream fis_covid_syn = new FileInputStream(covid_syn_path);
        BufferedReader br_covid_syn = new BufferedReader(new InputStreamReader(fis_covid_syn));
        ArrayList<String> covid_syn_list = new ArrayList<>();
        String line_temp;
        while((line_temp = br_covid_syn.readLine()) != null){
            covid_syn_list.add(line_temp);
        }
        br_covid_syn.close();
        fis_covid_syn.close();


        // 给doc sen relations建立hashmap, doc_id  sen_id_start	sen_id_end	abstract_length
        FileInputStream fis_doc_sen = new FileInputStream(doc_sen_path);
        BufferedReader br_doc_sen = new BufferedReader(new InputStreamReader(fis_doc_sen));
        HashMap<String, ArrayList<Integer>> doc_sen_map = new HashMap<>();  // 初始化一个map
        String line_doc_sen = br_doc_sen.readLine();
        while((line_doc_sen = br_doc_sen.readLine()) != null){
            String[] doc_sen_arr = line_doc_sen.split("\t");
            String doc_id_info = doc_sen_arr[0];
            if (doc_id_info.contains("-abstract")){
                doc_id_info = doc_id_info.substring(0, doc_id_info.indexOf("-abstract"));
            }
            int sen_id_start_info = Integer.valueOf(doc_sen_arr[1]);
            int sen_id_end_info = Integer.valueOf(doc_sen_arr[2]);
            int abstract_length_info = Integer.valueOf(doc_sen_arr[3]);
            ArrayList<Integer> sen_num = new ArrayList<>();
            sen_num.add(sen_id_start_info);
            sen_num.add(sen_id_end_info);
            sen_num.add(abstract_length_info);
            doc_sen_map.put(doc_id_info, sen_num);
        }
        System.out.println("doc_sen_map build finished");
        br_doc_sen.close();
        fis_doc_sen.close();


        //***********************************************************************************************
        // 现有keywords, disease_map, docInfo_map, covid_syn_list

        // 打开索引
        String indexPath1 = ".\\data\\index2";
        Directory directory1 = FSDirectory.open(new File(indexPath1).toPath());
        IndexReader indexReader1 = DirectoryReader.open(directory1);
        IndexSearcher indexSearcher1 = new IndexSearcher(indexReader1);

        String indexPath2 = ".\\data\\indexOfOutput";
        Directory directory2 = FSDirectory.open(new File(indexPath2).toPath());
        IndexReader indexReader2 = DirectoryReader.open(directory2);
        IndexSearcher indexSearcher2 = new IndexSearcher(indexReader2);

        ArrayList<String> disease_name_in_output = this.getDiseaseNameInOutput(test_file);
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(reportFileName, true), "utf-8"));

        // 以disease name为关键词，统计数据
        ArrayList<String> src_list = new ArrayList<>();
        int process = 0;
        int process_size = disease_name_in_output.size();         // 记录总进度
        for(String key1_first : disease_name_in_output){                                      // keywords已去除双引号
            process++;
            System.out.println(process + "/" + process_size + "\t" + key1_first);

            ArrayList<String> disease_syn = new ArrayList<>();
            disease_syn.add(key1_first);                                        // 把disease name存入关键词1数组
            String key_temp = key1_first;
            if(key1_first.contains(",")){
                key_temp = "\"" + key1_first + "\"";                          // 与map中格式保持一致
            }
            ArrayList<String> v_disease = disease_map.get(key_temp);
            if (v_disease.size() == 2) {                                        // 若有同义词，依次放入数组
                String[] v_dis_syn_arr = v_disease.get(1).split(";");
                for (String v_dis : v_dis_syn_arr){
                    disease_syn.add(v_dis.replace("\"", ""));
                }
            }

            ArrayList<String> syn_lower_list = new ArrayList<>();
            ArrayList<String> doc_id_list = new ArrayList<>();          // 获取包含disease_name的文本的doc_id（转换后）
            ArrayList<String> doc_id_list_set = new ArrayList<>();      // 获取去重的doc_id列表

            String regEx="[\n`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。， 、？•]";
            String aa = " ";
            Pattern p = Pattern.compile(regEx);
            for (String key1_str : disease_syn) {
                // 处理disease name，去除特殊字符，字母转小写
                String key1_str_low = key1_str.toLowerCase().replace("-", " ");
                for (int i = 0; i < regEx.length(); i++) {
                    char a = regEx.charAt(i);
                    if (key1_str_low.contains(String.valueOf(a))) {
                        Matcher m = p.matcher(key1_str_low);
                        key1_str_low = m.replaceAll(aa).trim();
                        break;
                    }
                }
                syn_lower_list.add(key1_str_low);       // 去除特殊字符，转为小写的disease name及同义词的列表
            }

            //  读取output文档, key1 doc_id sen text_field
            FileInputStream fis1 = new FileInputStream(test_file);
            BufferedReader br1 = new BufferedReader(new InputStreamReader(fis1));
            String line_temp2 = br1.readLine();
            while ((line_temp2 = br1.readLine()) != null) {
                if (key1_first.equals(line_temp2.substring(0, line_temp2.indexOf("\t")))) {  // 如果该行的disease name与for循环的disease name 相同
                    String[] line_arr = line_temp2.split("\t");
                    String doc_id = line_arr[1];          // 获取doc_id
                    if (doc_id.substring(0, 3).equals("PMC")) {         // 如果是PMC开头的文件，转换为相应的doc_id
                        doc_id = pmc_doc_map.get(doc_id);
                    }
                    doc_id_list.add(doc_id);        // 把doc_id存入列表，转换后
                }
            }
            br1.close();
            fis1.close();

            for (String di : doc_id_list){              // 获取去重的doc_id列表
                if(!doc_id_list_set.contains(di)){
                    doc_id_list_set.add(di);
                }
            }

            ArrayList<Integer> pairs_count_abstract_list = new ArrayList<>();
            ArrayList<Integer> pairs_count_body_text_list = new ArrayList<>();
            ArrayList<ArrayList<String>> body_sen_list = new ArrayList<>();             // 统计匹配到的句子列表
            ArrayList<ArrayList<String>> abstract_sen_list = new ArrayList<>();        // 统计匹配到的句子列表

            int cc = 0;
            // doc_id_list_set  doc_id_count
            for (String doc_j : doc_id_list_set) {     // 统计出现的疾病词对匹配到的句子列表
                String doc_id_lower = doc_j.toLowerCase();

                int pairs_count_a = 0;
                int pairs_count_b = 0;
                ArrayList<String> match_sens_in_body_text = new ArrayList<>();
                ArrayList<String> match_sens_in_abstract_text = new ArrayList<>();

                cc++;

                TermQuery query2 = new TermQuery(new Term("doc_id", doc_id_lower));
                TopDocs topDocs = indexSearcher2.search(query2, 10);
                ScoreDoc[] scoreDocs = topDocs.scoreDocs;
                for (ScoreDoc doc : scoreDocs) {
                    int document_id = doc.doc;
                    Document document = indexSearcher2.doc(document_id);
                    String key1 = document.get("key1");
                    if(key1.equals(key1_first)){
                        if(document.get("text_field").equals("body_text")) {
                            match_sens_in_body_text.add(document.get("sen_text"));
                            pairs_count_b++;
                        }
                        else if(document.get("text_field").equals("abstract_text")) {
                            match_sens_in_abstract_text.add(document.get("sen_text"));
                            pairs_count_a++;
                        }
                    }
                }

                System.out.println("to String " + cc);
                body_sen_list.add(match_sens_in_body_text);         // 该doc_id中匹配的正文中的句子全部加入列表
                abstract_sen_list.add(match_sens_in_abstract_text); // 摘要中的句子
                pairs_count_abstract_list.add(pairs_count_a);       // 摘要中满足条件的句子的个数
                pairs_count_body_text_list.add(pairs_count_b);      // 正文中满足条件的句子的个数
            }


            System.out.println("make conclusion");
            for (int i = 0; i < doc_id_list_set.size(); i++){

                String disease_name = key1_first;
                String disease_name_quota = disease_name;
                if (disease_name.contains(",")){
                    disease_name_quota = "\"" + disease_name + "\"";
                }
                String DOID = disease_map.get(disease_name_quota).get(0);
                String doc_id = doc_id_list_set.get(i);
                String pubmed_id = docInfo_map.get(doc_id).get(0);
                String doi = docInfo_map.get(doc_id).get(1);
                String title = docInfo_map.get(doc_id).get(3);
                String publish_time = docInfo_map.get(doc_id).get(4);
                String authors = docInfo_map.get(doc_id).get(5);
                String journal = docInfo_map.get(doc_id).get(6);
                int pairs_count_abstract = pairs_count_abstract_list.get(i);
                int pairs_count_body_text = pairs_count_body_text_list.get(i);
                String match_sens_in_abstract = abstract_sen_list.get(i).toString();
                String match_sens_in_body_text = body_sen_list.get(i).toString();

                String abstract_temp = "";
                String body_temp = "";
                int count_disease_abstract = 0;
                int count_disease_body = 0;
                int count_covid_abstract = 0;
                int count_covid_body = 0;

                boolean flag = true;
                if(doc_id.substring(0,3).equals("PMC")){
                    flag = false;
                }
                String doc_id_lower = doc_id.toLowerCase().trim();

                if (flag) {
                    TermQuery query1 = new TermQuery(new Term("doc_id", doc_id_lower));
                    TopDocs topDocs = indexSearcher1.search(query1, 10);
                    ScoreDoc[] scoreDocs = topDocs.scoreDocs;
                    for (ScoreDoc doc : scoreDocs) {
                        int document_id = doc.doc;
                        Document document = indexSearcher1.doc(document_id);
                        String text_temp = document.get("doc_text");
                        String[] line_arr3 = text_temp.split("\t");
                        abstract_temp = line_arr3[0].substring(9).toLowerCase();
                        body_temp = line_arr3[1].substring(10).toLowerCase();

                        SplitDocument splitDocument = new SplitDocument();
                        ArrayList<String> abstract_list_temp = splitDocument.getSentence(abstract_temp);
                        ArrayList<String> body_text_list_temp = splitDocument.getSentence(body_temp);
                        ArrayList<String> abstract_list = new ArrayList<>();
                        ArrayList<String> body_text_list = new ArrayList<>();
                        for (String ab : abstract_list_temp){
                            Matcher m = p.matcher(ab);
                            String temp = m.replaceAll(aa).trim().replace("-", " ").replace("\"", "");
                            abstract_list.add(temp);
                        }
                        for (String bt : body_text_list_temp){
                            Matcher m = p.matcher(bt);
                            String temp = m.replaceAll(aa).trim().replace("-", " ").replace("\"", "");
                            body_text_list.add(temp);
                        }

                        for (String d1 : syn_lower_list) {
                            for (String line1 : abstract_list) {
                                if (line1.contains(d1)) {
                                    count_disease_abstract++;
                                }
                            }
                        }

                        for (String d2 : syn_lower_list) {
                            for (String line2 : body_text_list) {
                                if (line2.contains(d2)) {
                                    count_disease_body++;
                                }
                            }
                        }

                        for (String c1 : covid_syn_list) {
                            c1 = c1.toLowerCase().replace("-", " ");
                            for (String line3 : abstract_list) {
                                if (line3.contains(c1)) {
                                    count_covid_abstract++;
                                }
                            }
                        }

                        for (String c2 : covid_syn_list) {
                            c2 = c2.toLowerCase().replace("-", " ");
                            for (String line4 : body_text_list) {
                                if (line4.contains(c2)) {
                                    count_covid_body++;
                                }
                            }
                        }
                    }
                }

                if(!flag) {

                    TermQuery query2 = new TermQuery(new Term("doc_id", doc_id_lower));
                    TopDocs topDocs = indexSearcher1.search(query2, 10);
                    ScoreDoc[] scoreDocs = topDocs.scoreDocs;
                    for (ScoreDoc doc : scoreDocs) {
                        int document_id = doc.doc;
                        Document document = indexSearcher1.doc(document_id);
                        String text_temp = document.get("doc_text");
                        body_temp = text_temp.substring(text_temp.indexOf("body_text=") + 10).toLowerCase();

                        SplitDocument splitDocument1 = new SplitDocument();
                        ArrayList<String> body_text_list_temp1 = splitDocument1.getSentence(body_temp);
                        ArrayList<String> body_text_list1 = new ArrayList<>();
                        for (String bt : body_text_list_temp1) {
                            Matcher m = p.matcher(bt);
                            String temp = m.replaceAll(aa).trim().replace("-", " ").replace("\"", "");
                            body_text_list1.add(temp);
                        }

                        for (String d2 : syn_lower_list) {
                            for (String line2 : body_text_list1) {
                                if (line2.contains(d2)) {
                                    count_disease_body++;
                                }
                            }
                        }

                        for (String c2 : covid_syn_list) {
                            c2 = c2.toLowerCase().replace("-", " ");
                            for (String line4 : body_text_list1) {
                                if (line4.contains(c2)) {
                                    count_covid_body++;
                                }
                            }
                        }
                    }
                }

                String source_string = disease_name + "\t" + DOID + "\t" + pubmed_id + "\t" + doi + "\t"
                        + title + "\t" + publish_time + "\t" + authors + "\t" + journal + "\t"
                        + pairs_count_abstract + "\t" + pairs_count_body_text + "\t" + count_disease_abstract + "\t"
                        + count_disease_body + "\t" + count_covid_abstract + "\t" + count_covid_body + "\t"
                        + match_sens_in_abstract + "\t" + match_sens_in_body_text + "\t" + doc_id + "\n";

                src_list.add(source_string);
                if(src_list.size() == 1000){
                    System.out.println("outputing...");
                    for (String s : src_list){
                        pw.write(s);
                        pw.flush(); // 需要刷新缓冲区才能输出
                    }
                    src_list.clear();
                    System.out.println("output over");
                }
            }
        }
        if (src_list.size() != 0){
            System.out.println("outputing...");
            for (String s : src_list){
                pw.write(s);
                pw.flush(); // 需要刷新缓冲区才能输出
            }
            src_list.clear();
            System.out.println("output over");
        }
        pw.close();
        indexReader1.close();
        indexReader2.close();
    }

    public static void main(String[] args) throws Exception {
        MakeReport makeReport = new MakeReport();
        makeReport.combineFiles();
        makeReport.setDocPart1(".\\data\\final_output\\final_output.txt", ".\\data\\final_output\\final_output_set_temp.txt");
        makeReport.setDoc(".\\data\\final_output\\final_output_set_temp.txt",".\\data\\final_output\\final_output_set.txt");

        System.out.println("create index");
        makeReport.createIndex(".\\data\\json_to_text", ".\\data\\index2");
        makeReport.createIndexForOutPut(".\\data\\final_output\\final_output_set.txt", ".\\data\\indexOfOutput");
        makeReport.reportPart1(".\\data\\final_output\\final_output_set.txt",".\\data\\report\\report_part1.txt");

//        PythonInterpreter pythonInterpreter = new PythonInterpreter();
    }
}
