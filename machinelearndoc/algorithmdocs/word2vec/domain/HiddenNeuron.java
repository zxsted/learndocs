package iReader.anti_spam.word2vec.domain;
/**
 * @author xiaoshan
 * @date   2014-07-15
 * @version 1.0.0
 * */
public class HiddenNeuron  extends Neuron{
	public double[] syn1 ; // hidden->out  隐藏层单元
	
	public HiddenNeuron(int layerSize){
		syn1 = new double[layerSize];       //实际上隐藏层就一个单元，用于计算一个窗口中的词的向量和
	}
}
