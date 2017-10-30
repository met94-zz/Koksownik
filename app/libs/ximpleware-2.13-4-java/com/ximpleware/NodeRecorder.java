/* 
 * Copyright (C) 2002-2017 XimpleWare, info@ximpleware.com
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
/*VTD-XML is protected by US patent 7133857, 7260652, an 7761459*/
/*All licenses to any parties in litigation with XimpleWare have been expressly terminated. No new license, and no renewal of any revoked license, 
 * is granted to those parties as a result of re-downloading software from this or any other website*/
package com.ximpleware;

/**
 * Node record allows one to record the node position of VTDNav. You 
 * can store/load multiple node position using NodeRecorder, which is 
 * also more spatially efficient than . However, the
 * internal representation of node is variable in length and recording a large
 * number of nodes could consume much memory. It is a <em>bad</em> idea to
 * record every node of an document. So be careful when using it.
 */
public class NodeRecorder {
    protected VTDNav vn;

    protected FastIntBuffer fib;

    public final static int BUF_SZ_EXPO = 7;// buffer page size is now 128

    int size; // in # of nodes

    int position; // in # of nodes

    int count; // in # of ints

    /**
     * The parameter-less constructor
     *  
     */
    public NodeRecorder() {
        vn = null;
        size = position = count=0;
        fib = new FastIntBuffer(BUF_SZ_EXPO);
    }

    public int nodeCount(){
    	return position;
    }
    /**
     * 
     * @param vn1
     */
    public NodeRecorder(VTDNav vn1) {
        bind(vn1);
        size = position = count = 0;
        fib = new FastIntBuffer(BUF_SZ_EXPO);
    }

    public void bind(VTDNav vn1) {
        if (vn1 == null)
            throw new IllegalArgumentException(
                    "NodeRecorder can't take a null VTDNav instatnce");
        vn = vn1;
    }

    /**
     * This function records the position of VN into the internal buffer
     *  
     */
    public void record() {
        //add the context and
        int i;
        switch (vn.context[0]) {
        case -1:
        	if (vn.atTerminal==false){
        		fib.append(0xff | 0x0000000);
        		//fib.append(0);
        		size++;
        		position++;
        		count++;
        	}else{
        		fib.append(0xff | 0x80000000);
        		fib.append(vn.LN);
        		size++;
        		position++;
        		count+=2;
        	}
            break;
        case 0:
            if (vn.atTerminal == false) {
                fib.append(0);
                count++;
            } else {
                fib.append(0x80000000);
                fib.append(vn.LN);
                count += 2;
            }
            size++;
            position++;
                
            break;
        case 1:
            if (vn.atTerminal == false) {
                fib.append(1);
                //fib.append(vn.context[1]);
                fib.append(vn.l1index);
                size++;
                position++;
                count += 2;
            } else {
                fib.append(0x80000001);
                //fib.append(vn.context[1]);
                fib.append(vn.l1index);
                fib.append(vn.LN);
                size++;
                position++;
                count += 3;
            }
            break;
        case 2:
            if (vn.atTerminal == false) {
                fib.append(2);
                count += 3;
            } else {
                fib.append(0x80000002);
                count += 4;
            }
            fib.append(vn.l1index);
            fib.append(vn.l2index);
            size++;
            position++;

            if (vn.atTerminal == true)
                fib.append(vn.LN);

            break;
        case 3:
            if (vn.atTerminal == false) {
                fib.append(3);
                count += 4;
            } else {
                fib.append(0x80000003);
                count += 5;
            }
            fib.append(vn.l1index);
            fib.append(vn.l2index);
            if (vn.shallowDepth)
            	fib.append(vn.l3index);
            else{
            	VTDNav_L5 vnl= (VTDNav_L5)(vn);
            	fib.append(vnl.l3index);
            }
            	
            size++;
            position++;

            if (vn.atTerminal == true)
                fib.append(vn.LN);

            break;
        default:
			if (vn.shallowDepth) {
				if (vn.atTerminal == false) {
					i = vn.context[0];
					fib.append(i);
					count += i + 1;
				} else {
					i = vn.context[0];
					fib.append(i | 0x80000000);
					count += i + 2;
				}
				for (int k = 4; k <= i; k++) {
					fib.append(vn.context[k]);
				}
				fib.append(vn.l1index);
				fib.append(vn.l2index);
				fib.append(vn.l3index);
				size++;
				position++;

				if (vn.atTerminal)
					fib.append(vn.LN);
			}else{
				VTDNav_L5 vnl = (VTDNav_L5)vn;
				switch (vn.context[0]) {
			       case 4:
			    	   
			        	if (vn.atTerminal == false) {
			                fib.append(4);
			                count += 5;
			            } else {
			                fib.append(0x80000004);
			                count += 6;
			            }
			            fib.append(vn.l1index);
			            fib.append(vn.l2index);
			            fib.append(vn.l3index);
			            fib.append(vnl.l4index);
			            size++;
			            position++;

			            if (vn.atTerminal == true)
			                fib.append(vn.LN);

			            break;
			        case 5:
			            if (vn.atTerminal == false) {
			                fib.append(5);
			                count += 6;
			            } else {
			                fib.append(0x80000005);
			                count += 7;
			            }
			            fib.append(vn.l1index);
			            fib.append(vn.l2index);
			            fib.append(vn.l3index);
			            fib.append(vnl.l4index);
			            fib.append(vnl.l5index);
			            size++;
			            position++;

			            if (vn.atTerminal == true)
			                fib.append(vn.LN);

			            break;   
			        default:
			            if (vn.atTerminal == false) {
			                i = vn.context[0];
			                fib.append(i);
			                count += i + 1;
			            } else {
			                i = vn.context[0];
			                fib.append(i | 0x80000000);
			                count += i + 2;
			            }
			            for (int k = 6; k <= i; k++) {
			                fib.append(vn.context[k]);
			            }
			            fib.append(vn.l1index);
			            fib.append(vn.l2index);
			            fib.append(vn.l3index);
			            fib.append(vnl.l4index);
			            fib.append(vnl.l5index);
			            size++;
			            position++;

			            if (vn.atTerminal)
			                fib.append(vn.LN);
			        }
				
				}
				
        }
    }

    /**
     * resetPointer() will set the pointer to the first node in NodeRecorder
     * This method is called when one wants to read the nodes in the nodeRecorder
     *  
     */
    public void resetPointer() {
        position = 0;
        count=0;
    }

    public void showContent(){
    	for(int i=0;i<fib.size;i++){
    		System.out.println(" "+fib.intAt(i));
    	}
    	System.out.println("count ==> "+count);
    }
    /**
     * Clear will erase all the nodes, internal buffers are reused
     *  
     */
    public void clear() {
        size = position = count = 0;
        fib.clear();
    }

    /**
     * This method set the cursor in VTDNav to the nodes as recorded
     * in NodeRecorder, and return the output of "getCurrentIndex()"
     * It is important to notice that you can only go forward, not 
     * backward
     * @return int
     *  
     */
	public int iterate() {
		int j, i;
		if (count < fib.size) {
			i = fib.intAt(count);
			boolean b = (i >= 0);
			if (b == false) {
				i = i & 0x7fffffff;
			}
			switch (i) {
			case 0xff:
				vn.context[0] = -1;
				if (!b){
					vn.atTerminal = true;
					vn.LN = fib.intAt(count+1);
					count+=2;			
				}else{
					vn.atTerminal = false;
					count++;
				}
				
				break;

			case 0:
				vn.context[0] = 0;
				if (b == false) {
					vn.atTerminal = true;
					vn.LN = fib.intAt(count + 1);
					count += 2;
				} else {
					vn.atTerminal = false;
					count++;
				}

				break;

			case 1:
				vn.context[0] = 1;
				//vn.context[1] = fib.intAt(count + 1);
				vn.l1index = fib.intAt(count + 1);
				vn.context[1] = vn.l1Buffer.upper32At(vn.l1index);
				if (b == false) {
					vn.atTerminal = true;
					vn.LN = fib.intAt(count + 2);
					count += 3;
				} else {
					vn.atTerminal = false;
					count += 2;
				}

				break;

			case 2:
				vn.context[0] = 2;
				
				vn.l1index = fib.intAt(count + 1);
				//vn.l2lower = fib.intAt(count + 4);
				//vn.l2upper = fib.intAt(count + 5);
				vn.l2index = fib.intAt(count + 2);
				vn.context[1] = vn.l1Buffer.upper32At(vn.l1index);
				vn.l2lower = vn.l1Buffer.lower32At(vn.l1index);
				vn.l2upper = vn.l2Buffer.size - 1;
				label: for (int k = vn.l1index + 1; k < vn.l1Buffer.size; k++) {
					i = vn.l1Buffer.lower32At(k);
					if (i != 0xffffffff) {
						vn.l2upper = i - 1;
						break label;
					}
				}
				vn.context[2] = vn.l2Buffer.upper32At(vn.l2index);
				if (b == false) {
					vn.atTerminal = true;
					vn.LN = fib.intAt(count + 3);
					count += 4;
				} else {
					vn.atTerminal = false;
					count += 3;
				}

				break;

			case 3:
				vn.context[0] = 3;
				
				vn.l1index = fib.intAt(count + 1);
				//vn.l2lower = fib.intAt(count + 5);
				//vn.l2upper = fib.intAt(count + 6);
				vn.l2index = fib.intAt(count + 2);
				//vn.l3lower = fib.intAt(count + 8);
				//vn.l3upper = fib.intAt(count + 9);
				vn.l3index = fib.intAt(count + 3);
				vn.context[1] = vn.l1Buffer.upper32At(vn.l1index);
				vn.l2lower = vn.l1Buffer.lower32At(vn.l1index);
				vn.l2upper = vn.l2Buffer.size - 1;
				label2: for (int k = vn.l1index + 1; k < vn.l1Buffer.size; k++) {
					i = vn.l1Buffer.lower32At(k);
					if (i != 0xffffffff) {
						vn.l2upper = i - 1;
						break label2;
					}
				}
				vn.context[2] = vn.l2Buffer.upper32At(vn.l2index);
				vn.l3lower = vn.l2Buffer.lower32At(vn.l2index);
				if (vn.shallowDepth){
					vn.l3upper = vn.l3Buffer.size - 1;
					label3: for (int k = vn.l2index + 1; k < vn.l2Buffer.size; k++) {
						i = vn.l2Buffer.lower32At(k);
						if (i != 0xffffffff) {
							vn.l3upper = i - 1;
							break label3;
						}
					}
					vn.context[3] = vn.l3Buffer.intAt(vn.l3index);
				}else{
					VTDNav_L5 vnl = (VTDNav_L5)vn;
					vnl.l3upper = vnl.l3Buffer.size - 1;
					label31: for (int k = vnl.l2index + 1; k < vnl.l2Buffer.size; k++) {
						i = vnl.l2Buffer.lower32At(k);
						if (i != 0xffffffff) {
							vnl.l3upper = i - 1;
							break label31;
						}
					}
					vnl.context[3] = vnl.l3Buffer.upper32At(vn.l3index);
				}
				if (b == false) {
					vn.atTerminal = true;
					vn.LN = fib.intAt(count + 4);
					count += 5;
				} else {
					vn.atTerminal = false;
					count += 4;
				}

				break;

			default:
				if (vn.shallowDepth) {
					vn.context[0] = i;
					for (j = 0; j < i-3; j++) {
						vn.context[j+4] = fib.intAt(count + j+1);
					}
					vn.l1index = fib.intAt(count + i-2);
					vn.context[1]=vn.l1Buffer.upper32At(vn.l1index);
					vn.l2lower = vn.l1Buffer.lower32At(vn.l1index);
					vn.l2upper = vn.l2Buffer.size - 1;
					label22: for (int k = vn.l1index + 1; k < vn.l1Buffer.size; k++) {
						int i1 = vn.l1Buffer.lower32At(k);
						if (i1 != 0xffffffff) {
							vn.l2upper = i1 - 1;
							break label22;
						}
					}
					//vn.l2lower = fib.intAt(count + i + 1);
					//vn.l2upper = fib.intAt(count + i + 2);
					vn.l2index = fib.intAt(count + i-1);
					vn.context[2] = vn.l2Buffer.upper32At(vn.l2index);
					vn.l3lower = vn.l2Buffer.lower32At(vn.l2index);
					vn.l3upper = vn.l3Buffer.size - 1;
					label33: for (int k = vn.l2index + 1; k < vn.l2Buffer.size; k++) {
						int i1 = vn.l2Buffer.lower32At(k);
						if (i1 != 0xffffffff) {
							vn.l3upper = i1 - 1;
							break label33;
						}
					}
					vn.l3index = fib.intAt(count + i );
					vn.context[3] = vn.l3Buffer.intAt(vn.l3index);
					if (b == false) {
						vn.atTerminal = true;
						vn.LN = fib.intAt(count +i+1);
						count += i + 2;
					} else {
						vn.atTerminal = false;
						count += i +1;
					}
					break;
				} else {
					VTDNav_L5 vnl = (VTDNav_L5) vn;
					switch (i) {
					case 4:
						vnl.context[0] = 4;
						vnl.l1index = fib.intAt(count + 1);
						vnl.l2index = fib.intAt(count + 2);
						vnl.l3index = fib.intAt(count + 3);
						vnl.l4index = fib.intAt(count + 4);
						vnl.l2lower = vnl.l1Buffer.lower32At(vnl.l1index);
						vnl.l2upper = vnl.l2Buffer.size - 1;
						label222: for (int k = vnl.l1index + 1; k < vnl.l1Buffer.size; k++) {
							i = vnl.l1Buffer.lower32At(k);
							if (i != 0xffffffff) {
								vnl.l2upper = i - 1;
								break label222;
							}
						}
						vnl.context[2] = vnl.l2Buffer.upper32At(vnl.l2index);
						vnl.l3lower = vnl.l2Buffer.lower32At(vnl.l2index);
						vnl.l3upper = vnl.l3Buffer.size - 1;
						label333: for (int k = vnl.l2index + 1; k < vnl.l2Buffer.size; k++) {
							i = vnl.l2Buffer.lower32At(k);
							if (i != 0xffffffff) {
								vnl.l3upper = i - 1;
								break label333;
							}
						}
						vnl.context[3] = vnl.l3Buffer.upper32At(vnl.l3index);
						vnl.l4lower = vnl.l3Buffer.lower32At(vnl.l3index);
						vnl.l4upper = vnl.l4Buffer.size - 1;
						label444: for (int k = vnl.l3index + 1; k < vnl.l3Buffer.size; k++) {
							i = vnl.l3Buffer.lower32At(k);
							if (i != 0xffffffff) {
								vnl.l4upper = i - 1;
								break label444;
							}
						}
						//vnl.l4lower = fib.intAt(count + 12);
						//vnl.l4upper = fib.intAt(count + 13);
						vnl.context[4] = vnl.l4Buffer.upper32At(vnl.l4index);
						if (b == false) {
							vn.atTerminal = true;
							vn.LN = fib.intAt(count + 5);
							count += 6;
						} else {
							vn.atTerminal = false;
							count += 5;
						}

						break;

					case 5:
						vnl.context[0] = 5;
						vnl.l1index = fib.intAt(count + 1);
						vnl.l2index = fib.intAt(count + 2);
						vnl.l3index = fib.intAt(count + 3);
						vnl.l4index = fib.intAt(count + 4);
						vnl.l5index = fib.intAt(count + 5);
						vnl.context[1] = vnl.l1Buffer.upper32At(vnl.l1index);
						
						vnl.l2lower = vnl.l1Buffer.lower32At(vnl.l1index);
						vnl.l2upper = vnl.l2Buffer.size - 1;
						label2222: for (int k = vnl.l1index + 1; k < vnl.l1Buffer.size; k++) {
							i = vnl.l1Buffer.lower32At(k);
							if (i != 0xffffffff) {
								vnl.l2upper = i - 1;
								break label2222;
							}
						}
						
						vnl.context[2] = vnl.l2Buffer.upper32At(vnl.l2index);
						vnl.l3lower = vnl.l2Buffer.lower32At(vnl.l2index);
						vnl.l3upper = vnl.l3Buffer.size - 1;
						label3333: for (int k = vnl.l2index + 1; k < vnl.l2Buffer.size; k++) {
							i = vnl.l2Buffer.lower32At(k);
							if (i != 0xffffffff) {
								vnl.l3upper = i - 1;
								break label3333;
							}
						}
						
						vnl.context[3] = vnl.l3Buffer.upper32At(vnl.l3index);
						vnl.l4lower = vnl.l3Buffer.lower32At(vnl.l3index);
						vnl.l4upper = vnl.l4Buffer.size - 1;
						label4444: for (int k = vnl.l3index + 1; k < vnl.l3Buffer.size; k++) {
							i = vnl.l3Buffer.lower32At(k);
							if (i != 0xffffffff) {
								vnl.l4upper = i - 1;
								break label4444;
							}
						}
						
						
						vnl.context[4] = vnl.l4Buffer.upper32At(vnl.l4index);
						
						vnl.l5lower = vnl.l4Buffer.lower32At(vnl.l4index);
						vnl.l5upper = vnl.l5Buffer.size - 1;
						label5: for (int k = vnl.l4index + 1; k < vnl.l4Buffer.size; k++) {
							i = vnl.l4Buffer.lower32At(k);
							if (i != 0xffffffff) {
								vnl.l5upper = i - 1;
								break label5;
							}
						}
						
						
						vnl.context[5] = vnl.l5Buffer.intAt(vnl.l5index);
						if (b == false) {
							vn.atTerminal = true;
							vn.LN = fib.intAt(count + 6);
							count += 7;
						} else {
							vn.atTerminal = false;
							count += 6;
						}

						break;

					default:
						vn.context[0] = i;
						for (j = 0; j < i-5; j++) {
							vn.context[j+6] = fib.intAt(count+1 + j);
						}
						vn.l1index = fib.intAt(count + i-4);
						vnl.l2lower = vnl.l1Buffer.lower32At(vnl.l1index);
						vnl.l2upper = vnl.l2Buffer.size - 1;
						label22222: for (int k = vnl.l1index + 1; k < vnl.l1Buffer.size; k++) {
							int i1 = vnl.l1Buffer.lower32At(k);
							if (i1 != 0xffffffff) {
								vnl.l2upper = i1 - 1;
								break label22222;
							}
						}
						vn.l2index = fib.intAt(count + i -3);
						vnl.context[2] = vnl.l2Buffer.upper32At(vnl.l2index);
						vnl.l3lower = vnl.l2Buffer.lower32At(vnl.l2index);
						vnl.l3upper = vnl.l3Buffer.size - 1;
						label33333: for (int k = vnl.l2index + 1; k < vnl.l2Buffer.size; k++) {
							int i1 = vnl.l2Buffer.lower32At(k);
							if (i1 != 0xffffffff) {
								vnl.l3upper = i1 - 1;
								break label33333;
							}
						}
						vn.l3index = fib.intAt(count + i -2);
						vnl.context[3] = vnl.l3Buffer.upper32At(vnl.l3index);
						vnl.l4lower = vnl.l3Buffer.lower32At(vnl.l3index);
						vnl.l4upper = vnl.l4Buffer.size - 1;
						label44444: for (int k = vnl.l3index + 1; k < vnl.l3Buffer.size; k++) {
							int i1 = vnl.l3Buffer.lower32At(k);
							if (i1 != 0xffffffff) {
								vnl.l4upper = i1 - 1;
								break label44444;
							}
						}
						vnl.l4index = fib.intAt(count + i -1);
						vnl.context[4] = vnl.l4Buffer.upper32At(vnl.l4index);
						vnl.l5lower = vnl.l4Buffer.lower32At(vnl.l4index);
						vnl.l5upper = vnl.l5Buffer.size - 1;
						label5: for (int k = vnl.l4index + 1; k < vnl.l4Buffer.size; k++) {
							int i1 = vnl.l4Buffer.lower32At(k);
							if (i1 != 0xffffffff) {
								vnl.l5upper = i1 - 1;
								break label5;
							}
						}
						
						vnl.l5index = fib.intAt(count + i );
						vnl.context[5] = vnl.l5Buffer.intAt(vnl.l5index);
						if (b == false) {
							vn.atTerminal = true;
							vn.LN = fib.intAt(count + i+1 );
							count += i +2;
						} else {
							vn.atTerminal = false;
							count += i+1;
						}
						break;
					}
				}
			}
			position++;
			return vn.getCurrentIndex();
		}
		return -1;
	}

}
