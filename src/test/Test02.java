package test;


import basic.COVID_TASK1;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import jnr.ffi.annotations.In;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Test02 {
//    public static void main(String[] args){
//        Properties props = new Properties();  // set up pipeline properties
//        props.put("annotators", "tokenize, ssplit, pos");   //分词、分句、词性标注和次元信息。
//        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
//        String txtWords = "introduction and objectives: the covid-19 outbreak has had an unclear impact on the treatment and outcomes of patients with st-segment elevation myocardial infarction (stemi).";  // 待处理文本
//        Annotation document = new Annotation(txtWords);
//        pipeline.annotate(document);
//        List<CoreMap> words = document.get(CoreAnnotations.SentencesAnnotation.class);
//        for(CoreMap word_temp: words) {
//            for (CoreLabel token: word_temp.get(CoreAnnotations.TokensAnnotation.class)) {
//                String word = token.get(CoreAnnotations.TextAnnotation.class);   // 获取单词信息
////                String lema = token.get(CoreAnnotations.LemmaAnnotation.class);  // 获取对应上面word的词元信息，即我所需要的词形还原后的单词
//                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
//                System.out.println(word + " " + pos);
//            }
//        }
//    }


    public static void main(String[] args) {
        ArrayList<ArrayList<Integer>> b = new ArrayList<>();
        for (int i = 0; i < 5 ; i++) {
            ArrayList<Integer> a = new ArrayList<>();
            for (int j = 0; j < 10; j++) {
                a.add(j);
            }
            b.add(a);
        }
        System.out.println(b);

    }






}
