package basic;

import java.io.*;
import java.util.List;
import java.lang.String;

import net.sf.ezmorph.bean.MorphDynaBean;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/*
该算法用于将.json文件提取为.txt文件
 */

public class JsonTestPmcJson {
    @SuppressWarnings({"static-access", "deprecation", "unchecked"})

    public static void main(String[] args) throws IOException {
        String sourceString;    //待写入字符串
        sourceString = "paper_id" + "\t" + "body_text" + "\n";
//        String folder_path = "./data/jsons/pmc_json";
        String folder_path = "C:\\Users\\Administrator\\Desktop\\新建文件夹\\data\\jsons\\pmc_json";


        // 新建文档存放转换后的文本
//        String output_path = ".\\data\\json_to_text\\pmc_json_to_text\\pmc_json_to_text.txt";
        String output_path = "C:\\Users\\Administrator\\Desktop\\新建文件夹\\data\\json_to_text\\pmc_json_to_text\\pmc_json_to_text.txt";
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


        File folder = new File(folder_path);
        File[] listOfFolders = folder.listFiles();
        for(File f : listOfFolders) {
            if (!f.isFile()) {
                File[] listOfFiles = f.listFiles();
                String a = f.getAbsolutePath();
                System.out.println(a);
                int count = 0;
                for (File json_files : listOfFiles) {
                    if (json_files.isFile()) {
                        String path = json_files.getAbsolutePath();
                        count++;
                        if(count%1000 == 0){
                            System.out.print(count + " ");}

                        File file = new File(path);
                        String check_line;
                        FileInputStream fileInputStream = new FileInputStream(path);
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
                        check_line = bufferedReader.readLine();
                        String start = check_line.substring(0, 1);
                        if (start.equals("[")) {
                            fileInputStream.close();
                        } else {
                            String lines_temp = "[" + check_line;
                            String line = null;
                            while ((line = bufferedReader.readLine()) != null) {
                                lines_temp = lines_temp + line;
                            }
                            fileInputStream.close();
                            lines_temp = lines_temp + "]";
                            FileOutputStream fileOutputStream = new FileOutputStream(file);
                            fileOutputStream.write(lines_temp.getBytes());
                            fileOutputStream.close();
                        }
                        String JsonContext = new Util().ReadFile(path);
                        JSONArray jsonArray = JSONArray.fromObject(JsonContext);
                    /*String s= java.net.URLDecoder.decode(JsonContext, "utf-8");
                    JSONObject jsonArray = new JSONObject();*/

                        int size = jsonArray.size();
                        for (int i = 0; i < size; i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            sourceString = "paper_id= " + jsonObject.get("paper_id") + "\t";

                            int j = 0;
                            sourceString += "body_text= ";
                            while (j < jsonObject.getJSONArray("body_text").size()) {
                                String temp = jsonObject.getJSONArray("body_text").getJSONObject(j).get("text") + "";
                                if (temp.contains("\n")){
                                    sourceString += temp.replace("\n","/n") + " ";
                                }else{
                                    sourceString += temp + " ";
                                }

                                j++;
                            }
                            sourceString += "\n";

                        }
                        List<MorphDynaBean> listObject = jsonArray.toList(jsonArray);
                        for (int i = 0, j = listObject.size(); i < j; i++) {
                            listObject.get(i);
                            //            System.out.println(listObject.get(i));
                        }
                        for (MorphDynaBean temp : listObject) {
                            temp.get("paper_id");
                            //            System.out.println(temp.get("paper_id"));
                        }
                        pw.write(sourceString);
                        pw.flush(); // 需要刷新缓冲区才能输出
                        fw.flush();

                    }
                }

            }
        }
        pw.close();
        fw.close();
    }
}