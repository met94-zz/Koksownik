package com.ximpleware;

public class intHash2 {
	private int mask1 = 0x7ff;
    //2048
	private int mask2 = 0xfffff800;
	private int pageSizeE = 3; // 32 * 4 bytes
	protected Object[] storage;
	private int hashWidth = 1<<11;
	private int maxDepth;
	protected int e;
	
	 public intHash2(int hashWidthExpo){
	        e=hashWidthExpo;
	        hashWidth = 1<<hashWidthExpo;
	        mask1 = (hashWidth) -1;
	        mask2 = (~mask1) & 0xffffffff;    
	        storage = new Object[hashWidth];
	    }
	 
	    public boolean isUnique(int i){
	        int temp = i & mask1;
	        if (temp>maxDepth){
	            maxDepth = temp;
	        }
	        if (storage[temp]==null) {
	            int[] ia= new int[1<<pageSizeE];
	            ia[0]=1;
	            ia[1]=i;
	            storage[temp]=ia;
	            return true;
	        }        
	        else{
	        	int[] ia = (int [])storage[temp];
	            int size = ia[0];
	            for (int j=1;j<=size;j++){
	                if (i == ia[j]){
	                    return false;
	                }
	            }
	            if (size < ia.length-1){
	               ia[0]++;
	               ia[size+1]=i;
	            }else{
	            	int[] ia_new= new int[ia.length<<1];
	            	System.arraycopy(ia, 0, ia_new, 0, ia.length);
	            	ia_new[0]++;
	            	ia_new[ia_new[0]]=i;
	            	storage[temp] = ia_new;
	            }
	            return true;            
	        }
	    }
	    final public int totalSize(){
	    	int total = 0;
	    	for (int i=0;i<storage.length;i++){
	    		if (storage[i]!=null){
	    			int[] ia= (int [])storage[i];
	    			total+= ia[0];
	    		}
	    	}
	    	return total;
	    }
	 final public void reset(){
	        for (int i=0;i<=maxDepth;i++){
	            if (storage[i]!=null){
	                ((int [])storage[i])[0]=0;
	            }
	        }
	    }
	   public static int determineHashWidth(int i){  
	    	
	    	// can we do better than this?
	        if (i<(1<<8))
	            return 3;
	    	if (i<(1<<9))
	    		return 4;
	    	if (i<(1<<10))
	    		return 5;
	    	if (i<(1<<11))
	    		return 6;
	    	if (i<(1<<12))
	    		return 7;
	    	if (i<(1<<13))
	    		return 8;
	    	if (i<(1<<14))
	    		return 9;
	    	if (i<(1<<15))
	    		return 10;
	    	if (i<(1<<16))
	    		return 11;
	    	if (i<(1<<17))
	    	    return 12;
	       	if (i<(1<<18))
	    	    return 13;
	       	if (i<(1<<19))
	    	    return 14;
	       	if (i<(1<<20))
	    	    return 15;
	       	if (i<(1<<21))
	    	    return 16;
	       	if (i<(1<<22))
	    	    return 17;
	       	if (i<(1<<23))
	    	    return 18;
	       	if (i<(1<<24))
	    	    return 19;
	       	if (i<(1<<25))
	       	    return 20;
	       	if (i<(1<<26))
	       	    return 21;
	    	if (i<(1<<27))
	       	    return 22;
	    	if (i<(1<<28))
	       	    return 23;
	       	return 24;
	    }
}
