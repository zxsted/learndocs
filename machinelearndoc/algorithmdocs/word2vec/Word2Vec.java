package iReader.anti_spam.word2vec.model;

import iReader.anti_spam.word2vec.domain.HiddenNeuron;
import iReader.anti_spam.word2vec.domain.Neuron;
import iReader.anti_spam.word2vec.domain.WordNeuron;
import iReader.anti_spam.word2vec.util.Haffman;
import iReader.anti_spam.word2vec.util.MapCount;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
/**
 * @author xiaoshan
 * @date   2014-07-16
 * @version 1.0.0
 * */
public class Word2Vec {
    private Map<String,Neuron> wordMap = new HashMap<String, Neuron>(); //字典
    
    //训练的特征数
    private int layerSize = 200;
    
    private int linecount = 0;
    
    // 单词长下文的窗口的大小
    private int window = 5;
    
    
    private double sample = 1e-3;                                //设定亚采样的参数，用于随机的将高频词的几个实例丢弃
    
    private double alpha =0.025;                                  //在网络训练时的学习速率，随着读取的文本集的规模改变而变
    private double startingAlpha = alpha;
    
    public int EXP_TABLE_SIZE = 1000;                            //设定log值表的大小，越大越精确，但是越耗内存
    
    private Boolean isCbow = false;                              //是否使用词袋模型
    
    private double[]  expTable = new double[EXP_TABLE_SIZE];     //用于存储预先计算log值的表，使用时查询
    
    private int trainWordsCount = 0;
    
    private int MAX_EXP = 6;                                    //log值表的精度 e-6 ~ e6
    
    public  Word2Vec(Boolean isChow,Integer layerSize,Integer window,Double alpha,Double sample) {
    	
    	createExpTable();
    	this.isCbow = isChow;   
    	this.layerSize = layerSize;
    	this.window = window;
    	this.alpha = alpha;
    	this.sample = sample;
    }
    
    public Word2Vec(){
       createExpTable();   	
    }
    
    /**
     * @description 预计算概率计算函数的值的查询表
     * */
    private void createExpTable(){
    	for(int i = 0; i < EXP_TABLE_SIZE; i++){
    		expTable[i] = Math.exp(((i / (double) EXP_TABLE_SIZE * 2 - 1) * MAX_EXP));
    		expTable[i] = expTable[i] /(expTable[i] + 1);
    	}
    }
    
    /**
     * @descripton 统计词频
     * @param file 分词后的文件
     * @throws IOException
     * */
    private void readVocab(File file) throws IOException{
    	MapCount<String> mc = new MapCount<String>();
    	BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
    		String line = null;
    		while((line = br.readLine()) != null){
    			String[] split = line.split("\t");
    			trainWordsCount += split.length - 1;
    			if(split.length == 1) continue;
    			for(int i = 1; i < split.length-1; i++ ){
    				mc.add(split[i]);
    			}
    		}
    		if(br != null) {
    			br.close();
    			br = null;
    		}
       for (Entry<String, Integer> element : mc.get().entrySet()){
    	   wordMap.put(element.getKey(), new WordNeuron(element.getKey(),element.getValue(),layerSize));
       }
    }
    
    /**
     * @description 构建网络的输出层 （层次sofmax）
     * @param file
     * @throws IOException
     * */
    public void learnFile(File file) throws IOException{
    	readVocab(file);
    	
    	//构建哈夫曼树
    	new Haffman(layerSize).make(wordMap.values());
    	
    	//为每个单词确定其根节点到叶节点的路径，并确定其编码
    	for(Neuron neuron: wordMap.values()){
    		((WordNeuron) neuron).makeNeurons();
    	}
    }
    
    /**
     * @trainModel
     * @param file  分好词的文件
     * @discription 模型的主驱动函数，功能是确定学习概率 alpha 和进行亚采样
     * */
    
    public void trainModel(File file) throws IOException{
    	BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        String line = null;
        long nextRandom = 0;
        int wordCount = 0;         //当前总共读取的词数
        int lastWordCount = 0;     //上一句读取的词数
        int wordCountActual = 0;   //本句实际读取的词数
        while((line = br.readLine()) != null){
        	if(wordCount - lastWordCount > 10000) {
        		System.out.println("alpha:" + alpha + "\tProgress:"
        				+(int) (wordCountActual) / (double) (trainWordsCount + 1) * 100 + "%"); 
        		
        		wordCountActual = wordCount - lastWordCount;  
        		lastWordCount = wordCount;
        		alpha = startingAlpha * (1- wordCountActual / (double) (trainWordsCount + 1));
        		
        		if(alpha < startingAlpha * 0.0001){        //根据实际一行中读取到的单词个数调整学习速率
        			alpha = startingAlpha * 0.0001;
        		}
        	}
        	
        	
        	String[] strs = line.split("\t");
        	wordCount += strs.length - 1;                 //累加读到的单词个数
        	List<WordNeuron> sentence = new ArrayList<WordNeuron>();
        	
//        	System.out.println("sentenc length is " + sentence.size());
        	if (strs.length < 2)
        		continue;
        	
        	for(int i = 1;i < strs.length; i++) {
        		Neuron entry = wordMap.get(strs[i]);
        		if (entry == null) {
        			continue;
        		}
        		//进行亚采样  ，根据单词的概率分布，以一定的概率丢弃当前单词
        		if(sample > 0) {
        			double ran = (Math.sqrt( entry.freq / (sample * trainWordsCount) + 1) *(sample * trainWordsCount / entry.freq)); 
        			nextRandom = nextRandom *25214903917L + 11;    //产生一个随机数
        			if(ran < (nextRandom & 0xFFFF) / 65536)
        				continue;
        		}
        		sentence.add((WordNeuron) entry);
        	}
        	
        	for(int index =0; index < sentence.size();index++){
        		nextRandom = nextRandom * 25214903917L +11;
        		
        		if(isCbow) {
        			cbowGram(index, sentence, (int) nextRandom % window);
        		} else {
        			skipGram(index,sentence,(int) nextRandom % window);
        		}
        	}
        	
        }
        
        System.out.println("Vocab size:" + wordMap.size());
        System.out.println("Words in train file" + trainWordsCount);
        System.out.println("succes train over");       
    }
    
    /**
     * @name skip-gram 模型
     * @param sentence
     * @param  b  ------词袋半径最大值w与实际词袋半径大小之差
     * @param index 词在句子中的下标
     * @discription  这里输出层使用的是层次softmax
     * */
    private void skipGram(int index,List<WordNeuron> sentence,int b) {
    	WordNeuron word = sentence.get(index);
//    	System.out.println("遍历了" + (linecount ++)+"行");
    	int a,c = 0;
    	
    	for(a = b; a < window * 2 + 1 - b ; a++ ){   //遍历实际词袋
    		if(a == window) {    //当a为窗口大小时，循环结束 因为该下表是index对应的词，不进行计算
    			continue;  
    		}
    		c = index - window + a;   //计算出相对于index词的此次for循环的词的位置
    		if(c < 0 || c>= sentence.size()) {   //如果c是在句子边界以外则跳过
    			continue;
    		}
    		
    		double[] neule = new double[layerSize];   //误差项
    		
    		//使用层次softmax
    		List<Neuron> neurons = word.neurons;  //找出index词的从根节点到它路径上的所有前驱节点
    		WordNeuron we = sentence.get(c);      //获取词袋中的当前c词
    		
    		for(int i = 0; i < neurons.size(); i++) {
    			
    			HiddenNeuron out = (HiddenNeuron) neurons.get(i);
    			double f = 0.0;
    			//Propagate hidden->out  : 前向传播，从隐藏层到输出层
    			for(int j = 0; j < layerSize; j++){  //计算词袋中的c词与index词的第i个前驱节点测内积
    				f+=we.syn0[j] * out.syn1[j];   
    			}
    			if(f <= -MAX_EXP || f >= MAX_EXP ){  //如果内积超过限定范围则跳过,f为默认值0
    				continue;
    			}else{
    				 f = (f + MAX_EXP) * (EXP_TABLE_SIZE / MAX_EXP / 2);   //找出f值再表中的位置
    				 f = expTable[(int) f];
    				 System.out.println("change -->" + (linecount ++)+"--time" +"\t" + f);
    			}
    			//'g' 是学习速率alpha乘上梯度值除以out层的权重cij
    			double g = (1 - word.codeArr[i] - f ) * alpha; 
    			
    			//Propagate errors output -> hidden   后向传播，从输出层传到隐藏层
    			for (c = 0; c < layerSize; c++){
    				neule[c] += g * out.syn1[c];
    			}
    			//修正 hidden —> out 的权重
    			for(c = 0; c < layerSize; c++) {
    				out.syn1[c] += g * we.syn0[c]; 
    			}
    		}
    		
    		//学习输入层到隐藏层的权重    这里直接将误差加到输入向量的原因是因为输入向量等于隐藏层输出向量，输入直接连接输出层了
    		for(int j = 0; j < layerSize; j++){
    			we.syn0[j] += neule[j];
    		}
    	}
    }
    
    
    /**
     * cbow-gram 词袋模型
     * @param index
     * @param sentence
     * @param b
     * */
    private void cbowGram(int index,List<WordNeuron> sentence,int b){
        WordNeuron word = sentence.get(index); 
        int a,c = 0;
        System.out.println("遍历了" + (linecount ++)+"行");
        List<Neuron> neurons = word.neurons;   //获取index词的从根节点到自身的所有前驱节点
        
        double[] neule = new double[layerSize];  //误差项 hidden —> out 
        double[] neul  = new double[layerSize];  //隐藏层向量 hidden
        WordNeuron last_word;                    //用于计算记录输入层到隐藏层的向量运算的暂存 节点对象
        
        //遍历index词的窗口中的所有词，计算隐藏层节点的前向传播向量值
        for(a = b ; a < window * 2 + 1 - b; a++){
        	if(a != window){
        		c = index -window + a;            //循环本窗口时的当前词
        		if(c < 0)                         //如果c超出句子的范围则略过
        			continue;
        		if(c > sentence.size())
        			continue;
        		last_word = sentence.get(c);
        		if(last_word == null)
        			continue;
        		for(c = 0; c < layerSize; c++) {
        			neul[c] += last_word.syn0[c];
        		}
        	}
        	
        //层次softmax
        for(int d = 0; d < neurons.size(); d++) {
        	HiddenNeuron out = (HiddenNeuron) neurons.get(d);
        	double f = 0.0;
        	
        	//propagate hidden -> out  前向传播 从隐藏层到输出层
        	for (c = 0; c < layerSize; c++){
        		f += neul[c]*out.syn1[c];
        	}
        		if(f < -MAX_EXP)      //如果f的值在预计算的表的范围以外则设置为默认值
        			continue;      
        		if(f > MAX_EXP)
        			continue;
        		else 
        			f = expTable[(int) ((f + MAX_EXP) * (EXP_TABLE_SIZE / MAX_EXP / 2))];
        		
        		double g = f * (1 - f) * (word.codeArr[d] - f) * alpha;
        		
        		for(c = 0; c < layerSize; c++){
        			neule[c] += g * out.syn1[c];
        		}
        		
        		// 后向传播 ，学习hidden -> out 的权重 
        		for(c = 0; c < layerSize; c++) {
        			out.syn1[c] += g * neul[c];
        		}
        }
        }
        for(a = b; a < window * 2 + 1 - b; a++){
        	if(a != window){
        		c = index - window +a;
        		if(a < 0)
        			continue;
        		if(a > sentence.size())
        			continue;
        		
        		last_word = sentence.get(c);
        		if(last_word == null)
        			continue;
        		for(c = 0; c < layerSize; c++) {
        			last_word.syn0[c] += neule[c];
        		}
        	}
        }
       } 
    
    
    
    //保存模型
    public void saveMethod(File file)  {
    	DataOutputStream dataOutputStream = null;
    	try{
    	dataOutputStream = new DataOutputStream(new FileOutputStream(file)) ;
    	dataOutputStream.writeInt(wordMap.size());
    	dataOutputStream.writeInt(layerSize);
    	
    	double[] syn0 = null;
    	for(Entry<String, Neuron> element : wordMap.entrySet()) {
    		dataOutputStream.writeUTF(element.getKey());
    		System.out.print(element.getKey());
    		syn0 = ((WordNeuron) element.getValue() ).syn0;
    		for(double d : syn0) {
    			dataOutputStream.writeFloat(((Double) d).floatValue());
    			System.out.print("\t"+((Double) d).floatValue());
    		}
    		System.out.println();
    	}
    	}catch(IOException e)
    	{ 
    		e.printStackTrace();
    	}
    	}
    
  //保存模型2
    public void saveMethodToTxt(File file)  {
    	FileWriter  fw= null;
    	try{
    	fw= new FileWriter(file) ;
    	fw.write(wordMap.size() + "\t");
    	fw.write(layerSize + "\n");
    	
    	double[] syn0 = null;
    	for(Entry<String, Neuron> element : wordMap.entrySet()) {
    		fw.write(element.getKey());
//    		System.out.print(element.getKey());
    		syn0 = ((WordNeuron) element.getValue() ).syn0;
    		for(double d : syn0) {
    			fw.write("\t"+d);
//    			System.out.print("\t"+((Double) d).floatValue());
    		}
    		fw.write("\n");
//    		System.out.println();
    	}
    	}catch(IOException e)
    	{ 
    		e.printStackTrace();
    	}
    	}//getter and setter

	public int getLayerSize() {
		return layerSize;
	}

	public void setLayerSize(int layerSize) {
		this.layerSize = layerSize;
	}

	public int getWindow() {
		return window;
	}

	public void setWindow(int window) {
		this.window = window;
	}

	public double getSample() {
		return sample;
	}

	public void setSample(double sample) {
		this.sample = sample;
	}

	public double getAlpha() {
		return alpha;
	}

	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}

	public Boolean getIsCbow() {
		return isCbow;
	}

	public void setIsCbow(Boolean isCbow) {
		this.isCbow = isCbow;
	}
   
    
    
}
