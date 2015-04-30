package atomicityChecker;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
public class LifeCycle {
	
  Key k;
  Val v;
  int countNode;
  int newNode;
  int countLine;
  int EdgeCount = 0;
  HashMap<flagKey,Integer> flagAtomic = new HashMap<flagKey,Integer>(); // flag set when inside atomic block tid -> {0,1}
									      //0- not inside 1-inside
  HashMap<flagKeyLife,Integer> flagAtomicLife = new HashMap<flagKeyLife,Integer>();
  HashMap<Integer,Integer> taskMap = new HashMap<Integer,Integer>(); // ,maps each thread to currently running task
									    //HashMap<Key,StringRW> varMap2 = new HashMap<Key,StringRW>();
  HashMap<flagKey,Integer> firsttime = new HashMap<flagKey,Integer>();
  HashMap<flagKeyLife,Integer> firsttimeLife = new HashMap<flagKeyLife,Integer>();
  HashMap<Integer, Pair> Tree = new HashMap<Integer, Pair>();
  HashMap<Integer, Integer> prevM =new HashMap<Integer, Integer>();
  Pair pair = new Pair();
  HashMap<Integer,Edge> EdgeStore = new HashMap<Integer,Edge>();
  HashMap<Integer, Integer> AttachQ = new HashMap<Integer, Integer>();
  HashSet<Edge> CausalEdges = new HashSet<Edge>();
  HashMap<Integer, Integer> TidNodeMap =  new HashMap<Integer, Integer>();
  HashMap<Integer,LifeValues> LifeCycle = new HashMap<Integer,LifeValues>();
  HashMap<Integer, LifeValues> TaskM = new HashMap<Integer, LifeValues>();
  HashMap<Integer, Integer> RecNode = new HashMap<Integer, Integer>();
  int lastUnlock = 0;
  HashMap<Integer, Integer> outside = new HashMap<Integer, Integer>();
  Stack<Edge> s;
  FileWriter treeFile;
  
 public void printMap(String args0 , HashMap<Integer, Pair> tree2) throws IOException
  {
	  Set<Integer> keys = tree2.keySet();
	  for(Integer kk:keys)
	  {
		  System.out.println(kk+"==> Source:"+tree2.get(kk).source+"  target:"+tree2.get(kk).target+"  Parent:"+tree2.get(kk).parent+"  Depth:"+tree2.get(kk).depth+"  Tree of:"+tree2.get(kk).tree);
		 // treeFile.write(kk+"==> Source:"+tree2.get(kk).source+"  target:"+tree2.get(kk).target+"  Parent:"+tree2.get(kk).parent+"  Depth:"+tree2.get(kk).depth+"  Tree of:"+tree2.get(kk).tree+"\n");
	  }
  }
  
public void print(State2 state,String line,  int node,int dest)
  {
	  System.out.println(line);
	  System.out.println("source: "+node+" dest: "+dest);
		Set<Integer> keys = state.C.keySet();
		System.out.print(" C:");
		  for(Integer kk:keys)
		  {
			  System.out.print(kk+"=> node:"+state.C.get(kk).node+", line:"+state.C.get(kk).line+"   ");
		  }
		  System.out.println();
		  keys = state.L.keySet();
		  System.out.print(" L:");
		  for(Integer kk:keys)
		  {
			  System.out.print(kk+"=> node:"+state.L.get(kk).node+", line:"+state.L.get(kk).line+"   ");
		  }
		  System.out.println();
		  Set<String> keyS = state.U.keySet();
		  System.out.print(" U:");
		  for(String kk:keyS)
		  {
			  System.out.print(kk+"=> node:"+state.U.get(kk).node+", line:"+state.U.get(kk).line+"   ");
		  }
		  System.out.println();
	  System.out.print(" H:");
	  System.out.println(state.H);
	  Set<ReadKey> keyR = state.R.keySet();
	  System.out.print(" R:");
	  for(ReadKey kk:keyR)
	  {
		  System.out.print(kk+"=> node:"+state.R.get(kk).node+", line:"+state.R.get(kk).line+"   ");
	  }
	  System.out.println();
	  Set<Key> keyW = state.W.keySet();
	  System.out.print(" W:");
	  for(Key kk:keyW)
	  {
		  System.out.print(kk+"=> node:"+state.W.get(kk).node+", line:"+state.W.get(kk).line+"   ");
	  }
	  System.out.println();
	  keys = state.F.keySet();
		System.out.print(" F:");
		  for(Integer kk:keys)
		  {
			  System.out.print(kk+"=> node:"+state.F.get(kk).node+", line:"+state.F.get(kk).line+"   ");
		  }
		  System.out.println();
		  keys = state.P.keySet();
			System.out.print(" P:");
			  for(Integer kk:keys)
			  {
				  System.out.print(kk+"=> node:"+state.P.get(kk).node+", line:"+state.P.get(kk).line+"   ");
			  }
			  System.out.println();
	  System.out.println("-------------------------------------------------------------------------------------------------------------------------------");
	  System.out.println(" ");	
  }

public void FindPath(int count, Edge e  , FileWriter fw) throws IOException
  {
	  Stack<Edge> s = new Stack<Edge>();
	  HashMap<Integer, Integer> Skip = new HashMap<Integer, Integer>();
	  Stack<Integer> index = new Stack<Integer>();
	  int flag = 0;
	  int i = count;
	  s.push(e);
	  index.push(i);
	  Edge top = new Edge();
	  top = s.peek();
	  while(e.dest.node != top.source.node)
	  {
		  flag =0;
		  while(i!=0 && flag!=1)
		  {
			  i--;
			  if(i!= 0 && EdgeStore.get(i).dest.node == top.source.node && !Skip.containsKey(i))
			  {
				  Edge nw = new Edge();
				  nw = EdgeStore.get(i);
				  s.push(nw);
				  index.push(i);
				  top = s.peek();
				  flag=1;
			  }
		  }
		  if(i==0 && flag!=1 && !s.isEmpty() )
		  {
			  
			  s.pop();
			  if(!s.isEmpty()) // added for lifecycle
			  {
				  top= s.peek();
				  int skip = index.pop();
				  i = index.peek();
				  Skip.put(skip, 1);
			  }
		  }
	  }
	  Stack<Edge> dummy = new Stack<Edge>();
	  int ret = reverseStackWithCheckingPriority(s,dummy,fw);
	System.out.println("NEW PATH"+s.toString());
	fw.write("\nNEW PATH"+s.toString());
	fw.write("\n");
	System.out.println("=======================================================================================================================================");
	fw.write("\n=======================================================================================================================================\n");
	fw.write("\n");
	/*PrintWriter out;
	try{
	out = new PrintWriter(new BufferedWriter(new FileWriter(filename, true)));
    out.println("the text");
}catch (IOException e1) {
	e1.printStackTrace();
}*/
  
  
  }
  
public int reverseStackWithCheckingPriority(Stack<Edge> s, Stack<Edge> Backup , FileWriter fw) throws IOException
  {
	  int ret = 4;
	  Stack<Edge> Dummy = new Stack<Edge>();
	  Edge e = new Edge();
	  while(!s.isEmpty())
	  {
		  e = s.pop();
		  Backup.push(e);
		  if(!CausalEdges.contains(e) && ret!=3 && TidNodeMap.get(e.source.node)!= TidNodeMap.get(e.dest.node))
		  {
			  ret =2;
		  }
		  else if(CausalEdges.contains(e) && ( ret==2 || ret ==1))
		  {
			  ret =3;
		  }
		  else if(!CausalEdges.contains(e) && TidNodeMap.get(e.source.node) == TidNodeMap.get(e.dest.node) && ret != 2 && ret != 3)
		  {
			  ret =1;
		  }
	  }
	  while(!Backup.isEmpty())
	  {
		  e =Backup.pop();
		  Dummy.push(e);
		  if(ret == 2 && CausalEdges.contains(e))
		  {
			  ret =3;
		  }
	  }
	  while(!Dummy.isEmpty())
	  {
		  s.push(Dummy.pop());
	  }
	  Backup.clear();
	  System.out.println("PRIORITY:"+ret);
	  fw.write("\nPRIORITY:"+ret);
	  return ret;
  }

public void printEdge(HashMap<Integer, Edge> edgeStore2)
  {
	  Set<Integer> keys = edgeStore2.keySet();
	  int size = keys.size();
	  System.out.println("Printing Edges"+ size);
	  for(Integer kk:keys)
	  {
		  System.out.println(kk+"==> ["+edgeStore2.get(kk).toString()+"]");
	  }
  }

public String excludeLineNum(String line)
  {
	  if(!line.contains("rwId:"))
	  {
		  String a = line.substring(0, line.indexOf(" "));
		  line = line.replaceFirst(a, " ");
	  }
	  return line;
  }

public  void phaseTree(String concat) throws IOException
  {
	  BufferedReader br = new BufferedReader(new FileReader(concat));
	  BufferedWriter brw = new BufferedWriter(new FileWriter("output.txt"));
	  String line;
	  int flag = 0;	
	  countLine =0;
	  try 
	  {
		  line = br.readLine();
		  flag =0;
		  while (line != null)
		  {
			  //System.out.println(line);
			  flag=0;
			  countLine++;
			  if (line.contains("RET") )
			  {
				  String p=line.substring((line.indexOf("msg")),(line.length()));
				  p = p.substring(4);
				  int p1 = Integer.valueOf(p);
				  String t = line.substring(line.indexOf("tid"), line.indexOf("msg")-1);
				  t = t.substring(4);
				  int t1 = Integer.valueOf(t);
				  if (p1 == (prevM.get(t1)))
				  {
					  prevM.put(t1,-1);
				  }
				  Pair pair = Tree.get(p1);
				  if (pair.tree != -1)
				  {
					  int d = pair.depth;
					  if (d>=0 || d<0)
					  {
						  line = line.concat(" endAtomic Depth: "+d+"\n");
						  line = excludeLineNum(line);
						  brw.write(countLine +" " +line);
						  flag=1;
					  }
				  }
			  }
			  if (line.contains("POST"))
			  {
				  Pair pair = new Pair();
				  String p = line.substring((line.indexOf("msg")),(line.indexOf("dest")-1));
				  p = p.substring(4);
				  int p1 = Integer.valueOf(p);
				  String t = line.substring(line.indexOf("src"), line.indexOf("msg")-1);
				  t = t.substring(4);
				  int t1 = Integer.valueOf(t);
				  String delay = line.substring(line.indexOf("delay")+6, line.length());
				  int Delay = Integer.valueOf(delay);
				  pair.source = t1;
				  if (Delay == 0)
				  {
					  if(prevM.containsKey(t1) && prevM.get(t1)!=-1)
						  pair.parent=prevM.get(t1);
					  else
	        			pair.parent=0;
				  }
				  else
				  {
					  pair.parent=0;
				  }
				  Tree.put(p1, pair);
			  }
			  else if(line.contains(" CALL "))
			  {
				  String dep = null;
				  int D=-1;
				  /*if(line.contains("beginAtomic"))
	        		{
	        			
	        			p1=line.substring((line.indexOf("msg")),(line.indexOf("beginAtomic")-1));
	        		}
	        		else*/
				  String p=line.substring((line.indexOf("msg")),(line.length()));
				  p = p.substring(4);
				  int p1 = Integer.valueOf(p);
				  String t = line.substring(line.indexOf("tid"), line.indexOf("msg")-1);
				  t = t.substring(4);
				  int t1 = Integer.valueOf(t);	        			
				  Pair pair=new Pair();
				  Pair pair2 = new Pair();
				  if(/*line.contains("beginAtomic") &&*/ !Tree.containsKey(p1))
				  {
					  pair.tree=p1; //t1
					  dep = line.substring(line.indexOf("Depth")+6, line.length());
					  D =Integer.valueOf(dep);
					  pair.depth=D;
				  }
				  if(Tree.containsKey(p1))
				  {
					  pair=Tree.get(p1);
					  pair.target=t1;
	        			
					  if(pair.source!=t1) // what if the tid is two dig
					  {
						  pair.parent=0;
						  pair.tree=p1;
					  }
					  else if(pair.source==t1)
					  {
						  pair2=Tree.get(pair.parent);
						  if(!line.contains("beginAtomic"))
						  {
							  if(pair.parent!=0)
							  {
								  pair2=Tree.get(pair.parent);
								  pair.depth=pair2.depth-1;
								  pair.tree = pair2.tree;
							  }
							  else
							  {
								  pair.tree=p1;
							  }
						  }
					  }
					  Tree.put(p1, pair); ////CHECK 1
				  }
				  else 
				  {
					  pair.source=-1;
					  pair.target=p1;
					  pair.parent=0;
					  pair.depth=D;
					  Tree.put(p1, pair); //// CHECK 2
				  }
				  Tree.put(p1, pair);  ///// CHECK 3
				  prevM.put(t1, p1);
				  if(!line.contains("beginAtomic") /*&& pair.depth>=0*/)
				  {
					  line = line.concat(" beginAtomic Depth: "+pair.depth+"\n");
					  line = excludeLineNum(line);
					  brw.write(countLine +" " +line);
					  flag=1;
				  }
			  }
			  if(flag==0 &&( !line.contains("METHOD")|| line.contains("meth:lock") || line.contains("meth:unlock") || line.contains("tryLock")))
			  {
				  line = excludeLineNum(line);
				  brw.write(countLine +" " +line+"\n");
				  flag=1;
			  }
			  line=br.readLine();
		  }
          //printMap(concat,Tree);   
          System.out.println();
          System.out.println();
	  }
	  catch (IOException e) 
	  {
		  e.printStackTrace();
	  } finally 
	  {
		  brw.close();
		  br.close();
	  }
  }

public void clearState(int i)
{
	State2 state = new State2();
	state.C.clear();
	state.L.clear();
	state.U.clear();
	state.R.clear();
	state.W.clear();
	state.F.clear();
	state.P.clear();
	state.H.clear();
	flagAtomic.clear();
	flagAtomicLife.clear();
	taskMap.clear();
	firsttime.clear();
	prevM.clear();
	EdgeStore.clear();
	AttachQ.clear();
	CausalEdges.clear();
	TidNodeMap.clear();
	lastUnlock =0;
	TaskM.clear();
	RecNode.clear();
	outside.clear();
	if(i == 0)
		Tree.clear();
	
}

public void markCalls(String concat) throws IOException
  {
	  BufferedReader br = new BufferedReader(new FileReader(concat));
	  BufferedReader br1 = new BufferedReader(new FileReader(concat));
	  String out = concat+"_IntermediateLife";
	  BufferedWriter brw = new BufferedWriter(new FileWriter(out));
	  HashMap<Integer , Integer> InsM = new HashMap<Integer, Integer>();
	  HashMap<Integer, Integer> currentTask = new HashMap<Integer, Integer>();
	  String line;
	  int tid = 0;
	  int taskid = 0;
	  int id = 0;
	  String component = null;
	  String state = null;
	  int instance = 0;
	  int intentId;
	  
	  try
	  {
		  line = br.readLine();
		  while(line != null)
		  {
			  StringTokenizer st = new StringTokenizer(line," "); 
			  while (st.hasMoreTokens())
		      {
				  String token = st.nextToken(); 
				  if (token.contains("tid") && !line.contains("FORK") && !line.contains("JOIN"))
		          {
		        	  tid = Integer.valueOf(token.substring(4));
		          }
		          else if (token.contains("msg:"))
		          {
		        	  taskid = Integer.valueOf(token.substring(4));
		          }
		          else if (token.contains("id:") && !token.contains("tid") && !line.contains("NOTIFY"))
		          {
		        	  id = Integer.valueOf(token.substring(3));
		          }
		          else if(token.contains("component:"))
		          {
		        	  component = token.substring(10);
		          }
		          else if(token.contains("state"))
		          {
		        	  state = token.substring(6);
		          }
		          else if(token.contains("instance:"))
		          {
		        	  instance = Integer.valueOf(token.substring(9));
		          }
		          else if(token.contains("intentId"))
		          {
		        	  intentId = Integer.valueOf(token.substring(9));
		          }
		      }
			  if(line.contains("CALL"))
			  {
				  currentTask.put(tid, taskid);
			  }
			  else if(line.contains("TRIGGER-LIFECYCLE"))
			  {
				  if(InsM.containsKey(id) && InsM.get(id) == tid)
				  {
					  LifeValues lv = new LifeValues();
					  lv.tid = tid;
					  lv.id = id; 
					  lv.component = component;
					  lv.state = state;
					  TaskM.put(currentTask.get(tid), lv);
				  }
			  }
			  else if(line.contains("INSTANCE-INTENT"))
			  {
				  InsM.put(instance, tid);
			  }
			  line = br.readLine();
		  }
		  
  ////////////////////     FOR EACH LIFECYCLE TASK THEIR COMPONENT AND IDS ARE SAVED /////////////////////
		  
		  
		  Set<Integer> keys = Tree.keySet();
		  for(Integer kk:keys)
		  {
			  if( Tree.get(kk).target!= -1 && TaskM.containsKey(Tree.get(kk).parent) && TaskM.get(Tree.get(kk).parent).component != null)
			  {
				  LifeValues lv = new LifeValues();
				  lv = TaskM.get(Tree.get(kk).parent);
				  TaskM.put(kk, lv);
			  }
		  }
		  
  ///////////////////     FOR EACH TASK POSTED FROM LIFECYCLE CALLBACKS --> THEIR COMPONENT AND IDS ARE SAVED ///////////////////// 
		  
		  printCall(concat,TaskM);
		  
  ////////////////// TaskM has been cleared  ////////////////////////
		  
		  int flag = 0;
		  int cl = 0; // line number count
		  String CL;  // line number converted to string and saved in CL
		  currentTask.clear();
		  line = br1.readLine();  // again start reading the same file
		  
		  while(line != null)
		  {
			  cl++;
			  CL = String.valueOf(cl);
			  StringTokenizer st = new StringTokenizer(line," "); //creates tokens of line and stores tid,taskid,child tid
			  while (st.hasMoreTokens())
		      {
				  String token = st.nextToken(); 
				  if (token.contains("tid") && !line.contains("FORK") && !line.contains("JOIN"))
		          {
		        	  tid = Integer.valueOf(token.substring(4));
		          }
		          else if (token.contains("msg:"))
		          {
		        	  taskid = Integer.valueOf(token.substring(4));
		          }
		          else if (token.contains("id:") && !token.contains("tid") && !line.contains("NOTIFY"))
		          {
		        	  id = Integer.valueOf(token.substring(3));
		          }
		          else if(token.contains("component:"))
		          {
		        	  component = token.substring(10);
		          }
		          else if(token.contains("state"))
		          {
		        	  state = token.substring(6);
		          }
		          else if(token.contains("instance:"))
		          {
		        	  instance = Integer.valueOf(token.substring(9));
		          }
		          else if(token.contains("intentId"))
		          {
		        	  intentId = Integer.valueOf(token.substring(9));
		          }
		      }
			  
			 flag = 0;
			  if(line.contains(" CALL ") && Tree.get(taskid).component != null) // means part of beginAtomic
			  {
				  flag = 1;
				  currentTask.put(tid, taskid);
				  line = excludeLineNum(line);
				  line = CL+" "+line;
				  line = line+" "+"beginAtomic\n";
				  brw.write(line);
			  }
			  else if(line.contains("RET"))
			  {
				  flag =1;
				  if(currentTask.containsKey(tid) && currentTask.get(tid) != -1)
				  {
					  currentTask.put(tid, -1);
					  line = excludeLineNum(line);
					  line = CL+" "+line;
					  line = line+" "+"endAtomic\n";
					  brw.write(line);
				  }
				  else
				  {
					  line = excludeLineNum(line);
					  line = CL+" "+line;
					  brw.write(line+"\n");
				  }
			  }
			  else if((!line.contains("METHOD")|| line.contains("meth:lock") || line.contains("meth:unlock") || line.contains("tryLock")))
			  {
				  line = excludeLineNum(line);
				  line = CL+" "+line;
				  brw.write(line+"\n");
			  }
			  line = br1.readLine();
		  }
  ///////////////////////////  MADE THE FILE WITH BEGINATOMIC AND ENDATOMIC ANNOTATIONS    ///////////////////////////////
	  }
	  catch(IOException e)
	  {
		  e.printStackTrace();
	  }
	  finally
	  {
		  br.close();
		  br1.close();
		  brw.close();
	  }
  }

public void printCall(String concat , HashMap<Integer,LifeValues> TaskM) throws IOException
  {
	try
	{
	concat = concat+"TREE";
	 treeFile = new FileWriter(concat);
	//  System.out.println("Mark tasks with intent ids");
	  treeFile.write("Mark tasks with intent ids\n\n");
	  Set<Integer> keys = TaskM.keySet();
	  for(Integer kk:keys)
	  {
		  Pair p = new Pair();
		  p = Tree.get(kk);
		  p.id = TaskM.get(kk).id;
		  p.component = TaskM.get(kk).component;
		  p.state =  TaskM.get(kk).state;
		  Tree.put(kk, p);
	  }
	  TaskM.clear();
	  keys = Tree.keySet();
	  for(Integer kk:keys)
	  {
		 // System.out.println("msg:"+kk+"==> Source:"+Tree.get(kk).source+"  target:"+Tree.get(kk).target+"  Parent:"+Tree.get(kk).parent+"  Depth:"+Tree.get(kk).depth+"  Tree of:"+Tree.get(kk).tree+"  id:"+Tree.get(kk).id+"  component:"+Tree.get(kk).component);
		  treeFile.write("msg:"+kk+"==> Source:"+Tree.get(kk).source+"  target:"+Tree.get(kk).target+"  Parent:"+Tree.get(kk).parent+"  Depth:"+Tree.get(kk).depth+"  Tree of:"+Tree.get(kk).tree+"  id:"+Tree.get(kk).id+"  component:"+Tree.get(kk).component+"  state:"+Tree.get(kk).state+"\n");
	  }
	}
	catch(IOException e)
	{
		e.printStackTrace();
	}
	finally
	{
		treeFile.close();
	}
  }

public void printIns(HashMap<Integer , Integer> InsM)
  {
	  System.out.println("inside printIns---");
	  Set<Integer> keys = InsM.keySet();
	  for(Integer kk:keys)
	  {
		 
		System.out.println("kk.toString:"+kk.toString()+" "+InsM.get(kk).toString()); 
	  }
  }

public void garbage_collection(int node, State2 state)
  {
	  
	  int flag =0;
	  Set<Integer> set = EdgeStore.keySet();
	  for(Integer i : set )
	  {
		  if(EdgeStore.get(i).getdest().node == node)
		  {
			  flag = 1;
		  }
	  }
	  if(flag == 0)
	  {
		  if(state.H.containsKey(node))
		  {
			  System.out.println("in garbage collection ");
		  }
		  state.H.remove(node);
	  }
  }

public  void phaseCheck(int number , String args0) throws IOException 
  {
	  countNode = 10000;
	  EdgeCount = 0;
	  newNode = 0;
	  String Num = String.valueOf(number);
	  String filename = args0.concat("output_tree.txt");
	  filename = filename.concat(Num);
	  File file =new File(filename);
	  FileWriter fw = new FileWriter(file); 
	  BufferedReader br = new BufferedReader(new FileReader("output.txt"));
	  String line;
	  int entered =0;
	  int lineNum=0;
	  int cycles = 0;
	  try 
	  {
		  line = br.readLine();
		  
		  int tid = 0 ;		//stores threadid on which current instruction is executed
		  int taskid = 0;		//taskid or eventid of posted task if instruction is post
		  int cTid = 0;		//child tid if insruction is fork
		  String lockObj = null;
		  String objId = null ;
		  String fieldId = null;
		  int nodeId = 0;
		  flagKey fk;
		  State2 state = new State2();
		  while(line!=null)
		  {
			  String linenum = line.substring(0, line.indexOf(" "));
			  lineNum = Integer.valueOf(linenum);
			  //System.out.println(" lineNum:"+lineNum);
			  entered =0;
			  StringTokenizer st = new StringTokenizer(line," "); //creates tokens of line and stores tid,taskid,child tid
			  while (st.hasMoreTokens())
		      {
				  String token = st.nextToken(); 
				  if (line.contains("FORK"))
		          {
					  if (token.contains("par-tid:"))
						  tid = Integer.valueOf(token.substring(8));
					  if (token.contains("child-tid:"))
						  cTid = Integer.valueOf(token.substring(10));
		          }
				  else if (line.contains("JOIN"))
		          {
					  if (token.contains("par-tid:"))
						  tid = Integer.valueOf(token.substring(8));
					  if (token.contains("child-tid:"))
						  cTid = Integer.valueOf(token.substring(10));
		          }
		          else if (token.contains("tid:"))
		          {
		        	  tid = Integer.valueOf(token.substring(4));
		        	  // when one thread can have only one atomic tree uncomment this code
		            	/*if(!firsttime.containsKey(tid))
		            	{
		            		firsttime.put(tid, 0);
		            	}*/
		          }
		          else if (token.contains("msg:"))
		          {
		        	  taskid = Integer.valueOf(token.substring(4));
		          }
		          else if (token.contains("src"))
		          {
		        	  tid = Integer.valueOf(token.substring(4));
		          }
		          else if (token.contains("lock-obj:")) 
		          {
		        	  lockObj = token.substring(9);
		          }
		          else if (token.contains("obj:") && !line.contains("meth:lock") && !line.contains("meth:unlock"))
		          {
		        	  objId = token.substring(4);
		          }
		          else if (token.contains("obj:") && line.contains("meth:lock") && line.contains("METHOD EXIT")  && !line.contains("$")) 
		          {
		        	  lockObj = token.substring(4);
		          }
		          else if (token.contains("obj:") && line.contains("meth:unlock") && line.contains("METHOD EXIT")  && !line.contains("$")) 
		          {
		        	  lockObj = token.substring(4);
		          }
		          else if (token.contains("field:")) 
		          {
		        	  fieldId = token.substring(6);
		          }
		          else if(token.contains("STATIC"))
		          {
		        	  objId = "STATIC";
		          }
		      }
			  //-------------------rules to change state
		      //------------flagAtomic is 1 means we are inside atomic block
			  // for every thread we can have multiple atomic trees executing at a time : key of flagatomic is threadId,tree root
			  if(line.contains(" CALL "))
			  {
				  String msg = line.substring(line.indexOf("msg")+4, line.indexOf("beginAtomic")-1);
				  int call = Integer.valueOf(msg);
				  taskMap.put(tid, call);
			  }
			  int root;
			  if(Tree.containsKey(taskMap.get(tid)))
			  {
				  root = Tree.get(taskMap.get(tid)).tree;
		    	   
			  }
			  else
				root = -1;
			  //System.out.println(line +" root "+root);
			  
			  fk = new flagKey(tid,root);
			  //--------firsttime denotes that the current task is first task of tree 
			  if(!(firsttime.containsKey(fk)) && line.contains("beginAtomic"))
			  {
				  firsttime.put(fk, 0);
				 // System.out.println(line +"first");
			  }	
		      if (line.contains("beginAtomic") && firsttime.get(fk)==0) //RULE ENTER :first task with begin atomic annotation
		      {
		    	  LinkedHashSet<Integer> ls = new LinkedHashSet<Integer>();//to store anscestors of a node
		    	  Value v = new Value();
		    	  v.node = ++ countNode;
		    	  v.line = lineNum;
		    	  state.C.put(tid, v);
		    	  flagAtomic.put(fk,1);
		    	  firsttime.put(fk,1);
		    	  v = new Value();
		    	  v.node = countNode;
		    	  v.line = lineNum;
		    	  state.L.put(tid,v);	//check 
		    	  v = new Value();
		    	  ls.add(++newNode);	//newNode ranges from 1 to 9999 ; each id assigned to root
		    	  state.H.put(countNode,ls);
		    	  //printState(line,state);
		      }
		      else if (line.contains("beginAtomic") && state.P.containsKey(taskid) && firsttime.get(fk) == 1) //RULE ENTER: child beginAtomic																task of Atomic block
		      {  
		    	  state.C.put(tid,state.P.get(taskid));
		    	  flagAtomic.put(fk, 1);
		    	  Value v = new Value();
		    	  v.node = countNode;
		    	  v.line = lineNum;
		    	  state.L.put(tid,v);  
		      }
			  if (!flagAtomic.containsKey(fk) || flagAtomic.get(fk) != 1)  //RULE INS OUTSIDE
			  {
				  nodeId = ++countNode;
			  }
			  else 
			  {
				  
				  Value v = new Value();
				  v = state.C.get(tid);
				  v.line = lineNum;
				  nodeId = v.node; //nodeId is set in beginAtomic // ***
				  state.C.put(tid,v);
			  }
			  TidNodeMap.put(nodeId,tid);
			  
			  if(line.contains("ATTACH-Q"))
			  {
				  AttachQ.put(tid, 1);
			  }
			  if (line.contains(" CALL ")) 
			  {
				  LinkedHashSet<Integer> ls = new LinkedHashSet<Integer>();//to store anscestors of a node
				  //taskMap.put(tid,taskid); //check if necessary
				  Value v = new Value();
				  v.node = nodeId;
				  v.line = lineNum;
				  state.L.put(tid,v);	//check
				  if (state.P.containsKey(taskid) && state.H.containsKey(state.P.get(taskid).node) && state.H.get(state.P.get(taskid).node) != null)
				  {
					  if (state.H.containsKey(nodeId)) 
					  {
						  ls.addAll(state.H.get(nodeId)); //  In Post rule if source and target are not same we put null in P corresponding to that task
						  								  //...so no edge will be added for cross post
					  }
					  ls.addAll(state.H.get(state.P.get(taskid).node));  
					  flagKey fk1 = new flagKey(tid,Tree.get(taskid).tree);	
					  if(flagAtomic.get(fk1)==0)		//if call is outside atomic block add edge otherwise not
					  {
						  ls.add(state.P.get(taskid).node); 
						 // print(state,line,state.P.get(taskid).node,nodeId);
						  entered =1;
					  }
					  
					  state.H.put(nodeId,ls);
				  }
				  
			  	}
				else if (line.contains("POST")) 
				{
					LinkedHashSet<Integer> ls = new LinkedHashSet<Integer>();//to store ancestors of a node
					Value v = new Value();
					if (Tree.containsKey(taskid) && (Tree.get(taskid).source != -1) && (Tree.get(taskid).target != -1))
					{
						if (Tree.get(taskid).source == Tree.get(taskid).target)
						{
							v.node = nodeId;
							v.line = lineNum;
							state.P.put(taskid,v);  
						}
					}
					if (state.L.containsKey(tid) && state.L.get(tid).node != -1 && state.L.get(tid).node != nodeId)
					{
						if (state.H.containsKey(nodeId)) 
						{
							ls.addAll(state.H.get(nodeId));
						}
						if(state.H.containsKey(state.L.get(tid).node))
						{
							ls.addAll(state.H.get(state.L.get(tid).node));
							ls.add(state.L.get(tid).node);
							state.H.put(nodeId,ls);
							//print(state,line,state.L.get(tid).node,nodeId);
							//System.out.println("priyanka mondal");
							entered =1;
							v = new Value();
							v.node = nodeId;
							v.line = lineNum;
							Edge e = new Edge(state.L.get(tid),v);
							EdgeStore.put(++EdgeCount,e);
							CausalEdges.add(e);
						}
					}
					v = new Value();
					v.node = nodeId;
					v.line = lineNum;
					state.L.put(tid,v);
					
					//System.out.println("L"+state.L.get(tid));
				}
				else if (line.contains("RET")) 
				{
					taskMap.put(tid,-1);	//thread is idle
					Value v = new Value();
					v.node = -1;
					v.line = lineNum;
					state.C.put(tid,v);
					flagAtomic.put(fk,0);
					state.L.put(tid,v);	// not connecting return to any instruction afterwards
					//print(state,line,state.L.get(tid).node,nodeId);
					entered =1;
				}
				else if (line.contains("THREADINIT"))  // *** THERE IS NO FORK FOR THE UI THREAD.. WHAT TO DO???
				{
					LinkedHashSet<Integer> ls = new LinkedHashSet<Integer>(); //to store ancestors of a node
					Value v = new Value();
					ls.add(++newNode); 	//same as in beginAtomic
					state.H.put(nodeId,ls);
					if (state.F.containsKey(tid) && state.H.containsKey(state.F.get(tid).node) && state.H.get(state.F.get(tid).node) != null)
					{
						if (state.H.containsKey(nodeId)) 
						{
							ls.addAll(state.H.get(nodeId));
						}
						ls.addAll(state.H.get(state.F.get(tid).node));
						ls.add(state.F.get(tid).node);
						v = new Value();
						v.node = nodeId;
						v.line = lineNum;
						Edge e = new Edge(state.F.get(tid),v);
						EdgeStore.put(++EdgeCount,e);
						CausalEdges.add(e);
						state.H.put(nodeId,ls);
						//print(state,line,state.F.get(tid).node,nodeId);
						entered =1;
					}
					 v = new Value();
					 v.node = nodeId;
					 v.line = lineNum;
					state.L.put(tid,v);
				}
				else if (line.contains("FORK")) 
				{
					LinkedHashSet<Integer> ls = new LinkedHashSet<Integer>();//to store anscestors of a node
					Value v = new Value();
					v.node = nodeId;
					v.line = lineNum;
					state.F.put(cTid,v);
					if(state.L.containsKey(tid) && state.L.get(tid).node!=-1 && state.L.get(tid).node!=nodeId)
					{
						if (state.H.containsKey(nodeId)) 
						{
							ls.addAll(state.H.get(nodeId));
						}
						ls.addAll(state.H.get(state.L.get(tid).node));
						ls.add(state.L.get(tid).node);
						state.H.put(nodeId,ls);
						v = new Value();
						v.node = nodeId;
						v.line = lineNum;
						Edge e = new Edge(state.L.get(tid),v);
						EdgeStore.put(++EdgeCount,e);
						//print(state,line,state.L.get(tid).node,nodeId);
						entered =1;
					}
					v.node = nodeId;
					v.line = lineNum;
					state.L.put(tid,v);
				}
				else if (line.contains("JOIN")) //check syntax of join 
				{
					LinkedHashSet<Integer> ls = new LinkedHashSet<Integer>();//to store anscestors of a node
					Value v = new Value();
					System.out.println("F:"+state.F.get(cTid).node+" "+state.F.get(cTid).line+" "+lineNum);
					if (state.F.containsKey(cTid) && state.H.containsKey(state.F.get(cTid).node) && state.H.get(state.F.get(cTid).node) != null)
					{// tid changed to ctid
						if (state.H.containsKey(nodeId)) 
						{
							ls.addAll(state.H.get(nodeId));
							//ls.addAll(state.H.get(state.F.get(tid)));
						}
						System.out.println("hey hi im in join");
						//////////////////////////
						if(state.H.get(state.F.get(cTid).node).contains(nodeId) /*&& state.U.get(lockObj)!=nodeId*/ )
						{														
							ls.addAll(state.H.get(state.F.get(cTid).node));
							ls.add(state.F.get(cTid).node);
							ls.remove(nodeId);
							v = new Value();
							v.node = nodeId;
							v.line = lineNum;
							Edge e = new Edge(state.F.get(cTid),v);
							EdgeStore.put(++EdgeCount,e);
							CausalEdges.add(e);
							cycles++;
							System.out.println(cycles+"-----------------CYCLE for THREADEXIT-JOIN EDGE!! "/*+state.U.get(lockObj).node+" "+nodeId*/);
							fw.write("\n"+ cycles+"-----------------CYCLE for THREADEXIT-JOIN EDGE!! "/*+state.U.get(lockObj).node+" "+nodeId*/);
							System.out.println("BLAME EDGE:"+"[("+state.F.get(cTid).node+","+state.F.get(cTid).line+"),("+nodeId+","+lineNum+")]");
							System.out.println("BLAME TRANSACTION: "+nodeId);
							fw.write("\nBLAME EDGE:"+"[("+state.F.get(cTid).node+","+state.F.get(cTid).line+"),("+nodeId+","+lineNum+")]");
							fw.write("\nBLAME TRANSACTION: "+nodeId);
							//printEdge(EdgeStore);
							s =new Stack<Edge>();
							//FindCycle(e.source, e.dest, e.source, e.dest, s,EdgeCount,state,0);
							FindPath(EdgeCount, e,fw);
						}
						////////////////////////
						else
						{
							ls.addAll(state.H.get(state.F.get(cTid).node)); // tid changed to ctid
							ls.add(state.F.get(cTid).node);
							state.H.put(nodeId,ls);
							v = new Value();
							v.node = nodeId;
							v.line = lineNum;
							Edge e = new Edge(state.F.get(cTid),v);// tid changed to ctid
							EdgeStore.put(++EdgeCount,e);
							CausalEdges.add(e);
						}
						//print(state,line,state.F.get(tid).node,nodeId);
						entered =1;
						//state.H.put(countNode,(state.H.get(state.F.get(tid))));
					}
					if(state.L.containsKey(tid) && state.L.get(tid).node!=-1 && state.L.get(tid).node!=nodeId)
					{
						if (state.H.containsKey(nodeId)) 
						{
							ls.addAll(state.H.get(nodeId));
							//ls.addAll(state.H.get(state.F.get(tid)));
						}
						ls.addAll(state.H.get(state.L.get(tid).node));
						ls.add(state.L.get(tid).node);
						state.H.put(nodeId,ls);
						v = new Value();
						v.node = nodeId;
						v.line = lineNum;
						Edge e = new Edge(state.F.get(tid),v);
						EdgeStore.put(++EdgeCount,e);
						CausalEdges.add(e);
						//print(state,line,state.L.get(tid).node,nodeId);
						entered =1;
					}
					v = new Value();
					v.node = nodeId;
					v.line = lineNum;
					state.L.put(tid,v);
				}
				else if (line.contains("THREADEXIT")) 
				{
					LinkedHashSet<Integer> ls = new LinkedHashSet<Integer>();//to store anscestors of a node
					Value v = new Value();
					if(state.L.containsKey(tid) && state.L.get(tid).node!=-1 && state.L.get(tid).node!=nodeId)
					{
						if (state.H.containsKey(nodeId)) 
						{
							ls.addAll(state.H.get(nodeId));
							//ls.addAll(state.H.get(state.F.get(tid)));
						}
						ls.addAll(state.H.get(state.L.get(tid).node));
						ls.add(state.L.get(tid).node);
						state.H.put(nodeId,ls);
						v = new Value();
						v.node = nodeId;
						v.line = lineNum;
						Edge e = new Edge(state.L.get(tid),v);
						EdgeStore.put(++EdgeCount,e);
						CausalEdges.add(e);
						//print(state,line,state.L.get(tid).node,nodeId);
						entered =1;
					}
					v.node = nodeId;
					v.line = lineNum;
					state.F.put(tid,v);
					//state.L.put(tid,v); //check : may be not needed
				}
				else if (line.contains("LOCK") && !line.contains("UNLOCK") || ((line.contains("meth:lock") || line.contains("meth:tryLock"))&& line.contains("METHOD EXIT") && !line.contains("$"))) 
				{
					LinkedHashSet<Integer> ls = new LinkedHashSet<Integer>();//to store anscestors of a node
					Value v = new Value();
					if (state.U.containsKey(lockObj) && state.H.containsKey(state.U.get(lockObj).node) && state.H.get(state.U.get(lockObj).node) != null && state.U.get(lockObj).node != nodeId)
					{									
						if (state.H.containsKey(nodeId)) 
						{
							ls.addAll(state.H.get(nodeId));
						}
						if(state.H.get(state.U.get(lockObj).node).contains(nodeId) /*&& state.U.get(lockObj)!=nodeId*/ )
						{														
							ls.addAll(state.H.get(state.U.get(lockObj).node));
							ls.add(state.U.get(lockObj).node);
							ls.remove(nodeId);
							v = new Value();
							v.node = nodeId;
							v.line = lineNum;
							Edge e = new Edge(state.U.get(lockObj),v);
							EdgeStore.put(++EdgeCount,e);
							cycles++;
							System.out.println(cycles+"-----------------CYCLE for RELEASE_ACQUIRE EDGE!! "/*+state.U.get(lockObj).node+" "+nodeId*/);
							fw.write("\n"+cycles+"-----------------CYCLE for RELEASE_ACQUIRE EDGE!! "/*+state.U.get(lockObj).node+" "+nodeId*/);
							System.out.println("BLAME EDGE:"+"[("+state.U.get(lockObj).node+","+state.U.get(lockObj).line+"),("+nodeId+","+lineNum+")]");
							System.out.println("BLAME TRANSACTION: "+nodeId);
							fw.write("\nBLAME EDGE:"+"[("+state.U.get(lockObj).node+","+state.U.get(lockObj).line+"),("+nodeId+","+lineNum+")]");
							fw.write("\nBLAME TRANSACTION: "+nodeId);
							//printEdge(EdgeStore);
							s =new Stack<Edge>();
							//FindCycle(e.source, e.dest, e.source, e.dest, s,EdgeCount,state,0);
							FindPath(EdgeCount, e,fw);
						}
						else if(AttachQ.containsKey(tid) || (!AttachQ.containsKey(tid) && lastUnlock!=tid))
						{
							v = new Value();
							v.node = nodeId;
							v.line = lineNum;
							Edge e = new Edge(state.U.get(lockObj),v);
							EdgeStore.put(++EdgeCount,e);
							ls.addAll(state.H.get(state.U.get(lockObj).node));
							ls.add(state.U.get(lockObj).node);
						}
						state.H.put(nodeId,ls);
							entered =1;
						//state.H.put(countNode,(state.H.get(state.U.get(lockObj))));
					}
					if(state.L.containsKey(tid) && state.L.get(tid).node != -1 && state.L.get(tid).node != nodeId)
					{
						if (state.H.containsKey(nodeId)) 
						{
							ls.addAll(state.H.get(nodeId));
						}
						ls.addAll(state.H.get(state.L.get(tid).node));
						ls.add(state.L.get(tid).node);
						state.H.put(nodeId,ls);
						v = new Value();
						v.node = nodeId;
						v.line = lineNum;
						Edge e = new Edge(state.L.get(tid),v);
						EdgeStore.put(++EdgeCount,e);
						CausalEdges.add(e);
						//print(state,line,state.L.get(tid).node,nodeId);
						entered =1;
					}
					v = new Value();
					v.node = nodeId;
					v.line = lineNum;
					state.L.put(tid,v);
				}
				
				else if (line.contains("UNLOCK") || (line.contains("meth:unlock") && line.contains("METHOD EXIT") && !line.contains("$"))) 
				{
					LinkedHashSet<Integer> ls = new LinkedHashSet<Integer>();//to store ancestors of a node
					Value v = new Value();
					v.node = nodeId;
					v.line = lineNum;
					state.U.put(lockObj,v);
					lastUnlock =tid;
					
					if(state.L.containsKey(tid) && state.L.get(tid).node!=-1 && state.L.get(tid).node!=nodeId)
					{
						if (state.H.containsKey(nodeId)) 
						{
							ls.addAll(state.H.get(nodeId));
						}
						ls.addAll(state.H.get(state.L.get(tid).node));
						ls.add(state.L.get(tid).node);
						state.H.put(nodeId,ls);
						v = new Value();
						v.node = nodeId;
						v.line = lineNum;
						Edge e = new Edge(state.L.get(tid),v);
						EdgeStore.put(++EdgeCount,e);
						CausalEdges.add(e);
					}
					v =new Value();
					v.node = nodeId;
					v.line = lineNum;
					state.L.put(tid,v);
					//print(state,line,state.L.get(tid).node,nodeId);
					entered =1;
				}
				else if (line.contains("READ"))  
				{
					LinkedHashSet<Integer> ls = new LinkedHashSet<Integer>();
					Value v = new Value();
					int task; 
					int Root;
					if(taskMap.containsKey(tid) && taskMap.get(tid) != -1)
					{
						task = taskMap.get(tid);
						Root = Tree.get(task).tree;
					}
					else
					{
						Root = -1;
					}
					
					ReadKey r = new ReadKey(objId,fieldId,tid,Root);
					Key k = new Key(objId,fieldId);
					v = new Value();
					v.node = nodeId;
					v.line = lineNum;
					state.R.put(r,v);
					if (state.L.containsKey(tid) && state.L.get(tid).node != -1 && state.L.get(tid).node != nodeId /*&& state.H.containsKey(state.L.get(k)) && state.H.get(state.L.get(k)) != null*/)
					{
						if (state.H.containsKey(nodeId)) 
						{
							ls.addAll(state.H.get(nodeId));
						}
						//System.out.println(line +" " +nodeId +" "+taskMap.get(tid));
						ls.addAll(state.H.get(state.L.get(tid).node));
						ls.add(state.L.get(tid).node);
						state.H.put(nodeId,ls);
						v = new Value();
						v.node = nodeId;
						v.line = lineNum;
						Edge e = new Edge(state.L.get(tid),v);
						EdgeStore.put(++EdgeCount,e);
						CausalEdges.add(e);
						//if(objId.equals("0x410e22a0") && fieldId.equals("104") && nodeId==1034)
								//print(state,line,state.L.get(tid).node,nodeId);
								entered =1;
					}
					v = new Value();
					v.node = nodeId;
					v.line = lineNum;
					state.L.put(tid,v);
					if (state.W.containsKey(k) && state.H.containsKey(state.W.get(k).node) && state.H.get(state.W.get(k).node) != null && nodeId != state.W.get(k).node)
					{
						if (state.H.containsKey(nodeId)) 
						{
							ls.addAll(state.H.get(nodeId)); 
						}
						if(state.H.get(state.W.get(k).node).contains(nodeId))
						{
							v = new Value();
							v.node = nodeId;
							v.line = lineNum;
							Edge e = new Edge(state.W.get(k),v);
							EdgeStore.put(++EdgeCount,e);
							cycles++;
							System.out.println(cycles+"----------------------CYCLE for WRITE-READ EDGE!! "/*+objId+" field:"+fieldId+" (src:"+state.W.get(k).node+","+state.W.get(k).line+") (dest:"+nodeId+","+lineNum+")"*/);
							fw.write("\n"+cycles+"----------------------CYCLE for WRITE-READ EDGE!! "/*+objId+" field:"+fieldId+" (src:"+state.W.get(k).node+","+state.W.get(k).line+") (dest:"+nodeId+","+lineNum+")"*/);
							System.out.println("BLAME EDGE:"+"[("+state.W.get(k).node+","+state.W.get(k).line+"),("+nodeId+","+lineNum+")]");
							System.out.println("BLAME TRANSACTION: "+nodeId);
							fw.write("\nBLAME EDGE:"+"[("+state.W.get(k).node+","+state.W.get(k).line+"),("+nodeId+","+lineNum+")]");
							fw.write("\nBLAME TRANSACTION: "+nodeId);
							//printEdge(EdgeStore);
							//System.out.println(EdgeStore);
							ls.addAll(state.H.get(state.W.get(k).node));
							ls.add(state.W.get(k).node);
							ls.remove(nodeId);
							s =new Stack<Edge>();
							//FindCycle(e.source, e.dest, e.source, e.dest, s,EdgeCount,state,0);
							FindPath(EdgeCount, e, fw);
							/*if(objId.equals("0x4110bec0") && fieldId.equals("80"))*/
								//print(state,line,state.W.get(k).node,nodeId);
							
						}
						else
						{
							v = new Value();
							v.node = nodeId;
							v.line = lineNum;
							Edge e = new Edge(state.W.get(k),v);
							EdgeStore.put(++EdgeCount,e);
							ls.addAll(state.H.get(state.W.get(k).node));
							ls.add(state.W.get(k).node);
						}
						state.H.put(nodeId,ls);
						//print(state,line,state.W.get(k).node,nodeId);
						entered =1;
					}
					
				}
				else if (line.contains("WRITE")) //check: what about read-static
				{
					LinkedHashSet<Integer> ls = new LinkedHashSet<Integer>();//to store ancestors of a node
					ReadKey r;
					Key k = new Key(objId,fieldId);
					Value v = new Value();
					if (state.W.containsKey(k) && state.H.containsKey(state.W.get(k).node) && state.H.get(state.W.get(k).node) != null && nodeId != state.W.get(k).node)
					{
						if (state.H.containsKey(nodeId)) 
						{
							ls.addAll(state.H.get(nodeId));
							//ls.addAll(state.H.get(state.F.get(tid)));
						}
						if(state.H.get(state.W.get(k).node).contains(nodeId))
						{
							v = new Value();
							v.node = nodeId;
							v.line = lineNum;
							Edge e = new Edge(state.W.get(k),v);
							EdgeStore.put(++EdgeCount,e);
							ls.addAll(state.H.get(state.W.get(k).node));
							ls.add(state.W.get(k).node);
							ls.remove(nodeId);
							cycles++;
							System.out.println(cycles+"--------------------CYCLE for WRITE WRITE EDGE!! "/*+objId+" "+fieldId+" src:"+state.W.get(k).node+" dest:"+nodeId*/);
							fw.write("\n"+cycles+"--------------------CYCLE for WRITE WRITE EDGE!! "/*+objId+" "+fieldId+" src:"+state.W.get(k).node+" dest:"+nodeId*/);
							System.out.println("BLAME EDGE:"+"[("+state.W.get(k).node+","+state.W.get(k).line+"),("+nodeId+","+lineNum+")]");
							System.out.println("BLAME TRANSACTION: "+nodeId);
							fw.write("\nBLAME EDGE:"+"[("+state.W.get(k).node+","+state.W.get(k).line+"),("+nodeId+","+lineNum+")]");
							fw.write("\nBLAME TRANSACTION: "+nodeId);
							//printEdge(EdgeStore);
							FindPath(EdgeCount, e,fw);
							//print(state,line,state.W.get(k),nodeId);
						}
						else
						{
							v = new Value();
							v.node = nodeId;
							v.line = lineNum;
							Edge e = new Edge(state.W.get(k),v);
							EdgeStore.put(++EdgeCount,e);
							ls.addAll(state.H.get(state.W.get(k).node));
							ls.add(state.W.get(k).node);
						}
						state.H.put(nodeId,ls);
						//ls.clear();
							entered =1;
						
					}
					 v = new Value();
					v.node = nodeId;
					v.line = lineNum;
					state.W.put(k,v);
					Iterator it = state.R.entrySet().iterator();
					LinkedHashSet<Integer> ls2 = new LinkedHashSet<Integer>();
				    while (it.hasNext()) 
				    {
				        Map.Entry pair = (Map.Entry)it.next();
				        r = (ReadKey) pair.getKey();
				        if ((r.getObjectId().equals(objId) )&& (r.getFieldId().equals(fieldId)) )
				        {
				        	if (state.H.containsKey(state.R.get(r).node) && state.H.get(state.R.get(r).node) != null && nodeId != state.R.get(r).node)
				        	{
				        		if (state.H.containsKey(nodeId)) 
				        		{
				        			ls2.addAll(state.H.get(nodeId));
				        		}
				        		if(state.H.get(state.R.get(r).node).contains(nodeId))
								{
				        			v = new Value();
									v.node = nodeId;
									v.line = lineNum;
									Edge e = new Edge(state.R.get(r),v);
									EdgeStore.put(++EdgeCount,e);
				        			ls2.addAll(state.H.get(state.R.get(r).node));/// *** CHANGED TO R
									ls2.add(state.R.get(r).node);
									ls2.remove(nodeId);
									cycles++;
									System.out.println(cycles+"----------------------CYCLE for READ-WRITE EDGE!!  "/*+objId+" "+fieldId+" src:"+state.R.get(r).node+" dest:"+nodeId*/);
									fw.write("\n"+cycles+"----------------------CYCLE for READ-WRITE EDGE!!  "/*+objId+" "+fieldId+" src:"+state.R.get(r).node+" dest:"+nodeId*/);
									System.out.println("BLAME EDGE:"+"[("+state.R.get(r).node+","+state.R.get(r).line+"),("+nodeId+","+lineNum+")]");
									System.out.println("BLAME TRANSACTION: "+nodeId);
									fw.write("\nBLAME EDGE:"+"[("+state.R.get(r).node+","+state.R.get(r).line+"),("+nodeId+","+lineNum+")]");
									fw.write("\nBLAME TRANSACTION: "+nodeId);
									//printEdge(EdgeStore);
									FindPath(EdgeCount, e,fw);
								}
				        		else
				        		{
				        			v = new Value();
									v.node = nodeId;
									v.line = lineNum;
									Edge e = new Edge(state.R.get(r),v);
									EdgeStore.put(++EdgeCount,e);
					        		ls2.addAll(state.H.get(state.R.get(r).node));
					        		ls2.add(state.R.get(r).node);
				        		}
				        		state.H.put(nodeId,ls2);
				        		//print(state,line,state.R.get(r).node,nodeId);
				        		entered =1;
				        	}
				        	it.remove(); // avoids a ConcurrentModificationException
				        }
				    }
				    LinkedHashSet<Integer> ls3 = new LinkedHashSet<Integer>();
				    if(state.L.containsKey(tid) && state.L.get(tid).node!=-1 && state.L.get(tid).node!=nodeId)
					{
						if (state.H.containsKey(nodeId)) 
						{
							ls3.addAll(state.H.get(nodeId));
						}
						ls3.addAll(state.H.get(state.L.get(tid).node));
						ls3.add(state.L.get(tid).node);
						state.H.put(nodeId,ls3);
						v = new Value();
						v.node = nodeId;
						v.line = lineNum;
						Edge e = new Edge(state.L.get(tid),v);
						EdgeStore.put(++EdgeCount,e);
						CausalEdges.add(e);
						//print(state,line,state.L.get(tid).node,nodeId);
						entered =1;
					}
				    v = new Value();
				    v.node = nodeId;
				    v.line = lineNum;
					state.L.put(tid,v);
				}
			  if(entered == 0)
			  {
				  //print(state,line,newNode,nodeId);
			  }
				line=br.readLine();
			}
			
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		} finally 
		{
			br.close();
			fw.close();
		}
	}
 
public void checkLifecycles(int number, String concat,String args0) throws IOException
  {
	  countNode = 10000;
	  EdgeCount = 0;
	  newNode = 0;
	  int cycles = 0;
	  int intentId;
	  int id = 0;
	  String component;
	  String State;
	  int cTid = 0;
	  String out = concat+"_IntermediateLife";
	  String Num = String.valueOf(number);
	  String filename = args0.concat("output_Life.txt");
	  filename = filename.concat(Num);
	  File file =new File(filename);
	  FileWriter fw = new FileWriter(file); 
	  BufferedReader br = new BufferedReader(new FileReader(out));
	  String lockObj = null;
	  String fieldId = null;
	  String line ;
	  line = br.readLine();
	  int tid = 0;
	  int taskid = 0;
	  String objId = null;
	  String Id;
	  int instance;
	  int lineNum = 0;
	  int nodeId;
	  int entered = 0;
	  int get_id;
	  State2 state = new State2();
	  try
	  {
		  while(line!= null)
		  {
			  String linenum = line.substring(0, line.indexOf(" "));
			  lineNum = Integer.valueOf(linenum);
			  StringTokenizer st = new StringTokenizer(line," ");
			  //System.out.println(line);
			  while (st.hasMoreTokens())
		      {
				  String token = st.nextToken(); 
				  if (line.contains("FORK"))
		          {
					  if (token.contains("par-tid:"))
						  tid = Integer.valueOf(token.substring(8));
					  if (token.contains("child-tid:"))
						  cTid = Integer.valueOf(token.substring(10));
		          }
				  else if (line.contains("JOIN"))
		          {
					  if (token.contains("par-tid:"))
						  tid = Integer.valueOf(token.substring(8));
					  if (token.contains("child-tid:"))
						  cTid = Integer.valueOf(token.substring(10));
		          }
				  else if (token.contains("tid:"))
		          {
		        	  tid = Integer.valueOf(token.substring(4));
		          }
		          else if (token.contains("msg:"))
		          {
		        	  taskid = Integer.valueOf(token.substring(4));
		          }
		          else if (token.contains("src:"))
		          {
		        	  tid = Integer.valueOf(token.substring(4));
		          }
		          else if (token.contains("id:") && !line.contains("NOTIFY") && !token.contains("tid"))
		          {
		        	  id = Integer.valueOf(token.substring(3));
		          }
		          else if(token.contains("component:"))
		          {
		        	  component = token.substring(10);
		          }
		          else if(token.contains("state:"))
		          {
		        	  State = token.substring(6);
		          }
		          else if(token.contains("instance:"))
		          {
		        	  instance = Integer.valueOf(token.substring(9));
		          }
		          else if(token.contains("intentId:"))
		          {
		        	  intentId = Integer.valueOf(token.substring(9));
		          }
				  else if (token.contains("lock-obj:")) 
		          {
		        	  lockObj = token.substring(9);
		          }
		          else if (token.contains("obj:") && !line.contains("meth:lock") && !line.contains("meth:unlock"))
		          {
		        	  objId = token.substring(4);
		          }
		          else if (token.contains("obj:") && line.contains("meth:lock") && line.contains("METHOD EXIT")  && !line.contains("$")) 
		          {
		        	  lockObj = token.substring(4);
		          }
		          else if (token.contains("obj:") && line.contains("meth:unlock") && line.contains("METHOD EXIT")  && !line.contains("$")) 
		          {
		        	  lockObj = token.substring(4);
		          }
		          else if (token.contains("field:")) 
		          {
		        	  fieldId = token.substring(6);
		          }
		          else if(token.contains("STATIC"))
		          {
		        	  objId = "STATIC";
		          }
		      }
			  if(line.contains(" CALL "))
			  {
				  taskMap.put(tid, taskid);
			  }
			  if(Tree.containsKey(taskMap.get(tid)) && Tree.get(taskMap.get(tid)).id != 0)
			  {
				  get_id = Tree.get(taskMap.get(tid)).id;
			  }
			  else
			  {
				  get_id = -1;
			  }
			  
			  flagKeyLife fk = new flagKeyLife(tid,get_id);
			 
		      if (line.contains("beginAtomic") && !flagAtomicLife.containsKey(fk)) //RULE ENTER :first task with begin atomic annotation
		      {
		    	  LinkedHashSet<Integer> ls = new LinkedHashSet<Integer>();//to store anscestors of a node
		    	  Value v = new Value();
		    	  v.node = ++ countNode;
		    	  v.line = lineNum;
		    	  state.C.put(tid, v);
		    	  flagAtomicLife.put(fk,1);
		    	  state.L.put(tid,v);	//   ***check : NOT REQUIRED THOUGH
		    	  ls.add(++newNode);	//newNode ranges from 1 to 999 ; each id assigned to root
		    	  state.H.put(countNode,ls);
		    	  RecNode.put(Tree.get(taskMap.get(tid)).id,countNode);
		      }
		      else if (line.contains("beginAtomic")  && flagAtomicLife.containsKey(fk)) //RULE ENTER: child beginAtomic																task of Atomic block
		      {  
		    	  Value v = new Value();
		    	  v.node = RecNode.get(Tree.get(taskid).id);
		    	  v.line = lineNum;
		    	  state.C.put(tid,v);
		    	  flagAtomicLife.put(fk, 1);
		    	  state.L.put(tid,v);  //   ***check : NOT REQUIRED THOUGH
		      }
			  if ((!flagAtomicLife.containsKey(fk) || flagAtomicLife.get(fk) != 1) &&  ( !taskMap.containsKey(tid) || !outside.containsKey(taskMap.get(tid))))  //RULE INS OUTSIDE
			  {
				  nodeId = ++countNode;
			  }
			  else 
			  {
				  Value v = new Value();
				  v.node = state.C.get(tid).node;
				  v.line = lineNum;
				  nodeId = v.node; //nodeId is set in beginAtomic // ***
				  state.C.put(tid,v);
			  }
			 // System.out.println(line+" node:"+nodeId);
			  TidNodeMap.put(nodeId,tid);
			  
			  if(line.contains("ATTACH-Q"))
			  {
				  AttachQ.put(tid, 1);
			  }
			  else if (line.contains(" CALL ")) 
			  {
				  LinkedHashSet<Integer> ls = new LinkedHashSet<Integer>();
				  LinkedHashSet<Integer> ls2 = new LinkedHashSet<Integer>();
				  Value v = new Value();
				  v.node = nodeId;
				  v.line = lineNum;
					if(/*(!flagAtomicLife.containsKey(fk) || flagAtomicLife.get(fk)== 0) && (!outside.containsKey(taskid))*/ !line.contains("beginAtomic"))
					{
						ls2.add(++newNode);
						state.H.put(nodeId, ls2);
						outside.put(taskid, nodeId); 
						state.C.put(tid, v);
					}
				  if (state.P.containsKey(taskid) && state.H.containsKey(state.P.get(taskid).node) && state.H.get(state.P.get(taskid).node) != null)
				  {
					  if (state.H.containsKey(nodeId)) 
					  {
						  ls.addAll(state.H.get(nodeId)); //  In Post rule if source and target are not same we put null in P corresponding to that task
						  								  //...so no edge will be added for cross post
					  }
					  ls.addAll(state.H.get(state.P.get(taskid).node));  
					  ls.add(state.P.get(taskid).node); 
					  entered =1;
					  state.H.put(nodeId,ls);
					  state.L.put(tid,v);
					  Edge e = new Edge(state.P.get(taskid),v);
					  EdgeStore.put(++EdgeCount,e);
				      CausalEdges.add(e);   //*** DONT KNOW ITS A CAUSAL EDGE OR NOT
				  }
			  	}
				else if (line.contains("POST")) 
				{
					LinkedHashSet<Integer> ls = new LinkedHashSet<Integer>();//to store ancestors of a node
					Value v = new Value();
					if (Tree.containsKey(taskid) && (Tree.get(taskid).source != -1) && (Tree.get(taskid).target != -1))
					{
						if (Tree.get(taskid).source == Tree.get(taskid).target)
						{
							v.node = nodeId;
							v.line = lineNum;
							state.P.put(taskid,v);  
						}
					}
					if (state.L.containsKey(tid) && state.L.get(tid).node != -1 && state.L.get(tid).node != nodeId)
					{
						if (state.H.containsKey(nodeId)) 
						{
							ls.addAll(state.H.get(nodeId));
						}
						if(state.H.containsKey(state.L.get(tid).node))
						{
							ls.addAll(state.H.get(state.L.get(tid).node));
						
							ls.add(state.L.get(tid).node);
							state.H.put(nodeId,ls);
							entered =1;
							v = new Value();
							v.node = nodeId;
							v.line = lineNum;
							Edge e = new Edge(state.L.get(tid),v);
							EdgeStore.put(++EdgeCount,e);
							CausalEdges.add(e);
						}
					}
					v = new Value();
					v.node = nodeId;
					v.line = lineNum;
					state.L.put(tid,v);
				}
				else if (line.contains("RET")) 
				{
						//thread is idle
					Value v = new Value();
					v.node = -1;
					v.line = lineNum;
					state.C.put(tid,v);
					if(line.contains("endAtomic"))
						flagAtomicLife.put(fk,0);
					else if(outside.get(taskMap.get(tid)) != -1)
						outside.put(taskid, -1);
					taskMap.put(tid,-1);
					state.L.put(tid,v);	// not connecting return to any instruction afterwards as v CONTAINS -1
					entered =1;
					if(!line.contains("endAtomic"))
					garbage_collection(nodeId, state);
				}
				else if (line.contains("THREADINIT"))  
				{
					LinkedHashSet<Integer> ls = new LinkedHashSet<Integer>(); 
					Value v = new Value();
					ls.add(++newNode); 	//same as in beginAtomic
					state.H.put(nodeId,ls);
					if (state.F.containsKey(tid) && state.H.containsKey(state.F.get(tid).node) && state.H.get(state.F.get(tid).node) != null)
					{
						if (state.H.containsKey(nodeId)) 
						{
							ls.addAll(state.H.get(nodeId));
						}
						ls.addAll(state.H.get(state.F.get(tid).node));
						ls.add(state.F.get(tid).node);
						v = new Value();
						v.node = nodeId;
						v.line = lineNum;
						Edge e = new Edge(state.F.get(tid),v);
						EdgeStore.put(++EdgeCount,e);
						CausalEdges.add(e);
						state.H.put(nodeId,ls);
						//print(state,line,state.F.get(tid).node,nodeId);
						entered =1;
						//System.out.println(e.toString());
					}
					 v = new Value();
					 v.node = nodeId;
					 v.line = lineNum;
					state.L.put(tid,v);
				}
				else if (line.contains("FORK")) 
				{
					LinkedHashSet<Integer> ls = new LinkedHashSet<Integer>();//to store anscestors of a node
					Value v = new Value();
					v.node = nodeId;
					v.line = lineNum;
					state.F.put(cTid,v);
					if(state.L.containsKey(tid) && state.L.get(tid).node!=-1 && state.L.get(tid).node!=nodeId)
					{
						if (state.H.containsKey(nodeId)) 
						{
							ls.addAll(state.H.get(nodeId));
						}
						//if(state.H.containsKey(state.L.get(tid).node))
						//{
							
						ls.addAll(state.H.get(state.L.get(tid).node));
						ls.add(state.L.get(tid).node);
						state.H.put(nodeId,ls);
						v = new Value();
						v.node = nodeId;
						v.line = lineNum;
						Edge e = new Edge(state.L.get(tid),v);
						EdgeStore.put(++EdgeCount,e);
						//print(state,line,state.L.get(tid).node,nodeId);
						entered =1;
						//System.out.println(e.toString());
					//	}
					}
					v.node = nodeId;
					v.line = lineNum;
					state.L.put(tid,v);
				}
				else if (line.contains("JOIN")) //check syntax of join 
				{
					LinkedHashSet<Integer> ls = new LinkedHashSet<Integer>();//to store anscestors of a node
					Value v = new Value();
					System.out.println("F:"+state.F.get(cTid).node+" "+state.F.get(cTid).line+" "+lineNum);
					if (state.F.containsKey(cTid) && state.H.containsKey(state.F.get(cTid).node) && state.H.get(state.F.get(cTid).node) != null)
					{// tid changed to ctid
						if (state.H.containsKey(nodeId)) 
						{
							ls.addAll(state.H.get(nodeId));
							//ls.addAll(state.H.get(state.F.get(tid)));
						}
						//////////////////////////
						if(state.H.get(state.F.get(cTid).node).contains(nodeId) /*&& state.U.get(lockObj)!=nodeId*/ )
						{														
							ls.addAll(state.H.get(state.F.get(cTid).node));
							ls.add(state.F.get(cTid).node);
							ls.remove(nodeId);
							state.H.put(nodeId,ls);
							v = new Value();
							v.node = nodeId;
							v.line = lineNum;
							Edge e = new Edge(state.F.get(cTid),v);
							EdgeStore.put(++EdgeCount,e);
							CausalEdges.add(e);
							cycles++;
							System.out.println(cycles+"-----------------CYCLE for THREADEXIT-JOIN EDGE!! "/*+state.U.get(lockObj).node+" "+nodeId*/);
							System.out.println("BLAME EDGE:"+"[("+state.F.get(cTid).node+","+state.F.get(cTid).line+"),("+nodeId+","+lineNum+")]");
							System.out.println("BLAME TRANSACTION: "+nodeId);
							fw.write(cycles+"-----------------CYCLE for THREADEXIT-JOIN EDGE!! ");
							fw.write("\nBLAME EDGE:"+"[("+state.F.get(cTid).node+","+state.F.get(cTid).line+"),("+nodeId+","+lineNum+")]");
							fw.write("\nBLAME TRANSACTION: "+nodeId);
							//printEdge(EdgeStore);
							s =new Stack<Edge>();
							//FindCycle(e.source, e.dest, e.source, e.dest, s,EdgeCount,state,0);
							FindPath(EdgeCount, e,fw);
							//System.out.println(e.toString());
						}
						////////////////////////
						else
						{
							ls.addAll(state.H.get(state.F.get(cTid).node)); // tid changed to ctid
							ls.add(state.F.get(cTid).node);
							state.H.put(nodeId,ls);
							v = new Value();
							v.node = nodeId;
							v.line = lineNum;
							Edge e = new Edge(state.F.get(cTid),v);// tid changed to ctid
							EdgeStore.put(++EdgeCount,e);
							CausalEdges.add(e);
							//System.out.println(e.toString());
						}
						//print(state,line,state.F.get(tid).node,nodeId);
						entered =1;
						//state.H.put(countNode,(state.H.get(state.F.get(tid))));
					}
					if(state.L.containsKey(tid) && state.L.get(tid).node!=-1 && state.L.get(tid).node!=nodeId)
					{
						if (state.H.containsKey(nodeId)) 
						{
							ls.addAll(state.H.get(nodeId));
							//ls.addAll(state.H.get(state.F.get(tid)));
						}
						ls.addAll(state.H.get(state.L.get(tid).node));
						ls.add(state.L.get(tid).node);
						state.H.put(nodeId,ls);
						v = new Value();
						v.node = nodeId;
						v.line = lineNum;
						Edge e = new Edge(state.F.get(tid),v);
						EdgeStore.put(++EdgeCount,e);
						CausalEdges.add(e);
						//System.out.println(e.toString());
						//print(state,line,state.L.get(tid).node,nodeId);
						entered =1;
					}
					v = new Value();
					v.node = nodeId;
					v.line = lineNum;
					state.L.put(tid,v);
				}
				else if (line.contains("THREADEXIT")) 
				{
					LinkedHashSet<Integer> ls = new LinkedHashSet<Integer>();//to store anscestors of a node
					Value v = new Value();
					if(state.L.containsKey(tid) && state.L.get(tid).node!=-1 && state.L.get(tid).node!=nodeId)
					{
						if (state.H.containsKey(nodeId)) 
						{
							ls.addAll(state.H.get(nodeId));
							//ls.addAll(state.H.get(state.F.get(tid)));
						}
						//if(state.H.containsKey(state.L.get(tid).node))
						//{
							ls.addAll(state.H.get(state.L.get(tid).node));
							ls.add(state.L.get(tid).node);
							state.H.put(nodeId,ls);
							v = new Value();
							v.node = nodeId;
							v.line = lineNum;
							Edge e = new Edge(state.L.get(tid),v);
							EdgeStore.put(++EdgeCount,e);
							CausalEdges.add(e);
							//System.out.println(e.toString());
							//print(state,line,state.L.get(tid).node,nodeId);
							entered =1;
						//}
					}
					v.node = nodeId;
					v.line = lineNum;
					state.F.put(tid,v);
					//state.L.put(tid,v); //check : may be not needed
				}
				else if (line.contains("LOCK") && !line.contains("UNLOCK") || ((line.contains("meth:lock") || line.contains("meth:tryLock"))&& line.contains("METHOD EXIT") && !line.contains("$"))) 
				{
					LinkedHashSet<Integer> ls = new LinkedHashSet<Integer>();//to store anscestors of a node
					Value v = new Value();
					if (state.U.containsKey(lockObj) && state.H.containsKey(state.U.get(lockObj).node) && state.H.get(state.U.get(lockObj).node) != null && state.U.get(lockObj).node != nodeId)
					{									
						if (state.H.containsKey(nodeId)) 
						{
							ls.addAll(state.H.get(nodeId));
						}
						if(state.H.get(state.U.get(lockObj).node).contains(nodeId) /*&& state.U.get(lockObj)!=nodeId*/ )
						{														
							ls.addAll(state.H.get(state.U.get(lockObj).node));
							ls.add(state.U.get(lockObj).node);
							ls.remove(nodeId);
							v = new Value();
							v.node = nodeId;
							v.line = lineNum;
							Edge e = new Edge(state.U.get(lockObj),v);
							EdgeStore.put(++EdgeCount,e);
							cycles++;
							System.out.println(cycles+"-----------------CYCLE for RELEASE_ACQUIRE EDGE!! "/*+state.U.get(lockObj).node+" "+nodeId*/);
							System.out.println("BLAME EDGE:"+"[("+state.U.get(lockObj).node+","+state.U.get(lockObj).line+"),("+nodeId+","+lineNum+")]");
							System.out.println("BLAME TRANSACTION: "+nodeId);
							fw.write(cycles+"-----------------CYCLE for RELEASE_ACQUIRE EDGE!! "/*+state.U.get(lockObj).node+" "+nodeId*/);
							fw.write("\nBLAME EDGE:"+"[("+state.U.get(lockObj).node+","+state.U.get(lockObj).line+"),("+nodeId+","+lineNum+")]");
							fw.write("\nBLAME TRANSACTION: "+nodeId);
							//printEdge(EdgeStore);
							//System.out.println(e.toString());
							FindPath(EdgeCount, e,fw);
						}
						else if(AttachQ.containsKey(tid) || (!AttachQ.containsKey(tid) && lastUnlock!=tid))
						{
							v = new Value();
							v.node = nodeId;
							v.line = lineNum;
							Edge e = new Edge(state.U.get(lockObj),v);
							EdgeStore.put(++EdgeCount,e);
							ls.addAll(state.H.get(state.U.get(lockObj).node));
							ls.add(state.U.get(lockObj).node);
						}
						state.H.put(nodeId,ls);
						//print(state,line,state.U.get(lockObj).node,nodeId);
						entered =1;
					}
					if(state.L.containsKey(tid) && state.L.get(tid).node != -1 && state.L.get(tid).node != nodeId)
					{
						if (state.H.containsKey(nodeId)) 
						{
							ls.addAll(state.H.get(nodeId));
						}
						//if(state.H.containsKey(state.L.get(tid).node))
						//{
							ls.addAll(state.H.get(state.L.get(tid).node));
							ls.add(state.L.get(tid).node);
							state.H.put(nodeId,ls);
							v = new Value();
							v.node = nodeId;
							v.line = lineNum;
							Edge e = new Edge(state.L.get(tid),v);
							EdgeStore.put(++EdgeCount,e);
							CausalEdges.add(e);
							//System.out.println(e.toString());
							//print(state,line,state.L.get(tid).node,nodeId);
						//}
						entered =1;
					}
					v = new Value();
					v.node = nodeId;
					v.line = lineNum;
					state.L.put(tid,v);
				}
				
				else if (line.contains("UNLOCK") || (line.contains("meth:unlock") && line.contains("METHOD EXIT") && !line.contains("$"))) 
				{
					LinkedHashSet<Integer> ls = new LinkedHashSet<Integer>();//to store ancestors of a node
					Value v = new Value();
					v.node = nodeId;
					v.line = lineNum;
					state.U.put(lockObj,v);
					lastUnlock =tid;
					
					if(state.L.containsKey(tid) && state.L.get(tid).node!=-1 && state.L.get(tid).node!=nodeId)
					{
						if (state.H.containsKey(nodeId)) 
						{
							ls.addAll(state.H.get(nodeId));
						}
						//if(state.H.containsKey(state.L.get(tid).node))
						//{
							
						ls.addAll(state.H.get(state.L.get(tid).node));
						ls.add(state.L.get(tid).node);
						state.H.put(nodeId,ls);
						v = new Value();
						v.node = nodeId;
						v.line = lineNum;
						Edge e = new Edge(state.L.get(tid),v);
						EdgeStore.put(++EdgeCount,e);
						CausalEdges.add(e);
						//System.out.println(e.toString());
						//}
					}
					v =new Value();
					v.node = nodeId;
					v.line = lineNum;
					state.L.put(tid,v);
					//print(state,line,state.L.get(tid).node,nodeId);
					entered =1;
				}
				else if (line.contains("READ"))  
				{
					LinkedHashSet<Integer> ls = new LinkedHashSet<Integer>();
					Value v = new Value();
					int task; 
					int idd;
					if(taskMap.containsKey(tid) && taskMap.get(tid) != -1)
					{
						task = taskMap.get(tid);
						idd = Tree.get(task).id;
					}
					else  /// *** THIS IS NOT REQUIRED AS BY DEFAULT OUTSIDE OF LIFECYCLE id IS -1
					{
						idd = -1;
					}
					ReadKey r = new ReadKey(objId,fieldId,tid,idd);
					Key k = new Key(objId,fieldId);
					v = new Value();
					v.node = nodeId;
					v.line = lineNum;
					state.R.put(r,v);
					
					if (state.L.containsKey(tid) && state.L.get(tid).node != -1 && state.L.get(tid).node != nodeId && state.H.containsKey(state.L.get(tid).node) && state.H.get(state.L.get(tid).node) != null)
					{
						if (state.H.containsKey(nodeId)) 
						{
							ls.addAll(state.H.get(nodeId));
						}
						ls.addAll(state.H.get(state.L.get(tid).node));
						ls.add(state.L.get(tid).node);
						state.H.put(nodeId,ls);
						v = new Value();
						v.node = nodeId;
						v.line = lineNum;
						Edge e = new Edge(state.L.get(tid),v);
						EdgeStore.put(++EdgeCount,e);
						CausalEdges.add(e);
								entered =1;
					}
					v = new Value();
					v.node = nodeId;
					v.line = lineNum;
					state.L.put(tid,v);
					if (state.W.containsKey(k) && state.H.containsKey(state.W.get(k).node) && state.H.get(state.W.get(k).node) != null && nodeId != state.W.get(k).node)
					{
						if (state.H.containsKey(nodeId)) 
						{
							ls.addAll(state.H.get(nodeId)); 
						}
						if(state.H.get(state.W.get(k).node).contains(nodeId))
						{
							v = new Value();
							v.node = nodeId;
							v.line = lineNum;
							Edge e = new Edge(state.W.get(k),v);
							EdgeStore.put(++EdgeCount,e);
							cycles++;
							System.out.println(cycles+"----------------------CYCLE for WRITE-READ EDGE!! "/*+objId+" field:"+fieldId+" (src:"+state.W.get(k).node+","+state.W.get(k).line+") (dest:"+nodeId+","+lineNum+")"*/);
							System.out.println("BLAME EDGE:"+"[("+state.W.get(k).node+","+state.W.get(k).line+"),("+nodeId+","+lineNum+")]");
							System.out.println("BLAME TRANSACTION: "+nodeId);
							fw.write(cycles+"----------------------CYCLE for WRITE-READ EDGE!! ");
							fw.write("\nBLAME EDGE:"+"[("+state.W.get(k).node+","+state.W.get(k).line+"),("+nodeId+","+lineNum+")]");
							fw.write("\nBLAME TRANSACTION: "+nodeId);
							//printEdge(EdgeStore);
							ls.addAll(state.H.get(state.W.get(k).node));
							ls.add(state.W.get(k).node);
							ls.remove(nodeId);
							FindPath(EdgeCount, e,fw);
								//print(state,line,state.W.get(k).node,nodeId);
							//System.out.println(e.toString());
							
						}
						else
						{
							v = new Value();
							v.node = nodeId;
							v.line = lineNum;
							Edge e = new Edge(state.W.get(k),v);
							EdgeStore.put(++EdgeCount,e);
							//System.out.println(e.toString());
							ls.addAll(state.H.get(state.W.get(k).node));
							ls.add(state.W.get(k).node);
						}
						state.H.put(nodeId,ls);
						//print(state,line,state.W.get(k).node,nodeId);
						entered =1;
					}
					
				}
				else if (line.contains("WRITE")) //check: what about read-static
				{
					LinkedHashSet<Integer> ls = new LinkedHashSet<Integer>();//to store ancestors of a node
					ReadKey r;
					Key k = new Key(objId,fieldId);
					Value v = new Value();
					if (state.W.containsKey(k) && state.H.containsKey(state.W.get(k).node) && state.H.get(state.W.get(k).node) != null && nodeId != state.W.get(k).node)
					{
						if (state.H.containsKey(nodeId)) 
						{
							ls.addAll(state.H.get(nodeId));
							//ls.addAll(state.H.get(state.F.get(tid)));
						}
						if(state.H.get(state.W.get(k).node).contains(nodeId))
						{
							v = new Value();
							v.node = nodeId;
							v.line = lineNum;
							Edge e = new Edge(state.W.get(k),v);
							EdgeStore.put(++EdgeCount,e);
							ls.addAll(state.H.get(state.W.get(k).node));
							ls.add(state.W.get(k).node);
							ls.remove(nodeId);
							cycles++;
							System.out.println(cycles+"--------------------CYCLE for WRITE WRITE EDGE!! "/*+objId+" "+fieldId+" src:"+state.W.get(k).node+" dest:"+nodeId*/);
							System.out.println("BLAME EDGE:"+"[("+state.W.get(k).node+","+state.W.get(k).line+"),("+nodeId+","+lineNum+")]");
							System.out.println("BLAME TRANSACTION: "+nodeId);
							fw.write(cycles+"--------------------CYCLE for WRITE WRITE EDGE!! ");
							fw.write("\nBLAME EDGE:"+"[("+state.W.get(k).node+","+state.W.get(k).line+"),("+nodeId+","+lineNum+")]");
							fw.write("\nBLAME TRANSACTION: "+nodeId);
							//printEdge(EdgeStore);
							//System.out.println(EdgeStore);
							FindPath(EdgeCount, e,fw);
							//System.out.println(e.toString());
						}
						else
						{
							v = new Value();
							v.node = nodeId;
							v.line = lineNum;
							Edge e = new Edge(state.W.get(k),v);
							EdgeStore.put(++EdgeCount,e);
							ls.addAll(state.H.get(state.W.get(k).node));
							ls.add(state.W.get(k).node);
							//System.out.println(e.toString());
						}
						state.H.put(nodeId,ls);
							entered =1;
						
					}
					 v = new Value();
					v.node = nodeId;
					v.line = lineNum;
					state.W.put(k,v);
					Iterator it = state.R.entrySet().iterator();
					LinkedHashSet<Integer> ls2 = new LinkedHashSet<Integer>();
				    while (it.hasNext()) 
				    {
				        Map.Entry pair = (Map.Entry)it.next();
				        r = (ReadKey) pair.getKey();
				        if ((r.getObjectId().equals(objId) )&& (r.getFieldId().equals(fieldId)) )
				        {
				        	if (state.H.containsKey(state.R.get(r).node) && state.H.get(state.R.get(r).node) != null && nodeId != state.R.get(r).node)
				        	{
				        		if (state.H.containsKey(nodeId)) 
				        		{
				        			ls2.addAll(state.H.get(nodeId));
				        		}
				        		if(state.H.get(state.R.get(r).node).contains(nodeId))
								{
				        			v = new Value();
									v.node = nodeId;
									v.line = lineNum;
									Edge e = new Edge(state.R.get(r),v);
									EdgeStore.put(++EdgeCount,e);
				        			ls2.addAll(state.H.get(state.R.get(r).node));/// *** CHANGED TO R
									ls2.add(state.R.get(r).node);
									ls2.remove(nodeId);
									cycles++;
									System.out.println(cycles+"----------------------CYCLE for READ-WRITE EDGE!!  "/*+objId+" "+fieldId+" src:"+state.R.get(r).node+" dest:"+nodeId*/);
									System.out.println("BLAME EDGE:"+"[("+state.R.get(r).node+","+state.R.get(r).line+"),("+nodeId+","+lineNum+")]");
									System.out.println("BLAME TRANSACTION: "+nodeId);
									fw.write(cycles+"----------------------CYCLE for READ-WRITE EDGE!!  ");
									fw.write("\nBLAME EDGE:"+"[("+state.R.get(r).node+","+state.R.get(r).line+"),("+nodeId+","+lineNum+")]");
									fw.write("\nBLAME TRANSACTION: "+nodeId);
									//printEdge(EdgeStore);
									//System.out.println(EdgeStore);
									FindPath(EdgeCount, e,fw);
									//System.out.println(e.toString());
								}
				        		else
				        		{
				        			v = new Value();
									v.node = nodeId;
									v.line = lineNum;
									Edge e = new Edge(state.R.get(r),v);
									EdgeStore.put(++EdgeCount,e);
					        		ls2.addAll(state.H.get(state.R.get(r).node));
					        		ls2.add(state.R.get(r).node);
				        		}
				        		state.H.put(nodeId,ls2);
				        		//print(state,line,state.R.get(r).node,nodeId);
				        		entered =1;
				        	}
				        	it.remove(); // avoids a ConcurrentModificationException
				        }
				    }
				    LinkedHashSet<Integer> ls3 = new LinkedHashSet<Integer>();
				    if(state.L.containsKey(tid) && state.L.get(tid).node!=-1 && state.L.get(tid).node!=nodeId)
					{
						if (state.H.containsKey(nodeId)) 
						{
							ls3.addAll(state.H.get(nodeId));
						}
						//if(state.H.containsKey(state.L.get(tid).node))
						//{
							
						ls3.addAll(state.H.get(state.L.get(tid).node));
						ls3.add(state.L.get(tid).node);
						state.H.put(nodeId,ls3);
						v = new Value();
						v.node = nodeId;
						v.line = lineNum;
						Edge e = new Edge(state.L.get(tid),v);
						EdgeStore.put(++EdgeCount,e);
						CausalEdges.add(e);
						//System.out.println(e.toString());
						//print(state,line,state.L.get(tid).node,nodeId);
						entered =1;
						//}
					}
				    v = new Value();
				    v.node = nodeId;
				    v.line = lineNum;
					state.L.put(tid,v);
				}
				line=br.readLine();
		  }
	  }
	  catch(IOException e)
	  {
		  e.printStackTrace();
	  }
	  finally
	  {
		  fw.close();
		  br.close();
	  }
  }
	
public static void main(String args[]) throws IOException 
	{	
		LifeCycle lc= new LifeCycle();
		//String con = args[0];
		String fileNum = args[1];
		int filenum = Integer.valueOf(fileNum);
		int i=0;
		for(i=1;i<=filenum;i++)
		{
			lc.clearState(0);
			String concat = args[0];
			String temp = String.valueOf(i);
			concat = concat.concat("abc_log.txt");
			concat = concat.concat(temp);
			lc.phaseTree(concat);
			System.out.println();
			System.out.println("----------------------------------------------------------TREE ATOMIC-CHECK STARTED for file number:"+i+"---------------------------------------------------------");
			System.out.println();
			lc.phaseCheck(i,args[0]);
			System.out.println();
			System.out.println("----------------------------------------------------------TREE ATOMIC-CHECK DONE for file number:"+i+"---------------------------------------------------------");
			System.out.println();
			lc.clearState(1);
			lc.markCalls(concat); // args[0] required for intermediate;
			System.out.println();
			System.out.println("----------------------------------------------------------LIFECYCLE ATOMIC-CHECK STARTED for file number:"+i+"---------------------------------------------------------");
			System.out.println();
			lc.checkLifecycles(i,concat, args[0]);
			System.out.println();
			System.out.println("----------------------------------------------------------LIFECYCLE ATOMIC-CHECK DONE for file number:"+i+"---------------------------------------------------------");
			System.out.println();
			lc.clearState(0);
		}
		
		//lc.phaseTree(con);
		//ba.phaseCheck();
		//lc.markCalls(con);
		//lc.checkLifecycles(con);
		
		System.out.println();
		System.out.println("-------------------------------------DONE----------------------------------------DONE-----------------------------------------DONE----------------------------------------");
	}
}