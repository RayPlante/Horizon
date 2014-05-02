
package misc;

import java.util.*;

public class Debug{
  boolean trace = false;
  boolean internal = false;
  boolean detail = false;
  boolean debugError = false;
  boolean warning = false;

  static private boolean Gtrace = true;
  static private boolean Ginternal = true;
  static private boolean Gdetail = true;
  static private boolean GdebugError = true;
  static private boolean Gwarning = true;

  static public final String ALL = "all";
  static public final String TRACE = "trace";
  static public final String INTERNAL = "internal";
  static public final String DETAIL = "detail";
  static public final String DEBUGERROR = "error";
  static public final String ERROR = "error";
  static public final String WARNING = "warning";

  public Debug( String s){
    StringTokenizer stok = new StringTokenizer(s);
    Vector v = new Vector(5);
    while( stok.hasMoreElements()){
      v.addElement( stok.nextToken());
    }
    String[] debugStrs = new String[v.size()];
    v.copyInto(debugStrs);
    v.removeAllElements();
    v = null;
    setDebugging(debugStrs);
  }

  public Debug( String[] args){
    setDebugging(args);
  }

  public Debug (){
  }

  static final public void GlobalDebug(String debug){
    StringTokenizer stok = new StringTokenizer(debug);
    while( stok.hasMoreElements()){
      String s =  stok.nextToken();
      if ( s.startsWith( ALL)){
	Gtrace = false;
	Ginternal = false;
	Gdetail = false;
	GdebugError = false;
	Gwarning = false;
      }
      else if ( s.startsWith(TRACE))
	Gtrace = false;
      else if ( s.startsWith(INTERNAL))
	Ginternal = false;
      else if ( s.startsWith(DETAIL))
	Gdetail = false;
      else if ( s.startsWith(DEBUGERROR))
	GdebugError = false;
      else if ( s.startsWith(WARNING))
	Gwarning = false;
    }
  }      
  private void setDebugging( String[] args){
    for ( int i=0; i < args.length; i++ ){
      if ( args[i].startsWith(ALL)){
	trace = true; internal = true; detail = true; debugError = true;
	warning = true;
      }
      else if ( args[i].startsWith(TRACE))
	trace = true;
      else if ( args[i].startsWith(INTERNAL))
	internal = true;
      else if ( args[i].startsWith(DETAIL))
	detail = true;
      else if ( args[i].startsWith(DEBUGERROR))
	debugError = true;
      else if ( args[i].startsWith(WARNING))
	warning = true;
    }
  }

  public void Trace(String s){
    if ( trace && Gtrace)
      System.out.println(s);
  }
  public void Internal(String s){
    if ( internal)
      System.out.println(s);
  }
  public void Detail(String s){
    if ( detail)
      System.out.println(s);
  }
  public void Error(String s){
    if ( debugError)
      System.out.println(s);
  }
  public void Warning(String s){
    if ( warning)
      System.out.println(s);
  }

  /*  
  public  static void main(String[] args){
    Debug d= new Debug(args);
    d = new Debug(args[0]);

    d.Trace("trace in");
    d.Internal("internal in");
    d.Detail("detail in");
    d.Error("error in");
    d.Warning("warning in");

    Debug.GlobalDebug("trace detail");
    System.out.println("GLOBAL Set-----------");
    d.Trace("trace in");
    d.Internal("internal in");
    d.Detail("detail in");
    d.Error("error in");
    d.Warning("warning in");
  }
  */  
}

