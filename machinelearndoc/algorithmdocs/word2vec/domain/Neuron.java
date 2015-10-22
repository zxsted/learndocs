package iReader.anti_spam.word2vec.domain;
/**
 * @author xiaoshan
 * @date   2014-07-15
 * @version 1.0.0
 * */
public abstract class Neuron implements Comparable<Neuron> {
	public int freq;      //词频
	public Neuron parent; //父节点
	public int code;      //哈弗曼数编码    0：左  1:右  
	
	@Override
	public int compareTo(Neuron o) {
		if(this.freq > o.freq) {
			return 1;
		}else {
			return -1;
		}
	}
}
