package iReader.anti_spam.word2vec.domain;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
/**
 * @author xiaoshan
 * @date   2014-07-15
 * @version 1.0.0
 * */
public class WordNeuron extends Neuron{
	public String  name;                   //词的字面意思
	public double[] syn0 = null;           //inpiut -> hidden
	public List<Neuron> neurons = null;    //从根节点到该词的所有节点  （哈弗曼编码树）
	public int[] codeArr = null;           //该叶节点保存的哈弗曼编码
	
	public WordNeuron(String name,int freq,int layerSize){     //layersize :隐藏层的长度
		this.name = name;
		this.freq = freq;
		this.syn0 = new double[layerSize];
		Random random = new Random();
		for(int i = 0; i < syn0.length; i++) {
			syn0[i] = (random.nextDouble() - 0.5) / layerSize;   //随机生成单词的网络输入数据。
		}
	}
	
	public List<Neuron> makeNeurons(){
		if(neurons != null) {
			return neurons;                //如果已经收集到了路径上的节点，就输出
		}
		Neuron neuron = this; 
		neurons = new LinkedList<Neuron>(); 
		
		while((neuron = neuron.parent) != null ){     //回溯父节点填充节点路径
			neurons.add(neuron);
		}
		
		Collections.reverse(neurons);                 //使根节点排在首位叶节点的父节点排在末尾
		
		codeArr = new int[neurons.size()];            //该词的哈弗曼编码数组，长度同前驱节点路径相等
		
//		System.out.println("codar length is :" + codeArr.length);
		
		for(int i = 0; i < neurons.size()-1; i++){
			codeArr[i] = neurons.get(i+1).code;
		}
		
		codeArr[codeArr.length-1] = this.code;
		
		return neurons;
	}
	
	
}
