public class DoubleArrayTrie {

	private static final Log LOG = LogFactory.getLog(DoubleArrayTrie.class);
	
	private final static int BUF_SIZE = 16384;
	private final static int UNIT_SIZE = 8;         //size of int + int
	
	private static class Node {
		int code;                     //节点存储的字的编码
		int depth;                   //节点所在树的层级
		int left;                         //节点的字典序的起始序号
		int right;                      //节点的字典序的终止序号
	};
	
	/*
	 * 下面这两个数组拥有一致的下标，每个节点 是DFA的一个状态
	 * 从状态s输入c到状态t的一个转移必须满足以下条件
	 *          base[s]  + c == t
	 *          check[base[s] + c] == s
	 *          
	 *  进行匹配的规则(假设当前状态为s ，对于输入的字符c 有)：
	 *           t = base[s] + c
	 *           if check[t] = s then
	 *           		next state = t
	 *           else
	 *           		fail
	 *           endif
	 */
	private int check[];           													//check 验证字符串是否有同一个状态转移而来
	private int base[];             													//存储当前的状态以供转移使用
	
	private boolean used[];   												//一个同上面数组长度相同的标识数组，用以表示对应位是否被用了
	private int size;																	//目前猜测是 建好后的双数组的长度
	private int allocSize;															//为双数组分配的长度
	private List<String> key;													//存储的字典列表
	private int keySize;
	private int length[];                                                           //存储字典中词条的字符长度
	private int value[];                                                             //存储指定字符在字典中索引的起始值（left），
	private int progress;
	private int nextCheckPos;                                                //下一个check的位置
	
	//boolean no_delete_
	int error_;
	
	/*当数组长度不够时，进行扩容
	 * @param newSize 新分配的长度
	 * @return allocSize 扩容后的长度
	 */
	private int resize(int newSize) {
		int[] base2 = new int[newSize];
		int[] check2 = new int[newSize];
		boolean[] used2 = new  boolean[newSize];
		
		boolean use2[] = new boolean[newSize];
		if(allocSize > 0 ) {
			System.arraycopy(base, 0, base2, 0, allocSize);
			System.arraycopy(check,0,check2,0,allocSize);
			System.arraycopy(used,0,used2,0,allocSize);
		}
		
		base = base2;
		check = check2;
		used = used2;
		
		return allocSize = newSize;
	}
	
	/**
	 *从按照字典序排列的词条字典中  获取一个节点的所有孩子节点
	 * @param parent :Node  当前节点的父节点
	 * @param siblings: 待填充的兄弟节点列表
	 * @return siblings.size() : 兄弟节点的个数
	 * */
	private int fetch(Node parent,List<Node> siblings) {
		
		if (error_ < 0) 
			return 0;
		
		int prev = 0;                        //指向在双数组中的位置的指针
		
		/*遍历当前节点在字典中的起始和终止范围，注意是字典序，先按首字母排序，然后是第二个字母，然后是第三个字母*/
		for(int i = parent.left; i < parent.right; i++) {
			/*如果i索引的词长度小于当前节点parent所在的层次则跳过*/
			if((length != null ? length[i] : key.get(i).length()) < parent.depth) 
				continue;
			String tmp = key.get(i);
			
			int cur = 0;
			/*如果索引到的词条的字符长度大于当前节点的深度，则设当前位置为当前节点深度+1*/
			LOG.info("length 数组长度："+length.length);
			if ((length != null ? length[i] : tmp.length()) != parent.depth)
				cur = (int) tmp.charAt(parent.depth) + 1;         //当前值为 ：char编码+1
			
			
			/*如果计算出的新位置小于前一个位置prev，则为错误 返回*/
			if(prev > cur) {                                           
				error_ = -3;                            
				return 0;
			}
			
		
			
			/*如果cur大于prev 或者兄弟节点列表的长度为0*/
			if (cur != prev || siblings.size() == 0) {
				/*新建当前位置字符对应的节点*/
				Node tmp_node = new Node();
				tmp_node.depth = parent.depth + 1;
				tmp_node.code = cur ;           //注意到这里cur 是 字符的code + 1  
				tmp_node.left = i;                    //设置当前位置为本节点在字典中的起始位置
				/*如果有左兄弟节点的话，则当前节点在字典中的其实位置就是左兄弟节点的终止位置*/
				if (siblings.size() != 0)
					siblings.get(siblings.size() - 1).right = i;
				siblings.add(tmp_node);
				LOG.info("新加入节点的code 为：" +tmp_node.code );
			}
			prev = cur;                    //移动在双数组中的指针为当前位置
		}
		
		/*遍历完，如果得到的兄弟节点的列表长度不为0 ，则为最后一个兄弟添加右界*/
		if (siblings.size() != 0) 
			siblings.get(siblings.size() -1).right = parent.right;
		
		return siblings.size();
	}
	
	/*
	 * 将一组兄弟插入到树中
	 * @param List<Node> siblings : 待查如的一族兄弟节点
	 * */
	private int insert(List<Node> siblings) {
		
		if (error_ < 0)
			return 0;
		
		int begin = 0;
		
		/*如果下一个check的位置 - 1 大于 等于当前第一个兄弟节点的字符码的位置，
		 						则设置pos 为nextCheckPos， 否则设置pos为第一个兄弟节点的字符码的位置*/
		int pos = ((siblings.get(0).code + 1 > nextCheckPos) ? siblings.get(0).code + 1:
			nextCheckPos) -1;
		int nonzero_num = 0;                          //check[pos]位非零计数
		int first = 0;
		
		/*如果双数组的容量小于pos，则将双数组扩容为 pos + 1的长度*/
		if(allocSize <= pos)
			resize(pos + 1)  ;      
		
		/*下面这个while循环是为了给兄弟列表寻找一个完全每有被使用的连续位置*/
		outer: while(true) {
			pos ++;                     //上一轮的pos位置已经被使用了，所以顺延一位
			
			/*如果双数组的容量小于pos，则将双数组扩容为 pos + 1的长度*/
			if(allocSize <= pos)
				resize(pos + 1)  ;   
			
			if(check[pos] != 0) {      //如果check数组中的pos位已经被使用了，则非零计数加一，进入下一轮while
				nonzero_num ++;
				continue;     
			}else if(first == 0) {          //如果该pos对应的check的位置不为0 ，而且first不为1 ，则设置nextCheckPos = pos; 
				nextCheckPos = pos;      
				first = 1;                                 //设置first 为 1, 表示开始添加了
			}
			
			/*回退获取双数组记录该兄弟列表的起始位置*/
			begin = pos - siblings.get(0).code;
			
			/*如果当前的双数组的容量小于所需要的空间则扩容*/
			if (allocSize <= (begin+siblings.get(siblings.size() -1).code)) {
				//progress can be zero
				double l = (1.05 > 1.0 * keySize / (progress + 1)) ? 1.05 :
					1.0*keySize/(progress + 1);
				
				resize((int) (allocSize * l));
			}
			
			/*如果当前的pos已经被用了，则进入while的下一轮循环*/
			if(used[begin])
				continue;
			
			/*遍历每一个兄弟节点，如果他们中的一个位置已经被站了，则跳入下一轮循环*/
			for(int i =1; i < siblings.size(); i++) 
				if(check[begin + siblings.get(i).code] != 0) 
					continue outer ;
			
			break ;            //如果全部位置已经确定可以使用，就跳入下一轮
			
		}
		
		/*
		 * --简单探索法 （heuristics ：启发法，探索法）
		 * 如果在index ‘next_check_pos’ 和‘check’ 之间的非空闲位置的比例大于一个固定的值
		 * (e.g 0.9),则'next_check_pos' 的index 将被 check 复写，即 next_check_pos 向前步进
		 * */
		if(1.0  * nonzero_num /(pos - nextCheckPos + 1) >= 0.95)
			nextCheckPos = pos;
		
		used[begin] = true;
		
		/*确定当前双线性数组的长度: 如果以前的size 大于 添加兄弟列表后的长度则size值不变，否则 为添加后的长度*/
		size = (size > begin + siblings.get(siblings.size() - 1).code + 1 ) ?
				size :  begin + siblings.get(siblings.size() - 1).code + 1;
		
		for ( int i = 0; i < siblings.size(); i++) 
			check[begin + siblings.get(i).code] = begin;        //当前的兄弟节点的check数组内容全部指向列表起始位置
		
		for (int i = 0; i <siblings.size(); i++) {
			List<Node> new_siblings = new ArrayList<Node>();      //存储每个兄弟节点的所有孩子节点的列表
			
			/*判断子节点列表的长度是否为零，同时填充新的兄弟列表，如果为零则创建叶子节点，否则创建分支节点并调用insert()递归建树*/
			if (fetch(siblings.get(i),new_siblings) == 0) { 
				base[begin + siblings.get(i).code] = (value != null) ? 
						 (-value[siblings.get(i).left]-1 ) :(-siblings.get(i).left - 1);    //此时base的当前项对应的是叶子节点，值对应的是词条字典中的起始位置 
						 
			/*如果Value数组不为 空，并且value数组的相反值为 正的，则报错并返回*/			 
			 if (value != null && (-value[siblings.get(i).left] - 1) >= 0) {
						error_ = -2;
						return 0;
					}
			 progress++;           //表示处理完一个节点
			} else {
				int h = insert(new_siblings);                            //如果节点有兄弟节点，则它非叶子节点，递归调用，建树
				base[begin + siblings.get(i).code] = h;           //当前base的项对应的项存储的孩子链表的头位置
			}
		}
		return begin;                                        //返回本次插入孩子链表的头位置
	}
	
	/*无参构造方法*/
public DoubleArrayTrie() {
	 check = null;
	 base = null;
	 used = null;
	 size = 0;
	 allocSize = 0;
	 error_ = 0;
}	


/*对象销毁方法，防止内存泄漏*/
void clear() {
	check = null;
	base = null;
	used = null;
	allocSize = 0;
	size = 0;
}


	

public static int getUnitSize() {
	return UNIT_SIZE;
}

public int getSize() {
	return size;
}

/*返回双数组的占用内存的大小*/
public int getTotalSize() {
	return size * UNIT_SIZE;
}

/*返回双数组中非零值的个数*/
public int getNonzeroSize() {
	int result = 0;
	for(int i = 0 ; i < size; i++) {
		if (check[i] != 0 ) {
			result ++;
		}
	}
	return result;
}

/**
 * 从根节点开始建树的函数,通过调用同名但是从某一分支开始建子树的build函数事项
 * @param key： List<String>  按字典序排列的词条字典
 * */
public int  build(List<String> key) {
	return build(key,null, null,key.size());
}

/**
 * 从指定分支节点开始建子树的build函数
 * @param _key： List<String>  按字典序排列的词条字典
 * @param  _length:int[] 
 * @param  _value : int[] 
 * @param _keySize : int 
 * */
public int build(List<String> _key,int _length[], int _value[],
		   int _keySize) {
	key = _key;											
	length = _length;								 //存储字典中词条的字符长度
	value = _value;  									//存储指定字符在字典中索引的起始值（left）
	progress = 0;
	
	resize(65536 * 32);                            //分配初始容量
	
	base[0] = 1;
	nextCheckPos = 0;   
	
	Node root_node = new Node();
	root_node.left = 0;
	root_node.right = keySize;
	root_node.depth = 0;
	
	List<Node> siblings = new ArrayList<Node>();
	fetch(root_node,siblings);
	LOG.info("row_num is : 327 兄弟列表长度为 ："+ siblings.size() );
	insert(siblings);
	
	used = null;
	key = null;
	
	return error_;
}

/****************************模型序列化部分************************************/

/**
 * 将持久化在文件中的数据加载到模型中
 * @param filename : String  模型持久化文件
 * @throws IOException 
 * */
public void load(String filename) throws IOException {
	File file = new File(filename);
	size = (int) file.length() / UNIT_SIZE;                //计算模型的数组的长度
	check = new int[size];
	base = new int[size];   
	
	DataInputStream is = null;
	try{
		is = new DataInputStream(new BufferedInputStream(
				                  new FileInputStream(file)));
		for (int i = 0; i < size; i++) {
			base[i] = is.readInt();
			check[i]  = is.readInt();
		}
	}finally{
		if (is != null) 
			is.close();
	}
}

/**
 * 将模型中的参数持久化到文件中
 * @param filename : 模型的持久化文件名
 * @throws IOException 
 * */
public void save(String filename) throws IOException {
	DataOutputStream out = null;
	try{
		out = new DataOutputStream(new BufferedOutputStream(
				new FileOutputStream(filename)));
		for(int i = 0; i < size; i++) {
			out.writeInt(base[i]);
			out.writeInt(check[i]);
		}
		out.close();
	}finally{
		if(out != null) 
			out.close();
	}
}

/*****************************************************************************/

/*********************查询部分************************************************/
/**
 * 对用户输入query进行整体查询,通过调用复用函数实现，复用函数可以从指定节点查询query的一部分
 * @param key : 用户输入的查询语句
 * */
public int extractMatchSearch(String key) {
	return exactMatchSearch(key,0,0,0);
}
/**
 * 从指定节点开始 ，对用户的输入进行部分查询
 * @param key  :String 用户的查询字符串
 * @param pos : 要查寻的字串的起始位置
 * @param len： 要查寻的字串的长度
 * @param nodePos：查询的起始节点
 * */
public int exactMatchSearch(String key,int pos,int len,int nodePos){
	if(len <= 0)
		len = key.length();
	if(nodePos <= 0)
		nodePos  = 0;
	
	int result = -1;
	
	char[] keyChars = key.toCharArray();
	
	int b = base[nodePos];
	int p;
	
	for(int i = pos; i < len ; i++) {
		p = b + (int) (keyChars[i]) + 1;                      //获取新的索引位置，b中存储的是起始位置begin -第一个孩子节点的编码值位置
		if (b == check[p])                                             // check中存储的是兄弟链的起始位置begin ， 如果check[index] == b[index],则表面这是一个连续的字串
			b = base[p];                                                     //根据索引沿树的分支路径向下顺延
		else
			return result;                                               //否则可以判定查询的字串在字典里没有
	}
	
	 p = b;                                                                          //现在是查询字串的最后一个字符
	 int n = base[p];                                                     //取出该字符对应的节点的值
	 if(b == check[p] && n < 0) {                               //如果该节点仍然在路径里，且其为负值，表明它的终点节点。
		 result = -n -1;                                                        //取相反值就是词条在字典中的索引位置
	 }
	 return result;                                                        //如果不满足上面的限制条件，则表明查询的字串不再字典中返回-1
}


/**
 * 前缀查询，返回指定前缀的所有值
 * @param key  :String 用户的查询字符串
 * */
public List<Integer> commonPrefixSearch(String key){

	
	return commonPrefixSearch( key,0,0,0);
}

/**
 * 前缀查询，返回指定前缀的所有值
* @param key  :String 用户的查询字符串
 * @param pos : 要查寻的字串的起始位置
 * @param len： 要查寻的字串的长度
 * @param nodePos：查询的起始节点
 * */
public List<Integer> commonPrefixSearch(String key,int pos,
		int len,int nodePos){
	if (len < 0)
		len = key.length();
	if(nodePos <= 0)
		nodePos = 0;
	
	List<Integer> result = new ArrayList<Integer>();
	
	char[] keyChars = key.toCharArray();
	
	int  b= base[nodePos];
	int n ;
	int p;
	
		for(int i = pos; i < len; i++) {
			p = b;
			n = base[p];
			
			if(b == check[p] && n < 0){
				result.add(-n-1);
			}
			
			p = b + (int) (keyChars[i]) + 1;
			if (b == check[p])
				b = base[p];
			else
				return result;
		}
		
		p=b;
		n=base[p];
		
		if(b == check[p] && n<0) {
			result.add(-n-1);
		}
	return result;
}


/****************************************************************************/


	//debug
	public void dump() {
		for(int i = 0 ; i < size; i ++) {
			System.err.println("i: " + i + " ["+ base[i] + ", " +check[i] + " ]" );
		}
	}
	
}
