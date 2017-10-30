package com.ximpleware;
import com.ximpleware.*;
public class testIntHash {
	static Runtime rt;
	public static void main(String[] args) throws VTDException {
		
		
		intHash ih2 = new intHash();
		for (int i1=0;i1<10;i1++){
			if (ih2.isUnique(i1)==true)
				System.err.println(" !!!!!!!!!!!!!!! ");
		}
		// TODO Auto-generated method stub
		rt = Runtime.getRuntime();
		long startMem = rt.totalMemory() - rt.freeMemory();
		
		intHash ih = new intHash(21);
		long endMem = rt.totalMemory() - rt.freeMemory();
		
		System.out.println("Memory Use: " + ((float) endMem - startMem)/(1<<20) + " MB.");

		for (int i=0;i<30000000;i++){
		  // System.out.println("i ==>"+i);
		   if ( ih.isUnique(i)!= true) 
			   System.err.println("something wrong"+i);;
		}
		System.out.println(" total size ==>"+ih.totalSize());
		//ih.reset();
		for (int i=0;i<30000000;i++){
			  // System.out.println("i ==>"+i);
			   if ( ih._isUnique(i)!= false) 
				   System.err.println("something wrong"+i);;
			}
		long endMem2 = rt.totalMemory() - rt.freeMemory();
		
		System.out.println("Memory Use: " + ((float) endMem2 - startMem)/(1<<20) + " MB.");
		
		String xmldoc = "<a><?pip?></a>";
		VTDGen vg = new VTDGen();
		vg.setDoc(xmldoc.getBytes());
		vg.parse(false);
		VTDNav vn = vg.getNav();
		int count = vn.getTokenCount();
		for (int i=0;i<count;i++){
			System.out.println("   --------->"+vn.toRawString(i));
		}
		
		
	}
}
