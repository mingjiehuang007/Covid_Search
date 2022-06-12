package basic;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Locale;


public class SplitDocument {

    public ArrayList<String> getSentence(String text) {

        BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.US);

        ArrayList<String> sentences = getSen(iterator, text);

//        for(int i = 0; i < size(sentences); i++){
//            System.out.println(sentences.get(i));
//        }
//
//        System.out.println("Number of sentences: " + size(sentences));

        return sentences;
    }

    private ArrayList<String> getSen(BreakIterator bi, String source) {
//        int counter = 0;
        ArrayList<String> count_sen = new ArrayList<>();

        bi.setText(source);             // 新文本设置为BreakIterator

        int lastIndex = bi.first();     // 获取第一个边界的索引

        while (lastIndex != BreakIterator.DONE) {
            int firstIndex = lastIndex;

            lastIndex = bi.next();

            if (lastIndex != BreakIterator.DONE) {
                String sentence = source.substring(firstIndex, lastIndex);

                //System.out.println("sentence = " + sentence);
                count_sen.add(sentence);

//                counter++;

            }
        }

//        count_sen.add(0, Integer.toString(counter));

        return count_sen;
    }
}

