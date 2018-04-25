package sqlcollector.utils;

public class ParserStatement {

    private String statement;

    public ParserStatement() { }

    public String parser(){
        return this.statement.replaceAll("\\s+", " ").trim();
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }
}
