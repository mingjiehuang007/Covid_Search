package basic;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.logging.Redwood;
import edu.stanford.nlp.util.logging.RedwoodConfiguration;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import sun.awt.image.ImageWatched;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class COVID_TASK1 {

    private static final String csvSplitBy = ",(?=([^\"]*\"[^\"]*\")*[^\"]*$)";  // 用来划分csv单元格
    private static final String index_path = "./data_task1/index1";

    private void writeNewFile(String output_path, String sourceString) throws Exception{
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

    private void appendToFile(String output_path, LinkedList<Attribute> source_list, String[] raws) throws Exception{
        try {
            System.out.println("outputting ");
            FileWriter fileWriter = new FileWriter(output_path, true);
            PrintWriter printWriter = new PrintWriter(fileWriter);

            DecimalFormat decimalFormat = new DecimalFormat("0.00000000");
            int i = 0 ;
            for (Attribute a : source_list) {
//                if (i == 100){
//                    break;
//                }
                String source_line = "";
                for (String r : raws) {
                    if (r.equals("name")) {
                        source_line += a.getName() + "\t";
                    } else if (r.equals("tf")) {
                        source_line += decimalFormat.format(a.getTf()) + "\t";
                    } else if (r.equals("df")) {
                        source_line += decimalFormat.format(a.getDf()) + "\t";
                    } else if (r.equals("tfidf")) {
                        source_line += decimalFormat.format(a.getTf_idf()) + "\t";
                    } else if (r.equals("pos")) {
                        source_line += a.getPostagger() + "\t";
                    }
                }
                printWriter.write(source_line.substring(0, source_line.length() - 1) + "\n");
                printWriter.flush();
                fileWriter.flush();
                i++;
            }
            printWriter.close();
            fileWriter.close();
            System.out.println("output over");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void splitSenToFile(String output_path) throws Exception{
        System.out.println("split sen to file start...");
        String data_path = "./data_task1/report/all1.csv";
        Reader file = new FileReader(data_path);
        Iterable<CSVRecord> records = CSVFormat.EXCEL.parse(file);
        this.writeNewFile(output_path, "");
        FileWriter fileWriter = new FileWriter(output_path, true);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        int num = 0;
        int count = 0;
        ArrayList<String> source_list = new ArrayList<>();
        for (CSVRecord record : records) {
            count++;
            if (count == 1) {
                continue;
            }
            if (count % 10000 == 0){
                System.out.println(count);
            }
            String disease_name = record.get(0);
            String sen_abstract = record.get(7);
            String sen_body_text = record.get(8);
            if (sen_abstract.length() == 2 || sen_abstract.length() == 1) {
                sen_abstract = "";
            } else if (sen_abstract.substring(0, 2).equals("['")) {
                sen_abstract = sen_abstract.substring(2, sen_abstract.length() - 2);
            }
            if (sen_body_text.length() == 2 || sen_abstract.length() == 1) {
                sen_body_text = "";
            } else if (sen_body_text.substring(0, 2).equals("['")) {
                sen_body_text = sen_body_text.substring(2, sen_body_text.length() - 2);
            }
            if (!sen_abstract.equals("")) {
                String[] sen_ab_arr = sen_abstract.split("','");    // 将单句从数组中取出
                ArrayList<String> sen_ab_list = new ArrayList<>();
                for (String sen : sen_ab_arr){
                    sen_ab_list.add(sen);
                }
                ArrayList<ArrayList<String>> sen_ab_tokenized = new ArrayList<>();
                sen_ab_tokenized = this.enTokenize(sen_ab_list);
                if (sen_ab_tokenized.size() > 0) {
                    for (ArrayList<String> list : sen_ab_tokenized) {
                        String sourceString = num + "\t" + disease_name + "\t";
                        for (String word : list) {
                            sourceString += word + "\t";
                        }
                        sourceString = sourceString.substring(0, sourceString.length() - 1);
                        sourceString = sourceString + "\n";
                        source_list.add(sourceString);
                        if (source_list.size() >= 10000) {
                            System.out.println("outputting...");
                            for (String s : source_list) {
                                printWriter.write(s);
                                printWriter.flush();
                                fileWriter.flush();
                            }
                            source_list.clear();
                            System.out.println("output over");
                        }
                        num++;
                    }
                }
            }
            if (!sen_body_text.equals("")) {
                String[] sen_bd_arr = sen_body_text.split("','");
                ArrayList<String> sen_bd_list = new ArrayList<>();
                for (String sen : sen_bd_arr){
                    sen_bd_list.add(sen);
                }
                ArrayList<ArrayList<String>> sen_bd_tokenized = new ArrayList<>();
                sen_bd_tokenized = this.enTokenize(sen_bd_list);
                if (sen_bd_tokenized.size() > 0) {
                    for (ArrayList<String> list : sen_bd_tokenized) {
                        String sourceString = num + "\t" + disease_name + "\t";
                        for (String word : list) {
                            sourceString += word + "\t";
                        }
                        sourceString = sourceString.substring(0, sourceString.length() - 1);
                        sourceString = sourceString + "\n";
                        source_list.add(sourceString);
                        if (source_list.size() >= 10000) {
                            System.out.println("outputting...");
                            for (String s : source_list) {
                                printWriter.write(s);
                                printWriter.flush();
                                fileWriter.flush();
                            }
                            source_list.clear();
                            System.out.println("output over");
                        }
                        printWriter.write(sourceString);
                        printWriter.flush();
                        fileWriter.flush();
                        num++;
                    }
                }
            }
        }
        if (source_list.size() != 0){
            System.out.println("outputting...");
            for (String s : source_list){
                printWriter.write(s);
                printWriter.flush();
                fileWriter.flush();
            }
            source_list.clear();
            System.out.println("output over");
        }
        printWriter.close();
        fileWriter.close();
        ((CSVParser) records).close();
        file.close();
    }

    private HashMap<String, String> getPos(Set<String> sens_set){
        RedwoodConfiguration.empty().apply();       // 关闭红字
//        Redwood.log("test redwood");
        Properties props = new Properties();  // set up pipeline properties
        props.put("annotators", "tokenize, ssplit, pos, lemma");   //分词、分句、词性标注和词元信息。
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        Annotation document;
        HashMap<String, String> map = new HashMap<>();
        int set_size = sens_set.size();
        int i = 0;
        for (String sen : sens_set) {
            i++;
            if (i % 1000 == 0) {
                System.out.println(i / 1000 + "|" + set_size / 1000);
            }
            document = new Annotation(sen);
            pipeline.annotate(document);
            List<CoreMap> words = document.get(CoreAnnotations.SentencesAnnotation.class);
            for (CoreMap word_temp : words) {
                for (CoreLabel token : word_temp.get(CoreAnnotations.TokensAnnotation.class)) {
//                String word = token.get(CoreAnnotations.TextAnnotation.class);      // 获取单词信息
                    String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);     // 获取词干
                    String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                    map.put(lemma, pos);
                }
            }
        }
        return map;
    }

    private void printPosToFile() throws Exception{
        System.out.println("print pos to file start...");
        String data_path = "./data_task1/report/split_words.txt";
        String output_path = "./data_task1/report/word_pos_relations.txt";
        this.writeNewFile(output_path, "");
        HashMap<String ,String> word_pos_map = new LinkedHashMap<>();
        FileWriter fw = new FileWriter(output_path, true);
        PrintWriter pw = new PrintWriter(fw);

        FileInputStream fis = new FileInputStream(data_path);
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
        Set<String> sens_set = new HashSet<>();
        String line = "";
        int i = 0;
        while((line = br.readLine()) != null){
            i++;
            if (i % 10000 == 0){
                System.out.println(i);
            }
            String line_temp = line.substring(line.indexOf("\t") + 1);
            if (!line_temp.contains("\t")){
                continue;
            }
            String sen = line_temp.substring(line_temp.indexOf("\t") + 1);
            sens_set.add(sen);
        }
        System.out.println("getting posttagers...");
        word_pos_map = this.getPos(sens_set);
        br.close();
        fis.close();
        Set<String> keys = word_pos_map.keySet();
        for (String key : keys){
            System.out.println("outputting...");
            String src_string = key + "\t" + word_pos_map.get(key) + "\n";
            pw.write(src_string);
            pw.flush();
            fw.flush();
        }
        pw.close();
        fw.close();
        System.out.println("print pos to file over");
    }

    public void createIndex() throws Exception{
        try {
            Analyzer analyzer = new StandardAnalyzer();
            Directory directory = FSDirectory.open(new File(index_path).toPath());
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            IndexWriter indexWriter = new IndexWriter(directory, config);
            indexWriter.deleteAll();

            // 域保存设置
            FieldType ft = new FieldType();
            ft.setIndexOptions(IndexOptions.DOCS_AND_FREQS);    // 仅对文档和词频建立索引
            ft.setStored(true);                                 // 保存索引
            ft.setTokenized(true);                              // 分词

            String data_path = "./data_task1/report/split_words.txt";

            FileInputStream fileInputStream = new FileInputStream(data_path);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
            int count = 0;
            int skip_count = 0;
            String line = "";
            while ((line = bufferedReader.readLine()) != null){
                count++;
                if (count % 10000 == 0) {
                    System.out.println(count / 10000 + " || 60");
                }
                String sen_id = line.substring(0 , line.indexOf("\t"));
                String line_temp = line.substring(line.indexOf("\t") + 1);
                if (!line_temp.contains("\t")){
                    skip_count++;
                    continue;
                }
                String disease_name = line_temp.substring(0, line_temp.indexOf("\t"));
                line_temp = line_temp.substring(line_temp.indexOf("\t") + 1);
                String sen = line_temp.trim();

                Field field1 = new StringField("disease_name", disease_name, Field.Store.YES);
                Field field2 = new StringField("sen_id", sen_id, Field.Store.YES);
                Field field3 = new Field("sen", sen, ft);
                Document doc = new Document();
                doc.add(field1);
                doc.add(field2);
                doc.add(field3);
                indexWriter.addDocument(doc);
            }
            indexWriter.forceMerge(1);// 最后一定要合并为一个segment，不然无法计算idf
            indexWriter.close();
            bufferedReader.close();
            fileInputStream.close();
            System.out.println("skip count: " + skip_count);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("index build finished");
    }

    private String[] simplify(String keyword){
        String regEx = "[\\n`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。， 、？]";
        String aa = " ";
        Pattern p = Pattern.compile(regEx);

        String key = keyword.toLowerCase().replace("-", " ");
        for (int i = 0; i < regEx.length(); i++) {
            char a = regEx.charAt(i);
            if (key.contains(String.valueOf(a))) {
                Matcher m = p.matcher(key);
                key = m.replaceAll(aa).trim();
                break;
            }
        }
        String[] key_arr = key.split("\\s+");

        return key_arr;
    }


    private String staByC_D_A(String covid_name, String disease_name, String attribute) throws Exception{
        ArrayList<String> string_of_T1 = new ArrayList<>();

        String data_path = "./data_task1/report/all1.csv";
        Reader file = new FileReader(data_path);
        Iterable<CSVRecord> records= CSVFormat.EXCEL.parse(file);
        String disease_name_lower = disease_name.toLowerCase();
        String attribute_lower = attribute.toLowerCase();
        int doc_count = 0;
        int sen_ab_count = 0;
        int sen_bd_count = 0;
//        int i = 0;

        for(CSVRecord record:records) {
//            i++;
//            if (i % 10000 == 0 ){
//                System.out.println(i);
//            }
            String disease_name_csv = record.get(0);
            if (disease_name_csv.equals(disease_name_lower)){
                String sen_abstract = record.get(7);
                String sen_body_text = record.get(8);
                if (sen_abstract.contains(attribute_lower) || sen_body_text.contains(attribute_lower)){
                    doc_count++;
                }
                if (sen_abstract.length() == 2 || sen_abstract.length() == 1) {
                    sen_abstract = "";
                }
                else if (sen_abstract.substring(0,2).equals("['")){
                    sen_abstract = sen_abstract.substring(2, sen_abstract.length() - 2);
                }
                if (sen_body_text.length() == 2 || sen_abstract.length() == 1) {
                    sen_body_text = "";
                }
                else if (sen_body_text.substring(0,2).equals("['")){
                    sen_body_text = sen_body_text.substring(2, sen_body_text.length() - 2);
                }
                if (!sen_abstract.equals("")){
                    String[] sen_ab_arr = sen_abstract.split("','");
                    for (String s1 : sen_ab_arr){
                        if (s1.contains(attribute_lower)){
                            sen_ab_count++;
                        }
                    }
                }
                if (!sen_body_text.equals("")){
                    String[] sen_bd_arr = sen_body_text.split("','");
                    for (String s2 : sen_bd_arr){
                        if (s2.contains(attribute_lower)){
                            sen_bd_count++;
                        }
                    }
                }
            }// if (disease_name_csv.equals(disease_name_lower))
        }

        ((CSVParser) records).close();
        file.close();
        String source_string = "disease name: " + disease_name + "\tdoc count: " + doc_count
                + "\tsen count: " + (sen_ab_count + sen_bd_count);
        System.out.println(source_string);

        String string_output = disease_name + "\t" + doc_count + "\t" + (sen_ab_count + sen_bd_count) + "\n";
        return string_output;
    }

    // 获取停用词
    private ArrayList<String> getStopWord() throws Exception{
        String stopWord_path = "./data_task1/StopWord.txt";
        FileInputStream fileInputStream = new FileInputStream(stopWord_path);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
        ArrayList<String> stopWord_list = new ArrayList<>();
        String line = "";
        while((line = bufferedReader.readLine()) != null){
            stopWord_list.add(line.trim());
        }
        bufferedReader.close();
        fileInputStream.close();
        return stopWord_list;
    }

    // 英文分词
    private ArrayList<ArrayList<String>> enTokenize(ArrayList<String> sens_list) throws Exception{
//        ArrayList<String> tokens = new ArrayList();
//        StringBuilder buffer  =   new  StringBuilder();
//        for  (int i = 0;i < source.length();i++)  {
//            char character = source.charAt(i);
//            if(Character.isLetter(character) || (character == '-') || Character.isDigit(character)){
//                buffer.append(character);
//            }else{
//                if (buffer.length() > 0){
//                    if(Character.isUpperCase(buffer.charAt(0))){
//                        buffer.setCharAt(0,Character.toLowerCase(buffer.charAt(0)));
//                    }
//                    tokens.add(buffer.toString());
//                    buffer = new StringBuilder();
//                }
//            }
//        }
//        if(buffer.length() > 0){
//            if(Character.isUpperCase(buffer.charAt(0))){
//                buffer.setCharAt(0,Character.toLowerCase(buffer.charAt(0)));
//            }
//            tokens.add(buffer.toString());
//        }
        RedwoodConfiguration.empty().apply();       // 关闭红字
        Properties props = new Properties();  // set up pipeline properties
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma");   //分词、分句、词性标注和词元信息。
        props.setProperty("tokenize.options", "untokenizable=noneDelete");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        Annotation document;

        ArrayList<ArrayList<String>> sen_list = new ArrayList<>();
        for (String source : sens_list) {
            document = new Annotation(source);
            pipeline.annotate(document);
            List<CoreMap> words = document.get(CoreAnnotations.SentencesAnnotation.class);
            ArrayList<String> tokens = new ArrayList();
            for (CoreMap word_temp : words) {
                for (CoreLabel token : word_temp.get(CoreAnnotations.TokensAnnotation.class)) {
                    String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);     // 获取词干
                    tokens.add(lemma);
                }
            }
            sen_list.add(tokens);  // 每个句子分句完成后，作为数据元素存入列表
        }

        ArrayList<String> stopWords = this.getStopWord();
        Pattern pattern1 = Pattern.compile("-?[0-9]+.?[0-9]+");   // 正则用于判断是否为整数或小数，是则返回true
        Pattern pattern2 = Pattern.compile("\\p{Punct}+");        // 正则用于判断是否由特殊符号组成，是则返回true
        Pattern pattern3 = Pattern.compile("^[-\\+]?[\\d]*$");        // 正则用于判断是否由特殊符号组成，是则返回true
        ArrayList<ArrayList<String>> tokenized_sens = new ArrayList<>();
        // 只去除停用词
        for (ArrayList<String> sen : sen_list) {
            ArrayList<String> words = new ArrayList<>();
            for (String word : sen) {
                if (!pattern1.matcher(word).matches() && !pattern2.matcher(word).matches() && !pattern3.matcher(word).matches() && !stopWords.contains(word)) {
                    if ((Character.isLetter(word.charAt(0)) || Character.isDigit(word.charAt(0))) && (Character.isLetter(word.charAt(word.length() - 1)) || Character.isDigit(word.charAt(word.length() - 1)))){
                        words.add(word);
                    }
                }
            }
            tokenized_sens.add(words);
        }
        return tokenized_sens;
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

    private ArrayList<String> toLowerCase(ArrayList<String> strings){
        ArrayList<String> new_list = new ArrayList<>();
        for (String s : strings){
            new_list.add(s.toLowerCase());
        }
        return new_list;
    }


    // 改写比较方法
    private ArrayList<Comparator<Attribute>> rewriteComparator(){
        Comparator<Attribute> comparator_tf = new Comparator<Attribute>() {
            @Override
            public int compare(Attribute o1, Attribute o2) {
                if (o1.getTf() < o2.getTf()){
                    return 1;
                }else if (o1.getTf() > o2.getTf()){
                    return -1;
                }
                else{
                    return 0;
                }
            }
        };

        Comparator<Attribute> comparator_df = new Comparator<Attribute>() {
            @Override
            public int compare(Attribute o1, Attribute o2) {
                if (o1.getDf() < o2.getDf()){
                    return 1;
                }
                else if (o1.getDf() > o2.getDf()){
                    return -1;
                }
                else{
                    return 0;
                }
            }
        };

        Comparator<Attribute> comparator_tfidf = new Comparator<Attribute>() {
            @Override
            public int compare(Attribute o1, Attribute o2) {
                if (o1.getTf_idf() < o2.getTf_idf()){
                    return 1;
                }else if (o1.getTf_idf() > o2.getTf_idf()){
                    return -1;
                }
                else{
                    return 0;
                }
            }
        };
        ArrayList<Comparator<Attribute>> list = new ArrayList<>();
        list.add(comparator_tf);
        list.add(comparator_df);
        list.add(comparator_tfidf);
        return list;
    }

    private HashMap<String,String> getWordPosMap() throws Exception{
        System.out.println("building word_pos_map...");
        String data_path = "./data_task1/report/word_pos_relations.txt";
        FileInputStream fis = new FileInputStream(data_path);
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));

        HashMap<String,String> word_pos_map = new HashMap<>();
        String line = "";
        while((line = br.readLine()) != null){
            String[] arr = line.split("\t");
            String word = arr[0];
            String pos = arr[1];
            word_pos_map.put(word, pos);
        }
        System.out.println("word_pos_map build over");
        return word_pos_map;
    }

    private void getTFIDF(String covid_name, String disease_name, Set<String> key_set1, Set<String> key_set2, HashMap<String,String> word_pos_map) throws Exception{
        try {
            Directory directroy = FSDirectory.open(new File(index_path).toPath());
            IndexReader indexReader = DirectoryReader.open(directroy);
            IndexSearcher indexSearcher = new IndexSearcher(indexReader);
            String disease_name_lower = disease_name.toLowerCase();

            TermQuery query1 = new TermQuery(new Term("disease_name", disease_name_lower));
            TopDocs topDocs = indexSearcher.search(query1, 99999999);
            long doc_counts = topDocs.totalHits;                            // 匹配到的句子总数
            ScoreDoc[] scoreDocs = topDocs.scoreDocs;

            ArrayList<String> sen_list = new ArrayList<>();         // 把所有匹配到的句子放入列表
            for (ScoreDoc doc : scoreDocs) {
                int docId = doc.doc;
                Document document = indexSearcher.doc(docId);
                sen_list.add(document.get("sen"));
            }

            Set<String> attr = new LinkedHashSet<>();   // 存放备选属性
            LinkedList<LinkedList<String>> all_sens_tokenized_withCD = new LinkedList<>();  // 分词后的句子，保留CD中的词

            for (String sen : sen_list){
                String[] sen_to_words = sen.split("\t");
                LinkedList<String> sen_to_words_list = new LinkedList<>();
                for (String word : sen_to_words){
                    sen_to_words_list.add(word);
                }
                all_sens_tokenized_withCD.add(sen_to_words_list);
            }
            LinkedList<LinkedList<String>> all_sens_tokenized = new LinkedList<>();             // 分词后的句子，去除CD中的词

            for (LinkedList<String> sen_split : all_sens_tokenized_withCD){
                LinkedList<String> word_list_temp = new LinkedList<>();
                for (String word : sen_split){
                    if (!key_set1.contains(word) && !key_set2.contains(word)){
                        word_list_temp.add(word);
                    }
                }
                all_sens_tokenized.add(word_list_temp);
            }

            for (LinkedList<String> attrs : all_sens_tokenized){
                attr.addAll(attrs);
            }

            long word_counts = 0;
            for (LinkedList<String> sens : all_sens_tokenized_withCD){
                word_counts += sens.size();
            }
            LinkedList<Attribute> result = new LinkedList<>();

            int i_sum = attr.size();
            for (int j = 0; j < i_sum/1000; j++){
                System.out.print("*");
            }
            System.out.println();

            int i = 0;
            for (String attr_word : attr){
                i++;
                if (i % 1000 == 0){
                    System.out.print("*");
                }
                long tc = 0;
                long dc = 0;
                for (LinkedList<String> sen : all_sens_tokenized_withCD){
                    // 获取dc和tc
                    if (sen.contains(attr_word)){
                        dc++;
                        // 获取tc
                        for (String word : sen){
                            if (word.equals(attr_word)){
                                tc++;
                            }
                        }
                    }
                }
                double tf = (new Double(tc).longValue()/1.0) / (new Double(word_counts).longValue()/1.0);
                double df = (new Double(dc).longValue()/1.0) / (new Double(doc_counts).longValue()/1.0);
                double idf = Math.log10(1.0 / df);
                double tf_idf = tf * idf;
                String postagger = word_pos_map.get(attr_word);
                Attribute attribute = new Attribute(attr_word, tf, df, tf_idf, postagger);
                result.add(attribute);
            }
            System.out.println();

            ArrayList<Comparator<Attribute>> comparators = this.rewriteComparator();

            Collections.sort(result,comparators.get(0));
            String output_path1 = "./data_task1/attributes/rankByTF1/tf_" + disease_name.replace("/", " or ") + ".txt";
            this.writeNewFile(output_path1, "");
            String[] raws1 = {"name" , "tf", "pos"};
            this.appendToFile(output_path1, result, raws1);

            Collections.sort(result,comparators.get(2));
            String output_path2 = "./data_task1/attributes/rankByTFIDF1/tfidf_" + disease_name.replace("/", " or ") + ".txt";
            this.writeNewFile(output_path2, "");
            String[] raws2 = {"name" , "tfidf", "pos"};
            this.appendToFile(output_path2, result, raws2);

            indexReader.close();
            directroy.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Set<String> getKeywordInReport() throws Exception{
        String data_path = "./data_task1/report/all1.csv";

        Reader file = new FileReader(data_path);
        Iterable<CSVRecord> records= CSVFormat.EXCEL.parse(file);
        Set<String> keyword_set = new LinkedHashSet<>();
        int i = 0;
        for (CSVRecord record : records){
            if (i++ == 0){
                continue;
            }
            String disease_name = record.get(0);
            keyword_set.add(disease_name);
        }
        ((CSVParser) records).close();
        file.close();
        System.out.println("get keywords in report over");
        return keyword_set;
    }

    private void staC_DbyA(String attribute) throws Exception{
        String first_line = "disease name\tdoc count\tsen count\n";
        String output_path = "./data_task1/attributes calculation/" + attribute + ".txt";
        this.writeNewFile(output_path, first_line);

        ArrayList<String> disease_list = new ArrayList<>();
        File dir = new File("./data_task1/attributes/rankByTF");
        File[] files = dir.listFiles();
        for (File f : files){
            String path = f.getPath();
            FileInputStream fis = new FileInputStream(path);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String line = "";
            while((line = br.readLine()) != null){
                String[] array = line.split("\t");
                if (array[0].equals(attribute)){
                    String file_name = f.getName();
                    disease_list.add(file_name.substring(3, file_name.length() - 4));
                    break;
                }
            }

        }
        System.out.println("number of diseases found: " + disease_list.size());

        FileWriter fw = new FileWriter(output_path, true);
        PrintWriter pw = new PrintWriter(fw);
        ArrayList<String> output_list = new ArrayList<>();
        for (String disease_name : disease_list){
            System.out.print("\t");
            String output_string = this.staByC_D_A("covid-19", disease_name, attribute);
            output_list.add(output_string);
        }
        System.out.println("outputting...");
        for (String s : output_list){
            pw.write(s);
            pw.flush();
            fw.flush();
        }
        pw.close();
        fw.close();
        System.out.println("output over");
    }


    public static void main(String[] args) throws Exception{
        COVID_TASK1 covid_task1 = new COVID_TASK1();
        covid_task1.splitSenToFile("./data_task1/report/split_words.txt" );
        covid_task1.createIndex();
        covid_task1.printPosToFile();

        // T1
//        covid_task1.staByC_D_A("COVID-19", "abdominal aortic aneurysm", "patients");

        // T2
        long start_time = System.nanoTime();
        HashMap<String,String> word_pos_map = covid_task1.getWordPosMap();

        ArrayList<String> keyword1_list = covid_task1.getkeywords1("data\\relations\\DiseaseInfo.txt");
        ArrayList<String> keyword2_list = covid_task1.getkeywords2("./data/relations/COVID-19_synonym");
        keyword1_list = covid_task1.toLowerCase(keyword1_list);
        keyword2_list = covid_task1.toLowerCase(keyword2_list);
        Set<String> keyword1_set = new LinkedHashSet<>();
        Set<String> keyword2_set = new LinkedHashSet<>();
        keyword1_set.addAll(keyword1_list);
        keyword2_set.addAll(keyword2_list);
        System.out.println("get keywords list finished");

        Set<String> keywords_in_report = covid_task1.getKeywordInReport();
        int key_sum = keywords_in_report.size();
        int i = 0;
        for (String disease_name : keywords_in_report){
            System.out.println(++i + "|" + key_sum + "\t" + disease_name);
            covid_task1.getTFIDF("covid-19", disease_name, keyword1_set, keyword2_set, word_pos_map);

        }

//        covid_task1.getTFIDF("covid-19", "heart disease", keyword1_set, keyword2_set, word_pos_map);

        long end_time = System.nanoTime();
        double time = new Double(end_time - start_time).longValue()/1000000000.0;
        System.out.println(time + "s");

        // T3
//        covid_task1.staC_DbyA("mortality");
    }

}
