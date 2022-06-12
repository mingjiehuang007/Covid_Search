package test;

import basic.Util;
import net.sf.ezmorph.bean.MorphDynaBean;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.io.*;
import java.util.List;

    /*
该算法用于将.json文件提取为.txt文件
 */

public class JsonTestJsonTest {

    @SuppressWarnings({"static-access", "deprecation", "unchecked"})

    public static void main(String[] args) throws IOException {
        String sourceString;    //待写入字符串
        sourceString = "disease_name\t" + "paper_id\t" + "sentence_match\t" + "title\t" + "authors\n";
        String file_path = "C:\\Users\\Administrator\\Desktop\\1000个句子(测试).json";

        // 新建文档存放转换后的文本
        String output_path = ".\\test\\json_to_text\\test_json_to_text.txt";
        byte[] sourceByte = sourceString.getBytes();
        try {
            File json_to_text = new File(output_path);//文件路径（路径+文件名）
            if (!json_to_text.exists()) {    //文件不存在则创建文件，先创建目录
                File dir = new File(json_to_text.getParent());
                dir.mkdirs();
                json_to_text.createNewFile();
            }
            FileOutputStream outStream = new FileOutputStream(json_to_text);    //文件输出流用于将数据写入文件
            outStream.write(sourceByte);
            outStream.close();    //关闭文件输出流
        } catch (Exception e) {
            e.printStackTrace();
        }
        FileWriter fw = new FileWriter(output_path, true);
        PrintWriter pw = new PrintWriter(fw);


        File f = new File(file_path);
        String a = f.getAbsolutePath();
        System.out.println(a);
        int count = 0;
        if (f.isFile()) {
            String path = f.getAbsolutePath();

            File file = new File(path);
            String check_line;
            FileInputStream fileInputStream = new FileInputStream(path);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
            check_line = bufferedReader.readLine();
            String start = check_line.substring(0, 1);
            if (start.equals("[")) {
                fileInputStream.close();
            } else {
                String source_str = "";
                String lines_temp = "[" + check_line + "]\n";
                source_str += lines_temp;
                while((lines_temp = bufferedReader.readLine()) != null){
                    lines_temp = "[" + lines_temp + "]\n";
                    source_str += lines_temp;
                }

                fileInputStream.close();
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(source_str.getBytes());
                fileOutputStream.close();
            }

            FileInputStream fis = new FileInputStream(path);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));


            String jsonContext = "";

            while ((jsonContext = br.readLine()) != null) {
                JSONArray jsonArray = JSONArray.fromObject(jsonContext);


                int size = jsonArray.size();
                for (int i = 0; i < size; i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    sourceString = "disease_name= " + jsonObject.get("disease_name") + "\t";
                    sourceString += "paper_id= " + jsonObject.get("paper_id") + "\t";
                    sourceString += "sentence_match= " + (jsonObject.get("sentence_match") + "").replace("\n","/n") + "\t";
                    sourceString += "title= " + (jsonObject.get("title") + "").replace("\n","/n") + "\t";
                    sourceString += "authors= " + (jsonObject.getJSONArray("authors") + "")
                            .replace("[", "").replace("]", "") + "\n";


                }
//            List<MorphDynaBean> listObject = jsonArray.toList(jsonArray);
//            for (int i = 0, j = listObject.size(); i < j; i++) {
//                listObject.get(i);
//                //            System.out.println(listObject.get(i));
//            }
//            for (MorphDynaBean temp : listObject) {
//                temp.get("paper_id");
//                //            System.out.println(temp.get("paper_id"));
//            }
                pw.write(sourceString);
                pw.flush(); // 需要刷新缓冲区才能输出
                fw.flush();

            }
        }

        pw.close();
        fw.close();

    }
}

