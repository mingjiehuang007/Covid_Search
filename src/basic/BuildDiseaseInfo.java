package basic;

import java.io.*;

public class BuildDiseaseInfo {

    public static void main(String[] args) throws Exception {
        BuildDiseaseInfo buildDiseaseInfo = new BuildDiseaseInfo();
        buildDiseaseInfo.run();
    }

    public void run() throws Exception {
        String path_file = ".\\data\\relations\\DiseaseInfo.txt";
        String source_line = "DOID" + "\t" + "Disease_NAME" + "\t" + "Synonym" + "\n";

        try {
            File file = new File(path_file);        //文件路径（路径+文件名）
            if (!file.exists()) {    //文件不存在则创建文件，先创建目录
                File dir_output = new File(file.getParent());
                dir_output.mkdirs();
                file.createNewFile();
            }

            FileOutputStream outStream = new FileOutputStream(file);    //文件输出流用于将数据写入文件
            byte[] sourceByte = source_line.getBytes();
            outStream.write(sourceByte);
            outStream.close();    //关闭文件输出流

        } catch (Exception e) {
            e.printStackTrace();
        }

        String csv = "./data/df_doid_name_synonym.csv";
        BufferedReader br = null;
        String line = "";
        String csvSplitBy = ",(?=([^\"]*\"[^\"]*\")*[^\"]*$)";

        FileWriter fw = new FileWriter(path_file, true);
        PrintWriter pw = new PrintWriter(fw);

        try {
            br = new BufferedReader(new FileReader(csv));
            br.readLine();

            int j = 0;
            while ((line = br.readLine()) != null) {
                // 用逗号作为分隔符
                String[] major = line.split(csvSplitBy);
                String disease = "";

                String regex = "\\*\\*\\*\\*\\*";
                if (major.length > 3) {
                    String[] synonym_arr = major[3].split(regex);
                    for (int i = 0; i < synonym_arr.length - 1; i++) {
                        disease += synonym_arr[i] + ";";
//                        System.out.println(synonym_arr[i]);
                    }
                    disease += synonym_arr[synonym_arr.length - 1];
                }
                source_line = major[1] + "\t" + major[2] + "\t" + disease + "\n";

                pw.write(source_line);
                pw.flush(); // 需要刷新缓冲区才能输出
                fw.flush();

                j++;
                if (j % 1000 == 0){
                    System.out.println(j);
                }

//                System.out.println(source_line);
//                System.out.println("----------------");
            }


        } catch (FileNotFoundException e) {

            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {

            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        pw.close();
        fw.close();

    }
}