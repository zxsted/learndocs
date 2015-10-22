package iReader.anti_spam.word2vec.model;

import iReader.anti_spam.word2vec.domain.WordEntry;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.Map.Entry;

public class MetricUtill {
	private HashMap<String, double[]> wordMap = new HashMap<String, double[]>(); // 保存词向量

	private BiTree bitree = null; // 获得二叉树的实例

	private int words;
	private int size;
	private int topNSize = 50;

	/**
	 * @describe 从词向量文件中加载数据集
	 * */
	public void fillWordMap(String filename) throws IOException {
		BufferedReader br = null;

		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					filename)));
			String line = null;
			String firstLine = br.readLine();
			String[] pair = firstLine.split("\t");
			int veclen = Integer.parseInt(pair[1]);
			String[] tempStrArr = null;
			while ((line = br.readLine()) != null) {
				tempStrArr = line.split("\t");
				String wordname = tempStrArr[0];
				wordMap.put(wordname, new double[veclen]);
				double[] tempArr = wordMap.get(wordname);
				// System.out.print(wordname);
				for (int i = 1; i < tempStrArr.length; i++) {
					tempArr[i - 1] = Double.parseDouble(tempStrArr[i]) * 1000;
					// System.out.print("\t"+resultMap.get(wordname)[i-1]);

				}
				// System.out.print("\n");
			}
		} finally {
			if (br != null) {
				br.close();
				br = null;
			}
		}
	}

	/**
	 * @description : 建立二叉树
	 * */
	public void buildTree() throws IOException {
		BiTree.setWordDic(wordMap);
		bitree = BiTree.getInstance();

	}

	/**
	 * @describe 构建一个set，尽量存储较大的那些词
	 * @param name
	 *            词的名称
	 * @param score
	 *            词的相似度评分
	 * @wordsEntrys 词列表，存储评分较大的那些词
	 * */

	private void insertTopN(String name, double score,
			List<WordEntry> wordsEntrys) {
		if (wordsEntrys.size() < topNSize) {
			wordsEntrys.add(new WordEntry(name, score));
			return;
		}

		double min = Double.MAX_VALUE;
		int minOffe = 0;
		for (int i = 0; i < topNSize; i++) {
			WordEntry wordEntry = wordsEntrys.get(i);
			if (min > wordEntry.score) {
				min = wordEntry.score;
				minOffe = i;
			}
		}

		if (score > min) {
			wordsEntrys.set(minOffe, new WordEntry(name, score));
		}
	}

	/**
	 * @description 寻找近义词（作废）
	 * @param word
	 * */
	public TreeSet<WordEntry> analogy(String words[]) {
		double[] vecsum = new double[wordMap.get(words[0]).length];
		double[] tempvec = null;
		for (int i = 0; i < words.length; i++) {
			tempvec = wordMap.get(words[i]);
			if (tempvec == null)
				continue;
			for (int j = 0; j < wordMap.get(words[0]).length; j++)
				vecsum[j] += wordMap.get(words[i])[j];
		}

		List<WordEntry> WordEntrys = new ArrayList<WordEntry>();
		double[] tempArr = null;
		String name = null;
		for (Map.Entry<String, double[]> entry : wordMap.entrySet()) {
			name = entry.getKey();

			tempArr = entry.getValue();
			double sum = 0;
			double down1 = 0;
			double down2 = 0;
			for (int i = 0; i < tempArr.length; i++) {
				sum += tempArr[i] * vecsum[i];
				down1 += Math.pow(tempArr[i], 2);
				down2 += Math.pow(vecsum[i], 2);
			}
			down1 = Math.sqrt(down1);
			down2 = Math.sqrt(down2);
			// System.out.println(down1 + "\t" + down2);
			if ((double) (down1 * down2) == 0)
				continue;
			insertTopN(name, sum / (double) (down1 * down2), WordEntrys);
		}
		return new TreeSet<WordEntry>(WordEntrys);
	}

	/**
	 * @description : 使用树结构寻找近义词 (作废)
	 * @param : words[] : 输入的由词组成的数组
	 * @param : depth : 树的查询深度
	 * */
	public TreeSet<WordEntry> treeAnalogy(String words[], int depth) {

		// 计算输入向量的和向量
		double[] vecsum = new double[wordMap.get(words[0]).length];
		double[] tempvec = null;
		for (int i = 0; i < words.length; i++) {
			tempvec = wordMap.get(words[i]);
			if (tempvec == null)
				continue;
			for (int j = 0; j < wordMap.get(words[0]).length; j++)
				vecsum[j] += wordMap.get(words[i])[j];
		}

		// 查询树 寻找 topN的相近的词
		Entry<String, double[]> meanpair = bitree.getMean(bitree.getWordDic());
		bitree.searchSimNode(vecsum, meanpair, bitree, depth);
		HashMap<String, Double> resultList = BiTree.getNodeList(); // 获取查询的词集合
		List<WordEntry> WordEntrys = new ArrayList<WordEntry>(); // 用于存储较大相似度词的列表
		for (String word : resultList.keySet()) {
			insertTopN(word, resultList.get(word), WordEntrys);
		}

		return new TreeSet<WordEntry>(WordEntrys);
	}

	public HashMap<String, Double> treeAnalogy2(String words[], double alpha,int depth) {

		// 计算输入向量的和向量
		double[] vecsum = new double[wordMap.get(words[0]).length];
		double[] tempvec = null;
		for (int i = 0; i < words.length; i++) {
			tempvec = wordMap.get(words[i]);
			if (tempvec == null)
				continue;
			for (int j = 0; j < wordMap.get(words[0]).length; j++)
				vecsum[j] += wordMap.get(words[i])[j];
		}

		// 查询树 寻找 topN的相近的词
		Entry<String, double[]> meanpair = bitree.getMeanpair();
		HashMap<String, Double> subset = bitree.getSimNodeSet(vecsum, meanpair,
				bitree,alpha, depth);

		System.out.println("subset`s length is :" + subset.size());
		HashMap<String, Double> resultList = subset;
		System.out.println("resultlist `s length is :" + subset.size());

		return resultList;
	}
	
	
	/**
	 * @description 返回相似词组
	 * @param words  词列表  
	 * @param BayiesWight 贝叶斯权重列表 （主要是 词在面感类别中的概率列表   p(word | 黄色类别)） 
	 * @param alpha  夹角差值
	 * @param depth  查询深度
	 * */
	public HashMap<String, Double> treeAnalogy3(String words[],HashMap<String,Double> BayiesWightDict, double alpha,int depth) {
         double  probsum = Double.MIN_VALUE;
         double probweight       =  Double.MIN_VALUE;
		// 计算输入向量的和向量
		double[] vecsum = new double[wordMap.get(words[0]).length];
		double[] tempvec = null;
		for (int i = 0; i < words.length; i++) {
			tempvec = wordMap.get(words[i]);
			probweight = BayiesWightDict.get(words[i]) ;
			probsum  +=  	probweight;
			if (tempvec == null)
				continue;
			for (int j = 0; j < wordMap.get(words[0]).length; j++)
				vecsum[j] += wordMap.get(words[i])[j]  * probweight;
		}
		for (int i  = 0 ; i  <  words.length ; i++){
			for(int j = 0; j  <  wordMap.get(words[0]).length;  j++ )
				wordMap.get(words[i])[j]  =  wordMap.get(words[i])[j] / probsum;
		}

		// 查询树 寻找 topN的相近的词
		Entry<String, double[]> meanpair = bitree.getMeanpair();
		HashMap<String, Double> subset = bitree.getSimNodeSet(vecsum, meanpair,
				bitree,alpha, depth);

		System.out.println("subset`s length is :" + subset.size());
		HashMap<String, Double> resultList = subset;
		System.out.println("resultlist `s length is :" + subset.size());

		return resultList;
	}
	

	// setter and getter
	public HashMap<String, double[]> getWordMap() {
		return wordMap;
	}

	public void setWordMap(HashMap<String, double[]> wordMap) {
		this.wordMap = wordMap;
	}

	public BiTree getBitree() {
		return bitree;
	}

	public void setBitree(BiTree bitree) {
		this.bitree = bitree;
	}

	public int getTopNSize() {
		return topNSize;
	}

	public void setTopNSize(int topNSize) {
		this.topNSize = topNSize;
	}

}
