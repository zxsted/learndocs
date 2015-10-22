package iReader.anti_spam.word2vec.domain;

public class Pair <T>{
   private T left ;
   private T right;
   
   public Pair(T left,T right){
	   this.left = left;
	   this.right = right;
   }
   
   
   public T getLeft() {
	return left;
}


public void setLeft(T left) {
	this.left = left;
}


public T getRight() {
	return right;
}


public void setRight(T right) {
	this.right = right;
}


@Override
   public boolean equals(Object other){
	  Pair<T> otherPair = (Pair<T>) other; 
	  return this.right == ((Pair<T>) other).right && this.left == ((Pair<T>) other).left;
   }
   
   
   @Override
   public int hashCode(){
	   int result = 17;
	   result = result * 31 + this.left.hashCode();
	   result = result * 31 + this.right.hashCode();
	   return result;
   }
}
