package test;


import basic.SplitDocument;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.omg.PortableInterceptor.INACTIVE;

import java.io.*;
import java.lang.reflect.Array;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/*
该算法用于建立索引，并根据索引进行查询
 */

public class SearchTest {

    private static long output_time = 0;
    private static long map_time = 0;

    private int getSenCount(String filePath) throws Exception{
        //用于统计abstract中的句子总数
        int sen_count = 0;
        File dir = new File(filePath);
        File[] files = dir.listFiles();
        for (File f : files) {
            String path = f.getPath();
            FileInputStream fileInputStream = new FileInputStream(path);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
            String line_temp;
            while((line_temp = bufferedReader.readLine()) != null){
                sen_count++;
            }
        }
        System.out.println("abstract sens count: " + sen_count);
        return sen_count;
    }

    private void writeNewFile(String output_path, String sourceString){
        try {
            File file_output = new File(output_path);        //文件路径（路径+文件名）
            if (!file_output.exists()) {    //文件不存在则创建文件，先创建目录
                File dir_output = new File(file_output.getParent());
                dir_output.mkdirs();
                file_output.createNewFile();
            }

            FileOutputStream outStream = new FileOutputStream(file_output);    //文件输出流用于将数据写入文件
            byte[] sourceByte = sourceString.getBytes();
            outStream.write(sourceByte);
            outStream.close();    //关闭文件输出流

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void createIndex(String srcFilePath, String index_path, int sen_start_id) throws Exception {
        // 把索引库保存在磁盘
        Directory directory = FSDirectory.open(new File(index_path).toPath());
        // 基于Directory对象创建一个IndexWriter对象
        IndexWriter indexWriter = new IndexWriter(directory, new IndexWriterConfig());
        // 读取磁盘上的文件，对应每个文件创建一个文档对象
        SplitDocument splitDoc = new SplitDocument();
        //删除全部文档
        indexWriter.deleteAll();

        File dir = new File(srcFilePath);
        File[] files = dir.listFiles();
        int sen_id = sen_start_id;

        for (File f : files) {
            String path = f.getPath();
            String line_temp;
            FileInputStream fileInputStream = new FileInputStream(path);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));

            while ((line_temp = bufferedReader.readLine()) != null) {
                // 创建field
                // 参数1：域的名称，参数2：域的内容，参数3：是否存储
                Field field1 = new TextField("sen_id", String.valueOf(sen_id), Field.Store.YES);
                Field field2 = new TextField("sen_text", line_temp, Field.Store.YES);
                sen_id++;
                // 创建文档对象
                Document document = new Document();
                // 向文档对象中添加域
                document.add(field1);
                document.add(field2);
                // 把文档对象写入索引库
                indexWriter.addDocument(document);
            }
        }
        // 关闭indexwriter对象
        indexWriter.close();
        System.out.println("index build finished");
    }


    private HashMap<Integer,List<String>> getHashMap() throws Exception{
        // 将doc和sen的关系存入HashMap
        HashMap<Integer, List<String>> hashMap = new HashMap<>();
        String path = ".\\test\\relations\\doc sen relations.txt";
        FileInputStream fileInputStream = new FileInputStream(path);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
        String line_temp = bufferedReader.readLine();
        line_temp = bufferedReader.readLine();
        while (line_temp != null) {
            String[] values = line_temp.split("\t");
            int key = Integer.valueOf(values[1]);
            String[] value_list = {values[0], values[3]};
            hashMap.put(key, Arrays.asList(value_list));

            line_temp = bufferedReader.readLine();
        }

        fileInputStream.close();
        bufferedReader.close();
        return hashMap;
    }

    private int[] getKeyArrays() throws Exception{
        // 将sen_id_start存入数组中
        ArrayList<Integer> key_list = new ArrayList<>();
        String path = ".\\test\\relations\\doc sen relations.txt";
        FileInputStream fileInputStream = new FileInputStream(path);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
        String line_temp = bufferedReader.readLine();
        while ((line_temp = bufferedReader.readLine()) != null) {
            String[] values = line_temp.split("\t");
            key_list.add(Integer.valueOf(values[1]));
        }
        fileInputStream.close();
        bufferedReader.close();
        int[] key_arr = new int[key_list.size()];
        for(int i = 0; i < key_list.size(); i++){
            key_arr[i] = key_list.get(i);
        }
        return key_arr;
    }

    private HashMap<String[],String> getScourceKeyMap(ArrayList<String> list1, ArrayList<String> list2, ArrayList<String[]> arr1, ArrayList<String[]> arr2) throws Exception{
        // 将处理过后的disease name和处理前的disease name存入HashMap
        HashMap<String[],String> hashMap = new HashMap<>();
        ArrayList<String> list = new ArrayList<>();
        list.addAll(list1);
        list.addAll(list2);
        ArrayList<String[]> arr = new ArrayList<>();
        arr.addAll(arr1);
        arr.addAll(arr2);
        for(int i = 0; i < list.size(); i++) {
            String[] key = arr.get(i);
            String value = list.get(i);
            hashMap.put(key, value);
        }
//        for (Map.Entry<String[], String> test:hashMap.entrySet()){
//            System.out.println("key="+Arrays.toString(test.getKey())+",values="+test.getValue());
//        }
        System.out.println("HashMap build finished");
        return hashMap;
    }


    private void searchIndex(ArrayList<String[]> key_arr_list1, ArrayList<String[]> key_arr_list2, String indexPath, String outputPath, HashMap<Integer,List<String>> hashMap1, HashMap<String[],String> hashMap2,int[] sen_id_start_arr) throws Exception {
        // 1.创建一个Directory对象，指定索引库的位置
        Directory directory = FSDirectory.open(new File(indexPath).toPath());
        // 2.创建一个IndexReader对象
        IndexReader indexReader = DirectoryReader.open(directory);
        // 3.创建一个IndexSearcher对象，构造方法中的参数indexReader对象
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        // 4.创建一个Query对象
        String tempString = "";
        ArrayList<String> string_list = new ArrayList<>();
        for (String[] keyword1_arr : key_arr_list1) {
            for (String[] keyword2_arr : key_arr_list2) {
                for (String s1 : keyword1_arr) {        // 输出关键词1至控制台，便于查看进度
                    System.out.print(s1 + " ");
                }
                System.out.print(" + ");
                for (String s2 : keyword2_arr) {        // 输出关键词2至控制台，便于查看进度
                    System.out.print(s2 + " ");
                }
                System.out.println();
                System.out.println("-------------");
                try {
                    // 对分割后的单词或短语创建查询，要求短语中的单词连续出现
                    PhraseQuery query1 = new PhraseQuery(0, "sen_text", keyword1_arr);
                    PhraseQuery query2 = new PhraseQuery(0, "sen_text", keyword2_arr);
                    // 利用BooleanQuery查询同时出现两个关键词语句
                    BooleanQuery.Builder query = new BooleanQuery.Builder();
                    // 满足查询要求的文档必须包含query1,MUST必须包含，SHOULD可以包含，MUST_NOT不包含
                    query.add(query1, BooleanClause.Occur.MUST);
                    query.add(query2, BooleanClause.Occur.MUST);
                    // 5.执行查询，得到一个TopDocs对象
                    TopDocs topDocs = indexSearcher.search(query.build(), 99999999);
                    // 6.取查询结果的总记录数
                    //System.out.println("查询记录总数：" + topDocs.totalHits);
                    // 7.取文档列表
                    ScoreDoc[] scoreDocs = topDocs.scoreDocs;
                    // 8.打印文档中的内容
                    for (ScoreDoc doc : scoreDocs) {
                        // 取文档id
                        int docId = doc.doc;
                        //根据id取文档对象
                        Document document = indexSearcher.doc(docId);

                        int sen_id = Integer.valueOf(document.get("sen_id"));
                        String doc_id = "";
                        String text_field = "";
                        int abstract_length = 0;

                        long map_start_time = System.nanoTime();
                        // 通过二分法，找到离sen_id最近的sen_id_start，且sen_id大于sen_id_start
                        int key_index = Arrays.binarySearch(sen_id_start_arr, sen_id);  // sen_id_start在数组中的位置，小于零则显示它应该插入的位置，且初始从-1计算
                        int key;    // sen_id_start的具体值
                        if(key_index >= 0){
                            key = sen_id_start_arr[key_index];
                        }
                        else{
                            key = sen_id_start_arr[-key_index - 2];
                        }
                        doc_id = hashMap1.get(key).get(0);
                        abstract_length = Integer.valueOf(hashMap1.get(key).get(1));
                        if (sen_id < (key_index + abstract_length - 1)) {
                            text_field = "abstract_text";
                        } else {
                            text_field = "body_text";
                        }

                        long map_end_time = System.nanoTime();
                        map_time += (map_end_time - map_start_time);

                        if (doc_id.contains("-abstract")) {
                            doc_id = doc_id.substring(0, doc_id.indexOf("-abstract"));
                        }
                        String keyword1 = hashMap2.get(keyword1_arr);
                        String keyword2 = hashMap2.get(keyword2_arr);
                        tempString = keyword1 + "\t" + keyword2 + "\t" + doc_id + "\t" + document.get("sen_text")
                                + "\t" + text_field + "\n";
                        string_list.add(tempString);

                        long output_start_time = System.nanoTime();

                        if(string_list.size() >= 10000){             // 每满10000句，输出到文本一次
                            System.out.println("outputing...");
                            FileWriter fw = new FileWriter(outputPath, true);
                            PrintWriter pw = new PrintWriter(fw);
                            for(String line : string_list){
                                pw.write(line);
                                pw.flush(); // 需要刷新缓冲区才能输出
                                fw.flush();
                            }
                            pw.close();
                            fw.close();
                            string_list.clear();    // 清空列表
                            System.out.println("output over");
                        }

                        long output_end_time = System.nanoTime();
                        output_time += output_end_time - output_start_time;
                    }

                } catch (Exception e) {
                    System.out.println("查询的过程中遇到异常,堆栈轨迹如下");
                    e.printStackTrace();
                }
            }
        }
        long output_start_time2 = System.nanoTime();
        if(string_list.size() != 0) {           // 如果查询完，且列表中还有剩余结果未输出，则补输出
            System.out.println("outputing...");
            FileWriter fw = new FileWriter(outputPath, true);
            PrintWriter pw = new PrintWriter(fw);
            for (String line : string_list) {
                pw.write(line);
                pw.flush(); // 需要刷新缓冲区才能输出
                fw.flush();
            }
            string_list.clear();
            pw.close();
            fw.close();
            System.out.println("output over");
        }
        long output_end_time2 = System.nanoTime();
        output_time += output_end_time2 - output_start_time2;
        // 9.关闭IndexReader对象
        indexReader.close();
    }

    private void buildDocSenRelation(String pathname, String output_filename) throws Exception{
        int next_doc_start = 0;
        String source_string = "doc_id" + "\t" + "sen_id_start" + "\t" + "sen_id_end" + "\t"
                + "abstract_length" + "\n";

        this.writeNewFile(".\\test\\relations\\" + output_filename + ".txt", source_string);

        FileWriter fw = new FileWriter(".\\test\\relations\\" + output_filename + ".txt", true);
        PrintWriter pw = new PrintWriter(fw);
        File dir = new File(pathname);
        File[] files = dir.listFiles();

        for(File f : files){
            System.out.println(f.getName());
            if (!f.isFile()) {
                Boolean flag = false;               // 判断文本是否属于摘要部分
                if (f.getName().contains("摘要")){
                    flag = true;
                }
                File[] listOfFiles = f.listFiles();
                for (File ff : listOfFiles){

                    String path = ff.getAbsolutePath();
                    FileInputStream fileInputStream = new FileInputStream(path);
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));

                    String doc_name = ff.getName();
                    String doc_id = doc_name.substring(0,doc_name.indexOf(".txt"));     // 去掉后缀
                    int ab_length = 0;
                    int sen_id_start = next_doc_start;
                    int sen_id_end = sen_id_start;

                    String line_temp = bufferedReader.readLine();
                    while ((line_temp = bufferedReader.readLine()) != null) {
                        sen_id_end++;
                    }

                    if(flag){                           // 在doc_id后面标注abstract，避免map里面同key覆盖
                        doc_id = doc_id + "-abstract";
                        ab_length = sen_id_end - sen_id_start + 1;
                    }

                    source_string = doc_id + "\t" + sen_id_start + "\t" + sen_id_end + "\t" + ab_length + "\n";
                    pw.write(source_string);
                    pw.flush(); // 需要刷新缓冲区才能输出
                    fw.flush();
                    next_doc_start = sen_id_end + 1;
                }
            }
        }
        pw.close();
        fw.close();
        System.out.println("Build doc_sen Finished");
    }

    public ArrayList<String> getkeywords1(String pathName) throws Exception{

        ArrayList<String> keyword_list = new ArrayList<>();

        FileInputStream fileInputStream = new FileInputStream(pathName);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
        String line = bufferedReader.readLine();
        while((line = bufferedReader.readLine()) != null) {
            String[] arr_temp = line.split("\t");
            keyword_list.add(arr_temp[1].replace("\"", ""));
            if (arr_temp.length >= 3) {
                String[] syn_arr = arr_temp[2].split(";");
                for (String syn : syn_arr) {
                    keyword_list.add(syn.replace("\"", ""));
                }
            }
        }
        fileInputStream.close();
        bufferedReader.close();
        return keyword_list;

    }

    public ArrayList<String> getkeywords2(String pathName) throws Exception{
        ArrayList<String> keyword_list = new ArrayList<>();

        FileInputStream fileInputStream = new FileInputStream(pathName);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
        String line = bufferedReader.readLine();
        while(line != null) {
            keyword_list.add(line);
            line = bufferedReader.readLine();
        }
        fileInputStream.close();
        bufferedReader.close();
        return keyword_list;
    }

    private ArrayList<String[]> simplify(ArrayList<String> keyword_list){
        ArrayList<String[]> key_arr_list = new ArrayList<>();
        String regEx = "[\\n`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。， 、？]";
        String aa = " ";
        Pattern p = Pattern.compile(regEx);

        for(String src_key : keyword_list) {
            String key = src_key.toLowerCase().replace("-", " ");
            for (int i = 0; i < regEx.length(); i++) {
                char a = regEx.charAt(i);
                if (key.contains(String.valueOf(a))) {
                    Matcher m = p.matcher(key);
                    key = m.replaceAll(aa).trim();
                    break;
                }
            }
            String[] key_arr = key.split("\\s+");
            key_arr_list.add(key_arr);
        }
        return key_arr_list;
    }


    public static void main(String[] args) throws Exception {
        SearchTest s = new SearchTest();
//        int abstract_sen_count = s.getSenCount(".\\test\\text\\包含COV-19词汇_摘要");
//
//        s.buildDocSenRelation(".\\test\\text", "doc sen relations"); // 建立文章句子关系表

//        // 调用函数，建立索引
//        s.createIndex(".\\test\\text\\包含COV-19词汇_摘要",".\\test\\index\\abstract_index",0);
//        s.createIndex(".\\test\\text\\包含COV-19词汇_正文",".\\test\\index\\body_text_index",abstract_sen_count);

        // 将所有关键词1和2导入列表
        ArrayList<String> keyword1_list = s.getkeywords1("src\\test\\DiseaseInfoTest.txt");
        ArrayList<String> keyword2_list = s.getkeywords2("./data/relations/COVID-19_synonym");

        // 对关键词1和2进行预处理
        ArrayList<String[]> key_arr_list1 = s.simplify(keyword1_list);
        ArrayList<String[]> key_arr_list2 = s.simplify(keyword2_list);

        HashMap<Integer,List<String>> hashMap1 = s.getHashMap();
        HashMap<String[], String> hashMap2 = s.getScourceKeyMap(keyword1_list, keyword2_list, key_arr_list1, key_arr_list2);
        int[] sen_id_start_arr = s.getKeyArrays();

        String sourceString = "key1\t" + "key2\t" + "doc_id\t" + "sen_text\t" + "text_field\n";    // 输出结果第一行
        s.writeNewFile(".\\test\\output\\abstract_output\\abstract_output.txt", sourceString);
        s.writeNewFile(".\\test\\output\\body_text_output\\body_text_output.txt", sourceString);

        long start_time = System.nanoTime();

        s.searchIndex(key_arr_list1, key_arr_list2, ".\\test\\index\\abstract_index", ".\\test\\output\\abstract_output\\abstract_output.txt", hashMap1, hashMap2, sen_id_start_arr);
        s.searchIndex(key_arr_list1, key_arr_list2, ".\\test\\index\\body_text_index", ".\\test\\output\\body_text_output\\body_text_output.txt", hashMap1, hashMap2, sen_id_start_arr);

        long end_time = System.nanoTime();
        long run_time = end_time - start_time - output_time;
        System.out.println("输出耗时" + new Double(output_time).longValue()/1000000000.0 + "s");
        System.out.println("HashMap耗时" + new Double(map_time).longValue()/1000000000.0 + "s");
        System.out.println("查询共耗时（包含map）" + new Double(run_time).longValue()/1000000000.0 + "s");

        System.out.println("控制台：查询结束");

    }
}