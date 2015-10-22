package iReader.anti_spam.word2vec.util;
/**
 * @author xiaoshan
 * @date   2014-08-15
 * @version 1.0.0
 * @describe  构建一棵哈夫曼树 ，这里用treeset代替堆排序，如果数据量的的话考虑实现堆排序
 * */

import iReader.anti_spam.word2vec.domain.HiddenNeuron;
import iReader.anti_spam.word2vec.domain.Neuron;
import iReader.anti_spam.word2vec.domain.TreeNode;

import java.util.Collection;
import java.util.TreeSet;
public class Haffman {
	private int layerSize;   
	
	public Haffman (int layerSize) {
		this.layerSize = layerSize;
	}
	
	private TreeSet<Neuron> set =new TreeSet<Neuron> ();  //借助该数据结构实现堆排序
	
	private void merger() {    //选取两个权重最小的两个节点，合并生成一个分支节点
		HiddenNeuron hn = new HiddenNeuron(layerSize);
		Neuron min1 = set.pollFirst();
		Neuron min2 = set.pollFirst();
		hn.freq = min1.freq + min2.freq;
		min1.parent = hn;
		min2.parent = hn;
		
		min1.code = 0;
		min2.code = 1;
		
		set.add(hn);    //将合并的节点加入堆中，自动重拍堆
	}
	
	
	
	public void make(Collection<Neuron> neurons) {     //训练模型时使用
		set.addAll(neurons);
		while(set.size() > 1) {
			merger();
		}
	}
	
	public void remake(Collection<Neuron> neurons){   //重建 时使用
			set.addAll(neurons);
			
			while(set.size() > 1){
				TreeNode tn = new TreeNode(this.layerSize);
				Neuron min1 = set.pollFirst();
				Neuron min2 = set.pollFirst();
				
				tn.freq = min1.freq + min2.freq;
				min1.parent = tn;
				min2.parent = tn;
				
				min1.code = 0;
				min2.code = 1;
				
				set.add(tn);
			}
	}

}
