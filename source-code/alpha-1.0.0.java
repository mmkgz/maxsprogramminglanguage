//alpha-1.0.0
//pushed 23:40 - July 2, 2024
//Java 8 / 1.8
import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.List;

public class Main {
    static class Value{
        public String type;
        public String value;
        public String ogType;
        public String ogValue;
        public static final Value SAFE_NULL = new Value("null");
        public Value(String value){

            this.value=value;
            ogValue=value;
            type=Value.checkType(value);
            ogType=type;
            //System.out.println(type+", "+value);
            if(type.equals("special character"))return;
            if(type.equals("string")){
                //System.out.print(value);
                if(value.startsWith("\"")&&value.endsWith("\"")||value.startsWith("'")&&value.endsWith("'"))value=value.substring(1,value.length()-1);
                this.value=value;
                int start=-1;
                int end=-1;
                char prev=' ';
                for (int i = 0; i < value.length(); i++) {
                    char thisChar=value.charAt(i);
                    if(prev!='\\') {
                        if (value.charAt(i) == '{') {
                            start=i;
                        }else if(value.charAt(i)=='}'){
                            if(start==-1){logError("a '}' used to end an inline value must be preceded by a '{'",false);}
                            else {
                                end=i;
                                //System.out.println("\\");
                                String substringed = value.substring(start,end+1);
                                String isolated = substringed.substring(1,substringed.length()-1);
                                //System.out.println(this.value);
                                //System.out.println(isolated);
                                //System.out.println("\\{"+isolated+"\\}");
                                this.value=this.value.replaceFirst("\\{"+isolated.replaceAll("\\*","\\\\*").replaceAll("\\+","\\\\+")+"\\}",calculate(isolated).getValue());

                            }
                        }
                    }
                    prev=thisChar;
                }
            }

            if(type.equals("variable")){

                Value var = variables.get(value);

                try{while (var.type.equals("variable")){var=variables.get(var.value);if(var==null){this.value=null;return;}} }
                catch(Exception e){e.printStackTrace();}
                this.value=var.getValue();
                type=var.type;
            }

        }
        public Value(String value,String type){

            this.value=value;
            ogValue=value;
            this.type=type;
            ogType=type;
            //System.out.println(type+", "+value);
            if(type.equals("variable")){

                Value var = variables.get(value);

                try{while (var.type.equals("variable")){var=variables.get(var.value);if(var==null)this.value=null;} }
                catch(Exception e){e.printStackTrace();}
                this.value=var.getValue();
                type=var.type;
            }

        }
        public static String checkType(String value){
            String type;
            if((value.startsWith("\"")&&value.endsWith("\""))||(value.startsWith("'")&&value.endsWith("'"))){type="string";  }
            else {type="number";
                try {
                    double i = Double.parseDouble(value);
                }catch(NumberFormatException e){
                    type="variable";
                    //System.out.println(value);
                    //System.out.println(value.length()==1);
                    //System.out.println(specialCharacters.contains(value));
                    if(!variables.containsKey(value))type="string";
                    if(value.length()==1&&specialCharacters.contains(value)) type="special character";

                }    }
            return type;
        }

        public String getValue(){

            return value;
        }

    }

    //parser variables
    public static ArrayList<String> code=new ArrayList<>();
    public static ArrayList[] tokens;
    public static String tokenSplitters = "+-*/=()[]{}:;,";
    public static File file;
    public static int currLine=0;


    //Function Variables

    public static HashMap<String,Value> variables = new HashMap<>();
    public static HashMap<String, Color> colors = new HashMap<>();
    public static HashMap<String,String> consoleColors = new HashMap<>();
    public static String specialCharacters = "+-*/=!@#$%^&*()[]{}|\\?,.<>`~";
    //calculate
    public static ScriptEngineManager mgr;
    public static ScriptEngine engine;
    public static Bindings tempEvalVariables;
    //if
    public static int ifsDeep=0;
    public static HashMap<Integer, Integer>ifStatements=new HashMap<>();
    //repeat
    public static int repeatBubbleStart=-1,repeatBubbleEnd=-1;
    public static int repeatsDeep=0, repeatsDeepModifier=0;
    public static HashMap<Integer, Integer>repeatStatements=new HashMap<>();
    //input
    public static Scanner scanner = new Scanner(System.in);
    public static Random random = new Random();
    static{
        consoleColors.put("black","\033[0;30m");
    }



    public static void main(String[] args) {

        //System.out.println(calculate("hello!"));
        //System.out.println("\033[1;31mHello");
        //System.out.println("uo");

        mgr = new ScriptEngineManager();
        engine = mgr.getEngineByName("JavaScript");
        tempEvalVariables = engine.createBindings();
                //System.out.println(engine==null);
        file=new File("C:\\Users\\mmaxl\\Info\\MyProgrammingLanguage\\src\\code.mk1");
        Scanner sc= null;
        try {
            sc = new Scanner(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        while(sc.hasNextLine()){
            String s = sc.nextLine();
            if(s.isEmpty())continue;
            code.add(s);
        }
        tokens = new ArrayList[code.size()];
        tokenize();
    }
    public static void tokenize(){
        for (int i = 0; i < code.size(); i++) {
            String line = code.get(i);
            int pointer=0;
            boolean inQuotations=false,inApostrophes=false;
            char prev=' ';
            tokens[i]=new ArrayList<String>();
            for (int j = 0; j < line.length(); j++) {
                char curr = line.charAt(j);
                if(prev!='\\') {
                    if (curr == '"') {
                        if (!inApostrophes) inQuotations = !inQuotations;
                    } else if (curr == '\'') {
                        if (!inQuotations) inApostrophes = !inApostrophes;
                    } else if(!inQuotations&&!inApostrophes){
                        if(curr==' '){
                            tokens[i].add(line.substring(pointer,j).trim());
                            //.0System.out.println(line.substring(pointer,j));
                            pointer=j+1;
                        }  else if(tokenSplitters.contains(String.valueOf(curr))){
                            if(!tokenSplitters.contains(String.valueOf(prev))){tokens[i].add(line.substring(pointer,j).trim());
                            //System.out.println(line.substring(pointer,j));
                                  }
                            tokens[i].add(line.substring(j,j+1).trim());
                            //System.out.println(line.substring(j,j+1));
                            pointer=j+1;
                        }
                    }
                }else{
                    if(inQuotations||inApostrophes){

                    }else{
                        
                    }
                }
                if(j==line.length()-1)     {
                    tokens[i].add(line.substring(pointer).trim());
                    //System.out.println(line.substring(pointer));
                    
                }
                prev=curr;
            }
            for (int j = 0; j < tokens[i].size(); j++) {
                if(tokens[i].get(j).toString().isEmpty()){tokens[i].remove(j);}
            }
            tokens[i].removeAll(Arrays.asList("",null," "));
            //System.out.println(Arrays.toString(tokens[i].toArray()));
        }
        runCode();
    }
    public static void runCode(){
        for (int i = 0; i < code.size(); i++) {
            currLine=i+1;
            runLine(tokens[i]);
        }
    }
    public static List<List<String>> splitBy(List<String>tokens,boolean include, String... splitBy){
        List<List<String>>parameters=new ArrayList<>();
        int pointer=0;
        for (int i = 0; i < tokens.size(); i++) {
            for (String s : splitBy) {
                if (tokens.get(i).equals(s)) {
                    parameters.add(tokens.subList(pointer, i));
                    if(include)parameters.add(tokens.subList(i,i+1));
                    pointer = i + 1;
                }

            }
            if(i==tokens.size()-1)    parameters.add(tokens.subList(pointer,tokens.size()));
        }



        return parameters;
    }
    public static ArrayList<String>[] fromToCodeSave(int startInclusive, int endInclusive){
        //System.out.println(startInclusive+", "+endInclusive);
        //System.out.println(endInclusive-startInclusive+1);
        ArrayList[] arr=new ArrayList[endInclusive-startInclusive+1];
        for (int i = startInclusive; i < endInclusive+1; i++) {//System.out.println(Arrays.toString(tokens));
            arr[i-startInclusive]=tokens[i];

        }
        return arr;
    }
    public static boolean notInIf(){
        return (ifStatements.isEmpty()||(ifStatements.get(ifStatements.size()-1)!=-1));
    }
    public static void runLine(List<String> tokens){
        //for (int j = 0; j < tokens[i].size(); j++) {//System.out.println(tokens[i].get(1)=="=");
        //String first = tokens.get(0);

        //endif tag check


        if(repeatsDeep-repeatsDeepModifier==0&&notInIf()){

        //VARIABLE SET
        if(tokens.size()>1&& Objects.equals(tokens.get(1), "=")){
            List<List<String>>parameters=splitBy(tokens.subList(2,tokens.size()),false,",");
            if(tokens.size()>2)if(!variables.containsKey(tokens.get(0))){variables.put(tokens.get(0),calculate(parameters.get(0)));  }else{variables.replace(tokens.get(0),calculate(parameters.get(0)));}
            else variables.remove(tokens.get(0));
            //System.out.println(Arrays.toString(variables.keySet().toArray()));





        } if(tokens.get(0).equals("repeat")){
            List<List<String>>parameters=splitBy(tokens.subList(1,tokens.size()),false,",");
            int times=Integer.parseInt(calculate(parameters.get(0)).getValue());
            if(parameters.size()>1)for (int i = 0; i < times; i++) {

                runLine(tokens.subList(3, tokens.size()));


            }
            else {
                repeatBubbleStart=currLine;
                repeatsDeep++;
                repeatStatements.put(repeatsDeep,times);
            }

        }


        else{
            doFunction(tokens.get(0),tokens.subList(1,tokens.size()));
        }
        }



        if(tokens.get(0).equals("if")) {
            //System.out.println("inif");
            boolean oneline =splitBy(tokens,false,",").get(1).get(0).equals("then");
            boolean dorun=checkStatement(tokens.subList(1,tokens.size()));
            if(oneline) {
                //System.out.println("it work");
                //System.out.println(tokens.size());
                //System.out.println(splitBy(tokens,false,",").get(1).get(0).equals("then"));

                if(currLine-1<code.size()){
                    //System.out.println(Main.tokens[currLine].get(0));
                    //System.out.println(Main.tokens[currLine].get(0));
                    if(Main.tokens[currLine].get(0).equals("else")){if(dorun){ifStatements.put(ifsDeep,1);
                        /*System.out.println("dont run else");*/}else {ifStatements.put(ifsDeep,-1);
                        /*System.out.println("do run else");*/}ifsDeep++;}
                }
                if (dorun) {

                    runLine(tokens.subList(tokens.indexOf("then")+1, tokens.size()));
                }
            }else{
                if(dorun)ifStatements.put(ifsDeep,1);
                else ifStatements.put(ifsDeep,-1);
            }
            if(!oneline) ifsDeep++;
        } else if(tokens.get(0).equals("else")){
            //System.out.println(ifStatements);
            //System.out.println(ifsDeep-1);
            if (ifStatements.get(ifsDeep-1) == 0) logError("'else' tag should also have an 'if' tag before it", false);
            //System.out.println(ifStatements.get(ifsDeep-1));

            if(ifStatements.get(ifsDeep-1)==-1)ifStatements.replace(ifsDeep-1,1);
            else if(ifStatements.get(ifsDeep-1)==1)ifStatements.replace(ifsDeep-1,-1);
            boolean dorun=false;
            //if(ifStatements.get(ifsDeep-1)==-1)dorun=false;
            if(ifStatements.get(ifsDeep-1)==1)dorun=true;
            //System.out.println(ifStatements.get(ifsDeep-1));
            boolean oneline=false;
            if(tokens.size()>1)oneline=true;
            if(oneline){
                if(dorun){
                    runLine(tokens.subList(tokens.indexOf("else")+1, tokens.size()));
                }
                ifStatements.remove(ifsDeep-1);
                ifsDeep--;
            }else{

            }
            //System.out.println(ifStatements.get(ifsDeep-1));
            //runLine(tokens.subList(tokens.indexOf("else")+1, tokens.size()));
        }else if(tokens.get(0).equals("endif")) {

            if (ifStatements.get(ifsDeep-1) == 0) logError("'endif' tag should also have an 'if' or 'else' tag before it", false);
            else {ifStatements.remove(ifsDeep-1);ifsDeep--;}

        }else if(tokens.get(0).equals("endrepeat")){
            repeatBubbleEnd=currLine-2;

            ArrayList<String>[] block = fromToCodeSave(repeatBubbleStart,repeatBubbleEnd);

            for (int i = 0; i <repeatStatements.get(repeatsDeep); i++) {
                for (int j = 0; j < block.length; j++) {
                    currLine=j+2;

                    //System.out.println(j+": "+code.get(currLine-1));
                    repeatsDeepModifier=repeatsDeep;
                    runLine(block[j]);
                }
            }
            repeatsDeep--;
        }
        //System.out.println(currLine+": "+ifsDeep);
        //System.out.println(ifStatements.size());
    }
    public static void setVariable(String var, String value){
        if(variables.containsKey(var))variables.replace(var,new Value(value));
        else variables.put(var,new Value(value));
    }
    public static void setVariable(String var, String value,String type){
        if(type.equals("num"))type="number";
        if(type.equals("text"))type="string";
        if(type.equals("str"))type="string";


        if(variables.containsKey(var))variables.replace(var,new Value(value,type));
        else variables.put(var,new Value(value,type));
    }
    public static void doFunction(String name,List<String>tokens){
        List<List<String>>parameters=splitBy(tokens,false,",");
        String toChange;
        switch(name){
            case "shout":
                System.out.println(parameters.get(0));
                System.out.println(calculate(parameters.get(0)).value);
                break;
            case "input":
                toChange=tokens.get(1);
                //String toType = "string";
                if(!variables.containsKey(toChange))variables.put(toChange,Value.SAFE_NULL);
                if(!Value.checkType(toChange).equals("variable"))logError("",false);
                setVariable(toChange,scanner.nextLine(),tokens.get(0));
                break;
            case "random":
                List<List<String>> tokenized=splitBy(tokens,false,",");
                toChange=tokens.get(1);
                //if(!Value.checkType(toChange).equals("variable"))logError("",false);
                if(!variables.containsKey(toChange))variables.put(toChange,Value.SAFE_NULL);
                int smaller = Integer.parseInt(calculate(tokenized.get(1)).getValue());
                int bigger = Integer.parseInt(calculate(tokenized.get(2)).getValue());
                if(tokens.get(0).equals("int"))variables.replace(toChange, new Value(String.valueOf(random.nextInt(bigger-smaller+1)+smaller)));

                break;
            case "console.color":
                break;
            case "wait":
                try {
                    Thread.sleep(Long.parseLong(calculate(parameters.get(0)).value));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            case "debug":
                //calculate(parameters.get(0));
                break;
        }
    }
    public static boolean checkStatement(List<String> tokens){
        return checkSimpleStatement(tokens);
    }
    public static boolean checkSimpleStatement(List<String> tokens){
        List<List<String>> params = splitBy(tokens,true,"=",",",">","<");
        //System.out.println(Arrays.toString(params.toArray()));
        //System.out.println(calculate(params.get(0)).value);
        Value a = calculate(params.get(0));
        Value b = calculate(params.get(2));
        //System.out.println(b.value);
        //System.out.println(a.value+", "+b.value);
        //System.out.println(a.type+", "+b.type);
        //System.out.println(params.get(1));
        //System.out.println(params.get(1).get(0));
        switch(params.get(1).get(0)){
            case "=":
                //System.out.println(Objects.equals(a.getValue(), b.getValue()));
                return(Objects.equals(a.getValue(), b.getValue())&&Objects.equals(a.type, b.type));
            case "<":
                //System.out.println(Objects.equals(a.getValue(), b.getValue()));
                //System.out.println(Double.parseDouble(a.getValue())< Double.parseDouble(b.getValue()));
                return(Double.parseDouble(a.getValue())< Double.parseDouble(b.getValue()));
            case ">":
                return(Double.parseDouble(a.getValue())> Double.parseDouble(b.getValue()));

        }
        return false;
    }
    public static Value calculate(List<String> params){
        String expression="";
        for (String param : params) {

            if(Value.checkType(param).equals("string")){
                /*System.out.println("temp"+(tempEvalVariables.size()+1));*/tempEvalVariables.put("temp"+(tempEvalVariables.size()+1),new Value(param).value);param="temp"+(tempEvalVariables.size());
                /*System.out.println("temp"+tempEvalVariables.size()+", "+tempEvalVariables.get("temp"+(tempEvalVariables.size()-1)));*/}
            expression = expression.concat(param);
        }

        for (String param : params) {
            if(Value.checkType(param).equals("string"))param="temp"+(tempEvalVariables.size());
            if (variables.containsKey(param)) expression = expression.replaceFirst(param, new Value(param).getValue());
        }
        //System.out.println(expression);
        try {
            //if(!tempEvalVariables.isEmpty()) System.out.println(tempEvalVariables.keySet());

            String result = String.valueOf(engine.eval(expression,tempEvalVariables).toString());
            Value val;
            String type = "number";
            //System.out.println(result);
            try{
                Double.parseDouble(result);
                val=new Value(result);
            }catch(NumberFormatException e){
                type="string";
                val=new Value(result);
            }
            //System.out.println(val.getValue());
            tempEvalVariables.clear();
            return val;
        } catch (ScriptException e) {
            tempEvalVariables.clear();
            throw new RuntimeException(e);

        }

    }
    public static String calculateRaw(List<String> params){
        String expression="";
        for (String param : params) {
            expression = expression.concat(param);
        }

        for (String param : params) {
            if (variables.containsKey(param)) expression = expression.replaceFirst(param, new Value(param).getValue());
        }
        try {
            String result = String.valueOf(engine.eval(expression).toString());
            Value val;
            String type = "number";
            //System.out.println(result);
            try{
                Double.parseDouble(result);
                val=new Value(result);
            }catch(NumberFormatException e){
                type="string";
                val=new Value("\""+result+"\"");
            }
            //System.out.println(val.getValue());
            return result;
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }
    }
    public static Value calculate(String expression){

        int pointer=0;
        boolean inQuotations=false,inApostrophes=false;
        char prev=' ';
        ArrayList<String>exTokens=new ArrayList<>();
        for (int j = 0; j < expression.length(); j++) {
            char curr = expression.charAt(j);
            if(prev!='\\') {
                if (curr == '"') {
                    if (!inApostrophes) inQuotations = !inQuotations;
                } else if (curr == '\'') {
                    if (!inQuotations) inApostrophes = !inApostrophes;
                } else if(!inQuotations&&!inApostrophes){
                    if(curr==' '){
                        exTokens.add(expression.substring(pointer,j).trim());
                        //.0System.out.println(expression.substring(pointer,j));
                        pointer=j+1;
                    }  else if(tokenSplitters.contains(String.valueOf(curr))){
                        if(!tokenSplitters.contains(String.valueOf(prev))){exTokens.add(expression.substring(pointer,j).trim());
                            //System.out.println(expression.substring(pointer,j));
                        }
                        exTokens.add(expression.substring(j,j+1).trim());
                        //System.out.println(expression.substring(j,j+1));
                        pointer=j+1;
                    }
                }
            }else{
                if(inQuotations||inApostrophes){

                }else{

                }
            }
            if(j==expression.length()-1)     {
                exTokens.add(expression.substring(pointer).trim());
                //System.out.println(expression.substring(pointer));

            }
            prev=curr;
        }
        //System.out.println(exTokens);
        return calculate(exTokens);
    }
    public static void logError(String errorText, boolean fatalError){
        if(fatalError)System.out.println("Fatal Error at line "+currLine+": "+errorText);
        else System.out.println("Error at line "+currLine+": "+errorText);
    }
}
