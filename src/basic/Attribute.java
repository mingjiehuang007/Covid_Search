package basic;

public class Attribute {

    private String name;

    private double tf;

    private double df;

    private double tf_idf;

    private String postagger;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public double getTf(){
        return tf;
    }
    public void setTf(double tf){
        this.tf = tf;
    }
    public double getDf(){
        return df;
    }
    public void setDf(double df){
        this.df = df;
    }
    public double getTf_idf(){
        return tf_idf;
    }
    public void setTf_idf(double tf_idf){
        this.tf_idf = tf_idf;
    }
    public String getPostagger(){
        return postagger;
    }
    public void setPostagger(String postagger){
        this.postagger = postagger;
    }

    public Attribute(String name, double tf, double df, double tf_idf, String postagger){
        this.name = name;
        this.tf = tf;
        this.df = df;
        this.tf_idf = tf_idf;
        this.postagger = postagger;
    }
}
