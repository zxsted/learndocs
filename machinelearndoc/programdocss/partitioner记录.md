[TOC]
######KeyFieldBasedPartitioner
```java
package org.apache.hadoop.mapreduce.lib.partition;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.MRJobConfig;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.lib.partition.KeyFieldHelper.KeyDescription;

@InterfaceAudience.Public
@InterfaceStability.Stable
public class KeyFieldBasePartitioner<k2,v2> extends Partitioner<K2,V2>
				implements Configurable {

	private static final Log LOG = LogFactory.getLog(
    				KeyFieldBasePartitioner.class.getName());
    public static String PARTITIONER_OPTIONS = 
         "mapreduce.partition.keypartitioner.options";
    private int numOfPartitionFields;
    private KeyFieldHelper keyFieldHelper = new KeyFieldHelper();
    
    private Configuration conf;
    
    public void setConf(Configuration conf) {
    	this.conf = conf;
        keyFieldHelper = new KeyFieldHelper();
        String keyFieldSeparator = 
        	conf.get(MRJobConfig.MAP_OUTPUT_KEY_FIELD_SEPERATOR,"\t");
        keyFieldHelper.setKeyFieldSeparator(keyFieldSeparator);
        if (conf.get("num.key.fields.for.partition") != null) {
        	LOG.warn("Using deprecated num.key.fields.for.partition. " +
             "Use mapreduce.partition.keypartitioner.options instance")
            this.numOfPartionFields = conf.getInt("num.key.fields.for.partition",0);
            keyFieldHelper.setKeyFieldSpec(1,numOfPartionFields);
        }else {
        	String option = conf.get(PARTITIONER_OPTIONS);
            keyFieldHelper.parseOption(option);
        }
    }
    
    public Configuration getConf(){
    	return conf;
    }
    
    //重载partition获取方法
    public int getPartition(K2 key,V2 value,int numReduceTasks) {
    	byte[] keyBytes;
        
        List<KeyDescription> allKeySpecs = keyFieldHelper.keySpecs();
        if (allKeySpecs.size() == 0) {
        	return getPartition(key.toString().hashCode(),numReduceTasks);
        }
        
        try{
        	keyBytes = key.toString().getBytes("UTF-8");
        }catch(UnsupportedEncodingException e) {
        	throw new RuntimeException("The current system  does not" + 
                     "support UTF-8 encoding!",e);
        }
        //return 0 if the key is empty
        if(keyBytes.length == 0) {
        	return 0;
        }
        
        int[] lengthIndicesFisrt = keyFieldHelper.getWordLengths(keyBytes,0,keyBytes.length);
        int currentHash = 0;
        for(KeyDescription leySpec : allKeySpecs) {
        	int startChar = keyFieldHelper.getStartOffset(keyBytes,0,keyBytes.length,
            		lengthIndicesFirst,keySpec);
            //no key found ! continue
            if(startChar < 0) {
            	continue;
            }
            int endChar = keyFieldHeper.getEndOffset(keyBytes,0,keyBytes.length,
            	lengthIndicesFirst,keySpec);
            currentHash = hashCode(keyBytes,startChar,endChar,currentHash);
        }
        return getPartition(currentHash,numReduceTasks);
    }
    
    
    //通用方法
    protected int hashCode(byte[] b, int start,int end ,int currentHash) {
    	for (int i = start ; i <= end; i++) {
        	currentHash = 31 * currentHash + b[i];
        }
        return currentHash;
    }
    
    //获取partion
    protected int getPartition(int hash,int numReduceTasks) {
    	return (hash & Integer.MAX_VALUE) % numReduceTasks;
    }
}

```
HashPartitoner
```java
package org.apache.hadoop.mapreduce.lib.partition;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.mapreduce.Partitioner;

@InterfaceAudience.Public
@InterfaceStability.Stable
public class HashPartitioner<K,V> extends Partitioner<K,V> {
	//使用object.hashCode（） 进行partition
    public int getPartition(K key,V value,int numReduceTasks) {
         return (key.hashCode() & Integer.MAX_VALUE) % numReduceTasks;
    }
}
```
































