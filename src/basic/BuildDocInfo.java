package basic;

import java.io.*;

public class BuildDocInfo {

    public static void main(String [] args) throws Exception{
        BuildDocInfo buildDocInfo = new BuildDocInfo();
        buildDocInfo.run();
    }

    public void run() throws  Exception{
        String path_file = ".\\data\\relations\\DocInfo.txt";
        String source_line = "doc_id" + "\t" + "pubmed_id" + "\t" + "doi" + "\t" + "pmc_id" + "\t" + "title" + "\t"
                + "publish_time" + "\t" + "authors" + "\t" + "journal" + "\n";

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

        String csv = "./data/metadata.csv";
        BufferedReader br = null;
        String line ="";
        String csvSplitBy = ",(?=([^\"]*\"[^\"]*\")*[^\"]*$)";

        FileWriter fw = new FileWriter(path_file, true);
        PrintWriter pw = new PrintWriter(fw);

        try {
            br = new BufferedReader(new FileReader(csv));
            br.readLine();

            int j = 0;
            while((line = br.readLine()) != null){

                // 用逗号作为分隔符
                String [] major = line.split(csvSplitBy);
                if (!major[1].equals("") || !major[5].equals("")) {
                    if(major[1].equals("")){
                        major[1] = major[5];
                    }
                    for(int i = 0; i < 12; i++){
                        if(major[i].equals("")){
                            major[i] = "0";
                        }
                    }

                    if(major[1].contains(";")){
                        String[] major_temp = major[1].split(";");
                        for(String m : major_temp){
                            source_line = m.trim() + "\t" + major[6] + "\t" + major[4] + "\t" + major[5] + "\t"
                                    + major[3] + "\t" + major[9] + "\t" + major[10] + "\t" + major[11] + "\n";
                            pw.write(source_line);
                            pw.flush(); // 需要刷新缓冲区才能输出
                            fw.flush();
                        }

                    }
                    else {
                        source_line = major[1] + "\t" + major[6] + "\t" + major[4] + "\t" + major[5] + "\t"
                                + major[3] + "\t" + major[9] + "\t" + major[10] + "\t" + major[11] + "\n";
                        pw.write(source_line);
                        pw.flush(); // 需要刷新缓冲区才能输出
                        fw.flush();
                    }

                    j++;
                    if (j % 10000 == 0){
                        System.out.println(j);
                    }
//                    System.out.println(source_line);
//                System.out.println("doc_id: " + major[1]);
//                System.out.println("pubmed_id: " + major[6]);
//                System.out.println("doi: " + major[4]);
//                System.out.println("title: " + major[3]);
//                System.out.println("publish_time: " + major[9]);
//                System.out.println("authors: " + major[10]);
//                System.out.println("journal: " + major[11]);
//                    System.out.println("----------------");
                }

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
