package iReader.anti_spam.word2vec.model;

import iReader.anti_spam.word2vec.domain.Pair;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.Vector;

public class BiTree {
	
	private static BiTree bitree = null;
	
	private BiTree left = null;
    private BiTree right = null;
    
    private double[] weightvec = new double[200];
    
    private String word;
    
    private int id ;
    
    private double degree;          //这里的sim其实是夹角
    
    public int subnum = 0;
    
    private static HashMap<String,double[]> wordDic = new HashMap<String,double[]>();
    
    private static HashMap<String,Double> digreeMap = new HashMap<String,Double>();  
    
    public static HashMap<String,Double> nodeList = new HashMap<String,Double> ();   //查询到的词列表
    
    private  static Entry<String,double[]> meanpair = null;
    
    private BiTree(){}
    
    private BiTree(double[] weightvec ,BiTree left, BiTree right, double degree,int id,String word) { 
        this.weightvec = weightvec;
        this.left = left;
        this.right = right;
        this.degree = degree;
        this.id = id;
        this.word = word;
    }
    
    
    /**
     * @description  获取单例类的方法
     * */
    
    public synchronized static BiTree getInstance(){
    	if(bitree == null) {
    		bitree = new BiTree();
    		
    		 meanpair =getMean( wordDic);
        	HashMap<String,Double> digreeMap = computesim(meanpair,wordDic);
        	ArrayList<Entry<String,Double>> sortedList =  sort(digreeMap);
             bitree = buildTree(sortedList);
    	}
    	return bitree;
    }
    
    
    /**
     * @description  加载数据
     * */
    public static void loadData(String file) throws IOException
    {
    	BufferedReader br = null;
    	
    	try{
    		
    		br = new BufferedReader(new FileReader(file));
    		String templine = null;
    		br.readLine();
    		int count =0;
    		while((templine = br.readLine()) != null) {
    			String [] temp = templine.split("\t");
    			String word = temp[0];
    			wordDic.put(word, new double[200]);
    			double[] temparray = wordDic.get(temp[0]);
    			for(int i = 0; i < temp.length-1;i++){
    				temparray[i] = Double.parseDouble(temp[i+1]);
    			}
    		}
    		System.out.println("dictionary lenght is " + wordDic.size());	
    	}finally{
    		if(br != null){
    			br.close();
    			br = null;
    		}
    	}
    }
    
    
    /**
     * @description  随机的挑选一个词向量  选择种子向量的方法1
     * */
    public  Entry<String,double[]> getSeed(HashMap<String,double[]> wordDic) {
    	double r = Math.random();
    	Entry<String,double[]>  pair = null;
    	int count = 0;
    	for(Entry<String,double[]> entry : wordDic.entrySet())
    	{
    		double prob =(double) count++ /(double) wordDic.size();
    		pair  = entry;
    		if (prob > r) break;
    	}  
    	
    	return pair;
    	
    }
    
    /**
     * @description  将文章中所有的词向量的平均向量作为种子向量      选择种子节点的方法2
     * */
    
    public static Entry<String,double[]> getMean(HashMap<String,double[]> wordDic) {
    	
    	int len = wordDic.size();
    	double [] resultvec = new double[wordDic.size()];
    	HashMap<String,double[]> oneMap = new HashMap<String,double[]>(1);
    	double[] tempvec = null;
    	for(String word: wordDic.keySet()) {
    		tempvec = wordDic.get(word);
    		
    		for(int i = 0; i < 200; i++) {
    			resultvec[i] += tempvec[i]/(double) len;
    		}
    	}
    	oneMap.put("mean", tempvec);
    	return  oneMap.entrySet().iterator().next();
    }
    
    /**
     * @description  计算相似度  余弦相似度
     * */
    private static double simdistance(double[] v1 ,double[] v2){
    	double b1 = 0.0;
    	double b2 = 0.0;
    	double m  = 0.0;
    	
    	for(int i = 0; i < 200; i++) {
    		b1 += v1[i]*v1[i];
    		b2 += v2[i]*v2[i];
    		m  += v1[i]*v2[i];
    	}
    	
    	return m / (Math.sqrt(b1)*Math.sqrt(b2));
    } 
    
   /**
    *@description   计算夹角 计算一个词典子集 与 指定点的夹角列表
    * */
    private static HashMap<String,Double> computesim(Entry<String,double[]> randomPair,HashMap<String,double[]> wordDic){
    	double sim = 0.0;
    	for(Entry<String,double[]> entry:wordDic.entrySet() ){
    		sim = simdistance(randomPair.getValue(),entry.getValue());
    		double digree = Math.acos(sim);
    		digreeMap.put(entry.getKey(),digree);
    	}
    	return digreeMap;
    }
    
    
    
    /**
     *@description   排序   对子集的夹角序列进行排序
     * */
    
    public static ArrayList<Entry<String,Double>> sort(HashMap<String,Double> digreeMap ){
    	ArrayList<Entry<String,Double>> arrayList = new ArrayList<Entry<String,Double>>(digreeMap.entrySet());
    	
       Collections.sort(arrayList , new Comparator<Entry<String,Double>>(){
    		
    		public int compare(Entry<String,Double> o1, Entry<String,Double> o2)  {
    			Entry<String,Double> obj1 = (Entry<String,Double>) o1;
    			Entry<String,Double> obj2 = (Entry<String,Double>) o2;
    			return obj1.getValue().compareTo( obj2.getValue());
    		}
    	});
    	return   arrayList;
    }
    
    
    /**
     * @description  建树(递归的)（溢出）
     * */
    private BiTree rebuildTree(ArrayList<Entry<String,Double>> arrayList,int begin,int end){
    	int midindex =begin +((end-begin + 1) / 2 ) + 1;
    	String word = arrayList.get(midindex).getKey();
    	double sim =  arrayList.get(midindex).getValue();
    	BiTree tree = new BiTree(wordDic.get(word),null,null,sim,0,word);
    	
    	if((end-begin) != 0) {   	
	    	tree.left  = rebuildTree( arrayList,begin,midindex-1);
	    	tree.right = rebuildTree( arrayList,midindex+1,end);
        	}
    		return tree;
    }

   
    
    /**
     * @description  非递归构建查询树 ，  使用序列中位数作为根节点
     * */
    private static  BiTree buildTree(ArrayList<Entry<String,Double>> arrayList){
    	//一个栈用于暂存
    	LinkedList<Pair<Integer>>   ll  = new LinkedList<Pair<Integer>> ();
    	int begin = 0 ;
    	int end  = arrayList.size() - 1;
    	ll.push(new Pair(begin,end) );
        //保存中间节点
    	HashMap<String ,BiTree> midNodes = new HashMap<String,BiTree> (); 

        
    	     int len = end - begin+1;
             int mid = 0;
    		 if( len  % 2 == 1) mid = len / 2 + begin;
    		 else if ( len % 2 == 0 ) mid = len / 2 + begin-1;

    		 String word = arrayList.get(mid).getKey();
    		 System.out.println("root name is :" + word + "index is : " + mid);
             double sim =  arrayList.get(mid).getValue();

    		 BiTree rootnode =  new BiTree(wordDic.get(word),null,null,sim,0,word);
    		 midNodes.put(word,rootnode);


             int count = 0;
    	while(ll.size() > 0) {
    		 
    	      Pair<Integer> pair = ll.pop();
    		  begin = pair.getLeft();
    		  end   = pair.getRight();
    		  len = end - begin+1;

    		 if(len == 1 || len == 0) continue;     //长度是1时没有左右孩子节点

    		 if( len  % 2 == 1) mid = len / 2 + 0 + begin;
    		 else if ( len % 2 == 0 ) mid = len / 2 + begin-1;

    		 word = arrayList.get(mid).getKey();
             sim =  arrayList.get(mid).getValue();
             if(count == 0 ) System.out.println(  "the first word is: " + word + "index is : " + mid);
             count++;

    		 BiTree thisnode =  midNodes.get(word);

             //left  子树
    		 int lbegin = begin;
    		 int lend = mid - 1;
    		 int llen = lend-lbegin+1;
    		 int lmid = 0;
    		 if((llen) >= 1) {
    		 if( llen  % 2 == 1) lmid = llen / 2  + lbegin;
    		 else if ( llen % 2 == 0 ) lmid = llen / 2 + lbegin -1;
    		 ll.push(new Pair(lbegin,lend));
    		    String lword= arrayList.get(lmid).getKey();
            	double lsim = arrayList.get(lmid).getValue();
            	BiTree btl =  new BiTree(wordDic.get(lword),null,null,lsim,0,lword);
                midNodes.put(lword,btl);
    			thisnode.left = btl;
    		 }else if(llen == 0) {
    			 lmid = lbegin;
    			 ll.push(new Pair(lbegin,lbegin));
    			String lword= arrayList.get(lmid).getKey();
             	double lsim = arrayList.get(lmid).getValue();
             	BiTree btl =  new BiTree(wordDic.get(lword),null,null,lsim,0,lword);
                midNodes.put(lword,btl);
     			thisnode.left = btl;
    		 }

    		 //right 子树
    		 int rbegin = mid + 1;
    		 int rend = end;
    		 int rlen =  rend - rbegin +1;
    		 int rmid = 0;
    		 if(rlen >= 1) {
    		 if( rlen  % 2 == 1) rmid = rlen / 2 +  rbegin;
    		 else if ( rlen % 2 == 0 ) rmid = rlen / 2 + rbegin - 1;
    		 ll.push(new Pair(rbegin,rend));
                String rword= arrayList.get(rmid).getKey();
            	double rsim = arrayList.get(rmid).getValue();
            	BiTree btr =  new BiTree(wordDic.get(rword),null,null,rsim,0,rword);
                midNodes.put(rword,btr);
    			thisnode.right = btr;
    		 }else if(rlen == 0) {
    			 rmid = rbegin;
    			 ll.push(new Pair(rbegin,rbegin));
    			String rword= arrayList.get(rmid).getKey();
             	double rsim = arrayList.get(rmid).getValue();
             	BiTree btr =  new BiTree(wordDic.get(rword),null,null,rsim,0,rword);
                midNodes.put(rword,btr);
     			thisnode.left = btr;
    		 }
    		 }//while

          return rootnode;
    	}//func

    
    /**
     * 读取树(没用到)
     * */
    private void readTree(BiTree bitree,int level,int depth){
    	
    		bitree.readTree(bitree.left, level, depth);
    		word = bitree.word;
    		for(int i = 0; i < level; i++)
    			System.out.print("\t");
    		System.out.println(word);
    		level++;
    		if(level < depth)
    	    bitree.readTree(bitree.right, level, depth);
    	
    }
    
    
    /**
     * @description  从树的指定节点开始查询，获取该节点的所有字节点
     * @param  tree
     * */
    public  void reWarkTree(BiTree node,double[] inputVec){
    	double   similary = simdistance(inputVec , node.weightvec);
    	nodeList.put(node.word,similary);
    	
    if(node.left != null)	reWarkTree(node.left, inputVec);
    if(node.right != null)	reWarkTree(node.right,inputVec);
    }
    
    /**
     * 选取与输入向量指定角度差的节点
     * */
    public  void searchSimNode(double[] inputVec,Entry<String,double[]> seedVec ,BiTree root,int depth){
    	int step = 0;

    	BiTree tempnode = root;
    	while (tempnode.right != null || tempnode.left != null) {
    		
    		if( tempnode.left != null && tempnode.right != null ) {
    			
    			double[] lnodeweightvec = tempnode.left.weightvec;
    			double lsimvalue = simdistance(inputVec,lnodeweightvec );
    			double lsubdegree = Math.acos(lsimvalue);
    			
    			double[] rnodeweightvec = tempnode.right.weightvec;
    			double rsimvalue = simdistance(inputVec,rnodeweightvec );
    			double rsubdegree = Math.acos(rsimvalue);
    			
    			if( lsubdegree < rsubdegree ){
    				tempnode = tempnode.left;
    				step ++;
    				if(step  >=  depth ) break; 
    			}else {
    				tempnode = tempnode.right;
    				step ++;
    				if(step  >=  depth ) break; 
    			}
    			
    		}
    		else 
    		 {
    			if(tempnode.right != null)
    				tempnode = tempnode.right;
	    			step ++;
					if(step  >=  depth ) break; 
    			else if(tempnode.left != null)
    				tempnode = tempnode.left;
					step ++;
					if(step  >=  depth ) break; 
    		 }
    	}
    	
    	
    	nodeList.clear();
    	reWarkTree(tempnode,inputVec);
    	
    }
    
    
    /**
     * @description 输入一个向量，遍历树查返回指定阈值范围内的查询子集
     *                                   度量标准：1.计算输入向量与标准向量的夹角与左右子节点的夹角差值的绝对值 ，选择最小的那个进入
     *                                                         2.如果与左右子节点的差值绝对值的差小于指定的一个范围，则返回父节点或其所有后代节点
     *  @param inputVec   查询向量的权重
     *  @param seedVec    构建查询树的种子节点
     *  @param root            查询树的根节点
     *  @param alpha         查询夹角阈值
     *  @param depth        允许的最大查询深度                                                    
     * */
    public HashMap<String,Double> getSimNodeSet(double[] inputVec,Entry<String,double[]> seedVec ,BiTree root,double  alpha ,int depth){
    	//保存结果的列表
    	HashMap<String,Double>  resultMap = new HashMap<String,Double> (); 
    	//计算 输入向量 与种子节点的夹角
    	double degree =  Math.acos(simdistance(inputVec, seedVec.getValue()));

    	LinkedList<BiTree> tempnodelist = new LinkedList<BiTree> ();     //缓存待查子树的根节点
    	tempnodelist.push(root);
    	
    	int level = 0;        //记录遍历深度  
    	
    	int oldsize = tempnodelist.size();       //記錄父節點級別的個數
    	int newsize = 0;                                         //記錄字節點的個數
    	
    	label:
    	while (tempnodelist.size() != 0){               //当待查寻的子树为0时退出
    		
    			BiTree node = tempnodelist.getFirst();
    	    	if((node.left == null) && (node.right == null)) {        //如果左右节点都为空 ，则删除当前子树 ，继续for循环  
    	    		tempnodelist.poll();
    	    		oldsize--;
    	    		continue;
    	    	}else if(node.left == null){                //如果左子节点为空 ，则删除当前子树 ，缓存右子树 ，继续for循环  
    	    		tempnodelist.poll();
    	    		tempnodelist.offer(node.right);
    	    		oldsize--;
    	    		newsize ++;
    	    		continue;
    	    	}else if(node.right == null) {            //如果右子节点为空 ，则删除当前子树 ，缓存左子树 ，继续for循环    
    	    		tempnodelist.poll();
    	    		tempnodelist.offer(node.left);
    	    		oldsize--;
    	    		newsize ++;
    	    		continue;
    	    	}
    	    	
    	    	//此时左右子节点都不为空，获取左右字节点的夹角
    	       	BiTree leftnode = node.left;
    	    	BiTree rightnode  =node.right;
    	    	
    	    	double ldegree =  leftnode.degree;
    	    	double rdegree = rightnode.degree;
    	    	
    	    	//计算与左右字节点夹角的绝对距离
    	    	double labs = Math.abs(degree - ldegree);
    	    	double rabs = Math.abs(degree - rdegree);
    	    	
	    	if (Math.abs(labs - rabs) < alpha) {   //如果左右节点角距离差小于阈值 ，则删除当前子树 ，缓存左右子树 ，继续for循环    
	    		tempnodelist.offer(leftnode);
	    		tempnodelist.offer(rightnode);
	    		tempnodelist.poll();
	    		oldsize--;
	    		newsize +=2;
	    	}else{
	    		if(labs < rabs && leftnode != null) {   //如果左角距离小于右角距离，则删除当前子树 ，缓存左子树 ，继续for循环
	    			tempnodelist.offer(leftnode);
	    			tempnodelist.poll();
    	    		oldsize--;
    	    		newsize ++;
	    		}else if(right !=null){                 //否则，则删除当前子树 ，缓存右子树 ，继续for循环
		    		tempnodelist.offer(rightnode);
		    		tempnodelist.poll();
    	    		oldsize--;
    	    		newsize ++;
	    		}
	    		
	    	}
	    	
	    	
	    	if(oldsize <= 0 && newsize >0){
	    		oldsize = newsize;
	    		newsize = 0;
	    		level ++;	    			
	    	}
	    	
	    	if(level >= depth ) break label;
	    	
    	} //while
    	
    	for (BiTree node : tempnodelist) {
    		reWarkTree(node ,inputVec);
    		resultMap.putAll(nodeList);
    		nodeList.clear();
    	}
    	
    	return resultMap;
    }
    
    
    
    
    public static Entry<String, double[]> getMeanpair() {
		return meanpair;
	}

	public static void setMeanpair(Entry<String, double[]> meanpair) {
		BiTree.meanpair = meanpair;
	}

	public static  HashMap<String, double[]> getWordDic() {
		return wordDic;
	}

	public  static  void setWordDic(HashMap<String, double[]> wordDic) {
		BiTree.wordDic = wordDic;
	}
	
	

	public static HashMap<String, Double> getNodeList() {
		return nodeList;
	}

	public static void setNodeList(HashMap<String, Double> nodeList) {
		BiTree.nodeList = nodeList;
	}


    
    
}
