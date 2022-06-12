package basic;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.ngram.NGramTokenFilter;
import org.apache.lucene.analysis.ngram.NGramTokenizer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.util.ArrayList;

public class test {

    private void createIndex(String srcFilePath, String index_path, int sen_start_id) throws Exception {
        // 把索引库保存在磁盘
        Directory directory = FSDirectory.open(new File(index_path).toPath());
        // 基于Directory对象创建一个IndexWriter对象
        // 重新定义一个分词器
        Analyzer analyzer = new Analyzer() {
            @Override
            protected TokenStreamComponents createComponents(String fieldName) {
                Tokenizer source = new NGramTokenizer(4,4);
                TokenStream filter = new StandardFilter(source);
                filter = new StandardFilter(filter);
                return new TokenStreamComponents(source, filter);
            }
        };
        IndexWriter indexWriter = new IndexWriter(directory, new IndexWriterConfig(analyzer));
        // 读取磁盘上的文件，对应每个文件创建一个文档对象
        SplitDocument splitDoc = new SplitDocument();
        //删除全部文档
        indexWriter.deleteAll();

        File dir = new File(srcFilePath);
        File[] files = dir.listFiles();
        int sen_id = sen_start_id;

        int count = 0;
        for (File f : files) {
            String path = f.getPath();
            String line_temp;
            FileInputStream fileInputStream = new FileInputStream(path);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
            line_temp = bufferedReader.readLine();
            if (line_temp.contains("abstract")) {

                while ((line_temp = bufferedReader.readLine()) != null) {
                    int abstract_begin = line_temp.indexOf("abstract=");
                    int body_text_begin = line_temp.indexOf("body_text=");
                    int body_text_end = line_temp.length() - 1;

                    String abstract_str = line_temp.substring(abstract_begin + 10, body_text_begin);
                    String body_text = line_temp.substring(body_text_begin + 11, body_text_end);
                    ArrayList<String> abstract_list = splitDoc.getSentence(abstract_str);
                    ArrayList<String> body_list = splitDoc.getSentence(body_text);
                    ArrayList<String> sens_list = abstract_list;
                    for (String i : body_list) {
                        sens_list.add(i);
                    }

                    for (String s : sens_list) {
                        //System.out.println(s);
                        // 创建field
                        // 参数1：域的名称，参数2：域的内容，参数3：是否存储
                        Field field1 = new TextField("sen_id", String.valueOf(sen_id), Field.Store.YES);
                        Field field2 = new TextField("sen_text", s, Field.Store.YES);
                        sen_id++;

                        // 创建文档对象
                        Document document = new Document();
                        // 向文档对象中添加域
                        document.add(field1);
                        document.add(field2);
                        // 把文档对象写入索引库
                        indexWriter.addDocument(document);

                        count++;
                        if (count%1000000 ==0){
                            System.out.println(count);
                        }
                    }
                }
            } else {
                while ((line_temp = bufferedReader.readLine()) != null) {
                    int body_text_begin = line_temp.indexOf("body_text=");
                    int body_text_end = line_temp.length() - 1;

                    String body_text = line_temp.substring(body_text_begin + 11, body_text_end);
                    ArrayList<String> body_list = splitDoc.getSentence(body_text);

                    for (String s : body_list) {
                        //System.out.println(s);
                        // 创建field
                        // 参数1：域的名称，参数2：域的内容，参数3：是否存储
                        Field field1 = new TextField("sen_id", String.valueOf(sen_id), Field.Store.YES);
                        Field field2 = new TextField("sen_text", s, Field.Store.YES);
                        sen_id++;

                        // 创建文档对象
                        Document document = new Document();
                        // 向文档对象中添加域
                        document.add(field1);
                        document.add(field2);
                        // 把文档对象写入索引库
                        indexWriter.addDocument(document);

                        count++;
                        if (count%1000000 ==0) {
                            System.out.println(count);
                        }
                    }
                }
            }
        }
        // 关闭indexwriter对象
        indexWriter.close();
        System.out.println("index build finished");
    }

    public static void main(String[] args) {

    }
}